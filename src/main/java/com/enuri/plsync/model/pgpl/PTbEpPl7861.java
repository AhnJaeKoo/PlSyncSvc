package com.enuri.plsync.model.pgpl;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name = "tb_ep_pl_7861")
@NoArgsConstructor
@AllArgsConstructor
public class PTbEpPl7861 implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	private long plNo;
	private int spmCd;
	private String gdCd;
	private String plStCd;
}