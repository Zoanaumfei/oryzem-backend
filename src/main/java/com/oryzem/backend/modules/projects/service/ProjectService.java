package com.oryzem.backend.modules.projects.service;

import com.oryzem.backend.modules.projects.domain.DateIndexItem;
import com.oryzem.backend.modules.projects.domain.Gate;
import com.oryzem.backend.modules.projects.domain.MetaItem;
import com.oryzem.backend.modules.projects.domain.MilestoneItem;
import com.oryzem.backend.modules.projects.domain.Phase;
import com.oryzem.backend.modules.projects.domain.ProjectEntityType;
import com.oryzem.backend.modules.projects.domain.ProjectKeys;
import com.oryzem.backend.modules.projects.domain.ProjectStatus;
import com.oryzem.backend.modules.projects.dto.CreateProjectRequest;
import com.oryzem.backend.modules.projects.dto.DueDateItem;
import com.oryzem.backend.modules.projects.dto.DueDateRangeResponse;
import com.oryzem.backend.modules.projects.dto.DueDateResponse;
import com.oryzem.backend.modules.projects.dto.Grid;
import com.oryzem.backend.modules.projects.dto.ProjectResponse;
import com.oryzem.backend.modules.projects.dto.ProjectSummaryResponse;
import com.oryzem.backend.modules.projects.dto.UpdateProjectRequest;
import com.oryzem.backend.modules.projects.repository.PagedResult;
import com.oryzem.backend.modules.projects.repository.ProjectRepository;
import com.oryzem.backend.shared.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

    private static final List<Gate> GATE_ORDER = List.of(Gate.ZP5, Gate.ELET, Gate.ZP7);
    private static final List<Phase> PHASE_ORDER = List.of(Phase.VFF, Phase.PVS, Phase.SO, Phase.TPPA, Phase.SOP);
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    private final ProjectRepository projectRepository;

    public ProjectResponse createProject(CreateProjectRequest request, String requestId) {
        requireRequestId(requestId);
        String projectId = request.projectId();

        Optional<MetaItem> metaOptional = projectRepository.getMeta(projectId, true);
        if (metaOptional.isPresent()) {
            MetaItem meta = metaOptional.get();
            if (requestId.equals(meta.getLastRequestId())) {
                if (meta.getStatus() == ProjectStatus.ACTIVE) {
                    return getProject(projectId);
                }
                if (meta.getStatus() == ProjectStatus.CREATING) {
                    return resumeCreate(request, requestId);
                }
            }
            if (meta.getStatus() == ProjectStatus.UPDATING) {
                throw new IllegalStateException("Project is being updated");
            }
            throw new IllegalStateException("Project already exists");
        }

        Instant now = Instant.now();
        MetaItem meta = MetaItem.builder()
                .pk(ProjectKeys.projectPk(projectId))
                .sk(ProjectKeys.metaSk())
                .projectId(projectId)
                .projectName(request.projectName())
                .status(ProjectStatus.CREATING)
                .lastRequestId(requestId)
                .updatedAt(now)
                .entityType(ProjectEntityType.META)
                .build();

        try {
            projectRepository.createMetaConditionally(meta);
        } catch (ConditionalCheckFailedException ex) {
            MetaItem existing = projectRepository.getMeta(projectId, true)
                    .orElseThrow(() -> new IllegalStateException("Project already exists"));
            if (requestId.equals(existing.getLastRequestId())
                    && existing.getStatus() == ProjectStatus.CREATING) {
                return resumeCreate(request, requestId);
            }
            if (existing.getStatus() == ProjectStatus.UPDATING) {
                throw new IllegalStateException("Project is being updated");
            }
            throw new IllegalStateException("Project already exists");
        }

        WriteSets writeSets = buildWriteSets(projectId, request.projectName(), request.grid(), now);
        projectRepository.batchPutMilestones(writeSets.milestonesToPut());
        projectRepository.batchPutDateItems(writeSets.dateItemsToPut());
        try {
            projectRepository.updateMetaStatus(projectId, ProjectStatus.ACTIVE, requestId, now, ProjectStatus.CREATING);
        } catch (ConditionalCheckFailedException ex) {
            log.info("Meta already ACTIVE for project {}", projectId);
        }

        return new ProjectResponse(
                projectId,
                request.projectName(),
                request.grid(),
                now.toString()
        );
    }

    public ProjectResponse updateProject(String projectId, UpdateProjectRequest request, String requestId) {
        requireRequestId(requestId);
        MetaItem meta = projectRepository.getMeta(projectId, true)
                .orElseThrow(() -> new NotFoundException("Project not found"));

        if (requestId.equals(meta.getLastRequestId())) {
            if (meta.getStatus() == ProjectStatus.ACTIVE) {
                return getProject(projectId);
            }
            if (meta.getStatus() == ProjectStatus.CREATING) {
                throw new IllegalStateException("Project is being created");
            }
        } else if (meta.getStatus() != ProjectStatus.ACTIVE) {
            throw new IllegalStateException("Project is locked for update");
        }

        Instant now = Instant.now();
        if (!requestId.equals(meta.getLastRequestId())) {
            try {
                projectRepository.updateMetaStatus(projectId, ProjectStatus.UPDATING, requestId, now, ProjectStatus.ACTIVE);
            } catch (ConditionalCheckFailedException ex) {
                throw new IllegalStateException("Project is locked for update");
            }
        }

        List<MilestoneItem> existingItems = projectRepository.queryProjectMilestones(projectId);
        DiffResult diff = diffGrid(meta.getProjectName(), projectId, existingItems, request.grid(), now);

        projectRepository.batchDeleteDateItems(diff.dateItemsToDelete());
        projectRepository.batchDeleteMilestones(diff.milestonesToDelete());
        projectRepository.batchPutMilestones(diff.milestonesToPut());
        projectRepository.batchPutDateItems(diff.dateItemsToPut());

        try {
            projectRepository.updateMetaStatus(projectId, ProjectStatus.ACTIVE, requestId, now, ProjectStatus.UPDATING);
        } catch (ConditionalCheckFailedException ex) {
            log.info("Meta already ACTIVE for project {}", projectId);
        }

        return new ProjectResponse(
                projectId,
                meta.getProjectName(),
                request.grid(),
                now.toString()
        );
    }

    public ProjectResponse getProject(String projectId) {
        MetaItem meta = projectRepository.getMeta(projectId, true)
                .orElseThrow(() -> new NotFoundException("Project not found"));

        List<MilestoneItem> items = projectRepository.queryProjectMilestones(projectId);
        Grid grid = buildGrid(items);

        String updatedAt = meta.getUpdatedAt() != null ? meta.getUpdatedAt().toString() : Instant.now().toString();
        return new ProjectResponse(projectId, meta.getProjectName(), grid, updatedAt);
    }

    public List<ProjectSummaryResponse> listProjects() {
        List<MetaItem> items = projectRepository.listProjectMetaItems();
        return items.stream()
                .map(item -> new ProjectSummaryResponse(
                        item.getProjectId(),
                        item.getProjectName(),
                        item.getStatus()
                ))
                .toList();
    }

    public DueDateResponse getDueDate(String date, String pageToken, int limit) {
        validateDate(date);
        return buildDueDateResponse(date, pageToken, limit);
    }

    public DueDateRangeResponse getDueDateRange(String startDate, int days, int limit) {
        validateDate(startDate);
        int window = normalizeDays(days);
        int pageSize = limit > 0 ? limit : 100;

        LocalDate start = LocalDate.parse(startDate, DATE_FORMAT);
        List<DueDateResponse> responses = new ArrayList<>(window);
        for (int i = 0; i < window; i++) {
            String date = start.plusDays(i).format(DATE_FORMAT);
            PagedResult<DateIndexItem> result = projectRepository.queryDateItems(date, null, pageSize);
            responses.add(buildDueDateResponse(date, result));
        }

        return new DueDateRangeResponse(startDate, window, responses);
    }

    private ProjectResponse resumeCreate(CreateProjectRequest request, String requestId) {
        Instant now = Instant.now();
        WriteSets writeSets = buildWriteSets(request.projectId(), request.projectName(), request.grid(), now);
        projectRepository.batchPutMilestones(writeSets.milestonesToPut());
        projectRepository.batchPutDateItems(writeSets.dateItemsToPut());
        try {
            projectRepository.updateMetaStatus(request.projectId(), ProjectStatus.ACTIVE, requestId, now, ProjectStatus.CREATING);
        } catch (ConditionalCheckFailedException ex) {
            log.info("Meta already ACTIVE for project {}", request.projectId());
        }

        return new ProjectResponse(
                request.projectId(),
                request.projectName(),
                request.grid(),
                now.toString()
        );
    }

    private WriteSets buildWriteSets(String projectId,
                                     String projectName,
                                     Grid grid,
                                     Instant updatedAt) {
        List<MilestoneItem> milestones = new ArrayList<>();
        List<DateIndexItem> dateItems = new ArrayList<>();

        if (grid == null || grid.dates() == null || grid.dates().isEmpty()) {
            return new WriteSets(milestones, dateItems);
        }

        Map<Integer, String> alsDescriptions =
                grid.alsDescriptions() == null ? Map.of() : grid.alsDescriptions();

        for (Map.Entry<Integer, Map<Gate, Map<Phase, String>>> alsEntry : grid.dates().entrySet()) {
            Integer als = alsEntry.getKey();
            Map<Gate, Map<Phase, String>> gateMap = alsEntry.getValue();
            if (als == null || gateMap == null) {
                continue;
            }
            String alsDescription = descriptionForAls(alsDescriptions, als);
            for (Map.Entry<Gate, Map<Phase, String>> gateEntry : gateMap.entrySet()) {
                Gate gate = gateEntry.getKey();
                Map<Phase, String> phaseMap = gateEntry.getValue();
                if (gate == null || phaseMap == null) {
                    continue;
                }
                for (Map.Entry<Phase, String> phaseEntry : phaseMap.entrySet()) {
                    Phase phase = phaseEntry.getKey();
                    if (phase == null) {
                        continue;
                    }
                    String value = normalizeDate(phaseEntry.getValue());
                    if (value.isEmpty()) {
                        continue;
                    }
                    milestones.add(buildMilestone(projectId, projectName, als, alsDescription, gate, phase, value, updatedAt));
                    dateItems.add(buildDateIndex(projectId, projectName, als, alsDescription, gate, phase, value, updatedAt));
                }
            }
        }

        return new WriteSets(milestones, dateItems);
    }

    private DiffResult diffGrid(String projectName,
                                String projectId,
                                List<MilestoneItem> existingItems,
                                Grid grid,
                                Instant updatedAt) {
        Map<CellKey, String> existing = new HashMap<>();
        Map<CellKey, String> existingDescriptions = new HashMap<>();
        for (MilestoneItem item : existingItems) {
            existing.put(new CellKey(item.getAls(), item.getGate(), item.getPhase()),
                    normalizeDate(item.getDate()));
            existingDescriptions.put(new CellKey(item.getAls(), item.getGate(), item.getPhase()),
                    normalizeDescription(item.getAlsDescription()));
        }

        Map<CellKey, String> incoming = new HashMap<>();
        Map<Integer, String> alsDescriptions =
                grid != null && grid.alsDescriptions() != null ? grid.alsDescriptions() : Map.of();
        if (grid != null && grid.dates() != null) {
            for (Map.Entry<Integer, Map<Gate, Map<Phase, String>>> alsEntry : grid.dates().entrySet()) {
                Integer als = alsEntry.getKey();
                Map<Gate, Map<Phase, String>> gateMap = alsEntry.getValue();
                if (als == null || gateMap == null) {
                    continue;
                }
                for (Map.Entry<Gate, Map<Phase, String>> gateEntry : gateMap.entrySet()) {
                    Gate gate = gateEntry.getKey();
                    Map<Phase, String> phaseMap = gateEntry.getValue();
                    if (gate == null || phaseMap == null) {
                        continue;
                    }
                    for (Map.Entry<Phase, String> phaseEntry : phaseMap.entrySet()) {
                        Phase phase = phaseEntry.getKey();
                        if (phase == null) {
                            continue;
                        }
                        String value = normalizeDate(phaseEntry.getValue());
                        incoming.put(new CellKey(als, gate, phase), value);
                    }
                }
            }
        }

        List<MilestoneItem> milestonePuts = new ArrayList<>();
        List<MilestoneItem> milestoneDeletes = new ArrayList<>();
        List<DateIndexItem> datePuts = new ArrayList<>();
        List<DateIndexItem> dateDeletes = new ArrayList<>();

        for (Map.Entry<CellKey, String> entry : existing.entrySet()) {
            CellKey key = entry.getKey();
            if (incoming.containsKey(key)) {
                continue;
            }
            String oldDate = entry.getValue();
            if (oldDate.isEmpty()) {
                continue;
            }
            milestoneDeletes.add(buildMilestoneKeyOnly(projectId, key));
            dateDeletes.add(buildDateIndexKeyOnly(projectId, key, oldDate));
        }

        for (Map.Entry<CellKey, String> entry : incoming.entrySet()) {
            CellKey key = entry.getKey();
            String newDate = entry.getValue();
            String oldDate = existing.getOrDefault(key, "");
            String newDescription = descriptionForAls(alsDescriptions, key.als);
            String oldDescription = existingDescriptions.getOrDefault(key, "");

            if (oldDate.isEmpty() && newDate.isEmpty()) {
                continue;
            }
            if (oldDate.isEmpty()) {
                milestonePuts.add(buildMilestone(projectId, projectName, key.als, newDescription, key.gate, key.phase, newDate, updatedAt));
                datePuts.add(buildDateIndex(projectId, projectName, key.als, newDescription, key.gate, key.phase, newDate, updatedAt));
                continue;
            }
            if (newDate.isEmpty()) {
                milestoneDeletes.add(buildMilestoneKeyOnly(projectId, key));
                dateDeletes.add(buildDateIndexKeyOnly(projectId, key, oldDate));
                continue;
            }
            if (!oldDate.equals(newDate)) {
                milestonePuts.add(buildMilestone(projectId, projectName, key.als, newDescription, key.gate, key.phase, newDate, updatedAt));
                dateDeletes.add(buildDateIndexKeyOnly(projectId, key, oldDate));
                datePuts.add(buildDateIndex(projectId, projectName, key.als, newDescription, key.gate, key.phase, newDate, updatedAt));
                continue;
            }
            if (!oldDescription.equals(newDescription)) {
                milestonePuts.add(buildMilestone(projectId, projectName, key.als, newDescription, key.gate, key.phase, newDate, updatedAt));
                datePuts.add(buildDateIndex(projectId, projectName, key.als, newDescription, key.gate, key.phase, newDate, updatedAt));
            }
        }

        return new DiffResult(milestonePuts, milestoneDeletes, datePuts, dateDeletes);
    }

    private Grid buildGrid(List<MilestoneItem> items) {
        Map<Integer, Map<Gate, Map<Phase, String>>> grid = new LinkedHashMap<>();
        Map<Integer, String> alsDescriptions = new LinkedHashMap<>();

        for (MilestoneItem item : items) {
            if (item.getAls() == null || item.getGate() == null || item.getPhase() == null) {
                continue;
            }
            grid.computeIfAbsent(item.getAls(), als -> createEmptyGateMap());
            alsDescriptions.putIfAbsent(item.getAls(), "");
            String value = normalizeDate(item.getDate());
            grid.get(item.getAls())
                    .get(item.getGate())
                    .put(item.getPhase(), value);
            if (item.getAls() != null) {
                String existing = alsDescriptions.get(item.getAls());
                String incoming = normalizeDescription(item.getAlsDescription());
                if (existing != null && existing.isBlank() && !incoming.isBlank()) {
                    alsDescriptions.put(item.getAls(), incoming);
                }
            }
        }

        return new Grid(alsDescriptions, grid);
    }

    private MilestoneItem buildMilestone(String projectId,
                                         String projectName,
                                         int als,
                                         String alsDescription,
                                         Gate gate,
                                         Phase phase,
                                         String date,
                                         Instant updatedAt) {
        return MilestoneItem.builder()
                .pk(ProjectKeys.projectPk(projectId))
                .sk(ProjectKeys.milestoneSk(als, gate, phase))
                .projectId(projectId)
                .projectName(projectName)
                .als(als)
                .alsDescription(alsDescription)
                .gate(gate)
                .phase(phase)
                .date(date)
                .updatedAt(updatedAt)
                .entityType(ProjectEntityType.PROJECT_MILESTONE)
                .build();
    }

    private MilestoneItem buildMilestoneKeyOnly(String projectId, CellKey key) {
        return MilestoneItem.builder()
                .pk(ProjectKeys.projectPk(projectId))
                .sk(ProjectKeys.milestoneSk(key.als, key.gate, key.phase))
                .build();
    }

    private DateIndexItem buildDateIndex(String projectId,
                                         String projectName,
                                         int als,
                                         String alsDescription,
                                         Gate gate,
                                         Phase phase,
                                         String date,
                                         Instant updatedAt) {
        return DateIndexItem.builder()
                .pk(ProjectKeys.datePk(date))
                .sk(ProjectKeys.dateSk(projectId, als, gate, phase))
                .projectId(projectId)
                .projectName(projectName)
                .als(als)
                .alsDescription(alsDescription)
                .gate(gate)
                .phase(phase)
                .date(date)
                .updatedAt(updatedAt)
                .entityType(ProjectEntityType.DATE_INDEX)
                .build();
    }

    private DateIndexItem buildDateIndexKeyOnly(String projectId, CellKey key, String date) {
        return DateIndexItem.builder()
                .pk(ProjectKeys.datePk(date))
                .sk(ProjectKeys.dateSk(projectId, key.als, key.gate, key.phase))
                .build();
    }

    private String normalizeDate(String value) {
        return (value == null || value.isBlank()) ? "" : value;
    }

    private String normalizeDescription(String value) {
        return (value == null || value.isBlank()) ? "" : value;
    }

    private String descriptionForAls(Map<Integer, String> alsDescriptions, int als) {
        if (alsDescriptions == null) {
            return "";
        }
        return normalizeDescription(alsDescriptions.get(als));
    }

    private Map<Gate, Map<Phase, String>> createEmptyGateMap() {
        Map<Gate, Map<Phase, String>> gateMap = new EnumMap<>(Gate.class);
        for (Gate gate : GATE_ORDER) {
            Map<Phase, String> phaseMap = new EnumMap<>(Phase.class);
            for (Phase phase : PHASE_ORDER) {
                phaseMap.put(phase, "");
            }
            gateMap.put(gate, phaseMap);
        }
        return gateMap;
    }

    private void validateDate(String date) {
        if (date == null || date.isBlank()) {
            throw new IllegalArgumentException("Date is required");
        }
        try {
            LocalDate.parse(date, DATE_FORMAT);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Date must be a valid YYYY-MM-DD");
        }
    }

    private DueDateResponse buildDueDateResponse(String date, String pageToken, int limit) {
        int pageSize = limit > 0 ? limit : 100;
        PagedResult<DateIndexItem> result = projectRepository.queryDateItems(date, pageToken, pageSize);
        return buildDueDateResponse(date, result);
    }

    private DueDateResponse buildDueDateResponse(String date, PagedResult<DateIndexItem> result) {
        List<DueDateItem> items = new ArrayList<>();
        for (DateIndexItem item : result.items()) {
            items.add(new DueDateItem(
                    item.getProjectId(),
                    item.getProjectName(),
                    item.getAls(),
                    normalizeDescription(item.getAlsDescription()),
                    item.getGate(),
                    item.getPhase(),
                    item.getDate()
            ));
        }

        return new DueDateResponse(date, items, result.nextPageToken());
    }

    private int normalizeDays(int days) {
        if (days <= 0) {
            return 21;
        }
        if (days > 60) {
            throw new IllegalArgumentException("Days must be between 1 and 60");
        }
        return days;
    }

    private void requireRequestId(String requestId) {
        if (requestId == null || requestId.isBlank()) {
            throw new IllegalArgumentException("Idempotency-Key is required");
        }
        try {
            UUID.fromString(requestId);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Idempotency-Key must be a valid UUID");
        }
    }

    private record CellKey(int als, Gate gate, Phase phase) {
    }

    private record WriteSets(List<MilestoneItem> milestonesToPut,
                             List<DateIndexItem> dateItemsToPut) {
    }

    private record DiffResult(List<MilestoneItem> milestonesToPut,
                              List<MilestoneItem> milestonesToDelete,
                              List<DateIndexItem> dateItemsToPut,
                              List<DateIndexItem> dateItemsToDelete) {
    }
}
