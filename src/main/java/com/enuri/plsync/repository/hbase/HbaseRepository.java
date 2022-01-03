package com.enuri.plsync.repository.hbase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import javax.annotation.Resource;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.enuri.plsync.enums.ModifyChgDtm;

@Repository
public class HbaseRepository {

	@Resource
	private NamedParameterJdbcTemplate hbaseJdbcTemplate;

	public List<Long> findPlNoByPlNoIn(int spmCd, List<String> gdCdList) {
		var map = new HashMap<String, Object>();
		map.put("spmCd", spmCd);
		map.put("gdCds", gdCdList);

		return hbaseJdbcTemplate.queryForList("""
				SELECT pl_no
				  FROM tb_ep_pl
				 WHERE spm_cd = :spmCd
				   AND gd_cd IN (:gdCds)
			""", map, Long.class);
	}

	public List<Map<String, Object>> findTbEpPlByPlNoIn(int spmCd, List<String> gdCdList) {
		var map = new HashMap<String, Object>();
		map.put("spmCd", spmCd);
		map.put("gdCds", gdCdList);

		return hbaseJdbcTemplate.queryForList("""
				SELECT spm_cd,
				       gd_cd,
				       pl_no,
				       '5' AS pl_st_cd
				  FROM tb_ep_pl
				 WHERE spm_cd = :spmCd
				   AND gd_cd IN (:gdCds)
				   AND pl_st_cd <> '5'
			""", map);
	}

	@SuppressWarnings("unchecked")
	public int upsertTbEpPlSetplStCd5(List<Map<String, Object>> list, ModifyChgDtm modifyChgDtm) {
		String sql = switch (modifyChgDtm) {
			case TRUE -> """
			  			UPSERT INTO tb_ep_pl (spm_cd, gd_cd, pl_no, pl_st_cd, chg_dtm)
							VALUES (:SPM_CD, :GD_CD, :PL_NO, :PL_ST_CD, now() - 31)
							ON DUPLICATE KEY UPDATE pl_st_cd = :PL_ST_CD, chg_dtm = now() - 31
					""";
			case FALSE -> """
			  			UPSERT INTO tb_ep_pl (spm_cd, gd_cd, pl_no, pl_st_cd)
							VALUES (:SPM_CD, :GD_CD, :PL_NO, :PL_ST_CD)
							ON DUPLICATE KEY UPDATE pl_st_cd = :PL_ST_CD
					""";
		};

		return IntStream.of(hbaseJdbcTemplate.batchUpdate(sql, list.toArray(Map[]::new))).sum();
	}
}
