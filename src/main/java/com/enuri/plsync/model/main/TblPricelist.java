package com.enuri.plsync.model.main;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="TBL_PRICELIST")
@Getter
@Setter
public class TblPricelist implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "PL_NO")
	private long PL_NO;
	@Column(name = "STATUS")
	private String PL_ST_CD;
	@Column(name = "SHOPCODE")
	private int SPM_CD;
	@Column(name = "GOODSCODE")
	private String GD_CD;
}