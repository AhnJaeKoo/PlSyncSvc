package com.enuri.plsync.repository.diff;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.enuri.plsync.model.diff.TbDiffStHst;
import com.enuri.plsync.repository.diff.custom.DiffRepository;

@Repository
public interface TbDiffStHstRepository extends JpaRepository<TbDiffStHst, Long>, DiffRepository {

}
