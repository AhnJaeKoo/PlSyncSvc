package com.enuri.plsync.config.datasource;

import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@EnableJpaRepositories(basePackages = "com.enuri.plsync.repository.batch",
						entityManagerFactoryRef = "batchEntityManagerFactory",
						transactionManagerRef = "batchTransactionManager")
public class BatchDataSourceConfig {

	@Bean
	@ConfigurationProperties(prefix = "spring.data-source-config.batch.jpa.property")
	public Properties batchHibernateProperties() {
		return new Properties();
	}

	@Bean
	@ConfigurationProperties(prefix = "spring.data-source-config.batch.datasource.hikari")
	public HikariConfig batchHikariConfig() {
		return new HikariConfig();
	}

	@Primary
    @Bean
    public DataSource batchDataSource() throws SQLException {
        return new HikariDataSource(batchHikariConfig());
    }

	@Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean batchEntityManagerFactory() throws SQLException {
        final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(batchDataSource());
        em.setPackagesToScan("com.enuri.plsync.model.batch");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        em.setJpaProperties(batchHibernateProperties());
        em.setPersistenceUnitName("batch");
        return em;
    }

	@Primary
	@Bean
	public JpaTransactionManager batchTransactionManager() throws SQLException {
		final JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(batchEntityManagerFactory().getObject());
		return transactionManager;
	}
}
