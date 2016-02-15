package cn.zxd.dataobject;

import java.io.Serializable;

public class Name implements Serializable {

	private static final long serialVersionUID = -373492101281037421L;
	
	private int id;
	private String name;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Name(int id, String name) {
		super();
		this.id = id;
		this.name = name;
	}
}
