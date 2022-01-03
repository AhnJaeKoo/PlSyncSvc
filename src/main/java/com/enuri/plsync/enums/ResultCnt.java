package com.enuri.plsync.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCnt {
	HO_NONE_CNT("source 기준으로 오라클 없는 count"),
	HM_NONE_CNT("source 기준으로 eloc 없는 count"),
	OH_NONE_CNT("오라클 기준으로 source에 없는 count"),
	MH_NONE_CNT("eloc 기준으로 source에 없는 count"),
	HOM_STCD_DIFF_CNT("source 기준으로 오라클/eloc 상태값 다른 count");

	private String desc;
}
