package com.enuri.plsync.batch.job;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.enuri.plsync.batch.item.reader.DiffItemReader;
import com.enuri.plsync.batch.item.writer.ElocNonePlNoItemWriter;
import com.enuri.plsync.batch.item.writer.HasBothStepItemWriter;
import com.enuri.plsync.batch.item.writer.SourceNonePlNoElocStepItemWriter;
import com.enuri.plsync.batch.job.listener.JobResultListener;
import com.enuri.plsync.enums.DiffType;
import com.enuri.plsync.enums.StCdType;
import com.enuri.plsync.enums.SyncDbType;
import com.enuri.plsync.model.diff.dto.DiffDto;
import com.enuri.plsync.repository.diff.TbDiffStHstRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class ElocSyncJobConfig {

	@Value("${spring.batch.chunk-size}")
	private int chunkSize;
	private String failed;

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final TbDiffStHstRepository tbDiffStHstRepository;
	private final HasBothStepItemWriter hasBothStepItemWriter;
	private final ElocNonePlNoItemWriter elocNonePlNoItemWriter;
	private final SourceNonePlNoElocStepItemWriter sourceNonePlNoElocStepItemWriter;
	private final DiffItemReader diffItemReader;

	@Resource
	private DataSource hbaseDataSource;
	@Resource
	private DataSource diffDataSource;

	@PostConstruct
	private void init() {
		failed = ExitStatus.FAILED.getExitCode();
	}

	@Bean
	public Job elocSyncJob() throws NumberFormatException, Exception {
		return jobBuilderFactory.get("elocSyncJob")
				.preventRestart()
				.listener(new JobResultListener(tbDiffStHstRepository))
				.start(startMsStCdStep(0))	// 테이블 상태값 S로 변경 -> 실패시 종료
					.on(failed)
					.end()
				.from(startMsStCdStep(0))
					.on("*")
					.to(elocNonePlNoStep()) 		//Hbase만 있고 eloc 없는경우 Hbase PlStCd = 5로 변경
					.next(sourceNonePlNoElocStep()) //Hbase에 없는경우 eloc plno 삭제
					.next(hasBothElocStep()) 		//Hbase, eloc 둘다 있는경우 PlStCd동기화 => 이건 너무 많아서 그냥 diff기준으로 넣는다.
					.end()
				.build();
	}

	@Bean
	@JobScope
	public Step startMsStCdStep(@Value("#{jobParameters[no]}") long no) {
		return stepBuilderFactory.get("startMsStCdStep").tasklet((contribution, chunkContext) -> {
			if (tbDiffStHstRepository.setStCd(no, SyncDbType.ELOC, StCdType.START) != 1) {
				contribution.setExitStatus(ExitStatus.FAILED);
			}

			return RepeatStatus.FINISHED;
		}).build();
	}

	@Bean
	@JobScope
	public Step elocNonePlNoStep() throws Exception {
		return stepBuilderFactory.get("elocNonePlNoStep")
				.<DiffDto, DiffDto>chunk(chunkSize)
				.reader(elocNonePlNoItemReader(""))	// diff 테이블 조회
				.writer(elocNonePlNoItemWriter)
				.build();
	}

	@Bean
	@JobScope
	public Step sourceNonePlNoElocStep() throws Exception {
		return stepBuilderFactory.get("sourceNonePlNoElocStep")
				.<DiffDto, DiffDto>chunk(chunkSize)
				.reader(sourceNonePlNoElocItemReader(""))
				.writer(sourceNonePlNoElocStepItemWriter)
				.build();
	}

	@Bean
	@JobScope
	public Step hasBothElocStep() throws Exception {
		return stepBuilderFactory.get("hasBothElocStep")
				.<DiffDto, DiffDto>chunk(chunkSize)
				.reader(hasBothNonePlNoElocItemReader(""))
				.writer(hasBothStepItemWriter)
				.build();
	}

	@Bean
	@StepScope
	public JdbcPagingItemReader<DiffDto> elocNonePlNoItemReader(@Value("#{jobParameters[tableName]}") String tableName) throws Exception {
	    return diffItemReader.jdbcPagingItemReaderBuilder("elocNonePlNoItemReader", tableName, SyncDbType.ELOC, DiffType.TARGET_NULL);
	}

	@Bean
	@StepScope
	public JdbcPagingItemReader<DiffDto> sourceNonePlNoElocItemReader(@Value("#{jobParameters[tableName]}") String tableName) throws Exception {
	    return diffItemReader.jdbcPagingItemReaderBuilder("sourceNonePlNoElocItemReader", tableName, SyncDbType.ELOC, DiffType.SOURCE_NULL);
	}

	@Bean
	@StepScope
	public JdbcPagingItemReader<DiffDto> hasBothNonePlNoElocItemReader(@Value("#{jobParameters[tableName]}") String tableName) throws Exception {
	    return diffItemReader.jdbcPagingItemReaderBuilder("hasBothNonePlNoElocItemReader", tableName, SyncDbType.ELOC, DiffType.HAS_BOTH);
	}
}