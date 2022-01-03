package com.enuri.plsync.repository.eloc.custom.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.enuri.plsync.model.eloc.Pricelist;
import com.enuri.plsync.repository.eloc.custom.PricelistRepositoryCustom;

@Repository
@Transactional(transactionManager="elocTransactionManager")
public class PricelistRepositoryCustomImpl implements PricelistRepositoryCustom {

	@PersistenceContext(unitName = "eloc")
	private EntityManager elocEm;

	@SuppressWarnings("unchecked")
	@Override
	public List<Pricelist> findByPlNoIn(List<Long> plNos) {
		StringBuilder joinSql = new StringBuilder();
		List<Pricelist> result = new ArrayList<>();

		plNos.forEach(plNo -> {
			if (joinSql.length() == 0) {
				joinSql.append("SELECT %d AS pl_no ".formatted(plNo));
			} else {
				joinSql.append(" UNION ALL SELECT %d AS pl_no ".formatted(plNo));
			}
		});

		if (!plNos.isEmpty()) {
			String sql = """
					SELECT a.pl_no,
					       '5' AS pl_status,
					       a.pl_vcode AS pl_vcode,
					       a.pl_goodscode AS pl_goodscode
					  FROM pricelist a,
					       (%s) b
					 WHERE a.pl_no = b.pl_no
					""".formatted(joinSql.toString());

			Query query = elocEm.createNativeQuery(sql, Pricelist.class);
			result = query.getResultList();
		}

		return result;
	}
}