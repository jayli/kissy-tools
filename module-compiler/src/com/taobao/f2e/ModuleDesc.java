package com.taobao.f2e;

public class ModuleDesc implements Cloneable {
	String path;
	String encoding;
	String base;
	String cdnBase;
	String moduleName;

	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
