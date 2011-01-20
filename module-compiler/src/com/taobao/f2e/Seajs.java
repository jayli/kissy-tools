package com.taobao.f2e;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

import java.util.ArrayList;

/**
 * @author yiminghe@gmail.com
 * @since 2011-01-20
 */
public class Seajs extends Main {
	/**
	 * @param moduleName module's name
	 * @param root	   module ast's root node
	 * @return normalized dep names
	 */
	protected String[] getDeps(String moduleName, Node root) {
		ArrayList<String> re = new ArrayList<String>();
		Node r = root.getFirstChild().getFirstChild().getChildAtIndex(2);
		if (r.getType() == Token.ARRAYLIT) {
			Node first = r.getFirstChild();
			while (first != null) {
				/**
				 * depName can be relative ./ , ../
				 */
				re.add(getDepModuleName(moduleName, first.getString()));
				first = first.getNext();
			}
		}
		return re.toArray(new String[re.size()]);
	}
}
