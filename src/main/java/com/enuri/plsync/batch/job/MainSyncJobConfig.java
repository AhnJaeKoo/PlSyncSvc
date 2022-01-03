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
import com.enuri.plsync.batch.item.writer.HasBothStepItemWriter;
import com.enuri.plsync.batch.item.writer.MainNonePlNoItemWriter;
import com.enuri.plsync.batch.item.writer.SourceNonePlNoMainStepItemWriter;
import com.enuri.plsync.batch.job.listener.JobResultListener;
import com.enuri.plsync.enums.DiffType;
import com.enuri.plsync.enums.StCdType;
import com.enuri.plsync.enums.SyncDbType;
import com.enuri.plsync.model.diff.dto.DiffDto;
import com.enuri.plsync.repository.diff.TbDiffStHstRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class MainSyncJobConfig {

	@Value("${spring.batch.chunk-size}")
	private int chunkSize;
	private String failed;

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final TbDiffStHstRepository tbDiffStHstRepository;
	private final HasBothStepItemWriter hasBothStepItemWriter;
	private final MainNonePlNoItemWriter mainNonePlNoItemWriter;
	private final SourceNonePlNoMainStepItemWriter sourceNonePlNoMainStepItemWriter;
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
	public Job mainSyncJob() throws NumberFormatException, Exception {
		return jobBuilderFactory.get("mainSyncJob")
				.preventRestart()
				.listener(new JobResultListener(tbDiffStHstRepository))
				.start(startOraStCdStep(0))	// 테이블 상태값 S로 변경 -> 실패시 종료
					.on(failed)
					.end()
				.from(startOraStCdStep(0))
					.on("*")
					.to(mainNonePlNoStep()) 		// Hbase만 있고 oracle 없는경우 Hbase PlStCd = 5로 변경
					.next(sourceNonePlNoMainStep()) // Hbase에 없는경우 오라클 plno 삭제
					.next(hasBothMainStep()) 		// Hbase, oracle 둘다 있는경우 PlStCd동기화 => 이건 너무 많아서 그냥 diff기준으로 넣는다.
					.end()
				.build();
	}

	@Bean
	@JobScope
	public Step startOraStCdStep(@Value("#{jobParameters[no]}") long no) {
		return stepBuilderFactory.get("startOraStCdStep").tasklet((contribution, chunkContext) -> {
			if (tbDiffStHstRepository.setStCd(no, SyncDbType.MAIN, StCdType.START) != 1) {
				contribution.setExitStatus(ExitStatus.FAILED);
			}

			return RepeatStatus.FINISHED;
		}).build();
	}

	@Bean
	@JobScope
	public Step mainNonePlNoStep() throws Exception {
		return stepBuilderFactory.get("mainNonePlNoStep")
				.<DiffDto, DiffDto>chunk(chunkSize)
				.reader(mainNonePlNoItemReader(""))	// diff 테이블 조회
				.writer(mainNonePlNoItemWriter)
				.build();
	}

	@Bean
	@JobScope
	public Step sourceNonePlNoMainStep() throws Exception {
		return stepBuilderFactory.get("sourceNonePlNoMainStep")
				.<DiffDto, DiffDto>chunk(chunkSize)
				.reader(sourceNonePlNoMainItemReader(""))
				.writer(sourceNonePlNoMainStepItemWriter)
				.build();
	}

	@Bean
	@JobScope
	public Step hasBothMainStep() throws Exception {
		return stepBuilderFactory.get("hasBothMainStep")
				.<DiffDto, DiffDto>chunk(chunkSize)
				.reader(hasBothNonePlNoMainItemReader(""))
				.writer(hasBothStepItemWriter)
				.build();
	}

	@Bean
	@StepScope
	public JdbcPagingItemReader<DiffDto> mainNonePlNoItemReader(@Value("#{jobParameters[tableName]}") String tableName) throws Exception {
	    return diffItemReader.jdbcPagingItemReaderBuilder("mainNonePlNoItemReader",tableName, SyncDbType.MAIN, DiffType.TARGET_NULL);
	}

	@Bean
	@StepScope
	public JdbcPagingItemReader<DiffDto> sourceNonePlNoMainItemReader(@Value("#{jobParameters[tableName]}") String tableName) throws Exception {
	    return diffItemReader.jdbcPagingItemReaderBuilder("sourceNonePlNoMainItemReader", tableName, SyncDbType.MAIN, DiffType.SOURCE_NULL);
	}

	@Bean
	@StepScope
	public JdbcPagingItemReader<DiffDto> hasBothNonePlNoMainItemReader(@Value("#{jobParameters[tableName]}") String tableName) throws Exception {
	    return diffItemReader.jdbcPagingItemReaderBuilder("hasBothNonePlNoMainItemReader", tableName, SyncDbType.MAIN, DiffType.HAS_BOTH);
	}
}