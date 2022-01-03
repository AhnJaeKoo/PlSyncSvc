package com.enuri.plsync.repository.main;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.enuri.plsync.model.main.TblPricelist;
import com.enuri.plsync.repository.main.custom.TblPricelistRepositoryCustom;

@Repository
public interface TblPricelistRepository extends JpaRepository<TblPricelist, Long>, TblPricelistRepositoryCustom {

	@Modifying
	@Query("delete TblPricelist m where m.PL_NO in :plNos")
	int deleteByPlNoIn(@Param("plNos") List<Long> plNos);
}