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
@EnableJpaRepositories(basePackages = "com.enuri.plsync.repository.pgpl",
						entityManagerFactoryRef = "pgEpPlEntityManagerFactory",
						transactionManagerRef = "pgEpPlTransactionManager",
						repositoryImplementationPostfix = "Impl")
public class PgEpPlDataSourceConfig {

	@Bean
	@ConfigurationProperties(prefix = "spring.data-source-config.pgpl.jpa.property")
	public Properties pgEpPlHibernateProperties() {
		return new Properties();
	}

	@Bean
	@ConfigurationProperties(prefix = "spring.data-source-config.pgpl.datasource.hikari")
	public HikariConfig pgEpPlHikariConfig() {
		return new HikariConfig();
	}

    @Bean
    public DataSource pgEpPlDataSource() throws SQLException {
        return new HikariDataSource(pgEpPlHikariConfig());
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean pgEpPlEntityManagerFactory() throws SQLException {
        final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(pgEpPlDataSource());
        em.setPackagesToScan("com.enuri.plsync.model.pgpl");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        em.setJpaProperties(pgEpPlHibernateProperties());
        em.setPersistenceUnitName("pgEpPl");
        return em;
    }

	@Bean
	public JpaTransactionManager pgEpPlTransactionManager() throws SQLException {
		final JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(pgEpPlEntityManagerFactory().getObject());
		return transactionManager;
	}
}
