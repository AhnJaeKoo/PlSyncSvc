package com.enuri.plsync.util;

import javax.persistence.Query;

import org.hibernate.transform.Transformers;

import lombok.experimental.UtilityClass;

/**
  * @description : JPA 관련된 재사용 가능한 메소드들로 구성.
  * 			   추후 소스가 커지면 상세분류해야함. 현재는 JPA에 대한 UTIL이라고만 크게 지칭함
  * @Since : 2021. 6. 17.
  * @Author : AnJaeKoo
  * @History :
  */
@UtilityClass
public final class JpaUtil {

	/**
	  * @description : query map 방식으로 적용
	  * @param : Query
	  * @return : Query
	  */
	@SuppressWarnings("deprecation")
	public Query setQueryToMap(Query query) {
		return query.unwrap(org.hibernate.query.Query.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
	}
}
