package com.liveclass.notification;

import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.postgresql.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

	public static final PostgreSQLContainer postgresContainer = new PostgreSQLContainer("postgres:latest");

	static {
		postgresContainer.start();
	}
}
