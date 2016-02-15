package cn.zxd.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import cn.zxd.annotaion.CacheBatchDelete;
import cn.zxd.annotaion.CacheDelete;
import cn.zxd.annotaion.CacheGetAndPut;
import cn.zxd.dataobject.Name;

@Service
public class UserService {

	@CacheGetAndPut(key = "o2o.city.open", suffix = { "id", "sex" })
	public String save(int id, int sex) {
		return "jim";
	}

	@CacheDelete(key = "o2o.city.open", suffix = { "id", "sex" })
	public void update(int id, int sex) {
		System.out.println("update user");
	}

	@CacheGetAndPut(key = "test", suffix = { "name#id", "name#name" })
	public Name save2(Name name) {
		return name;
	}

	@CacheDelete(key = "test", suffix = { "name#id", "name#name" })
	public void update2(Name name) {
		System.out.println("update2 user");
	}

	@CacheGetAndPut(key = "test", suffix = { "name#id", "name#name" })
	public String save3(Map<String, Object> param) {
		return "";
	}

	@CacheDelete(key = "test", suffix = { "name#id", "name#name" })
	public void update3(Map<String, Object> param) {
		System.out.println("update3 user");
	}

	@CacheGetAndPut(key = "test", suffix = { "name#id" })
	public String save4(Map<String, Object> param) {
		return "";
	}

	@CacheDelete(key = "test", keyWithReturnValue = true)
	public int update4(Map<String, Object> param) {
		return 3;
	}

	@CacheGetAndPut(key = "test", suffix = { "name#id" })
	public String save5(Map<String, Object> param) {
		return "lucy";
	}

	@CacheDelete(key = "test", ignoreSuffix = true)
	public void update5(Map<String, Object> param) {
	}

	@CacheDelete(key = "o2o.weixin.res.version", ignoreSuffix = true)
	public void delete() {
	}
	
	@CacheGetAndPut(key="o2o.stock.spec.list",suffix={"id"})
	public String save7(int id){
		return "jim";
	}

	@CacheBatchDelete(keys = { "o2o.stock.spec.list", "o2o.stock.goods.list", "o2o.stock.goods.name.list" })
	public void batchDelete() {

	}

}
