package com.oryzem.backend.modules.birthdays.domain;

import com.oryzem.backend.core.tenant.TenantKeyCodec;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamoDbBean
public class MonthlyBirthday {

    private Integer month;
    private String monthKey;
    private Integer day;
    private String name;
    private String nameKey;
    private Integer corporateMonth;
    private Integer corporateYear;
    private String photoKey;
    private String tenantId;

    public Integer getMonth() {
        return month;
    }

    public String getName() {
        return name;
    }

    @DynamoDbPartitionKey
    @DynamoDbAttribute("month")
    public String getMonthKey() {
        return monthKey;
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("name")
    public String getNameKey() {
        return nameKey;
    }

    @DynamoDbAttribute("day")
    public Integer getDay() {
        return day;
    }

    @DynamoDbAttribute("corporate_month")
    public Integer getCorporateMonth() {
        return corporateMonth;
    }

    @DynamoDbAttribute("corporate_year")
    public Integer getCorporateYear() {
        return corporateYear;
    }

    @DynamoDbAttribute("photo_key")
    public String getPhotoKey() {
        return photoKey;
    }

    @DynamoDbAttribute("tenantId")
    public String getTenantId() {
        return tenantId;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public void setMonthKey(String monthKey) {
        this.monthKey = monthKey;
        String decoded = TenantKeyCodec.decode(monthKey);
        this.month = decoded == null ? null : Integer.valueOf(decoded);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNameKey(String nameKey) {
        this.nameKey = nameKey;
        this.name = TenantKeyCodec.decode(nameKey);
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public void setCorporateMonth(Integer corporateMonth) {
        this.corporateMonth = corporateMonth;
    }

    public void setCorporateYear(Integer corporateYear) {
        this.corporateYear = corporateYear;
    }

    public void setPhotoKey(String photoKey) {
        this.photoKey = photoKey;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}

