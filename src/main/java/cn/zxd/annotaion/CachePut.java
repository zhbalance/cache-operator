package cn.zxd.annotaion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.LOCAL_VARIABLE })
public @interface CachePut {

	String key() default "";

	int expire() default 86400;
}
