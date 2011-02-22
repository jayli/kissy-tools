package com.taobao.f2e;


public class Test {
	public static void main(String[] args) {
		System.out.println("xX".replaceAll("(?i)x",""));

		System.out.println("y/z?xxx".replaceAll("\\?.*$",""));
		System.out.println("y/z".replaceAll("\\?.*$",""));
	}
}
