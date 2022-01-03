package com.enuri.plsync.repository.main.custom;

import java.util.List;

import com.enuri.plsync.model.main.TblPricelist;

public interface TblPricelistRepositoryCustom {

	public List<TblPricelist> findByPlNoIn(List<Long> plNos);
}