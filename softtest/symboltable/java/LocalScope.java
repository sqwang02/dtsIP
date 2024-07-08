/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */
package softtest.symboltable.java;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import softtest.ast.java.ASTName;
import softtest.ast.java.ASTPrimarySuffix;
import softtest.ast.java.SimpleNode;
import softtest.util.java.Applier;

public class LocalScope extends AbstractScope {

	protected Map variableNames = new HashMap();

	public NameDeclaration addVariableNameOccurrence(NameOccurrence occurrence) {
		NameDeclaration decl = findVariableHere(occurrence);
		if (decl != null && !occurrence.isThisOrSuper()) {
			List nameOccurrences = (List) variableNames.get(decl);
			nameOccurrences.add(occurrence);
			// added by xqing
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

	@Override
	public Map getVariableDeclarations() {
		VariableUsageFinderFunction f = new VariableUsageFinderFunction(variableNames);
		Applier.apply(f, variableNames.keySet().iterator());
		return f.getUsed();
	}

	public void addDeclaration(VariableNameDeclaration nameDecl) {
		if (variableNames.containsKey(nameDecl)) {
			throw new RuntimeException("Variable " + nameDecl + " is already in the symbol table");
		}
		variableNames.put(nameDecl, new ArrayList());
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

	@Override
	public String toString() {
		return "LocalScope:" + glomNames(variableNames.keySet().iterator());
	}

	// added by xqing
	@Override
	public String dump() {
		StringBuffer b = new StringBuffer();
		b.append("LocalScope: ");
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
