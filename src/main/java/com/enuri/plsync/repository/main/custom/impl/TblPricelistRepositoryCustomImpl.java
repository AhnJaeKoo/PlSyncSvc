package com.enuri.plsync.repository.main.custom.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.enuri.plsync.model.main.TblPricelist;
import com.enuri.plsync.repository.main.custom.TblPricelistRepositoryCustom;

@Repository
@Transactional(transactionManager="mainTransactionManager")
public class TblPricelistRepositoryCustomImpl implements TblPricelistRepositoryCustom {

	@PersistenceContext(unitName = "main")
	private EntityManager mainEm;

	@SuppressWarnings("unchecked")
	@Override
	public List<TblPricelist> findByPlNoIn(List<Long> plNos) {
		StringBuilder joinSql = new StringBuilder();
		List<TblPricelist> result = new ArrayList<>();

		plNos.forEach(plNo -> {
			if (joinSql.length() == 0) {
				joinSql.append("SELECT %d AS pl_no FROM dual ".formatted(plNo));
			} else {
				joinSql.append(" UNION ALL SELECT %d AS pl_no FROM dual ".formatted(plNo));
			}
		});

		if (!plNos.isEmpty()) {
			String sql = """
					SELECT a.pl_no AS PL_NO,
					       '5' AS STATUS,
					       a.shop_code AS SHOPCODE,
					       a.goodscode AS GOODSCODE
					  FROM tbl_pricelist a,
					       (%s) b
					 WHERE a.pl_no = b.pl_no
					""".formatted(joinSql.toString());

			Query query = mainEm.createNativeQuery(sql, TblPricelist.class);
			result = query.getResultList();
		}

		return result;
	}
}