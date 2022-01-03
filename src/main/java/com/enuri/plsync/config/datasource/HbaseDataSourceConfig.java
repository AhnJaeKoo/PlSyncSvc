package com.enuri.plsync.config.datasource;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.phoenix.queryserver.client.Driver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

@Configuration
public class HbaseDataSourceConfig {

	@Value("${spring.data-source-config.hbase.datasource.jdbc-url}")
	private String jdbcUrl;
	@Value("${spring.data-source-config.hbase.datasource.driver-class-name}")
	private Class<? extends Driver> driverClassName;
	@Value("${spring.data-source-config.hbase.datasource.username}")
	private String username;
	@Value("${spring.data-source-config.hbase.datasource.password}")
	private String password;

	@Bean
	public DataSource hbaseDataSource() throws SQLException {
		SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
		dataSource.setUrl(jdbcUrl);
		dataSource.setDriverClass(driverClassName);
		dataSource.setUsername(username);
		dataSource.setPassword(password);
		return dataSource;
	}

	@Bean
	public NamedParameterJdbcTemplate hbaseJdbcTemplate() throws SQLException {
		JdbcTemplate template = new JdbcTemplate(hbaseDataSource());
		return new NamedParameterJdbcTemplate(template);
	}
}
