package com.oryzem.backend.modules.vehicles.repository;

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
        getTable().putItem(vehicleProject);
        return vehicleProject;
    }

    public Optional<VehicleProject> findById(String projectId, String als) {
        Key key = Key.builder()
                .partitionValue(projectId)
                .sortValue(als)
                .build();

        VehicleProject vehicleProject = getTable().getItem(key);
        return Optional.ofNullable(vehicleProject);
    }

    public List<VehicleProject> findByProjectId(String projectId) {
        QueryConditional conditional = QueryConditional.keyEqualTo(
                Key.builder()
                        .partitionValue(projectId)
                        .build()
        );

        List<VehicleProject> items = new ArrayList<>();
        for (var page : getTable().query(r -> r.queryConditional(conditional))) {
            items.addAll(page.items());
        }
        return items;
    }
}
