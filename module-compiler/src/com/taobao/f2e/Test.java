package com.taobao.f2e;


import java.util.regex.Pattern;

public class Test {

	static void prepare() {
		Main main = new Main();
		main.setOutput("../test/kissy_combo/page/run.combo.js");
		main.setRequires(new String[]{"page/run"});
	}

	public static void main(String[] args) {
		System.out.println(Pattern.compile("(ua)(/.*)?$").matcher("ua/xx").replaceAll("$2"));
	}
}
