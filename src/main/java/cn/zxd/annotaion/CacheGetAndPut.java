package cn.zxd.annotaion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.LOCAL_VARIABLE })
public @interface CacheGetAndPut {

	String key() default "";

	/**
	 * 如果该字段不为空,则真正的key为Keys.mapping.get(key)+suffix
	 * 
	 * @return
	 */
	String[] suffix() default {};

	int expire() default 120;

	String[] paramKeys() default {};
}
