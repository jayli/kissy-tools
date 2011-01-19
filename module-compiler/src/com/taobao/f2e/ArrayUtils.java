package com.taobao.f2e;

public class ArrayUtils {
	public static int indexOf(String[] arr, String item) {
		for (int i = 0; i < arr.length; i++) {
			if (arr[i].equals(item)) return i;
		}
		return -1;
	}

	public static String join(String[] attr, String sep) {
		StringBuffer sb = new StringBuffer();
		for (String a : attr) {
			sb.append(a);
			sb.append(sep);
		}
		String re = sb.toString();
		//remove last sep
		return re.substring(0, re.length() - sep.length());
	}
}
