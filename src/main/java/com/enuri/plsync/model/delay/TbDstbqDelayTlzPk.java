package com.enuri.plsync.model.delay;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor // 생성자 자동 생성
@Setter
@Getter
@Embeddable
public class TbDstbqDelayTlzPk implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = "dstbq_dtm_str")
	private String dstbqDtmStr;
	@Column(name = "srv_dcd")
	private String srvDcd;
	@Column(name = "gd_dcd")
	private String gdDcd;
}
