package com.enuri.plsync.repository.eloc;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.enuri.plsync.model.eloc.Pricelist;
import com.enuri.plsync.repository.eloc.custom.PricelistRepositoryCustom;

@Repository
public interface PricelistRepository extends JpaRepository<Pricelist, Long>, PricelistRepositoryCustom {

}