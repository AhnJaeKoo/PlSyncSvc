package com.enuri.plsync.repository.diff.custom.impl;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.sql.DataSource;

import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.enuri.plsync.enums.DiffType;
import com.enuri.plsync.enums.StCdType;
import com.enuri.plsync.enums.SyncDbType;
import com.enuri.plsync.repository.diff.custom.DiffRepository;
import com.enuri.plsync.util.JpaUtil;

@Repository
@Transactional(transactionManager="diffTransactionManager")
public class DiffRepositoryImpl implements DiffRepository {

	@PersistenceContext(unitName = "diff")
	private EntityManager diffEm;

	@Resource
	private DataSource diffDataSource;

	@SuppressWarnings("unchecked")
	@Override
	public List<Map<String, Object>> findDiffTable(SyncDbType syncDbType) {

		String whereSql = switch (syncDbType) {
			case MAIN -> "a.oraStCd = :type";
			case ELOC -> "a.msStCd = :type";
			default -> "";
		};

		String sql = """
				SELECT a.no AS no,
					   a.diffTbNm AS diffTbNm,
					   b.spmCd AS spmCd
				  FROM TbDiffStHst a INNER JOIN FETCH TbEpSpm b on a.spmNm = b.spmAlias
				 WHERE %s
				ORDER BY a.no
				""".formatted(whereSql);

		Query query = diffEm.createQuery(sql);
		query.setParameter("type", StCdType.READY.getType());
		query = JpaUtil.setQueryToMap(query);

		return query.getResultList();
	}

	@Override
	public PagingQueryProvider createQueryProvider(String tableName, SyncDbType syncDbType, DiffType diffType) throws Exception {
		String selectSql = """
					no,
					coalesce(gd_cd, '') AS gdCd,
					coalesce(pl_st_cd, '') AS plStCd,
					coalesce(pl_no, 0) AS plNo,
					coalesce(m_pl_no, 0) AS mPlNo,
					coalesce(o_pl_no, 0) AS oPlNo,
					o_plstcd AS oPlstcd,
					m_plstcd AS mPlstcd
				""";

		String whereSql = switch (syncDbType) {
			case MAIN ->
				switch (diffType) {
					case HAS_BOTH -> """
							pl_no is not null
							AND o_pl_no is not null
							AND pl_st_cd <> o_plstcd
							AND m_plstcd <> '5'
							AND o_plstcd = '5'
							""";
					case SOURCE_NULL -> """
							pl_no is null
							AND m_pl_no is null
							AND o_pl_no is not null
							AND o_plstcd <> '5'
							""";
					case TARGET_NULL ->	"""
							pl_no is not null
							AND m_pl_no is not null
							AND o_pl_no is null
							AND pl_st_cd <> '5'
							""";
				};
			case ELOC ->
				switch (diffType) {
					case HAS_BOTH -> """
							pl_no is not null
							AND m_pl_no is not null
							AND pl_st_cd <> m_plstcd
							AND m_plstcd = '5'
							""";
					case SOURCE_NULL -> """
							pl_no is null
							AND m_pl_no is not null
							AND m_plstcd <> '5'
							""";
					case TARGET_NULL ->	"""
							pl_no is not null
							AND m_pl_no is null
							AND pl_st_cd <> '5'
							""";
				};
		};

	    SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
	    queryProvider.setDataSource(diffDataSource); // Database에 맞는 PagingQueryProvider를 선택하기 위해
	    queryProvider.setSelectClause(selectSql);
	    queryProvider.setFromClause("from %s".formatted(tableName));
	    queryProvider.setWhereClause(whereSql);
	    queryProvider.setSortKey("no");

	    return queryProvider.getObject();
	}

	@Override
	public int setStCd(long no, SyncDbType syncDbType, StCdType stCdType) {
		String setSql = switch (syncDbType) {
		case MAIN ->
			switch (stCdType) {
				case START -> "m.oraStCd = :stCd, m.oraSDtm = :dtm";
				case END, FAIL -> "m.oraStCd = :stCd, m.oraEDtm = :dtm";
				default -> throw new IllegalArgumentException("Unexpected value: " + stCdType);
			};
		case ELOC ->
			switch (stCdType) {
				case START ->	"m.msStCd = :stCd, m.msSDtm = :dtm";
				case END, FAIL -> "m.msStCd = :stCd, m.msEDtm = :dtm";
				default -> throw new IllegalArgumentException("Unexpected value: " + stCdType);
			};
		};

		String sql = """
				UPDATE TbDiffStHst m
				   SET %s
				 WHERE m.no = :no
				""".formatted(setSql);

		Query query = diffEm.createQuery(sql);
		query.setParameter("stCd", stCdType.getType());
		query.setParameter("no", no);
		query.setParameter("dtm", new Timestamp(System.currentTimeMillis()));

		return query.executeUpdate();
	}

	@Override
	public boolean isStart(SyncDbType syncDbType) {
		String whereSql = switch (syncDbType) {
			case MAIN -> "m.oraStCd = :type";
			case ELOC -> "m.msStCd = :type";
			default -> "";
		};

		String sql = """
				SELECT count(m)
				  FROM TbDiffStHst m
				 WHERE %s
				""".formatted(whereSql);

		TypedQuery<Long> query = diffEm.createQuery(sql, Long.class);
		query.setParameter("type", StCdType.START.getType());
		long cnt = query.getSingleResult();

		return cnt == 0;
	}
}