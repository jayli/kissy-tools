package com.taobao.f2e;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.apache.commons.cli.*;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

/**
 * invoke module compiler for kissy
 *
 * @author yiminghe@gmail.com
 * @since 2011-01-18
 */
public class Main {

	/**
	 * packages.
	 */
	private Packages packages = new Packages();

	/**
	 * exclude pattern for modules.
	 */
	private Pattern excludePattern;

	/**
	 * whether print -min at cdn combo mode.
	 */
	private boolean minSuffix = false;

	/**
	 * stack of visited modules to detect circular dependency
	 */
	private ArrayList<String> modulesVisited = new ArrayList<String>();

	/**
	 * requires mods name for current application.
	 */
	private String[] requires = new String[0];

	/**
	 * combined mods 's code 's output file path.
	 */
	private String output = "";//"d:/code/kissy_git/kissy-tools/module-compiler/test/kissy/combine.js";

	/**
	 * combined mods 's code 's output file encoding.
	 */
	private String outputEncoding = "utf-8";

	/**
	 * all processed modules.
	 */
	protected ArrayList<Module> modules = new ArrayList<Module>();

	/**
	 * whether overwrite module's file with module name added.
	 */
	private boolean fixModuleName = false;

	/**
	 * whether enable combo mode.
	 */
	private boolean outputCombo = false;

	/**
	 * all combo ed urls grouped by package.
	 */
	private HashMap<String, StringBuffer> comboUrls = new HashMap<String, StringBuffer>();

	public void setExcludePattern(Pattern excludePattern) {
		this.excludePattern = excludePattern;
	}

	public void setOutputCombo(boolean outputCombo) {
		this.outputCombo = outputCombo;
	}

	public void setMinSuffix(boolean minSuffix) {
		this.minSuffix = minSuffix;
	}

	public void setFixModuleName(boolean fixModuleName) {
		this.fixModuleName = fixModuleName;
	}

	public void setRequires(String[] requires) {
		this.requires = requires;
	}


	public void setOutput(String output) {
		this.output = output;
	}

	public Packages getPackages() {
		return packages;
	}

	public void setOutputEncoding(String outputEncoding) {
		this.outputEncoding = outputEncoding;
	}

	public void run() {

		long start = System.currentTimeMillis();

		for (String requiredModuleName : requires) {
			combineRequire(requiredModuleName);
		}

		//combo need to handled separately for each base
		if (outputCombo) {
			Set<String> keys = comboUrls.keySet();
			for (String key : keys) {
				System.out.println(comboUrls.get(key).toString());
			}
		} else {
			ArrayList<String> combinedFiles = new ArrayList<String>();
			StringBuilder finalCodes = new StringBuilder();

			for (Module m : modules) {
				combinedFiles.add(m.getName());
				finalCodes.append(m.getCode());
			}

			String re = "/*\n Combined modules by KISSY Module Compiler: \n\n " +
					ArrayUtils.join(combinedFiles.toArray(new String[combinedFiles.size()]), "\n ")
					+ "\n*/\n\n" + finalCodes.toString();

			if (output != null) {
				FileUtils.outputContent(re, output, outputEncoding);
				System.out.println("success generated:  " + output);
			} else {
				System.out.println(re);
			}
		}

		System.out.print("duration: " + (System.currentTimeMillis() - start));
	}

	/**
	 * x -> a,b,c : x depends on a,b,c
	 * add a,b,c then add x to final code buffer
	 *
	 * @param requiredModuleName module name required
	 */
	private void combineRequire(String requiredModuleName) {

		// if css file, do not combine with js files
		// !TODO generate a combined css file
		if (requiredModuleName.endsWith(".css")) {
			return;
		}

		// if specify exclude this module, just return
		if (excludePattern != null &&
				excludePattern.matcher(requiredModuleName).matches()) {
			return;
		}

		Module requiredModule = packages.getModule(requiredModuleName);

		if (!requiredModule.isModuleExists()) {
			System.out.println("warning  module's file not found: " + requiredModuleName
					+ ": " + requiredModule.getFullpath());
			return;
		}

		if (requiredModule.getAstRoot() == null) {
			System.out.println("!! format or syntax error in module: " + requiredModule.getFullpath());
			System.exit(1);
		}

		//x -> a,b,c
		//a -> b
		//when requiredModuleName=x and encounter b ,just return
		//reduce redundant parse and recursive
		if (modules.contains(requiredModule)) {
			return;
		}

		if (modulesVisited.contains(requiredModuleName)) {
			String error = "cyclic dependence: " +
					ArrayUtils.join(modulesVisited.toArray(new String[modulesVisited.size()]),
							",") + "," + requiredModuleName;
			//if silence ,just return
			System.out.println("error: " + error);
			System.exit(1);
			return;
		}

		//mark as start for cyclic detection
		modulesVisited.add(requiredModuleName);

		completeModuleName(requiredModule);

		String[] requires = requiredModule.getRequires();

		for (String require : requires) {
			combineRequire(require);
		}

		//remove mark for cyclic detection
		modulesVisited.remove(modulesVisited.size()-1);

		modules.add(requiredModule);

		if (fixModuleName && !requiredModule.isWithModuleName()) {
			requiredModule.updateCodeToFile();
		}

		if (outputCombo) {
			//generate combo url by each base
			outputCombo(requiredModule);
		}

	}

