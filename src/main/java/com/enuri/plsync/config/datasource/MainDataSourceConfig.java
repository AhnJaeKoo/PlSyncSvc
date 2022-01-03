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
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@EnableJpaRepositories(basePackages = "com.enuri.plsync.repository.main",
						entityManagerFactoryRef = "mainEntityManagerFactory",
						transactionManagerRef = "mainTransactionManager",
						repositoryImplementationPostfix = "Impl")
@EnableTransactionManagement
public class MainDataSourceConfig {

	@Bean
	@ConfigurationProperties(prefix = "spring.data-source-config.main.jpa.property")
    public Properties mainHibernateProperties() {
		return new Properties();
    }

	@Bean
	@ConfigurationProperties(prefix = "spring.data-source-config.main.datasource.hikari")
    public HikariConfig mainHikariConfig() {
        return new HikariConfig();
    }

    @Bean
    public DataSource mainDataSource() throws SQLException {
        return new HikariDataSource(mainHikariConfig());
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean mainEntityManagerFactory() throws SQLException {
    	final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(mainDataSource());
        em.setPackagesToScan("com.enuri.plsync.model.main");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        em.setJpaProperties(mainHibernateProperties());
        em.setPersistenceUnitName("main");
        return em;
    }

	@Bean
	public JpaTransactionManager mainTransactionManager() throws SQLException {
		final JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(mainEntityManagerFactory().getObject());
		return transactionManager;
	}
}
