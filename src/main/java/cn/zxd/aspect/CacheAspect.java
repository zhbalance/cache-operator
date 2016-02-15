package cn.zxd.aspect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import cn.zxd.annotaion.CacheBatchDelete;
import cn.zxd.annotaion.CacheDelete;
import cn.zxd.annotaion.CacheGetAndPut;
import cn.zxd.mapping.Keys;
import cn.zxd.memcached.MemcacheUtil;
import net.rubyeye.xmemcached.KeyIterator;
import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.impl.KetamaMemcachedSessionLocator;
import net.rubyeye.xmemcached.utils.AddrUtil;

@Aspect
public class CacheAspect {

	private Logger logger = Logger.getLogger(this.getClass());

	private MemcachedClient memcachedClient;

	// true表示显示日志信息,false表示不显示
	private boolean debug;

	// true表示使用缓存,false表示不使用
	private boolean cache;

	private List<InetSocketAddress> addressList;

	/**
	 * constructor
	 * 
	 * @param server memcache server地址
	 * @param debug 是否打印日志
	 * @param cache 是否使用缓存
	 * @throws Exception
	 */
	public CacheAspect(String server, boolean debug, boolean cache) throws Exception {
		if (StringUtils.isBlank(server)) {
			logger.error("memcache服务器地址为空,构造失败");
		} else {
			logger(logger, "memcache地址:" + server);
			this.addressList = AddrUtil.getAddresses(server);
			XMemcachedClientBuilder builder = new XMemcachedClientBuilder(addressList);
			builder.setSessionLocator(new KetamaMemcachedSessionLocator());
			memcachedClient = builder.build();
			this.debug = debug;
			this.cache = cache;
		}
	}

	@SuppressWarnings("deprecation")
	@Around(value = "@annotation(d)", argNames = "d")
	public Object delete(ProceedingJoinPoint joinPoint, CacheDelete d) throws Throwable {
		if (!this.cache) {
			return joinPoint.proceed();
		}
		if (StringUtils.isBlank(d.key())) {
			logger.error("原始key为空");
			return joinPoint.proceed();
		}

		String key = d.key();
		logger(logger, "CacheDelete:原始key=" + key + "#" + Arrays.toString(d.suffix()));
		if (Keys.mapping.containsKey(key)) {
			key = Keys.mapping.get(key);
			if (d.suffix().length > 0) {
				for (String arg : d.suffix()) {
					Object val = this.getValue(joinPoint, arg);
					if (val != null) {
						key += "#" + val;
					}
				}
			}
		} else {
			logger(logger, "CacheDelete:Keys.mapping中未找到值,key=" + key);
			return joinPoint.proceed();
		}

		// 忽略suffix清除缓存
		if (d.ignoreSuffix()) {
			logger(logger, "CacheDelete:ignoreSuffix=true");
			if (this.addressList == null || this.addressList.size() == 0) {
				logger.error("memcache服务器地址为空,忽略suffix清除缓存失败");
				return joinPoint.proceed();
			}
			for (InetSocketAddress address : addressList) {
				KeyIterator it = this.memcachedClient.getKeyIterator(address);
				if (it != null) {
					while (it.hasNext()) {
						try {
							String cacheKey = it.next();
							if (StringUtils.contains(cacheKey, key)) {
								logger(logger, "CacheDelete:忽略suffix清除缓存完成,key=" + key + ",cacheKey=" + cacheKey);
								MemcacheUtil.delete(memcachedClient, cacheKey);
							}
						} catch (NoSuchElementException e) {
							logger.error("NoSuchElementException", e);
						}
					}
				}
			}
		} else {
			logger(logger, "CacheDelete:ignoreSuffix=false");
			if (d.keyWithReturnValue()) {
				Object obj = joinPoint.proceed();
				logger(logger, "CacheDelete:方法返回值作为key的一部分,key=" + obj);
				if (obj != null) {
					key += "#" + obj;
				}
			}
			logger(logger, "CacheDelete:清除缓存,key=" + key);
			MemcacheUtil.delete(memcachedClient, key);
		}
		return joinPoint.proceed();
	}

	@SuppressWarnings("deprecation")
	@Around(value = "@annotation(bd)", argNames = "bd")
	public Object batchDelete(ProceedingJoinPoint joinPoint, CacheBatchDelete bd) throws Throwable {
		if (!this.cache) {
			return joinPoint.proceed();
		}
		if (bd.keys().length == 0) {
			logger.error("原始keys为空");
			return joinPoint.proceed();
		}

		if (this.addressList == null || this.addressList.size() == 0) {
			logger.error("memcache服务器地址为空,忽略suffix清除缓存失败");
			return joinPoint.proceed();
		}

		String[] keys = bd.keys();
		for (String key : keys) {
			logger(logger, "CacheBatchDelete:原始key=" + key);
			if (Keys.mapping.containsKey(key)) {
				key = Keys.mapping.get(key);
			} else {
				logger(logger, "Keys.mapping中未找到值,key=" + key);
				continue;
			}
			for (InetSocketAddress address : addressList) {
				KeyIterator it = this.memcachedClient.getKeyIterator(address);
				if (it != null) {
					while (it.hasNext()) {
						try {
							String cacheKey = it.next();
							if (StringUtils.contains(cacheKey, key)) {
								logger(logger, "CacheBatchDelete:清除缓存完成,key=" + key + ",cacheKey=" + cacheKey);
								MemcacheUtil.delete(memcachedClient, cacheKey);
							}
						} catch (NoSuchElementException e) {
							logger.error("NoSuchElementException", e);
						}
					}
				}
			}
		}
		return joinPoint.proceed();
	}

