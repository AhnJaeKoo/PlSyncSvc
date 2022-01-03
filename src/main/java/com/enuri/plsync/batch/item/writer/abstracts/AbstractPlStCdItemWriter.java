package com.enuri.plsync.batch.item.writer.abstracts;

import java.util.List;
import java.util.Optional;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.enuri.plsync.enums.ResultCnt;
import com.enuri.plsync.enums.SyncDbType;
import com.enuri.plsync.global.Const;
import com.enuri.plsync.model.diff.TbDiffStHst;
import com.enuri.plsync.model.diff.dto.DiffDto;
import com.enuri.plsync.repository.diff.TbDiffStHstRepository;
import com.enuri.plsync.repository.eloc.PricelistRepository;
import com.enuri.plsync.repository.hbase.HbaseRepository;
import com.enuri.plsync.repository.main.TblPricelistRepository;
import com.enuri.plsync.repository.pgpl.TbEpPlRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
  * @description : Eloc에 없고 hbase/pg에만 있는 plno 찾아 pl_st_cd 5처리함
  * @Since : 2021. 9. 1.
  * @Author : AnJaeKoo
  * @History :
  */
@Component
@StepScope
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractPlStCdItemWriter implements ItemWriter<DiffDto> {
	@Autowired
	protected HbaseRepository hbaseRepository;
	@Autowired
	protected TbEpPlRepository tbEpPlRepository;
	@Autowired
	protected PricelistRepository pricelistRepository;
	@Autowired
	protected TbDiffStHstRepository tbDiffStHstRepository;
	@Autowired
	protected TblPricelistRepository tblPricelistRepository;

	@Value("#{jobParameters[spmCd]}")
	protected int spmCd;
	@Value("#{jobParameters[syncDbType]}")
	protected SyncDbType syncDbType;
	@Value("#{jobParameters[no]}")
	protected long no;

	protected final ResultCnt resultCnt;

	@Override
	public void write(List<? extends DiffDto> items) throws Exception {
    	int upsertCnt = switch (spmCd) {
    		case Const.SHOP_TMON, Const.SHOP_WMP -> setPgPlStCd(items, spmCd);
    		default -> setHbasePlStCd(items, spmCd);
    	};

        log.info("upsertCnt = {}건", upsertCnt);

        Optional<TbDiffStHst> opt = tbDiffStHstRepository.findById(no);

        if (opt.isPresent()) {
        	setResultCnt(opt.get(), upsertCnt);
        }
	}

	private void setResultCnt(TbDiffStHst tbDiffStHst, int upsertCnt) {
		switch (resultCnt) {
			case HO_NONE_CNT -> tbDiffStHst.setHoNoneCnt(tbDiffStHst.getHoNoneCnt() + upsertCnt);
			case HM_NONE_CNT -> tbDiffStHst.setHmNoneCnt(tbDiffStHst.getHmNoneCnt() + upsertCnt);
			case OH_NONE_CNT -> tbDiffStHst.setOhNoneCnt(tbDiffStHst.getOhNoneCnt() + upsertCnt);
			case MH_NONE_CNT -> tbDiffStHst.setMhNoneCnt(tbDiffStHst.getMhNoneCnt() + upsertCnt);
			case HOM_STCD_DIFF_CNT -> tbDiffStHst.setHomStcdDiffCnt(tbDiffStHst.getHomStcdDiffCnt() + upsertCnt);
		};

		tbDiffStHstRepository.save(tbDiffStHst);
	};

	protected abstract int setHbasePlStCd(List<? extends DiffDto> list, int spmCd) throws InterruptedException;
	protected abstract int setPgPlStCd(List<? extends DiffDto> list, int spmCd);
}
