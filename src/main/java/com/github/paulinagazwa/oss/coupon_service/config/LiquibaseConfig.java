package com.github.paulinagazwa.oss.coupon_service.config;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@EnableConfigurationProperties(LiquibaseProperties.class)
public class LiquibaseConfig {

	@Bean
	@ConditionalOnProperty(prefix = "spring.liquibase", name = "enabled", havingValue = "true", matchIfMissing = true)
	public SpringLiquibase liquibase(DataSource dataSource, LiquibaseProperties properties) {

		SpringLiquibase liquibase = new SpringLiquibase();

		liquibase.setDataSource(dataSource);
		liquibase.setChangeLog(properties.getChangeLog());
		liquibase.setContexts(properties.getContexts());
		liquibase.setDefaultSchema(properties.getDefaultSchema());
		liquibase.setLiquibaseSchema(properties.getLiquibaseSchema());
		liquibase.setLiquibaseTablespace(properties.getLiquibaseTablespace());
		liquibase.setDatabaseChangeLogTable(properties.getDatabaseChangeLogTable());
		liquibase.setDatabaseChangeLogLockTable(properties.getDatabaseChangeLogLockTable());
		liquibase.setDropFirst(properties.isDropFirst());
		liquibase.setShouldRun(properties.isShouldRun() && properties.isEnabled());

		return liquibase;
	}

	@Bean
	public static BeanFactoryPostProcessor dependsOnLiquibasePostProcessor() {

		return beanFactory -> {
			if (beanFactory.containsBeanDefinition("entityManagerFactory")
					&& beanFactory.containsBeanDefinition("liquibase")) {
				beanFactory.getBeanDefinition("entityManagerFactory")
						.setDependsOn("liquibase");
			}
		};
	}
}
