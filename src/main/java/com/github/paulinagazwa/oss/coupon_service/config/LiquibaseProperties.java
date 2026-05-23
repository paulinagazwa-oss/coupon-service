package com.github.paulinagazwa.oss.coupon_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "spring.liquibase")
public class LiquibaseProperties {

	private boolean enabled = true;

	private String changeLog = "classpath:db/changelog/changelog.xml";

	private String contexts;

	private String defaultSchema;

	private String liquibaseSchema;

	private String liquibaseTablespace;

	private String databaseChangeLogTable = "DATABASECHANGELOG";

	private String databaseChangeLogLockTable = "DATABASECHANGELOGLOCK";

	private boolean dropFirst = false;

	private boolean shouldRun = true;

}
