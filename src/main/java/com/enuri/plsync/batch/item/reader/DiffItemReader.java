package com.enuri.plsync.batch.item.reader;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Component;

import com.enuri.plsync.enums.DiffType;
import com.enuri.plsync.enums.SyncDbType;
import com.enuri.plsync.model.diff.dto.DiffDto;
import com.enuri.plsync.repository.diff.TbDiffStHstRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DiffItemReader {
	@Value("${spring.batch.chunk-size}")
	private int chunkSize;

	@Resource
	private DataSource diffDataSource;
	private final TbDiffStHstRepository tbDiffStHstRepository;

	public JdbcPagingItemReader<DiffDto> jdbcPagingItemReaderBuilder(String itemName, String tableName, SyncDbType syncDbType, DiffType diffType) throws Exception {
		return new JdbcPagingItemReaderBuilder<DiffDto>()
				.name(itemName)
		        .pageSize(chunkSize)
		        .fetchSize(chunkSize)
		        .dataSource(diffDataSource)
		        .rowMapper(new BeanPropertyRowMapper<>(DiffDto.class))
		        .queryProvider(tbDiffStHstRepository.createQueryProvider(tableName, syncDbType, diffType))
		        .build();
	}
}