	@Around(value = "@annotation(gp)", argNames = "gp")
	public Object getAndPut(ProceedingJoinPoint joinPoint, CacheGetAndPut gp) throws Throwable {
		Object obj = null;
		if (!this.cache) {
			return joinPoint.proceed();
		}
		StringBuilder key = new StringBuilder();
		if (gp.paramKeys().length > 0) {
			Signature sign = joinPoint.getSignature();
			if (sign != null) {
				key.append(sign.getDeclaringTypeName()).append("#").append(sign.getName());
			}
			MethodSignature signature = (MethodSignature) joinPoint.getSignature();
			String[] argsKeyArr = signature.getParameterNames();
			if (argsKeyArr != null && argsKeyArr.length > 0) {
				Object[] argsValueArr = joinPoint.getArgs();
				for (String arg : gp.paramKeys()) {
					for (int i = 0; i < argsKeyArr.length; i++) {
						if (StringUtils.equals(arg, argsKeyArr[i])) {
							key.append("#").append(argsValueArr[i]);
						}
					}
				}
			}
			logger(logger, "CacheGetAndPut:原始key=" + key.toString() + "#" + Arrays.toString(gp.suffix()));
		} else if (StringUtils.isNotBlank(gp.key())) {
			String key1 = gp.key();
			logger(logger, "CacheGetAndPut:原始key=" + key1 + "#" + Arrays.toString(gp.suffix()));
			if (Keys.mapping.containsKey(key1)) {
				key1 = Keys.mapping.get(key1);
				if (gp.suffix().length > 0) {
					for (String arg : gp.suffix()) {
						Object val = this.getValue(joinPoint, arg);
						if (val != null) {
							key1 += "#" + val;
						}
					}
				}
			} else {
				logger(logger, "Keys.mapping中未找到值,key=" + key1);
			}
			key.append(key1);
		}
		if (StringUtils.isNotBlank(key.toString())) {
			logger(logger, "CacheGetAndPut:转化后的key=" + key.toString());
			obj = MemcacheUtil.get(memcachedClient, key.toString());
			if (obj != null) {
				logger(logger, "CacheGetAndPut:已从缓存中获取");
				return obj;
			}
			obj = joinPoint.proceed();
			if (obj != null) {
				logger(logger, "CacheGetAndPut:已从方法返回值获取并添加到缓存");
				MemcacheUtil.put(memcachedClient, key.toString(), obj, gp.expire());
			} else {
				logger(logger, "方法返回值为null,不缓存");
			}
		} else {
			logger(logger, "CacheGetAndPut:转化后的key为空");
			obj = joinPoint.proceed();
		}
		return obj;
	}

	private void logger(Logger logger, String message) {
		if (this.debug) {
			logger.warn(message);
		}
	}

	// 从map或者DO中获取,key/filed对应的值
	@SuppressWarnings("rawtypes")
	private Object getValue(Object obj, String field)
			throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Object value = null;
		if (obj instanceof Map) {
			Map map = (Map) obj;
			value = map.get(field);
		} else {
			Class<?> cl = obj.getClass();
			String firstLetter = field.substring(0, 1).toUpperCase();
			String getMethodName = "get" + firstLetter + field.substring(1);
			Method getMethod = cl.getMethod(getMethodName, new Class[] {});
			value = getMethod.invoke(obj, new Object[] {});
		}
		return value;
	}

	private Object getValue(ProceedingJoinPoint joinPoint, String field) {
		try {
			MethodSignature signature = (MethodSignature) joinPoint.getSignature();
			String[] argsKeyArr = signature.getParameterNames();
			if (argsKeyArr == null || argsKeyArr.length == 0) {
				logger.error("getValue argsKeyArr==null");
				return null;
			}
			Object[] argsValueArr = joinPoint.getArgs();
			if (StringUtils.contains(field, "#")) {// 从对象/map中获取
				String[] arr = StringUtils.split(field, "#");
				if (arr != null && arr.length == 2) {
					String parameter = arr[0];
					String key = arr[1];

					for (Object obj : argsValueArr) {
						if (obj instanceof Map) {
							return this.getValue(obj, key);
						}
					}

					for (int i = 0; i < argsKeyArr.length; i++) {
						if (StringUtils.equals(parameter, argsKeyArr[i])) {
							Object obj = argsValueArr[i];
							return this.getValue(obj, key);
						}
					}
				}
				return this.getValue(joinPoint.getArgs()[0], field);
			} else {// 直接获取
				for (int i = 0; i < argsKeyArr.length; i++) {
					if (StringUtils.equals(field, argsKeyArr[i])) {
						return argsValueArr[i];
					}
				}
			}
		} catch (Exception e) {
			logger.error("CacheAspect getValue Exception", e);
		}
		return null;
	}
}
