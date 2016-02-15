package cn.zxd.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan(basePackages = { "cn.zxd.service" })
@Import(value = { AspectConfig.class })
public class Config {
	
}
