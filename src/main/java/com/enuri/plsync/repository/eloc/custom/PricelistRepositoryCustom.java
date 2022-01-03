package com.enuri.plsync.repository.eloc.custom;

import java.util.List;

import com.enuri.plsync.model.eloc.Pricelist;

public interface PricelistRepositoryCustom {
	public List<Pricelist> findByPlNoIn(List<Long> plNos);
}