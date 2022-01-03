package com.enuri.plsync.model.diff;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="tb_diff_st_hst")
@Getter
@Setter
public class TbDiffStHst implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private long no;
	private String spmNm;
	private String diffTbNm;
	@Column(name="diff_c_dtm")
	private Timestamp diffCDtm;
	@Column(name="ora_s_dtm")
	private Timestamp oraSDtm;
	@Column(name="ora_e_dtm")
	private Timestamp oraEDtm;
	private String oraStCd;
	@Column(name="ms_s_dtm")
	private Timestamp msSDtm;
	@Column(name="ms_e_dtm")
	private Timestamp msEDtm;
	private String msStCd;
	private int hoNoneCnt;
	private int hmNoneCnt;
	private int ohNoneCnt;
	private int mhNoneCnt;
	private int homStcdDiffCnt;
}