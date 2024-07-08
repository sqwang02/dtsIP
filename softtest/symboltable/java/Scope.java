/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */
package softtest.symboltable.java;

import java.util.Map;
//added by xqing 
import java.util.List;
import softtest.callgraph.java.*;

/**
 * Provides methods which all scopes must implement
 * <p/>
 * See JLS 6.3 for a description of scopes
 */
public interface Scope {

    /**
     * Returns a Map (VariableNameDeclaration->List(NameOccurrence,NameOccurrence)) of declarations that
     * exist at this scope
     */
    Map getVariableDeclarations();

    /**
     * Returns a Map (VariableNameDeclaration->List(NameOccurrence,NameOccurrence)) of declarations that
     * exist at this scope
     */
    Map getClassDeclarations();

    //added by xqing 
    Map getMethodDeclarations();
    /**
     * Add a class declaration to this scope.
     * Insert ClassNameDeclaration node into classNames, whose key contains ASTClassOrInterfaceDeclaration
     */
    void addDeclaration(ClassNameDeclaration decl);

    /**
     * Add a variable declaration to this scope
     */
    void addDeclaration(VariableNameDeclaration decl);

    /**
     * Add a method declaration to this scope
     */
    void addDeclaration(MethodNameDeclaration decl);

    /**
     * Tests whether or not a NameOccurrence is directly contained in the scope
     * Note that this search is just for this scope - it doesn't go diving into any
     * child scopes.
     */
    boolean contains(NameOccurrence occ);

    /**
     * Adds a NameOccurrence to this scope - only call this after getting
     * a true back from contains()
     */
    NameDeclaration addVariableNameOccurrence(NameOccurrence occ);

    /**
     * Points this scope to its parent
     */
    void setParent(Scope parent);

    /**
     * Retrieves this scope's parent
     */
    Scope getParent();
    //added by xqing
  
    List getChildrens();
    
    void resolveTypes(TypeSet typeset);
    
    void resolveCallRelation(CGraph g);
    
    void initDomains();
    
    void initDefUse();
    
    String print();
    
    boolean isSelfOrAncestor(Scope ancestor);   
    /**
     * Goes searching up the tree for this scope's enclosing ClassScope
     * This is handy if you're buried down in a LocalScope and need to
     * hop up to the ClassScope to find a method name.
     */
    ClassScope getEnclosingClassScope();

    /**
     * Goes searching up the tree for this scope's enclosing SourceFileScope
     * This is handy if you're buried down in a LocalScope and need to
     * hop up to the SourceFileScope to find a class name.
     */
    SourceFileScope getEnclosingSourceFileScope();

    /**
     * Goes searching up the tree for this scope's enclosing SourceFileScope
     * This is handy if you're buried down in a LocalScope and need to
     * hop up to the MethodScope to find a method parameter.
     */
    MethodScope getEnclosingMethodScope();
}
