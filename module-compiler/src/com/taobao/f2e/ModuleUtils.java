package com.taobao.f2e;

/**
 * utils for module
 */
public class ModuleUtils {
	/**
	 * remove timestamp contained in moduleName
	 *
	 * @param moduleName
	 * @return pure moduleName used for load module file
	 */
	public static String filterModuleName(String moduleName) {
		return moduleName.replaceAll("\\?.*$", "");
	}
}
