/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */
package softtest.symboltable.java;


import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import softtest.ast.java.ASTArguments;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTPrimaryExpression;
import softtest.ast.java.ASTPrimaryPrefix;
import softtest.ast.java.ASTPrimarySuffix;
import softtest.ast.java.SimpleNode;

public class NameFinder {

    private LinkedList names = new LinkedList();

    public NameFinder(ASTPrimaryExpression node) {
        ASTPrimaryPrefix prefix = (ASTPrimaryPrefix) node.jjtGetChild(0);
        if (prefix.usesSuperModifier()) {
            add(new NameOccurrence(prefix, "super"));
        } else if (prefix.usesThisModifier()) {
            add(new NameOccurrence(prefix, "this"));
        }
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            checkForNameChild((SimpleNode) node.jjtGetChild(i));
        }
    }

    public List getNames() {
        return names;
    }

    private void checkForNameChild(SimpleNode node) {
        if (node.getImage() != null) {
            add(new NameOccurrence(node, node.getImage()));
        }
        if (node.jjtGetNumChildren() > 0 && node.jjtGetChild(0) instanceof ASTName) {
            ASTName grandchild = (ASTName) node.jjtGetChild(0);
            for (StringTokenizer st = new StringTokenizer(grandchild.getImage(), "."); st.hasMoreTokens();) {
                add(new NameOccurrence(grandchild, st.nextToken()));
            }
        }
        if (node instanceof ASTPrimarySuffix && ((ASTPrimarySuffix) node).isArguments()) {
            NameOccurrence occurrence = (NameOccurrence) names.getLast();
            occurrence.setIsMethodOrConstructorInvocation();
            ASTArguments args = (ASTArguments) ((ASTPrimarySuffix) node).jjtGetChild(0);
            occurrence.setArgumentCount(args.getArgumentCount());
        }
        
        // BUGFIX this.p 20090408
        /*if (node instanceof ASTPrimarySuffix && node.getPrevSibling() instanceof ASTPrimaryPrefix) {
        	ASTPrimaryPrefix pp = (ASTPrimaryPrefix) node.getPrevSibling();
        	ASTPrimarySuffix ps = (ASTPrimarySuffix) node;
        	if (pp.usesThisModifier() && ps.jjtGetNumChildren() == 0 && ps.getImage() != null) {
        		add(new NameOccurrence(ps, ps.getImage()));
        	}
        }*/
    }

    private void add(NameOccurrence name) {
        names.add(name);
        if (names.size() > 1) {
            NameOccurrence qualifiedName = (NameOccurrence) names.get(names.size() - 2);
            qualifiedName.setNameWhichThisQualifies(name);
        }
    }


    @Override
	public String toString() {
        StringBuffer result = new StringBuffer();
        for (Iterator i = names.iterator(); i.hasNext();) {
            NameOccurrence occ = (NameOccurrence) i.next();
            result.append(occ.getImage());
        }
        return result.toString();
    }
}
