package com.taobao.f2e;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.FileInputStream;

import info.monitorenter.cpdetector.io.*;
import java.nio.charset.Charset;

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
	// added by jayli
	// 编码要自动识别，不要手动指定
    private String encoding = "utf-8";
    /**
     * module package 's file path.
     */
    private String packageBase;
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

    public void setAstRoot(Node root) {
        this.astRoot = root;
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

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        if (content != null) {
            return content;
        } else {
            return content = FileUtils.getFileContent(fullpath, getEncode(fullpath));
        }
    }

    /**
     * S.add(func); -> S.add("moduleName",func);
     */
    public void completeModuleName(boolean saveToFile) {
        Module module = this;
		try {
			Node moduleNameNode = module.getModuleNameNode();
			if (moduleNameNode.getType() != Token.STRING) {
				// 补全Modulename
				moduleNameNode.addChildAfter(Node.newString(module.getName()),
						moduleNameNode.getParent().getChildBefore(moduleNameNode));
				module.setCode(AstUtils.toSource(module.getAstRoot()));
				if (saveToFile) {
					//FileUtils.outputContent(code, fullpath, encoding);
					FileUtils.outputContent(module.getContent().replace("KISSY.add(","KISSY.add(\""+module.getName()+"\","), fullpath, getEncode(fullpath));
				}
			} else {
				// 写入源文件
				module.setCode(module.getContent());
			}
		}catch(Exception ex){

		}
    }

	// added by jayli
	public String getEncode(String path){  
		/*------------------------------------------------------------------------
		  使用需要用到三个第三方JAR包：antlr.jar、chardet.jar和cpdetector.jar
		  cpDetector是基于统计学原理的，不保证完全正确。
		  --------------------------------------------------------------------------*/
		CodepageDetectorProxy detector = CodepageDetectorProxy.getInstance();

		/*-------------------------------------------------------------------------
		  ParsingDetector可用于检查HTML、XML等文件或字符流的编码,构造方法中的参数用于
		  指示是否显示探测过程的详细信息，为false不显示。
		  ---------------------------------------------------------------------------*/
		detector.add(new ParsingDetector(false));
		detector.add(JChardetFacade.getInstance());//用到antlr.jar、chardet.jar

		// ASCIIDetector用于ASCII编码测定
		detector.add(ASCIIDetector.getInstance());

		// UnicodeDetector用于Unicode家族编码的测定
		detector.add(UnicodeDetector.getInstance());
		java.nio.charset.Charset charset = null;
		File f = new File(path);
		try {
			charset = detector.detectCodepage(f.toURI().toURL());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if (charset != null)
			return charset.name();
		else
			return null;
	}

    public String[] getRequires() {
        if (requires != null) {
            return requires;
        }
        Node astRoot = this.getAstRoot();
        return ModuleUtils.getRequiresFromAst(astRoot, name);
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

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public void setPackageBase(String packageBase) {
        this.packageBase = packageBase;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private Node getModuleNameNode() {
        astRoot = this.getAstRoot();
        Node getProp = astRoot.getFirstChild().getFirstChild().getFirstChild();
        //add method's first parameter is not string，add module name automatically
        return getProp.getNext();
    }

    public String getModuleNameFromNode() {
		try{
			Node moduleNameNode = this.getModuleNameNode();
			if (moduleNameNode != null && moduleNameNode.getType() == Token.STRING) {
				return moduleNameNode.getString();
			}
			return null;
		} catch(Exception e){
			return null;
		}
    }

    public boolean isValidFormat() {
        Node t, root = this.getAstRoot();
        if (root == null) {
            return false;
        } else if (root.getType() != Token.SCRIPT) {
            return false;
        }
        t = root.getFirstChild();
        if (t == null) {
            return false;
        } else if (t.getType() != Token.EXPR_RESULT) {
            return false;
        }
        t = t.getFirstChild();
        if (t == null) {
            return false;
        } else if (t.getType() != Token.CALL) {
            return false;
        }
        t = t.getFirstChild();
        if (t == null) {
            return false;
        } else if (t.getType() != Token.GETPROP) {
            return false;
        }

        // t.getNext(); => module name . str,type==STRING

        t = t.getFirstChild();

        if (t == null) {
            return false;
        } else if (t.getType() != Token.NAME) {
            return false;
        } else if (!t.getString().equals("KISSY")) {
            return false;
        }


        t = t.getNext();

        if (t == null) {
            return false;
        } else if (t.getType() != Token.STRING) {
            return false;
        } else if (!t.getString().equals("add")) {
            return false;
        }

        return true;

    }
}
