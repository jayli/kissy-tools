package com.taobao.f2e;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * KISSY Packages config.
 *
 * @author yiminghe@gmail.com
 * @since 2012-08-06
 */
public class Packages {

	//map about module name and its des ,for cache
	private static HashMap<String, Module> nameModuleMap = new HashMap<String, Module>();

	/**
	 * package encoding
	 */
	private String[] encodings = {"utf-8"};

	/**
	 * package base urls
	 */
	private String[] baseUrls = {
			//"d:/code/kissy_git/kissy-tools/module-compiler/test/kissy/"
	};

	public String[] getEncodings() {
		return encodings;
	}

	public void setEncodings(String[] encodings) {
		this.encodings = encodings;
	}

	public String[] getBaseUrls() {
		return baseUrls;
	}

	public void setBaseUrls(String[] baseUrls) {

		ArrayList<String> re = new ArrayList<String>();
		for (String base : baseUrls) {
			base = FileUtils.escapePath(base);
			if (!base.endsWith("/")) {
				base += "/";
			}
			re.add(base);
		}
		this.baseUrls = re.toArray(new String[re.size()]);
	}

	public Module getModule(String moduleName) {

		if (nameModuleMap.get(moduleName) != null) {
			return nameModuleMap.get(moduleName);
		}

		Packages packages = this;
		String[] baseUrls = packages.getBaseUrls();
		String[] encodings = packages.getEncodings();
		String path = packages.getModuleFullPath(moduleName);
		String baseUrl = path.replaceFirst("(?i)" + moduleName + ".js$", "");
		int index = ArrayUtils.indexOf(baseUrls, baseUrl);
		if (index == -1 || index >= encodings.length) {
			index = 0;
		}
		String encoding = encodings[index];
		Module module = new Module();
		module.setEncoding(encoding);
		module.setFullpath(path);
		module.setPackageBase(baseUrl);
		module.setName(moduleName);
		nameModuleMap.put(moduleName, module);
		return module;
	}


	private String getModuleFullPath(String moduleName) {
		Packages packages = this;
		String r = FileUtils.escapePath(moduleName);
		String[] baseUrls = packages.getBaseUrls();
		if (r.charAt(0) == '/') {
			r = r.substring(1);
		}
		if (!r.endsWith(".js") && !r.endsWith(".JS")) {
			r += ".js";
		}
		String path = "";
		for (String baseUrl : baseUrls) {
			path = baseUrl + r;
			if (new File(path).exists()) {
				break;
			}
		}
		return path;
	}
}