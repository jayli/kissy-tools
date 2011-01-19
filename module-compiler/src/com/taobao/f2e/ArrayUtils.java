package com.taobao.f2e;

/**
 * Created by IntelliJ IDEA.
 * User: chengyu
 * Date: 11-1-19
 * Time: 上午11:39
 * To change this template use File | Settings | File Templates.
 */
public class ArrayUtils {
	public static int indexOf(String[] arr, String item) {
		for (int i = 0; i < arr.length; i++) {
			if (arr[i].equals(item)) return i;
		}
		return -1;
	}
}