	private void outputCombo(Module module) {

		if (!comboUrls.containsKey(module.getPackageBase())) {
			comboUrls.put(module.getPackageBase(), new StringBuffer());
		}

		StringBuffer comboUrl = comboUrls.get(module.getPackageBase());

		if (comboUrl.length() > 0) {
			comboUrl.append(",");
		} else {
			comboUrl.append(module.getPackageCdnBase()).append("??");
		}

		comboUrl.append(module.getName())
				.append(minSuffix ? "-min" : "")
				.append(".js");
	}

	/**
	 * S.add(func); -> S.add("moduleName",func);
	 * @param module
	 */
	private void completeModuleName(Module module) {
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

	public static void commandRunnerCLI(String[] args) throws Exception {

		Options options = new Options();
		options.addOption("encodings", true, "baseUrls 's encodings");
		options.addOption("baseUrls", true, "baseUrls");
		options.addOption("cdnUrls", true, "cdnUrls");
		options.addOption("minSuffix", true, "minSuffix");
		options.addOption("requires", true, "requires");
		options.addOption("excludeReg", true, "excludeReg");
		options.addOption("output", true, "output");

		options.addOption("v", "version", false, "version");

		options.addOption("outputEncoding", true, "outputEncoding");
		options.addOption("outputCombo", true, "outputCombo");
		options.addOption("fixModuleName", true, "fixModuleName");
		// create the command line parser
		CommandLineParser parser = new GnuParser();
		CommandLine line;
		try {
			// parse the command line arguments
			line = parser.parse(options, args);
		} catch (ParseException exp) {
			System.out.println("Unexpected exception:" + exp.getMessage());
			return;
		}

		if (line.hasOption("v")) {
			System.out.println("KISSY Module Compiler 2.0");
			return;
		}

		Main m = new Main();

		Packages packages = m.getPackages();

		String encodingStr = line.getOptionValue("encodings");
		if (encodingStr != null) {
			packages.setEncodings(encodingStr.split(","));
		}
		String baseUrlStr = line.getOptionValue("baseUrls");
		if (baseUrlStr != null) {
			packages.setBaseUrls(baseUrlStr.split(","));
		}


		String cdnUrlsStr = line.getOptionValue("cdnUrls");
		if (cdnUrlsStr != null) {
			packages.setCdnUrls(cdnUrlsStr.split(","));
		}

		String fixModuleName = line.getOptionValue("fixModuleName");
		if (fixModuleName != null) {
			m.setFixModuleName(true);
		}

		String requireStr = line.getOptionValue("requires");
		if (requireStr != null) {
			m.setRequires(requireStr.split(","));
		}

		String excludeReg = line.getOptionValue("excludeReg");
		if (excludeReg != null) {
			m.setExcludePattern(Pattern.compile(excludeReg));
		}

		String minSuffixStr = line.getOptionValue("minSuffix");
		if (minSuffixStr != null) {
			m.setMinSuffix(true);
		}

		m.setOutput(line.getOptionValue("output"));

		String outputEncoding = line.getOptionValue("outputEncoding");
		if (outputEncoding != null) {
			m.setOutputEncoding(outputEncoding);
		}


		String outputCombo = line.getOptionValue("outputCombo");
		if (outputCombo != null) {
			m.setOutputCombo(true);
		}

		m.run();

	}

	public static void main(String[] args) throws Exception {
		System.out.println("current path: " + new File(".").getAbsolutePath());
		System.out.println("current args: " + Arrays.toString(args));
		commandRunnerCLI(args);
	}
}
