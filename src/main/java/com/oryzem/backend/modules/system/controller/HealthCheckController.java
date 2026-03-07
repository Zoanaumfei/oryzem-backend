package com.oryzem.backend.modules.system.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;
import software.amazon.awssdk.services.dynamodb.model.ListTablesRequest;
import software.amazon.awssdk.services.dynamodb.model.ListTablesResponse;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.ReturnConsumedCapacity;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazon.awssdk.services.dynamodb.model.TableDescription;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/health")
@Tag(name = "Health", description = "Operational endpoints for checking DynamoDB connectivity and table details")
@SecurityRequirement(name = "bearerAuth")
public class HealthCheckController {

    private final DynamoDbClient dynamoDbClient;

    public HealthCheckController(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    @GetMapping("/dynamodb")
    @Operation(
            summary = "Check DynamoDB health",
            description = "Performs a lightweight connectivity check and returns sample metadata for DynamoDB."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "DynamoDB is reachable"),
            @ApiResponse(responseCode = "503", description = "DynamoDB is unavailable"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<Map<String, Object>> checkDynamoDB() {
        Map<String, Object> response = new HashMap<>();

        try {
            Instant start = Instant.now();

            ListTablesResponse listResponse = dynamoDbClient.listTables(
                    ListTablesRequest.builder()
                            .limit(5)
                            .build()
            );

            Duration duration = Duration.between(start, Instant.now());

            response.put("status", "UP");
            response.put("service", "Amazon DynamoDB");
            response.put("region", dynamoDbClient.serviceClientConfiguration().region().toString());
            response.put("responseTime", duration.toMillis() + "ms");
            response.put("availableTables", listResponse.tableNames().size());
            response.put("tableNames", listResponse.tableNames());

            if (!listResponse.tableNames().isEmpty()) {
                String tableName = listResponse.tableNames().get(0);
                try {
                    DescribeTableResponse describeResponse = dynamoDbClient.describeTable(
                            DescribeTableRequest.builder()
                                    .tableName(tableName)
                                    .build()
                    );

                    response.put("sampleTable", tableName);
                    response.put("tableStatus", describeResponse.table().tableStatus().toString());
                    response.put("itemCount", describeResponse.table().itemCount());
                } catch (Exception e) {
                    response.put("tableDetails", "Unable to describe table: " + e.getMessage());
                }
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "DOWN");
            response.put("service", "Amazon DynamoDB");
            response.put("error", e.getMessage());
            response.put("errorType", e.getClass().getSimpleName());

            return ResponseEntity.status(503).body(response);
        }
    }

    @GetMapping("/dynamodb/tables")
    @Operation(
            summary = "List DynamoDB tables",
            description = "Returns all visible DynamoDB tables and a summary for each one."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tables returned successfully"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while loading tables"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<Map<String, Object>> listAllTables() {
        Map<String, Object> response = new HashMap<>();

        try {
            ListTablesResponse listResponse = dynamoDbClient.listTables();

            response.put("success", true);
            response.put("count", listResponse.tableNames().size());
            response.put("tables", listResponse.tableNames());

            Map<String, Object> tableDetails = new HashMap<>();
            for (String tableName : listResponse.tableNames()) {
                try {
                    DescribeTableResponse describeResponse = dynamoDbClient.describeTable(
                            DescribeTableRequest.builder()
                                    .tableName(tableName)
                                    .build()
                    );

                    TableDescription table = describeResponse.table();
                    Map<String, Object> details = new HashMap<>();
                    details.put("status", table.tableStatus().toString());
                    details.put("itemCount", table.itemCount());
                    details.put("sizeBytes", table.tableSizeBytes());
                    details.put("created", table.creationDateTime() != null
                            ? table.creationDateTime().toString()
                            : "N/A");

                    details.put("primaryKey", table.keySchema().stream()
                            .map(ks -> ks.attributeName() + " (" + ks.keyType() + ")")
                            .collect(Collectors.toList())
                    );

                    tableDetails.put(tableName, details);
                } catch (Exception e) {
                    tableDetails.put(tableName, "Error: " + e.getMessage());
                }
            }

            response.put("details", tableDetails);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/dynamodb/table/{tableName}")
    @Operation(
            summary = "Inspect a DynamoDB table",
            description = "Returns metadata, keys, indexes, and a simple scan test for a specific DynamoDB table."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Table details returned successfully"),
            @ApiResponse(responseCode = "404", description = "Table not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected error while inspecting the table"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<Map<String, Object>> checkTable(
            @Parameter(description = "DynamoDB table name", example = "oryzem-orders")
            @PathVariable String tableName) {
        Map<String, Object> response = new HashMap<>();

        try {
            DescribeTableResponse describeResponse = dynamoDbClient.describeTable(
                    DescribeTableRequest.builder()
                            .tableName(tableName)
                            .build()
            );

            TableDescription table = describeResponse.table();

            response.put("exists", true);
            response.put("tableName", table.tableName());
            response.put("status", table.tableStatus().toString());
            response.put("arn", table.tableArn());
            response.put("itemCount", table.itemCount());
            response.put("sizeBytes", table.tableSizeBytes());
            response.put("created", table.creationDateTime().toString());

            response.put("primaryKey", table.keySchema().stream()
                    .map(ks -> Map.of(
                            "attribute", ks.attributeName(),
                            "type", ks.keyType().toString()
                    ))
                    .collect(Collectors.toList())
            );

            response.put("attributes", table.attributeDefinitions().stream()
                    .map(ad -> Map.of(
                            "name", ad.attributeName(),
                            "type", ad.attributeType().toString()
                    ))
                    .collect(Collectors.toList())
            );

            if (!table.globalSecondaryIndexes().isEmpty()) {
                response.put("globalSecondaryIndexes", table.globalSecondaryIndexes().stream()
                        .map(gsi -> Map.of(
                                "name", gsi.indexName(),
                                "status", gsi.indexStatus().toString(),
                                "keySchema", gsi.keySchema().stream()
                                        .map(ks -> ks.attributeName() + " (" + ks.keyType() + ")")
                                        .collect(Collectors.toList())
                        ))
                        .collect(Collectors.toList())
                );
            }

            try {
                ScanResponse scanResponse = dynamoDbClient.scan(
                        ScanRequest.builder()
                                .tableName(tableName)
                                .limit(1)
                                .returnConsumedCapacity(ReturnConsumedCapacity.TOTAL)
                                .build()
                );

                response.put("scanTest", Map.of(
                        "success", true,
                        "itemsScanned", scanResponse.scannedCount(),
                        "itemsReturned", scanResponse.count(),
                        "consumedCapacity", scanResponse.consumedCapacity() != null
                                ? scanResponse.consumedCapacity().capacityUnits()
                                : 0
                ));
            } catch (Exception e) {
                response.put("scanTest", Map.of(
                        "success", false,
                        "error", e.getMessage()
                ));
            }

            return ResponseEntity.ok(response);

        } catch (ResourceNotFoundException e) {
            response.put("exists", false);
            response.put("error", "Table '" + tableName + "' not found");
            return ResponseEntity.status(404).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
