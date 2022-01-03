package com.enuri.plsync.batch.job.listener;

import java.util.Map;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.JobParameter;

import com.enuri.plsync.enums.StCdType;
import com.enuri.plsync.enums.SyncDbType;
import com.enuri.plsync.repository.diff.TbDiffStHstRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JobResultListener implements JobExecutionListener {
	private final TbDiffStHstRepository tbDiffStHstRepository;
	private String tableName;
	private String jobName;
	private long jobId;
	private long no;
	private SyncDbType syncDbType;

	@Override
	public void beforeJob(JobExecution jobExecution) {
		getResource(jobExecution);
		printLog(jobExecution.getStatus().toString());
	}

	@Override
	public void afterJob(JobExecution jobExecution) {
		printLog(jobExecution.getStatus().toString());

		StCdType stCdType = switch (jobExecution.getStatus()) {
			case COMPLETED -> StCdType.END;
			case FAILED -> StCdType.FAIL;
			default -> throw new IllegalArgumentException("Unexpected value: " + jobExecution.getStatus());
		};

		tbDiffStHstRepository.setStCd(no, syncDbType, stCdType);
	}

	private void printLog(String status) {
		log.info("{} : job = {}({}), no = {}, tableName = {}", status, jobName, jobId, no, tableName);
	}

	private void getResource(JobExecution jobExecution) {
		Map<String, JobParameter> map = jobExecution.getJobParameters().getParameters();
		tableName = map.get("tableName").toString();
		jobName = jobExecution.getJobInstance().getJobName();
		jobId = jobExecution.getJobInstance().getId();
		no = Long.parseLong(map.get("no").toString());
		syncDbType = getSyncDbType(map.get("syncDbType").toString());
	}

	private SyncDbType getSyncDbType (String type) {
		return switch (type) {
			case "MAIN" -> SyncDbType.MAIN;
			case "ELOC" -> SyncDbType.ELOC;
			default -> throw new IllegalArgumentException("Unexpected value: " + type);
		};
	}
}
