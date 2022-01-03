package com.enuri.plsync.model.eloc;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="PRICELIST")
@Getter
@Setter
public class Pricelist implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "pl_no")
	private long PL_NO;
	@Column(name = "pl_status")
	private String PL_ST_CD;
	@Column(name = "pl_vcode")
	private int SPM_CD;
	@Column(name = "pl_goodscode")
	private String GD_CD;
}