package com.enuri.plsync.batch.item.writer;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.enuri.gm.common.util.MapUtil;
import com.enuri.plsync.batch.item.writer.abstracts.AbstractPlStCdItemWriter;
import com.enuri.plsync.enums.ModifyChgDtm;
import com.enuri.plsync.enums.ResultCnt;
import com.enuri.plsync.model.diff.dto.DiffDto;
import com.enuri.plsync.model.eloc.Pricelist;

/**
  * @description : pl이 hbase/pg (X) main/eloc (O) sourceDB에 pl_st_cd 5처리 후 chg_dtm 시간을 한달전으로 변경한다.
  * @Since : 2021. 9. 1.
  * @Author : AnJaeKoo
  * @History :
  */
@Component
@StepScope
public class SourceNonePlNoElocStepItemWriter extends AbstractPlStCdItemWriter {

	@Value("${spring.batch.sleep-time}")
	private long sleepTime;

	public SourceNonePlNoElocStepItemWriter() {
		super(ResultCnt.MH_NONE_CNT);
	}

	@Override
	protected int setHbasePlStCd(List<? extends DiffDto> list, int spmCd) throws InterruptedException {
		List<Long> mPlNoList = getMPlNoList(list);	// 비교연산을 위해 oPlNo 모음 생성
        List<Pricelist> ePricelists = pricelistRepository.findByPlNoIn(mPlNoList);	// eloc 조회
        List<String> eGdCdList = ePricelists.stream()	// eloc 조회내역에서 gdCd만 list로 추출
        		.map(Pricelist::getGD_CD)
        		.collect(Collectors.toList());

        List<Long> hPlNoList = hbaseRepository.findPlNoByPlNoIn(spmCd, eGdCdList);	// hbase에서 pl_no 추출
        ePricelists.removeIf(entity -> hPlNoList.contains(entity.getPL_NO()));	// hbase에 있는건 제거
        Thread.sleep(sleepTime);
		return hbaseRepository.upsertTbEpPlSetplStCd5(pricelistToList(ePricelists), ModifyChgDtm.TRUE);	// hbase에 없는건들이니 upsert into 로 5 처리
	}

	@Override
	protected int setPgPlStCd(List<? extends DiffDto> list, int spmCd) {
		List<Long> mPlNoList = getMPlNoList(list);	// 비교연산을 위해 oPlNo 모음 생성
	    List<Pricelist> ePricelists = pricelistRepository.findByPlNoIn(mPlNoList);	// eloc 조회
	    List<Long> pPlNoList = tbEpPlRepository.findPlNoByPlNoIn(spmCd, mPlNoList);	//pg에 현재 있는건인지 실시간 확인
	    ePricelists.removeIf(entity -> pPlNoList.contains(entity.getPL_NO()));	// pg에 있는건 제거
		return tbEpPlRepository.insertTbEpPl(spmCd, pricelistToList(ePricelists));	// pg에 없는건들이니 upsert into 로 5 처리
	}

	private List<Map<String, Object>> pricelistToList(List<Pricelist> ePricelists) {
		return ePricelists.stream().map(MapUtil::ConverObjectToMap).collect(Collectors.toList());
	}

	private List<Long> getMPlNoList(List<? extends DiffDto> list) {
		return list.stream().map(DiffDto::getMPlNo).collect(Collectors.toList());
	}
}