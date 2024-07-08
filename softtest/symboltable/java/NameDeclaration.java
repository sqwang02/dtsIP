/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */
package softtest.symboltable.java;

import softtest.ast.java.SimpleNode;

public interface NameDeclaration {
    SimpleNode getNode();

    String getImage();

    Scope getScope();
}
