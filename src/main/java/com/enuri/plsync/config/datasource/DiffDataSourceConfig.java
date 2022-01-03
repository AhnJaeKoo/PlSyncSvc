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
@EnableJpaRepositories(basePackages = "com.enuri.plsync.repository.diff",
						entityManagerFactoryRef = "diffEntityManagerFactory",
						transactionManagerRef = "diffTransactionManager",
						repositoryImplementationPostfix = "Impl")
public class DiffDataSourceConfig {

	@Bean
	@ConfigurationProperties(prefix = "spring.data-source-config.diff.jpa.property")
	public Properties diffHibernateProperties() {
		return new Properties();
	}

	@Bean
	@ConfigurationProperties(prefix = "spring.data-source-config.diff.datasource.hikari")
	public HikariConfig diffHikariConfig() {
		return new HikariConfig();
	}

    @Bean
    public DataSource diffDataSource() throws SQLException {
        return new HikariDataSource(diffHikariConfig());
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean diffEntityManagerFactory() throws SQLException {
        final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(diffDataSource());
        em.setPackagesToScan("com.enuri.plsync.model.diff");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        em.setJpaProperties(diffHibernateProperties());
        em.setPersistenceUnitName("diff");
        return em;
    }

	@Bean
	public JpaTransactionManager diffTransactionManager() throws SQLException {
		final JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(diffEntityManagerFactory().getObject());
		return transactionManager;
	}
}
