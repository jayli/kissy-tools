package com.taobao.f2e;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

import java.io.File;
import java.io.FileReader;
import java.util.*;

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

	public void setOutputCombo(boolean outputCombo) {
		this.outputCombo = outputCombo;
	}

	//when module is generated to finalCodes,mark this module
	//module name as key
	private HashSet<String> genned = new HashSet<String>();


	// two status : start and null
	//if encouter start when process a module ,it must exist cyclic dependance
	//throw error
	private HashMap<String, String> moduleStatus = new HashMap<String, String>();

	private StringBuffer finalCodes = new StringBuffer();

	private String[] requires = new String[0];
	private HashSet<String> excludes = new HashSet<String>();

	private String output = "";//"d:/code/kissy_git/kissy-tools/module-compiler/test/kissy/combine.js";
	private String outputEncoding = "utf-8";

	//when module ast modified , serialized code goes here
	//module name as key
	protected HashMap<String, String> moduleCodes = new HashMap<String, String>();


	private boolean outputCombo = false;

	private HashMap<String, StringBuffer> comboUrls = new HashMap<String, StringBuffer>();

	//map about module name and its des ,for cache
	private HashMap<String, ModuleDesc> nameDescMap = new HashMap<String, ModuleDesc>();

	private ArrayList<String> combinedFiles = new ArrayList<String>();

	public void addExcludes(String[] excludes) {
		for (String exclude : excludes) {
			collectExclude(exclude);
		}
	}

	//collect this module and all its dependent modules
	private void collectExclude(String excludeModuleName) {
		if (excludes.contains(excludeModuleName)) return;
		excludes.add(excludeModuleName);
		String content = getContent(excludeModuleName);
		Node root = AstUtils.parse(content);
		checkModuleName(excludeModuleName, root);
		String[] depends = getDeps(excludeModuleName, root);
		addExcludes(depends);
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
		for (String requiredModuleName : requires) {
			combineRequire(requiredModuleName);
		}

		//combo need to handled seperately for each base
		if (outputCombo) {
			Set<String> keys = comboUrls.keySet();
			for (String key : keys) {
				System.out.println(comboUrls.get(key).toString());
			}
		} else {
			if (output != null) {
				String re = "/**\n combined files : \n\n" + ArrayUtils.join(
						combinedFiles.toArray(new String[combinedFiles.size()]), "\n")
						+ "\n**/\n\n" + finalCodes.toString();
				FileUtils.outputContent(re, output, outputEncoding);
				System.out.println("success generated   :  " + output);
			} else {
				System.out.println(finalCodes.toString());
			}
		}

	}

	/**
	 * x -> a,b,c : x depends on a,b,c
	 * add a,b,c then add x to final code buffer
	 *
	 * @param requiredModuleName module name required
	 */
	private void combineRequire(String requiredModuleName) {

		//check whether module's file is valid
		ModuleDesc moduleDesc = getModuleDesc(requiredModuleName);
		if (!new File(moduleDesc.path).exists()) {
			System.out.println("warning : module's file not found : " + requiredModuleName);
			return;
		}

		//if sepcify exclude this module ,just return
		if (excludes.contains(requiredModuleName)) return;


		//x -> a,b,c
		//a -> b
		//when requiredModuleName=x and encouter b ,just return
		//reduce redundant parse and recursive
		if (genned.contains(requiredModuleName)) return;

		if (moduleStatus.get(requiredModuleName) != null) {
			String error = "cyclic dependence : " + requiredModuleName;
			//throw new Error(error);
			//if silence ,just return
			System.out.println("warning : " + error);
			return;
		}
		//mark as start for cyclic detection
		moduleStatus.put(requiredModuleName, "start");

		String[] deps = getDepsAndCheckModuleName(requiredModuleName);
		for (String dep : deps) {
			combineRequire(dep);
		}

		if (genned.contains(requiredModuleName)) {
			throw new Error("it must not happen");
		}

		//remove mark for cyclic detection
		moduleStatus.remove(requiredModuleName);

		genned.add(requiredModuleName);

		if (this.outputCombo) {
			//generate combo url by each base
			outputCombo(requiredModuleName);
		} else {
			//just append this file's content
			outputContent(requiredModuleName);
		}

	}


	private void outputContent(String requiredModuleName) {
		//first get modified code if ast modified
		String code = moduleCodes.get(requiredModuleName);
		ModuleDesc desc = getModuleDesc(requiredModuleName);
		if (code == null) {
			code = getContent(requiredModuleName);
		} else {
			FileUtils.outputContent(code, desc.path, desc.encoding);
		}

		//append path info ,easy debug
		combinedFiles.add(new File(desc.path).getAbsolutePath());
		finalCodes.append(code).append("\n");
	}

	private void outputCombo(String requiredModuleName) {
		//first get modified code if ast modified
		String code = moduleCodes.get(requiredModuleName);
		ModuleDesc desc = getModuleDesc(requiredModuleName);
		if (code != null) {
			FileUtils.outputContent(code, desc.path, desc.encoding);
		}

		if (!comboUrls.containsKey(desc.base)) {
			comboUrls.put(desc.base, new StringBuffer());
		}

		StringBuffer comboUrl = comboUrls.get(desc.base);
		if (comboUrl.length() > 0) {
			comboUrl.append(",");
		} else {
			comboUrl.append(desc.base).append("??");
		}
		comboUrl.append(requiredModuleName);
	}

	/**
	 * @param moduleName must be absolute
	 * @return {String|null} module's code,null表示出错（文件不存在？）
	 */
	private String getContent(String moduleName) {
		//System.out.println("get file content :" + moduleName);
		ModuleDesc desc = getModuleDesc(moduleName);
		return FileUtils.getFileContent(desc.path, desc.encoding);
	}


	private ModuleDesc getModuleDesc(String moduleName) {
		if (nameDescMap.get(moduleName) != null)
			return nameDescMap.get(moduleName);

		String path = getModuleFullPath(moduleName);
		String baseUrl = path.replaceFirst("(?i)" + moduleName + ".js$", "");
		int index = ArrayUtils.indexOf(baseUrls, baseUrl);
		if (index == -1 || index >= encodings.length) index = 0;
		String encoding = encodings[index];
		ModuleDesc desc = new ModuleDesc();
		desc.encoding = encoding;
		desc.path = path;
		desc.base = baseUrl;
		desc.moduleName = moduleName;
		nameDescMap.put(moduleName, desc);
		return desc;
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
		String depModuleName;
		//no relative path
		if (relativeDepName.indexOf("../") == -1
				&& relativeDepName.indexOf("./") == -1) {
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


		//construct dep module's desc
		if (nameDescMap.get(depModuleName) != null) return depModuleName;

		ModuleDesc moduleDesc = getModuleDesc(moduleName);
		try {
			ModuleDesc depDesc = (ModuleDesc) moduleDesc.clone();
			depDesc.path = depDesc.base + depModuleName + ".js";

			nameDescMap.put(depModuleName, depDesc);
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return depModuleName;
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

		System.out.println("current path : " + new File(".").getAbsolutePath());

		String propertyFile = args.length > 0 ? args[0] : "";
		if (propertyFile.equals(""))
			propertyFile = "d:/code/kissy_git/kissy-tools/module-compiler/seajs_require.properties";
		System.out.println("load parameter from :  " + propertyFile);
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

		String excludeStr = p.getProperty("excludes");
		if (excludeStr != null) {
			m.addExcludes(excludeStr.split(","));
		}

		m.setOutput(p.getProperty("output"));

		String outputEncoding = p.getProperty("outputEncoding");
		if (outputEncoding != null) {
			m.setOutputEncoding(outputEncoding);
		}


		String outputCombo = p.getProperty("outputCombo");
		if (outputCombo != null) {
			m.setOutputCombo(true);
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
