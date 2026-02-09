package com.oryzem.backend.modules.initiatives.service;

import com.oryzem.backend.modules.initiatives.domain.Initiative;
import com.oryzem.backend.modules.initiatives.domain.InitiativeKeys;
import com.oryzem.backend.modules.initiatives.domain.InitiativeNotFoundException;
import com.oryzem.backend.modules.initiatives.dto.InitiativeRequest;
import com.oryzem.backend.modules.initiatives.dto.InitiativeResponse;
import com.oryzem.backend.modules.initiatives.repository.InitiativeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InitiativeService {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd")
                    .withResolverStyle(ResolverStyle.STRICT);

    private static final Set<String> ALLOWED_STATUSES = Set.of("IN_PROGRESS", "CONCLUDED");

    private final InitiativeRepository repository;

    public InitiativeResponse createInitiative(InitiativeRequest request) {
        InitiativePayload payload = buildPayload(request);

        List<Initiative> existing = repository.findByInitiativeId(payload.initiativeId());
        if (!existing.isEmpty()) {
            throw new IllegalStateException("Initiative already exists");
        }

        String now = Instant.now().toString();
        Initiative initiative = buildInitiative(payload, now, now);

        try {
            repository.saveIfAbsent(initiative);
        } catch (ConditionalCheckFailedException ex) {
            throw new IllegalStateException("Initiative already exists");
        }

        return InitiativeMapper.toResponse(initiative, "Initiative created successfully");
    }

    public InitiativeResponse updateInitiative(InitiativeRequest request) {
        InitiativePayload payload = buildPayload(request);

        List<Initiative> existing = repository.findByInitiativeId(payload.initiativeId());
        if (existing.isEmpty()) {
            throw new InitiativeNotFoundException(payload.initiativeId());
        }

        String now = Instant.now().toString();
        String createdAt = existing.stream()
                .map(Initiative::getCreatedAt)
                .filter(value -> value != null && !value.isBlank())
                .findFirst()
                .orElse(now);

        Initiative updated = buildInitiative(payload, createdAt, now);
        repository.save(updated);

        for (Initiative old : existing) {
            if (!sameKeys(old, updated)) {
                repository.delete(old.getPk(), old.getSk());
            }
        }

        return InitiativeMapper.toResponse(updated, "Initiative updated successfully");
    }

    public InitiativeResponse deleteInitiative(String initiativeId) {
        String normalizedId = normalizeRequired(initiativeId, "InitiativeId");

        List<Initiative> existing = repository.findByInitiativeId(normalizedId);
        if (existing.isEmpty()) {
            throw new InitiativeNotFoundException(normalizedId);
        }

        existing.forEach(item -> repository.delete(item.getPk(), item.getSk()));
        Initiative deleted = existing.get(0);

        return InitiativeMapper.toResponse(deleted, "Initiative deleted successfully");
    }

    public List<InitiativeResponse> listInitiativesByYear(String year) {
        validateYear(year);
        List<Initiative> initiatives = repository.findAllByYear(year);
        return initiatives.stream()
                .map(initiative -> InitiativeMapper.toResponse(initiative, "Initiative listed"))
                .collect(Collectors.toList());
    }

    private Initiative buildInitiative(InitiativePayload payload, String createdAt, String updatedAt) {
        String pk = InitiativeKeys.yearPk(payload.year());
        String sk = InitiativeKeys.initiativeSk(
                payload.initiativeType(),
                payload.initiativeStatus(),
                payload.initiativeNameLower(),
                payload.initiativeId()
        );

        return Initiative.builder()
                .pk(pk)
                .sk(sk)
                .initiativeId(payload.initiativeId())
                .initiativeName(payload.initiativeName())
                .initiativeNameLower(payload.initiativeNameLower())
                .initiativeDescription(payload.initiativeDescription())
                .initiativeType(payload.initiativeType())
                .initiativeDueDate(payload.initiativeDueDate())
                .initiativeStatus(payload.initiativeStatus())
                .leaderName(payload.leaderName())
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    private InitiativePayload buildPayload(InitiativeRequest request) {
        String initiativeId = normalizeRequired(request.getInitiativeId(), "InitiativeId");
        String initiativeName = normalizeRequired(request.getInitiativeName(), "InitiativeName");
        String initiativeDescription = normalizeRequired(request.getInitiativeDescription(), "InitiativeDescription");
        String initiativeType = normalizeRequired(request.getInitiativeType(), "InitiativeType");
        LocalDate dueDate = parseDate(request.getInitiativeDueDate());
        String initiativeStatus = normalizeStatus(request.getInitiativeStatus());
        String leaderName = normalizeOptional(request.getLeaderName());

        String initiativeNameLower = initiativeName.toLowerCase(Locale.ROOT);
        String formattedDate = dueDate.format(DATE_FORMAT);
        String year = String.valueOf(dueDate.getYear());

        return new InitiativePayload(
                initiativeId,
                initiativeName,
                initiativeNameLower,
                initiativeDescription,
                initiativeType,
                formattedDate,
                initiativeStatus,
                leaderName,
                year
        );
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("InitiativeDueDate is required");
        }
        try {
            return LocalDate.parse(value, DATE_FORMAT);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("InitiativeDueDate must be a valid YYYY-MM-DD", ex);
        }
    }

    private String normalizeStatus(String value) {
        String normalized = normalizeRequired(value, "InitiativeStatus").toUpperCase(Locale.ROOT);
        if (!ALLOWED_STATUSES.contains(normalized)) {
            throw new IllegalArgumentException("InitiativeStatus must be IN_PROGRESS or CONCLUDED");
        }
        return normalized;
    }

    private String normalizeRequired(String value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return trimmed;
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void validateYear(String year) {
        if (year == null || year.isBlank()) {
            throw new IllegalArgumentException("Year is required");
        }
        String trimmed = year.trim();
        if (!trimmed.matches("^\\d{4}$")) {
            throw new IllegalArgumentException("Year must be a valid YYYY");
        }
    }

    private boolean sameKeys(Initiative left, Initiative right) {
        return Objects.equals(left.getPk(), right.getPk())
                && Objects.equals(left.getSk(), right.getSk());
    }

    private record InitiativePayload(
            String initiativeId,
            String initiativeName,
            String initiativeNameLower,
            String initiativeDescription,
            String initiativeType,
            String initiativeDueDate,
            String initiativeStatus,
            String leaderName,
            String year
    ) {
    }
}
