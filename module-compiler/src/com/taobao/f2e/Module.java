package com.taobao.f2e;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

import java.io.File;
import java.util.ArrayList;

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
            FileUtils.outputContent(code, fullpath, encoding);
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
        String[] requires = ModuleUtils.getRequiresFromAst(astRoot, name);
        ArrayList<String> rs = new ArrayList<String>();
        for (String require : requires) {
            if (!require.startsWith("#")) {
                rs.add(require);
            }
        }
        return rs.toArray(new String[rs.size()]);
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
        Node moduleNameNode = this.getModuleNameNode();
        if (moduleNameNode != null && moduleNameNode.getType() == Token.STRING) {
            return moduleNameNode.getString();
        }
        return null;
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
