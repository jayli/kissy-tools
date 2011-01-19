package com.taobao.f2e;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;

/**
 * invoke module compiler for kissy
 *
 * @author yiminghe@gmail.com
 * @since 2011-01-18
 */
public class Main {

	public String[] encodings = {"utf-8"};
	public String[] baseUrls = {"d:/code/kissy_git/module-compiler/test/kissy/"};
	public HashSet<String> loaded = new HashSet<String>();
	public StringBuffer gen = new StringBuffer();
	public String[] requires = new String[0];
	public String output = "d:/code/kissy_git/module-compiler/test/kissy/combine.js";
	public String outputEncoding = "utf-8";

	public void run() {
		for (String r : requires) {
			combineRequire(r);
		}
		if (output != null) {
			FileUtils.outputContent(gen.toString(), output, outputEncoding);
		} else {
			System.out.println(gen.toString());
		}
	}


	private void combineRequire(String r) {
		String[] deps = getDeps(r);
		for (String dep : deps) {
			combineRequire(dep);
		}
		if (loaded.contains(r)) return;
		loaded.add(r);
		gen.append(getContent(r));
	}

	private String getContent(String r) {
		r = r.replaceAll("\\\\", "/");
		if (r.charAt(0) == '/') {
			r = r.substring(1);
		}
		if (!r.endsWith(".js") && !r.endsWith(".JS")) {
			r += ".js";
		}
		String path = "", baseUrl_ = "";
		for (String baseUrl : baseUrls) {
			baseUrl_ = baseUrl;
			path = baseUrl + r;
			if (new File(path).exists()) break;
		}
		int index = ArrayUtils.indexOf(baseUrls, baseUrl_);
		if (index == -1) index = 0;
		return FileUtils.getFileContent(path, encodings[index]);
	}

	private String[] getDeps(String path) {
		ArrayList<String> re = new ArrayList<String>();
		String content = getContent(path);
		Node root = AstUtils.parse(content);
		Node r = root.getFirstChild().getFirstChild().getLastChild();
		if (r.getType() == Token.OBJECTLIT) {
			Node first = r.getFirstChild();
			while (first != null) {
				if (first.getString().equals("requires")) {
					Node list = first.getFirstChild();
					if (list.getType() == Token.ARRAYLIT) {
						Node fl = list.getFirstChild();
						while (fl != null) {
							re.add(fl.getString());
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


	public static void main(String[] args) throws Exception {
		String propertyFile = args.length > 0 ? args[0] : "";
		//propertyFile = "d:/code/kissy_git/module-compiler/require.properties";
		Properties p = new Properties();
		p.load(new FileReader(propertyFile));
		Main m = new Main();
		String encodingStr = p.getProperty("encodings");
		if (encodingStr != null) {
			m.encodings = encodingStr.split(",");
		}
		String baseUrlStr = p.getProperty("baseUrls");
		if (baseUrlStr != null) {
			m.baseUrls = baseUrlStr.split(",");
		}

		String requireStr = p.getProperty("requires");
		if (requireStr != null) {
			m.requires = requireStr.split(",");
		}

		m.output = p.getProperty("output");

		String outputEncoding = p.getProperty("outputEncoding");
		if (outputEncoding != null) {
			m.outputEncoding = outputEncoding;
		}

		m.run();

	}
}
