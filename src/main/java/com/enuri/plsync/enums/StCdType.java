package com.enuri.plsync.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StCdType {
	READY("C"), START("S"), END("E"), FAIL("F");

	private String type;
}
