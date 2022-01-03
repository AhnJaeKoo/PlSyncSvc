package com.enuri.plsync.batch.item.writer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.enuri.plsync.batch.item.writer.abstracts.AbstractPlStCdItemWriter;
import com.enuri.plsync.enums.ModifyChgDtm;
import com.enuri.plsync.enums.ResultCnt;
import com.enuri.plsync.enums.SyncDbType;
import com.enuri.plsync.model.diff.dto.DiffDto;

/**
  * @description : Hbase/pg <> main/eloc => pl_st_cd 값이 다른경우 target DB의 값으로 source DB값 변경한다.
  * @Since : 2021. 9. 1.
  * @Author : AnJaeKoo
  * @History :
  */
@Component
@StepScope
public class HasBothStepItemWriter extends AbstractPlStCdItemWriter {

	public HasBothStepItemWriter() {
		super(ResultCnt.HOM_STCD_DIFF_CNT);
	}

	@Value("${spring.batch.sleep-time}")
	private long sleepTime;

	@Override
	protected int setHbasePlStCd(List<? extends DiffDto> list, int spmCd) throws InterruptedException {
		var paramList = new ArrayList<Map<String, Object>>();

		list.forEach(dto -> {	// 비교 연산을 위해 리스트 추가
			var map = new HashMap<String, Object>();
			map.put("SPM_CD", spmCd);
			map.put("GD_CD", dto.getGdCd());
			map.put("PL_NO", dto.getPlNo());
			map.put("PL_ST_CD", getPlstcd(dto));	// target DB 기준으로 상태값 변경
			paramList.add(map);
		});

		Thread.sleep(sleepTime);
		return hbaseRepository.upsertTbEpPlSetplStCd5(paramList, ModifyChgDtm.FALSE);	// update 처리
	}

	@Override
	protected int setPgPlStCd(List<? extends DiffDto> list, int spmCd) {
		var paramList = new ArrayList<Map<String, Object>>();

		list.forEach(dto -> {	// 비교 연산을 위해 리스트 추가
			var map = new HashMap<String, Object>();
			map.put("plNo", dto.getPlNo());
			map.put("plStCd", getPlstcd(dto));	// target DB 기준으로 상태값 변경
			paramList.add(map);
		});

		return tbEpPlRepository.setPlStCd(spmCd, paramList);	// update 처리
	}

	private String getPlstcd(DiffDto dto) {
		return SyncDbType.MAIN == syncDbType ? dto.getOPlstcd() : dto.getMPlstcd();
	}
}
