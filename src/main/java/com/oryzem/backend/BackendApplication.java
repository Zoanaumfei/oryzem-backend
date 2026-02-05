package com.oryzem.backend;

import com.oryzem.backend.modules.projects.repository.ProjectRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
@ConfigurationPropertiesScan
public class BackendApplication {

	private static final Logger log = LoggerFactory.getLogger(BackendApplication.class);

	@Autowired
	private ProjectRepository projectRepository;

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void logProjectTableName() {
		log.info("Projects DynamoDB table: {}", projectRepository.getTableName());
	}
}
