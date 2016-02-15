package cn.zxd.annotaion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CacheDelete {

	String key() default "";

	/**
	 * 如果该字段不为空,则真正的key为Keys.mapping.get(key)+suffix
	 * 
	 * @return
	 */
	String[] suffix() default {};

	/**
	 * 方法返回值作为key的一部分
	 * 
	 * @return
	 */
	boolean keyWithReturnValue() default false;

	/**
	 * 忽略suffix
	 * 
	 * @return
	 */
	boolean ignoreSuffix() default false;
}
