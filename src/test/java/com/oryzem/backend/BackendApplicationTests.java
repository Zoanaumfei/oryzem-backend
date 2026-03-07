package com.oryzem.backend;

import com.oryzem.backend.modules.canonical.service.CanonicalIngestionContextResolver;
import com.oryzem.backend.modules.canonical.service.CanonicalOrderPersistenceService;
import com.oryzem.backend.modules.files.service.S3PresignService;
import com.oryzem.backend.modules.orders.service.CanonicalOrderCommandService;
import com.oryzem.backend.modules.orders.service.CanonicalOrderQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@SpringBootTest
@ActiveProfiles("test")
class BackendApplicationTests {

	@MockitoBean
	private DynamoDbClient dynamoDbClient;

	@MockitoBean
	private DynamoDbEnhancedClient dynamoDbEnhancedClient;

	@MockitoBean
	private S3PresignService s3PresignService;

	@MockitoBean
	private CanonicalIngestionContextResolver canonicalIngestionContextResolver;

	@MockitoBean
	private CanonicalOrderPersistenceService canonicalOrderPersistenceService;

	@MockitoBean
	private CanonicalOrderQueryService canonicalOrderQueryService;

	@MockitoBean
	private CanonicalOrderCommandService canonicalOrderCommandService;

	@Test
	void contextLoads() {
	}

}
