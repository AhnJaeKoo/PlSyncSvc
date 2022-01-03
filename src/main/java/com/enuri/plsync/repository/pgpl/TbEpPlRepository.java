package com.enuri.plsync.repository.pgpl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.enuri.plsync.model.pgpl.PTbEpPl;
import com.enuri.plsync.repository.pgpl.custom.TbEpPlRepositoryCustom;

@Repository
public interface TbEpPlRepository extends JpaRepository<PTbEpPl, Long>, TbEpPlRepositoryCustom {

}