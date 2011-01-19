package com.taobao.f2e;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * Created by IntelliJ IDEA.
 * User: chengyu
 * Date: 11-1-18
 * Time: 下午2:34
 * To change this template use File | Settings | File Templates.
 */
public class FileUtils {
	public static String getFileContent(String path, String encoding) {
		StringBuffer sb = new StringBuffer();
		BufferedReader r = null;
		try {
			r = new BufferedReader(new InputStreamReader(new FileInputStream(path), encoding));
			String line;
			while ((line = r.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (r != null) r.close();
			} catch (Exception e) {
			}
		}
		return sb.toString();
	}

	public static void outputContent(String content, String path, String encoding) {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(path, encoding);
			pw.println(content);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (pw != null) {
				pw.close();
			}
		}
	}

}
