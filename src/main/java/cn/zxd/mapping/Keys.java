package cn.zxd.mapping;

import java.util.HashMap;
import java.util.Map;

/**
 * 缓存的key
 */
public class Keys {

	public static Map<String, String> mapping = new HashMap<String, String>(128);

	static {
		//本地测试用
		mapping.put("test", String.valueOf(5454780041114831l));
	}

	public static void main(String[] args) {
		System.out.println(Math.random());
	}
}
