package com.enuri.plsync.repository.pgpl.custom.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.sql.DataSource;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.enuri.gm.common.statement.NamedPreparedStatement;
import com.enuri.gm.common.util.MapUtil;
import com.enuri.plsync.model.pgpl.PTbEpPl;
import com.enuri.plsync.model.pgpl.PTbEpPl6508;
import com.enuri.plsync.model.pgpl.PTbEpPl7861;
import com.enuri.plsync.repository.pgpl.custom.TbEpPlRepositoryCustom;
import com.enuri.plsync.util.JpaUtil;

import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@Transactional(transactionManager="pgEpPlTransactionManager")
public class TbEpPlRepositoryCustomImpl implements TbEpPlRepositoryCustom {

	@PersistenceContext(unitName = "pgEpPl")
	private EntityManager pgEpPlEm;

	@Resource
	private DataSource pgEpPlDataSource;

	private Class<?> getEntityName(int spmCd) {
		return switch (spmCd) {
			case 6641 -> PTbEpPl.class;
			case 6508 -> PTbEpPl6508.class;
			case 7861 -> PTbEpPl7861.class;
			default -> throw new IllegalArgumentException("Unexpected value: " + spmCd);
		};
	}

	private String getTableName(int spmCd) {
		return switch (spmCd) {
			case 6641 -> "tb_ep_pl";
			case 6508 -> "tb_ep_pl_6508";
			case 7861 -> "tb_ep_pl_7861";
			default -> throw new IllegalArgumentException("Unexpected value: " + spmCd);
		};
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Long> findPlNoByPlNoIn(int spmCd, List<Long> plNos) {
		String sql = """
				SELECT m.plNo
				  FROM %s m
				 WHERE m.plNo IN :plNos
				""".formatted(getEntityName(spmCd).getSimpleName());

		Query query = pgEpPlEm.createQuery(sql);
		query.setParameter("plNos", plNos);
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Map<String, Object>> findByPlNoIn(int spmCd, List<Long> plNos) {
		String sql = """
				SELECT m.plNo AS plNo,
					   m.spmCd AS spmCd,
					   m.gdCd AS gdCd,
					   '5' AS plStCd
				  FROM %s m
				 WHERE m.plNo IN :plNos
				   AND plStCd <> '5'
				""".formatted(getEntityName(spmCd).getSimpleName());

		Query query = pgEpPlEm.createQuery(sql);
		query.setParameter("plNos", plNos);
		return JpaUtil.setQueryToMap(query).getResultList();
	}

	@Override
	public int setPlStCd(int spmCd, List<Map<String, Object>> list) {
		int result = 0;
		String sql = """
				/* TbEpPlRepositoryCustomImpl:setPlStCd:91 */
				UPDATE %s
				   SET pl_st_cd = :plStCd
				 WHERE pl_no = :plNo
				""".formatted(getTableName(spmCd));

		try {
			Connection con = pgEpPlDataSource.getConnection();
			@Cleanup NamedPreparedStatement pstmt = NamedPreparedStatement.prepareStatement(con, sql);

			for (Map<String, Object> map : list) {
				pstmt.clearParameters();
				pstmt.setString("plStCd", map.get("plStCd").toString());
				pstmt.setLong("plNo", (long)map.get("plNo"));
				pstmt.addBatch();
			}

			result = IntStream.of(pstmt.executeBatch()).sum();
		} catch (SQLException e) {
			log.error("", e);
		}

		return result;
	}

	public int insertTbEpPl(int spmCd, List<Map<String, Object>> list) {
		int result = 0;

		String sql = """
				/* TbEpPlRepositoryCustomImpl:insertTbEpPl:125 */
				INSERT INTO %s
					(PL_NO,
					 PL_ST_CD,
					 SPM_CD,
					 GD_CD,
					 CHG_DTM)
				VALUES (:PL_NO,
						:PL_ST_CD,
						:SPM_CD,
						:GD_CD,
						:CHG_DTM)
				""".formatted(getTableName(spmCd));

		try {
			Connection con = pgEpPlDataSource.getConnection();
			@Cleanup NamedPreparedStatement pstmt = NamedPreparedStatement.prepareStatement(con, sql);
			Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now().minusDays(31));

			for (Map<String, Object> map : list) {
				pstmt.clearParameters();
				pstmt.setLong("PL_NO", (long)map.get("PL_NO"));
				pstmt.setString("PL_ST_CD", MapUtil.valueToStringOrEmpty(map, "PL_ST_CD"));
				pstmt.setInt("SPM_CD", (int)map.get("SPM_CD"));
				pstmt.setString("GD_CD", MapUtil.valueToStringOrEmpty(map, "GD_CD"));
				pstmt.setTimestamp("CHG_DTM", timestamp);
				pstmt.addBatch();
			}

			result = IntStream.of(pstmt.executeBatch()).sum();
		} catch (SQLException e) {
			log.error("", e);
		}

		return result;
	}
}