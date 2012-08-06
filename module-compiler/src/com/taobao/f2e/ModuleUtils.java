package com.taobao.f2e;

/**
 * utils for module
 */
public class ModuleUtils {


	/**
	 * @param moduleName      event/ie
	 * @param relativeDepName 1. event/../s to s
	 *                        2. event/./s to event/s
	 *                        3. ../h to h
	 *                        4. ./h to event/h
	 * @return dep's normal path
	 */
	public static String getDepModuleName(String moduleName, String relativeDepName) {
		relativeDepName = FileUtils.escapePath(relativeDepName);
		moduleName = FileUtils.escapePath(moduleName);
		String depModuleName;
		//no relative path
		if (!relativeDepName.contains("../") &&
				!relativeDepName.contains("./")) {
			depModuleName = relativeDepName;

		} else {
			//at start,consider moduleName
			if (relativeDepName.indexOf("../") == 0
					|| relativeDepName.indexOf("./") == 0) {
				int lastSlash = moduleName.lastIndexOf("/");
				String archor = moduleName;
				if (lastSlash == -1) {
					archor = "";
				} else {
					archor = archor.substring(0, lastSlash + 1);
				}
				return FileUtils.normPath(archor + relativeDepName);
			}
			//at middle,just norm
			depModuleName = FileUtils.normPath(relativeDepName);
		}
		return depModuleName;
	}
}
