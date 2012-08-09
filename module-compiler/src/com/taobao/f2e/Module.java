package com.taobao.f2e;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

import java.io.File;

/**
 * KISSY Module Format.
 *
 * @author yiminghe@gmail.com
 * @since 2012-08-06
 */
public class Module {
	/**
	 * module 's full file path.
	 */
	private String fullpath;
	/**
	 * encoding of module 's code file.
	 */
	private String encoding = "utf-8";
	/**
	 * module package 's file path.
	 */
	private String packageBase;
	/**
	 * module package 's cdn path.
	 */
	private String packageCdnBase;
	/**
	 * module name.
	 */
	private String name;
	/**
	 * module file 's content
	 */
	private String content = null;
	/**
	 * module 's complete code
	 */
	private String code;
	/**
	 * module 's require module  name.
	 */
	private String[] requires = null;
	/**
	 * whether current module definition has module name.
	 */
	private boolean withModuleName = true;
	/**
	 * module code 's ast root.
	 */
	private Node astRoot = null;

	public boolean isModuleExists() {
		return new File(fullpath).exists();
	}

	public Node getAstRoot() {
		if (astRoot != null) {
			return astRoot;
		}
		try {
			String content = this.getContent();
			astRoot = AstUtils.parse(content, name);
			return astRoot;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getContent() {
		if (content != null) {
			return content;
		} else {
			return content = FileUtils.getFileContent(fullpath, encoding);
		}
	}

	/**
	 * S.add(func); -> S.add("moduleName",func);
	 */
	public void completeModuleName() {
		Module module = this;
		Node moduleNameNode = module.getModuleNameNode();
		if (moduleNameNode.getType() != Token.STRING) {
			moduleNameNode.addChildAfter(Node.newString(module.getName()),
					moduleNameNode.getParent().getChildBefore(moduleNameNode));
			module.setWithModuleName(false);
			module.setCode(AstUtils.toSource(module.getAstRoot()));
		} else {
			module.setWithModuleName(true);
			module.setCode(module.getContent());
		}
	}

	public void updateCodeToFile() {
		FileUtils.outputContent(code, fullpath, encoding);
	}

	public String[] getRequires() {
		if (requires != null) {
			return requires;
		}
		Node astRoot = this.getAstRoot();
		return requires = ModuleUtils.getRequiresFromAst(astRoot, name);
	}

	public boolean isWithModuleName() {
		return withModuleName;
	}

	public void setWithModuleName(boolean withModuleName) {
		this.withModuleName = withModuleName;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getFullpath() {
		return fullpath;
	}

	public void setFullpath(String fullpath) {
		this.fullpath = fullpath;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getPackageBase() {
		return packageBase;
	}

	public void setPackageBase(String packageBase) {
		this.packageBase = packageBase;
	}

	public String getPackageCdnBase() {
		return packageCdnBase;
	}

	public void setPackageCdnBase(String packageCdnBase) {
		this.packageCdnBase = packageCdnBase;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Node getModuleNameNode() {
		astRoot = this.getAstRoot();
		Node getProp = astRoot.getFirstChild().getFirstChild().getFirstChild();
		//add method's first parameter is not string，add module name automatically
		return getProp.getNext();
	}
}
