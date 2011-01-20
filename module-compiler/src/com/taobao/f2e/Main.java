package com.taobao.f2e;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

/**
 * invoke module compiler for kissy
 *
 * @author yiminghe@gmail.com
 * @since 2011-01-18
 */
public class Main {

	private String[] encodings = {"utf-8"};
	private String[] baseUrls = {
			//"d:/code/kissy_git/kissy-tools/module-compiler/test/kissy/"
	};
	//when module is generated to finalCodes,mark this module
	//module name as key
	private HashSet<String> genned = new HashSet<String>();

	//
	private StringBuffer finalCodes = new StringBuffer();
	private String[] requires = new String[0];
	private String output = "";//"d:/code/kissy_git/kissy-tools/module-compiler/test/kissy/combine.js";
	private String outputEncoding = "utf-8";

	//when module ast modified , serialized code goes here
	//module name as key
	private HashMap<String, String> moduleCodes = new HashMap<String, String>();

	public String[] getEncodings() {
		return encodings;
	}

	public String[] getBaseUrls() {
		return baseUrls;
	}

	public String[] getRequires() {
		return requires;
	}

	public String getOutput() {
		return output;
	}

	public String getOutputEncoding() {
		return outputEncoding;
	}

	public void setEncodings(String[] encodings) {
		this.encodings = encodings;
	}


	public void setRequires(String[] requires) {
		this.requires = requires;
	}


	public void setOutput(String output) {
		this.output = output;
	}


	public void setOutputEncoding(String outputEncoding) {
		this.outputEncoding = outputEncoding;
	}

	public void setBaseUrls(String[] bases) {
		ArrayList<String> re = new ArrayList<String>();
		for (String base : bases) {
			base = FileUtils.escapePath(base);
			if (!base.endsWith("/")) {
				base += "/";
			}
			re.add(base);
		}
		this.baseUrls = re.toArray(new String[re.size()]);
	}

	public void run() {
		for (String r : requires) {
			combineRequire(r);
		}
		if (output != null) {
			FileUtils.outputContent(finalCodes.toString(), output, outputEncoding);
		} else {
			System.out.println(finalCodes.toString());
		}
	}


	private void combineRequire(String r) {
		String[] deps = getDepsAndCheckModuleName(r);
		for (String dep : deps) {
			combineRequire(dep);
		}
		if (genned.contains(r)) return;
		genned.add(r);

		//first get modified code if ast modified
		String code = moduleCodes.get(r);
		if (code == null) {
			code = getContent(r);
		}
		finalCodes.append(code);
	}

	/**
	 * @param moduleName must be absolute
	 * @return module's code
	 */
	private String getContent(String moduleName) {
		String path = getModuleFullPath(moduleName);
		String baseUrl = path.replaceFirst(moduleName + "$", "");
		int index = ArrayUtils.indexOf(baseUrls, baseUrl);
		if (index == -1) index = 0;
		return FileUtils.getFileContent(path, encodings[index]);
	}

	private String getModuleFullPath(String moduleName) {
		String r = FileUtils.escapePath(moduleName);
		if (r.charAt(0) == '/') {
			r = r.substring(1);
		}
		if (!r.endsWith(".js") && !r.endsWith(".JS")) {
			r += ".js";
		}
		String path = "";
		for (String baseUrl : baseUrls) {
			path = baseUrl + r;
			if (new File(path).exists()) break;
		}
		return path;
	}

	/**
	 * @param moduleName	  event/ie
	 * @param relativeDepName 1. event/../s to s
	 *                        2. event/./s to event/s
	 *                        3. ../h to h
	 *                        4. ./h to event/h
	 * @return dep's normal path
	 */
	protected String getDepModuleName(String moduleName, String relativeDepName) {
		relativeDepName = FileUtils.escapePath(relativeDepName);
		moduleName = FileUtils.escapePath(moduleName);

		//no relative path
		if (relativeDepName.indexOf("../") == -1
				&& relativeDepName.indexOf("./") == -1)
			return relativeDepName;

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
		return FileUtils.normPath(relativeDepName);
	}

	/**
	 * @param moduleName module's name
	 * @param root	   module ast's root node
	 * @return normalized dep names
	 */
	protected String[] getDeps(String moduleName, Node root) {
		ArrayList<String> re = new ArrayList<String>();
		Node r = root.getFirstChild().getFirstChild().getLastChild();
		if (r.getType() == Token.OBJECTLIT) {
			Node first = r.getFirstChild();
			while (first != null) {
				if (first.getString().equals("requires")) {
					Node list = first.getFirstChild();
					if (list.getType() == Token.ARRAYLIT) {
						Node fl = list.getFirstChild();
						while (fl != null) {
							/**
							 * depName can be relative ./ , ../
							 */
							re.add(getDepModuleName(moduleName, fl.getString()));
							fl = fl.getNext();
						}
					}
					break;
				}
				first = first.getNext();
			}
		}
		return re.toArray(new String[re.size()]);
	}

	/**
	 * S.add(func); -> S.add("moduleName",func);
	 *
	 * @param moduleName module's name
	 * @param root	   module's root ast node
	 */
	protected void checkModuleName(String moduleName, Node root) {
		Node getProp = root.getFirstChild().getFirstChild().getFirstChild();
		//add method's first parameter is not string，add module name automatically
		if (getProp.getNext().getType() != Token.STRING) {
			getProp.getParent().addChildAfter(Node.newString(moduleName), getProp);
			//serialize ast to code cache
			moduleCodes.put(moduleName, AstUtils.toSource(root));
		}
	}

	private String[] getDepsAndCheckModuleName(String moduleName) {
		String content = getContent(moduleName);
		Node root = AstUtils.parse(content);
		checkModuleName(moduleName, root);
		return getDeps(moduleName, root);
	}


	public static void commandRunner(String[] args) throws Exception {
		String propertyFile = args.length > 0 ? args[0] : "";
		if (propertyFile.equals(""))
			propertyFile = "d:/code/kissy_git/kissy-tools/module-compiler/require.properties";
		Properties p = new Properties();
		p.load(new FileReader(propertyFile));
		String mainClass = p.getProperty("mainClass");
		if (mainClass == null)
			mainClass = "com.taobao.f2e.Main";
		Main m = (Main) Class.forName(mainClass).newInstance();
		String encodingStr = p.getProperty("encodings");
		if (encodingStr != null) {
			m.setEncodings(encodingStr.split(","));
		}
		String baseUrlStr = p.getProperty("baseUrls");
		if (baseUrlStr != null) {
			m.setBaseUrls(baseUrlStr.split(","));
		}

		String requireStr = p.getProperty("requires");
		if (requireStr != null) {
			m.setRequires(requireStr.split(","));
		}

		m.setOutput(p.getProperty("output"));

		String outputEncoding = p.getProperty("outputEncoding");
		if (outputEncoding != null) {
			m.setOutputEncoding(outputEncoding);
		}

		m.run();

	}


	public static void testGetDepModuleName() throws Exception {
		Main m = new Main();
		System.out.println(m.getDepModuleName("event/base", "./ie").equals("event/ie"));
		System.out.println(m.getDepModuleName("event/base", "../dom/ie").equals("dom/ie"));
		System.out.println(m.getDepModuleName("event/base", "dom/./ie").equals("dom/ie"));
		System.out.println(m.getDepModuleName("event/base", "dom/../event/ie").equals("event/ie"));
		System.out.println(m.getDepModuleName("event", "./dom").equals("dom"));
	}

	public static void main(String[] args) throws Exception {
		//testGetDepModuleName();
		commandRunner(args);
	}
}
