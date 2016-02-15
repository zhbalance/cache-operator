package cn.zxd.memcached;

import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.exception.MemcachedException;

public class MemcacheUtil {

	// 默认的命名空间

	private static Logger logger = Logger.getLogger(MemcacheUtil.class);

	/**
	 * 从缓存中通过key获取value
	 * 
	 * @param memcachedClient
	 * @param key
	 * @return
	 * @throws MemcachedException
	 * @throws InterruptedException
	 * @throws TimeoutException
	 */
	public static Object get(MemcachedClient memcachedClient, String key) throws MemcachedException, InterruptedException, TimeoutException {
		return memcachedClient.get(key);
	}

	/**
	 * 将value放入缓存,默认的过期时间为WeixinConstant.CACHE_EXPIRE_TIME
	 * 
	 * @param memcachedClient
	 * @param key
	 * @param value
	 * @throws MemcachedException
	 * @throws InterruptedException
	 * @throws TimeoutException
	 */
	public static void put(MemcachedClient memcachedClient, String key, Object value, int expire)
			throws MemcachedException, InterruptedException, TimeoutException {
		memcachedClient.set(key, expire, value);
	}

	/**
	 * 从缓存中删除一个key
	 * 
	 * @param memcachedClient
	 * @param key
	 * @throws MemcachedException
	 * @throws InterruptedException
	 * @throws TimeoutException
	 */
	public static void delete(MemcachedClient memcachedClient, String key) throws MemcachedException, InterruptedException, TimeoutException {
		boolean bool = memcachedClient.delete(key);
		logger.info("MemcachedClient delete bool=" + bool);

	}
}
