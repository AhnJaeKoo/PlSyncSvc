package com.enuri.plsync.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DiffType {
	HAS_BOTH("양쪽 다 plno 있음"), SOURCE_NULL("원본DB에 plno 없음"), TARGET_NULL("비교대상 DB에 plno 없음");

	private String desc;
}
