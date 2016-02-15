package cn.zxd.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import cn.zxd.aspect.CacheAspect;

@Configuration
@EnableAspectJAutoProxy
public class AspectConfig {

	@Bean
	public CacheAspect getMemcacheAspect() {
		CacheAspect m = null;
		try {
			m = new CacheAspect("172.16.28.92:11211",true,true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return m;
	}
}
