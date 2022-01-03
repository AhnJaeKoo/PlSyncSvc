package com.enuri.plsync.model.diff;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="TB_EP_SPM")
@Getter
@Setter
public class TbEpSpm implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private String spmNm;
	private String spmAlias;
	private int spmCd;
}