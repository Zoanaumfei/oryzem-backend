package com.oryzem.backend.modules.birthdays.domain;

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
    private Integer day;
    private String name;
    private Integer corporateMonth;
    private Integer corporateYear;
    private String photoKey;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("month")
    public Integer getMonth() {
        return month;
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("name")
    public String getName() {
        return name;
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

    public void setMonth(Integer month) {
        this.month = month;
    }

    public void setName(String name) {
        this.name = name;
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
}

