/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */
package softtest.symboltable.java;

import softtest.ast.java.SimpleNode;

public abstract class AbstractNameDeclaration implements NameDeclaration {

    protected SimpleNode node;

    public AbstractNameDeclaration(SimpleNode node) {
        this.node = node;
    }

    public SimpleNode getNode() {
        return node;
    }

    public String getImage() {
        return node.getImage();
    }

    public Scope getScope() {
        return node.getScope();
    }
}
