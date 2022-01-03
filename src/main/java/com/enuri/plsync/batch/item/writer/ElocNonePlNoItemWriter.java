package com.enuri.plsync.batch.item.writer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.enuri.plsync.batch.item.writer.abstracts.AbstractPlStCdItemWriter;
import com.enuri.plsync.enums.ModifyChgDtm;
import com.enuri.plsync.enums.ResultCnt;
import com.enuri.plsync.model.diff.dto.DiffDto;
import com.enuri.plsync.model.eloc.Pricelist;

/**
  * @description : Eloc에 없고 hbase/pg에만 있는 plno 찾아 pl_st_cd 5처리함
  * @Since : 2021. 9. 1.
  * @Author : AnJaeKoo
  * @History :
  */
@Component
@StepScope
public class ElocNonePlNoItemWriter extends AbstractPlStCdItemWriter {

	@Value("${spring.batch.sleep-time}")
	private long sleepTime;

	public ElocNonePlNoItemWriter() {
		super(ResultCnt.HM_NONE_CNT);
	}

	@Override
	protected int setHbasePlStCd(List<? extends DiffDto> list, int spmCd) throws InterruptedException {
		var hGdCdList = new ArrayList<String>();
    	var hPlNoList = new ArrayList<Long>();

    	// 비교 연산을 위해 리스트 추가
        list.forEach(dto -> {
        	hGdCdList.add(dto.getGdCd());
        	hPlNoList.add(dto.getPlNo());
        });

        // Hbase 현재 있는건인지 실시간 조회
        List<Map<String, Object>> hTbEpPlList = hbaseRepository.findTbEpPlByPlNoIn(spmCd, hGdCdList);
        hTbEpPlList.removeIf(map -> !hPlNoList.contains((long)map.get("PL_NO")));

        // eloc 실시간 조회
        List<Pricelist> ePricelists = pricelistRepository.findByPlNoIn(hTbEpPlList.stream()
        																		.map(map -> (long)map.get("PL_NO"))
        																		.collect(Collectors.toList()));

        List<Long> ePlNoList = getPlNoList(ePricelists);	// eloc entity에서 plno만 추출 (비교위해)
        hTbEpPlList.removeIf(map -> ePlNoList.contains((long)map.get("PL_NO")));	// eloc에 없고 hbase에 있는것만 추출
        Thread.sleep(sleepTime);
		return hbaseRepository.upsertTbEpPlSetplStCd5(hTbEpPlList, ModifyChgDtm.FALSE);	// update 처리
	}

	@Override
	protected int setPgPlStCd(List<? extends DiffDto> list, int spmCd) {
		List<Long> pPlNoList = list.stream()	// 비교연산을 위해 plNo 모음 생성
    			.map(DiffDto::getPlNo)
    			.collect(Collectors.toList());

        List<Map<String, Object>> pTbEpPlList = tbEpPlRepository.findByPlNoIn(spmCd, pPlNoList);	//pg에 현재 있는건인지 실시간 확인
        List<Pricelist> ePricelists = pricelistRepository.findByPlNoIn(pPlNoList);	// eloc 실시간 조회
        List<Long> ePlNoList = getPlNoList(ePricelists);	// eloc entity에서 plno만 추출 (비교위해)
        pTbEpPlList.removeIf(map -> ePlNoList.contains((long) map.get("plNo")));	// eloc에 없고 pg에 있는것만 추출
		return tbEpPlRepository.setPlStCd(spmCd, pTbEpPlList);	// update 처리
	}

	private List<Long> getPlNoList(List<Pricelist> list) {
		return list.stream().map(Pricelist::getPL_NO).collect(Collectors.toList());
	}
}
