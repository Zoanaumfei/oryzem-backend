package com.oryzem.backend;

import com.oryzem.backend.modules.projects.repository.ProjectRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.event.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
@ConfigurationPropertiesScan
public class BackendApplication {

	private static final Logger log = LoggerFactory.getLogger(BackendApplication.class);

	private final ObjectProvider<ProjectRepository> projectRepositoryProvider;

	public BackendApplication(ObjectProvider<ProjectRepository> projectRepositoryProvider) {
		this.projectRepositoryProvider = projectRepositoryProvider;
	}

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void logProjectTableName() {
		projectRepositoryProvider.ifAvailable(repository ->
				log.info("Projects DynamoDB table: {}", repository.getTableName()));
	}
}
