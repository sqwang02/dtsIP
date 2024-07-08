/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */
package softtest.symboltable.java;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import softtest.ast.java.ASTConstructorDeclaration;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTPrimarySuffix;
import softtest.ast.java.SimpleNode;
import softtest.util.java.Applier;

public class MethodScope extends AbstractScope {

	protected Map variableNames = new HashMap();

	private SimpleNode node;

	public MethodScope(SimpleNode node) {
		this.node = node;
	}

	@Override
	public MethodScope getEnclosingMethodScope() {
		return this;
	}

	@Override
	public Map getVariableDeclarations() {
		VariableUsageFinderFunction f = new VariableUsageFinderFunction(variableNames);
		Applier.apply(f, variableNames.keySet().iterator());
		return f.getUsed();
	}

	public NameDeclaration addVariableNameOccurrence(NameOccurrence occurrence) {
		NameDeclaration decl = findVariableHere(occurrence);
		if (decl != null && !occurrence.isThisOrSuper()) {
			((List) variableNames.get(decl)).add(occurrence);
			//added by xqing
			occurrence.setDeclaration(decl);
			SimpleNode n = occurrence.getLocation();
			if (n instanceof ASTName) {
				((ASTName) n).setNameDeclaration(decl);
			} // TODO what to do with PrimarySuffix case?
			// added by xqing
			else if (n instanceof ASTPrimarySuffix) {
				// this.i
				((ASTPrimarySuffix) n).setNameDeclaration(decl);
			}
		}
		return decl;
	}

	public void addDeclaration(VariableNameDeclaration variableDecl) {
		if (variableNames.containsKey(variableDecl)) {
			throw new RuntimeException("Variable " + variableDecl + " is already in the symbol table");
		}
		variableNames.put(variableDecl, new ArrayList());
	}

	@Override
	public NameDeclaration findVariableHere(NameOccurrence occurrence) {
		if (occurrence.isThisOrSuper() || occurrence.isMethodOrConstructorInvocation()) {
			return null;
		}
		ImageFinderFunction finder = new ImageFinderFunction(occurrence.getImage());
		Applier.apply(finder, variableNames.keySet().iterator());
		return finder.getDecl();
	}

	public String getName() {
		if (node instanceof ASTConstructorDeclaration) {
			return this.getEnclosingClassScope().getClassName();
		}
		return ((SimpleNode) node.jjtGetChild(1)).getImage();
	}

	@Override
	public String toString() {
		return "MethodScope:" + glomNames(variableNames.keySet().iterator());
	}

	//added by xqing
	public SimpleNode getAstTreeNode() {
		return node;
	}

	@Override
	public String dump() {
		StringBuffer b = new StringBuffer();
		b.append("MethodScope(" + getName() + "): ");
		if (!variableNames.isEmpty()) {
			b.append("(variables: ");
			Iterator i = variableNames.keySet().iterator();
			while (i.hasNext()) {
				VariableNameDeclaration mnd = (VariableNameDeclaration) i.next();
				b.append(mnd.getImage().toString());
				if (i.hasNext()) {
					b.append(",");
				}
			}
			b.append(")");
		}
		return b.toString();
	}
}
