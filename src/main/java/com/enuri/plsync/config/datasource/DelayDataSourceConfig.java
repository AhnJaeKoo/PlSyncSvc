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
@EnableJpaRepositories(basePackages = "com.enuri.plsync.repository.delay",
						entityManagerFactoryRef = "delayEntityManagerFactory",
						transactionManagerRef = "delayTransactionManager",
						repositoryImplementationPostfix = "Impl")
public class DelayDataSourceConfig {

	@Bean
	@ConfigurationProperties(prefix = "spring.data-source-config.delay.jpa.property")
	public Properties delayHibernateProperties() {
		return new Properties();
	}

	@Bean
	@ConfigurationProperties(prefix = "spring.data-source-config.delay.datasource.hikari")
	public HikariConfig delayHikariConfig() {
		return new HikariConfig();
	}

    @Bean
    public DataSource delayDataSource() throws SQLException {
        return new HikariDataSource(delayHikariConfig());
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean delayEntityManagerFactory() throws SQLException {
        final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(delayDataSource());
        em.setPackagesToScan("com.enuri.plsync.model.delay");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        em.setJpaProperties(delayHibernateProperties());
        em.setPersistenceUnitName("delay");
        return em;
    }

	@Bean
	public JpaTransactionManager delayTransactionManager() throws SQLException {
		final JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(delayEntityManagerFactory().getObject());
		return transactionManager;
	}
}
