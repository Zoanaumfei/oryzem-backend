package com.oryzem.backend.modules.birthdays.repository;

import com.oryzem.backend.core.tenant.TenantKeyCodec;
import com.oryzem.backend.core.tenant.TenantScope;
import com.oryzem.backend.modules.birthdays.domain.MonthlyBirthday;
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
        getTable().putItem(toPersisted(birthday));
        return birthday;
    }

    public Optional<MonthlyBirthday> findById(Integer month, String name) {
        Key key = Key.builder()
                .partitionValue(TenantKeyCodec.encode(month == null ? null : String.valueOf(month)))
                .sortValue(TenantKeyCodec.encode(name))
                .build();
        MonthlyBirthday birthday = getTable().getItem(key);
        return Optional.ofNullable(birthday).map(this::toDomain);
    }

    public void delete(Integer month, String name) {
        Key key = Key.builder()
                .partitionValue(TenantKeyCodec.encode(month == null ? null : String.valueOf(month)))
                .sortValue(TenantKeyCodec.encode(name))
                .build();
        getTable().deleteItem(key);
    }

    public List<MonthlyBirthday> findAllByMonth(Integer month) {
        QueryConditional conditional = QueryConditional.keyEqualTo(
                Key.builder()
                        .partitionValue(TenantKeyCodec.encode(month == null ? null : String.valueOf(month)))
                        .build()
        );

        List<MonthlyBirthday> birthdays = new ArrayList<>();
        for (var page : getTable().query(r -> r.queryConditional(conditional))) {
            for (MonthlyBirthday birthday : page.items()) {
                birthdays.add(toDomain(birthday));
            }
        }
        return birthdays;
    }

    public List<MonthlyBirthday> findAllByNameContains(String name) {
        List<MonthlyBirthday> birthdays = new ArrayList<>();
        for (var page : getTable().scan()) {
            for (MonthlyBirthday birthday : page.items()) {
                if (TenantScope.current().equals(birthday.getTenantId())
                        && containsName(birthday, name)) {
                    birthdays.add(toDomain(birthday));
                }
            }
        }
        return birthdays;
    }

    public List<MonthlyBirthday> findAllByMonthAndNameContains(Integer month, String name) {
        QueryConditional conditional = QueryConditional.keyEqualTo(
                Key.builder()
                        .partitionValue(TenantKeyCodec.encode(month == null ? null : String.valueOf(month)))
                        .build()
        );

        List<MonthlyBirthday> birthdays = new ArrayList<>();
        for (var page : getTable().query(r -> r.queryConditional(conditional))) {
            for (MonthlyBirthday birthday : page.items()) {
                if (containsName(birthday, name)) {
                    birthdays.add(toDomain(birthday));
                }
            }
        }
        return birthdays;
    }

    public List<MonthlyBirthday> findAll() {
        List<MonthlyBirthday> birthdays = new ArrayList<>();
        getTable().scan()
                .items()
                .forEach(birthday -> {
                    if (TenantScope.current().equals(birthday.getTenantId())) {
                        birthdays.add(toDomain(birthday));
                    }
                });
        return birthdays;
    }

    private boolean containsName(MonthlyBirthday birthday, String name) {
        String currentName = birthday.getName();
        return currentName != null && currentName.contains(name);
    }

    private MonthlyBirthday toPersisted(MonthlyBirthday birthday) {
        MonthlyBirthday copy = copy(birthday);
        copy.setTenantId(TenantScope.current());
        copy.setMonthKey(TenantKeyCodec.encode(copy.getMonth() == null ? null : String.valueOf(copy.getMonth())));
        copy.setNameKey(TenantKeyCodec.encode(copy.getName()));
        return copy;
    }

    private MonthlyBirthday toDomain(MonthlyBirthday birthday) {
        MonthlyBirthday copy = copy(birthday);
        copy.setMonth(birthday.getMonth());
        copy.setName(birthday.getName());
        copy.setTenantId(birthday.getTenantId());
        return copy;
    }

    private MonthlyBirthday copy(MonthlyBirthday birthday) {
        return MonthlyBirthday.builder()
                .month(birthday.getMonth())
                .monthKey(birthday.getMonthKey())
                .day(birthday.getDay())
                .name(birthday.getName())
                .nameKey(birthday.getNameKey())
                .corporateMonth(birthday.getCorporateMonth())
                .corporateYear(birthday.getCorporateYear())
                .photoKey(birthday.getPhotoKey())
                .tenantId(birthday.getTenantId())
                .build();
    }
}

