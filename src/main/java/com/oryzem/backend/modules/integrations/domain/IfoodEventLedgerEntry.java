package com.oryzem.backend.modules.integrations.domain;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@DynamoDbBean
public class IfoodEventLedgerEntry {

    private String merchantId;
    private String eventId;
    private String channel;
    private String orderId;
    private String eventCode;
    private String source;
    private Long processedAtEpochSeconds;
    private Long expiresAtEpochSeconds;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("merchantId")
    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("eventId")
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    @DynamoDbAttribute("channel")
    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    @DynamoDbAttribute("orderId")
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @DynamoDbAttribute("eventCode")
    public String getEventCode() {
        return eventCode;
    }

    public void setEventCode(String eventCode) {
        this.eventCode = eventCode;
    }

    @DynamoDbAttribute("source")
    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @DynamoDbAttribute("processedAtEpochSeconds")
    public Long getProcessedAtEpochSeconds() {
        return processedAtEpochSeconds;
    }

    public void setProcessedAtEpochSeconds(Long processedAtEpochSeconds) {
        this.processedAtEpochSeconds = processedAtEpochSeconds;
    }

    @DynamoDbAttribute("expiresAtEpochSeconds")
    public Long getExpiresAtEpochSeconds() {
        return expiresAtEpochSeconds;
    }

    public void setExpiresAtEpochSeconds(Long expiresAtEpochSeconds) {
        this.expiresAtEpochSeconds = expiresAtEpochSeconds;
    }
}
