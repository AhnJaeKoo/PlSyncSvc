package com.enuri.plsync.service;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Service;

import com.enuri.plsync.enums.StCdType;
import com.enuri.plsync.enums.SyncDbType;
import com.enuri.plsync.repository.delay.TbDstbqDelayTlzRepository;
import com.enuri.plsync.repository.diff.TbDiffStHstRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobService {

	private final JobLauncher jobLauncher;
	private final Job mainSyncJob;
	private final Job elocSyncJob;
	private final TbDiffStHstRepository tbDiffStHstRepository;
	private final TbDstbqDelayTlzRepository tbDstbqDelayTlzRepository;

	private JobParameters createInitJobParam(Map<String, Object> param) {
		var map = new LinkedHashMap<String, JobParameter>();
		map.put("time", new JobParameter(System.currentTimeMillis()));
		map.put("tableName", new JobParameter((String) param.get("diffTbNm")));
		map.put("no", new JobParameter((long) param.get("no")));
		map.put("spmCd", new JobParameter(param.get("spmCd").toString()));
		map.put("syncDbType", new JobParameter(param.get("syncDbType").toString()));
		return new JobParameters(map);
	}

	public String mainSyncJob() {
		return runJob(SyncDbType.MAIN, mainSyncJob);
	}

	public String elocSyncJob() {
		return runJob(SyncDbType.ELOC, elocSyncJob);
	}

	private String runJob(SyncDbType syncDbType, Job job) {
		String result = "job empty~!!";

		// 기존 동작건이 있으면 실행 안한다.
		if (tbDiffStHstRepository.isStart(syncDbType)) {
			// 분산큐 지연건이 없을때만 구동
			if (tbDstbqDelayTlzRepository.countByDelay() == 0) {
				// diff 테이블 리스트 조회..테이블명, no, 샵코드 추출
				for (Map<String, Object> map : tbDiffStHstRepository.findDiffTable(syncDbType)) {
					try {
						map.put("syncDbType", syncDbType);
						jobLauncher.run(job, createInitJobParam(map));
						result = "success job~!!";
					} catch (Exception e) {
						log.error("", e);
						result = "fail job!!";
						tbDiffStHstRepository.setStCd((long)map.get("no"), syncDbType, StCdType.FAIL);
						break;
					}
				}
			} else {
				result = "분산큐 지연~!!";
			}
		} else {
			result = "already job~!!";
		}

		log.info("runJob result = {}", result);
		return result;
	}
}