package com.enuri.plsync.repository.pgpl.custom;

import java.util.List;
import java.util.Map;

public interface TbEpPlRepositoryCustom {

	public List<Long> findPlNoByPlNoIn(int spmCd, List<Long> plNos);
	public List<Map<String, Object>> findByPlNoIn(int spmCd, List<Long> plNos);
	public int setPlStCd(int spmCd, List<Map<String, Object>> list);
	public int insertTbEpPl(int spmCd, List<Map<String, Object>> list);
}