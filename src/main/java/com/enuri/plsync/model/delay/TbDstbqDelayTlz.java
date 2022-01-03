package com.enuri.plsync.model.delay;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="TB_DSTBQ_DELAY_TLZ")
@Getter
@Setter
public class TbDstbqDelayTlz implements Serializable {
	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private TbDstbqDelayTlzPk id;
	private int minDelayMs;
	private int maxDelayMs;
	private Timestamp ins_ts;
}