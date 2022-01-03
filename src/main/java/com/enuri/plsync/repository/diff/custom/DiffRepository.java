package com.enuri.plsync.repository.diff.custom;

import java.util.List;
import java.util.Map;

import org.springframework.batch.item.database.PagingQueryProvider;

import com.enuri.plsync.enums.DiffType;
import com.enuri.plsync.enums.StCdType;
import com.enuri.plsync.enums.SyncDbType;

public interface DiffRepository {

	public List<Map<String, Object>> findDiffTable(SyncDbType syncDbType);
	public PagingQueryProvider createQueryProvider(String tableName, SyncDbType syncDbType, DiffType diffType) throws Exception;
	public int setStCd(long no, SyncDbType syncDbType, StCdType stCdType);
	public boolean isStart(SyncDbType syncDbType);
}