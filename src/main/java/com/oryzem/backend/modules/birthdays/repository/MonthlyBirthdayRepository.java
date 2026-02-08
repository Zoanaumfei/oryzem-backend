package com.oryzem.backend.modules.birthdays.repository;

import com.oryzem.backend.modules.birthdays.domain.MonthlyBirthday;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
public class MonthlyBirthdayRepository {

    private final DynamoDbEnhancedClient enhancedClient;
    private final String tableName;

    public MonthlyBirthdayRepository(
            DynamoDbEnhancedClient enhancedClient,
            @Value("${app.dynamodb.tables.birthdays:MonthlyBirthday}") String tableName
    ) {
        this.enhancedClient = enhancedClient;
        this.tableName = tableName;
    }

    private DynamoDbTable<MonthlyBirthday> getTable() {
        return enhancedClient.table(tableName, TableSchema.fromBean(MonthlyBirthday.class));
    }

    public MonthlyBirthday save(MonthlyBirthday birthday) {
        log.info("Saving birthday: {}/{}", birthday.getMonth(), birthday.getName());
        getTable().putItem(birthday);
        return birthday;
    }

    public Optional<MonthlyBirthday> findById(Integer month, String name) {
        Key key = Key.builder()
                .partitionValue(month)
                .sortValue(name)
                .build();
        MonthlyBirthday birthday = getTable().getItem(key);
        return Optional.ofNullable(birthday);
    }

    public void delete(Integer month, String name) {
        Key key = Key.builder()
                .partitionValue(month)
                .sortValue(name)
                .build();
        getTable().deleteItem(key);
    }

    public List<MonthlyBirthday> findAllByMonth(Integer month) {
        QueryConditional conditional = QueryConditional.keyEqualTo(
                Key.builder()
                        .partitionValue(month)
                        .build()
        );

        List<MonthlyBirthday> birthdays = new ArrayList<>();
        for (var page : getTable().query(r -> r.queryConditional(conditional))) {
            birthdays.addAll(page.items());
        }
        return birthdays;
    }

    public List<MonthlyBirthday> findAllByNameContains(String name) {
        Expression filter = buildNameContainsExpression(name);
        List<MonthlyBirthday> birthdays = new ArrayList<>();
        for (var page : getTable().scan(r -> r.filterExpression(filter))) {
            birthdays.addAll(page.items());
        }
        return birthdays;
    }

    public List<MonthlyBirthday> findAllByMonthAndNameContains(Integer month, String name) {
        QueryConditional conditional = QueryConditional.keyEqualTo(
                Key.builder()
                        .partitionValue(month)
                        .build()
        );
        Expression filter = buildNameContainsExpression(name);

        List<MonthlyBirthday> birthdays = new ArrayList<>();
        for (var page : getTable().query(r -> r.queryConditional(conditional)
                .filterExpression(filter))) {
            birthdays.addAll(page.items());
        }
        return birthdays;
    }

    public List<MonthlyBirthday> findAll() {
        List<MonthlyBirthday> birthdays = new ArrayList<>();
        getTable().scan()
                .items()
                .forEach(birthdays::add);
        return birthdays;
    }

    private Expression buildNameContainsExpression(String name) {
        return Expression.builder()
                .expression("contains(#name, :name)")
                .expressionNames(Map.of("#name", "name"))
                .expressionValues(Map.of(
                        ":name",
                        AttributeValue.builder().s(name).build()
                ))
                .build();
    }
}

