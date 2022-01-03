package com.enuri.plsync.config.datasource;

import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@EnableJpaRepositories(basePackages = "com.enuri.plsync.repository.eloc",
						entityManagerFactoryRef = "elocEntityManagerFactory",
						transactionManagerRef = "elocTransactionManager",
						repositoryImplementationPostfix = "Impl")
public class ElocDataSourceConfig {

	@Bean
	@ConfigurationProperties(prefix = "spring.data-source-config.eloc.jpa.property")
	public Properties elocHibernateProperties() {
		return new Properties();
	}

	@Bean
	@ConfigurationProperties(prefix = "spring.data-source-config.eloc.datasource.hikari")
	public HikariConfig elocHikariConfig() {
		return new HikariConfig();
	}

	@Bean
	public DataSource elocDataSource() throws SQLException {
	    return new HikariDataSource(elocHikariConfig());
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean elocEntityManagerFactory() throws SQLException {
	    final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
	    em.setDataSource(elocDataSource());
	    em.setPackagesToScan("com.enuri.plsync.model.eloc");
	    em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
	    em.setJpaProperties(elocHibernateProperties());
	    em.setPersistenceUnitName("eloc");
	    return em;
	}

	@Bean
	public JpaTransactionManager elocTransactionManager() throws SQLException {
		final JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(elocEntityManagerFactory().getObject());
		return transactionManager;
	}
}
