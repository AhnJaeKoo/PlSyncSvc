package com.enuri.plsync.model.diff.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DiffDto implements Serializable {

	private static final long serialVersionUID = 1L;

	private long no;
	private String gdCd;
	private String plStCd;
	private int modelNo;
	private long plNo;
	private long mPlNo;
	private long oPlNo;
	private String oPlstcd;
	private String mPlstcd;
}
