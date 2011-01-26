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
		Node getProp = root.getFirstChild().getFirstChild().getFirstChild();
		Node moduleNameNode = getProp.getNext();
		Node requireNode = moduleNameNode.getNext();
		if (requireNode.getType() == Token.ARRAYLIT) {
			Node first = requireNode.getFirstChild();
			while (first != null) {
				/**
				 * depName can be relative ./ , ../
				 */
				re.add(getDepModuleName(moduleName, first.getString()));
				first = first.getNext();
			}
		} else if (requireNode.getType() == Token.FUNCTION) {
			Node factoryNode = requireNode;
			findRequire(factoryNode, re);
			requireNode = new Node(Token.ARRAYLIT);
			for (String depName : re) {
				Node dep = Node.newString(depName);
				requireNode.addChildToBack(dep);
			}
			factoryNode.getParent().addChildBefore(requireNode, factoryNode);

			this.moduleCodes.put(moduleName, AstUtils.toSource(root));

			//normalize dep module name
			ArrayList<String> normalRe = new ArrayList<String>();
			for (String r : re) {
				normalRe.add(getDepModuleName(moduleName, r));
			}
			re = normalRe;
		}
		return re.toArray(new String[re.size()]);
	}


	private void findRequire(Node factoryNode, ArrayList<String> re) {
		Node first = factoryNode.getFirstChild();
		while (first != null) {
			if (first.getType() == Token.CALL) {
				Node name = first.getFirstChild();
				if (name.getType() == Token.NAME && name.getString().equals("require")) {
					Node dep = name.getNext();
					if (dep != null && dep.getType() == Token.STRING) {
						//keep dep name still when modify ast
						re.add(dep.getString());
					}
				}
			}
			findRequire(first, re);
			first = first.getNext();
		}
	}
}
