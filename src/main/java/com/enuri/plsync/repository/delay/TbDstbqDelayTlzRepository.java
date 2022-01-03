package com.enuri.plsync.repository.delay;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.enuri.plsync.model.delay.TbDstbqDelayTlz;
import com.enuri.plsync.model.delay.TbDstbqDelayTlzPk;


@Repository
public interface TbDstbqDelayTlzRepository extends JpaRepository<TbDstbqDelayTlz, TbDstbqDelayTlzPk> {

	@Query(value = """
			SELECT count(*)
			FROM tb_dstbq_delay_tlz
			WHERE srv_dcd in ('s', 'm')
			AND gd_dcd in ('1','2')
			AND ins_ts >= current_timestamp + '-1min'
			AND max_delay_ms > 1800
			AND min_delay_ms > 300
			""", nativeQuery = true)
	long countByDelay();
}
