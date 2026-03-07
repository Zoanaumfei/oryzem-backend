package com.oryzem.backend.modules.vehicles.repository;

import com.oryzem.backend.core.tenant.TenantKeyCodec;
import com.oryzem.backend.core.tenant.TenantScope;
import com.oryzem.backend.modules.vehicles.domain.VehicleProject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class VehicleProjectRepository {

    private final DynamoDbEnhancedClient enhancedClient;
    private final String tableName;

    public VehicleProjectRepository(
            DynamoDbEnhancedClient enhancedClient,
            @Value("${app.dynamodb.tables.vehicles:Vehicles_Project}") String tableName
    ) {
        this.enhancedClient = enhancedClient;
        this.tableName = tableName;
    }

    private DynamoDbTable<VehicleProject> getTable() {
        return enhancedClient.table(tableName, TableSchema.fromBean(VehicleProject.class));
    }

    public VehicleProject save(VehicleProject vehicleProject) {
        log.info("Saving vehicle project: {}/{}", vehicleProject.getProjectId(), vehicleProject.getAls());
        getTable().putItem(toPersisted(vehicleProject));
        return vehicleProject;
    }

    public Optional<VehicleProject> findById(String projectId, String als) {
        Key key = Key.builder()
                .partitionValue(TenantKeyCodec.encode(projectId))
                .sortValue(TenantKeyCodec.encode(als))
                .build();

        VehicleProject vehicleProject = getTable().getItem(key);
        return Optional.ofNullable(vehicleProject).map(this::toDomain);
    }

    public List<VehicleProject> findByProjectId(String projectId) {
        QueryConditional conditional = QueryConditional.keyEqualTo(
                Key.builder()
                        .partitionValue(TenantKeyCodec.encode(projectId))
                        .build()
        );

        List<VehicleProject> items = new ArrayList<>();
        for (var page : getTable().query(r -> r.queryConditional(conditional))) {
            for (VehicleProject item : page.items()) {
                items.add(toDomain(item));
            }
        }
        return items;
    }

    public List<VehicleProject> findAll() {
        List<VehicleProject> items = new ArrayList<>();
        for (var page : getTable().scan()) {
            for (VehicleProject item : page.items()) {
                if (TenantScope.current().equals(item.getTenantId())) {
                    items.add(toDomain(item));
                }
            }
        }
        return items;
    }

    private VehicleProject toPersisted(VehicleProject vehicleProject) {
        VehicleProject copy = copy(vehicleProject);
        copy.setTenantId(TenantScope.current());
        copy.setProjectKey(TenantKeyCodec.encode(copy.getProjectId()));
        copy.setAlsKey(TenantKeyCodec.encode(copy.getAls()));
        return copy;
    }

    private VehicleProject toDomain(VehicleProject vehicleProject) {
        VehicleProject copy = copy(vehicleProject);
        copy.setProjectId(vehicleProject.getProjectId());
        copy.setAls(vehicleProject.getAls());
        copy.setTenantId(vehicleProject.getTenantId());
        return copy;
    }

    private VehicleProject copy(VehicleProject vehicleProject) {
        return VehicleProject.builder()
                .projectId(vehicleProject.getProjectId())
                .projectKey(vehicleProject.getProjectKey())
                .als(vehicleProject.getAls())
                .alsKey(vehicleProject.getAlsKey())
                .customer(vehicleProject.getCustomer())
                .projectName(vehicleProject.getProjectName())
                .description(vehicleProject.getDescription())
                .rgTemplate(vehicleProject.getRgTemplate())
                .status(vehicleProject.getStatus())
                .progress(vehicleProject.getProgress())
                .me(vehicleProject.getMe())
                .pvs(vehicleProject.getPvs())
                .s0(vehicleProject.getS0())
                .sop(vehicleProject.getSop())
                .tppa(vehicleProject.getTppa())
                .vff(vehicleProject.getVff())
                .bodyFramePvs(vehicleProject.getBodyFramePvs())
                .bodyFrameS0(vehicleProject.getBodyFrameS0())
                .bodyFrameVff(vehicleProject.getBodyFrameVff())
                .pvsEletTbt(vehicleProject.getPvsEletTbt())
                .s0EletTbt(vehicleProject.getS0EletTbt())
                .vffEletTbt(vehicleProject.getVffEletTbt())
                .pvsM100(vehicleProject.getPvsM100())
                .s0M100(vehicleProject.getS0M100())
                .vffM100(vehicleProject.getVffM100())
                .pvsZp5Tbt(vehicleProject.getPvsZp5Tbt())
                .s0Zp5Tbt(vehicleProject.getS0Zp5Tbt())
                .vffZp5Tbt(vehicleProject.getVffZp5Tbt())
                .pvsZp7Tbt(vehicleProject.getPvsZp7Tbt())
                .s0Zp7Tbt(vehicleProject.getS0Zp7Tbt())
                .vffZp7Tbt(vehicleProject.getVffZp7Tbt())
                .updatedAt(vehicleProject.getUpdatedAt())
                .updatedBy(vehicleProject.getUpdatedBy())
                .tenantId(vehicleProject.getTenantId())
                .build();
    }
}
