/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */
package softtest.symboltable.java;

import softtest.ast.java.*;

import java.util.List;
import java.util.Stack;

import softtest.ast.java.ASTAllocationExpression;
import softtest.ast.java.ASTBlock;
import softtest.ast.java.ASTCatchStatement;
import softtest.ast.java.ASTClassOrInterfaceBody;
import softtest.ast.java.ASTClassOrInterfaceDeclaration;
import softtest.ast.java.ASTCompilationUnit;
import softtest.ast.java.ASTConstructorBlock;
import softtest.ast.java.ASTConstructorDeclaration;
import softtest.ast.java.ASTDoStatement;
import softtest.ast.java.ASTEnumDeclaration;
import softtest.ast.java.ASTFinallyStatement;
import softtest.ast.java.ASTForStatement;
import softtest.ast.java.ASTIfStatement;
import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.ASTMethodDeclarator;
import softtest.ast.java.ASTPackageDeclaration;
import softtest.ast.java.ASTSwitchStatement;
import softtest.ast.java.ASTSynchronizedStatement;
import softtest.ast.java.ASTTryStatement;
import softtest.ast.java.ASTVariableDeclaratorId;
import softtest.ast.java.ASTWhileStatement;
import softtest.ast.java.JavaParserVisitorAdapter;
import softtest.ast.java.Node;
import softtest.ast.java.SimpleJavaNode;
import softtest.ast.java.SimpleNode;

/**
 * Visitor for scope creation.
 * Visits all nodes of an AST and creates scope objects for nodes representing
 * syntactic entities which may contain declarations. For example, a block
 * may contain variable definitions (which are declarations) and
 * therefore needs a scope object where these declarations can be associated,
 * whereas an expression can't contain declarations and therefore doesn't need
 * a scope object.
 * With the exception of global scopes, each scope object is linked to its
 * parent scope, which is the scope object of the next embedding syntactic
 * entity that has a scope.
 */
public class ScopeAndDeclarationFinder extends JavaParserVisitorAdapter {

    /**
     * A stack of scopes reflecting the scope hierarchy when a node is visited.
     * This is used to set the parents of the created scopes correctly.
     */
    private Stack scopes = new Stack();

    /**
     * Sets the scope of a node and adjustes the scope stack accordingly.
     * The scope on top of the stack is set as the parent of the given scope,
     * which is then also stored on the scope stack.
     *
     * @param newScope the scope for the node.
     * @param node     the AST node for which the scope is to be set.
     * @throws java.util.EmptyStackException if the scope stack is empty.
     */
    private void addScope(Scope newScope, SimpleNode node) {
        newScope.setParent((Scope) scopes.peek());
        scopes.push(newScope);
        node.setScope(newScope);
    }

    /**
     * Creates a new local scope for an AST node.
     * The scope on top of the stack is set as the parent of the new scope,
     * which is then also stored on the scope stack.
     *
     * @param node the AST node for which the scope has to be created.
     * @throws java.util.EmptyStackException if the scope stack is empty.
     */
    private void createLocalScope(SimpleNode node) {
        addScope(new LocalScope(), node);
    }

    /**
     * Creates a new method scope for an AST node.
     * The scope on top of the stack is set as the parent of the new scope,
     * which is then also stored on the scope stack.
     *
     * @param node the AST node for which the scope has to be created.
     * @throws java.util.EmptyStackException if the scope stack is empty.
     */
    private void createMethodScope(SimpleNode node) {
        addScope(new MethodScope(node), node);
    }

    /**
     * Creates a new class scope for an AST node.
     * The scope on top of the stack is set as the parent of the new scope,
     * which is then also stored on the scope stack.
     *
     * @param node the AST node for which the scope has to be created.
     * @throws java.util.EmptyStackException if the scope stack is empty.
     */
    private void createClassScope(SimpleNode node) {
        if (node instanceof ASTClassOrInterfaceBody) {
            addScope(new ClassScope(), node);
        } else {
            addScope(new ClassScope(node.getImage()), node);
        }
    }

    /**
     * Creates a new global scope for an AST node.
     * The new scope is stored on the scope stack.
     *
     * @param node the AST node for which the scope has to be created.
     */
    private void createSourceFileScope(SimpleNode node) {
        // When we do full symbol resolution, we'll need to add a truly top-level GlobalScope.
        Scope scope;
        List packages = node.findChildrenOfType(ASTPackageDeclaration.class);
        if (!packages.isEmpty()) {
            Node n = (Node) packages.get(0);
            scope = new SourceFileScope(((SimpleNode) n.jjtGetChild(0)).getImage());
        } else {
            scope = new SourceFileScope();
        }
        scopes.push(scope);
        node.setScope(scope);
    }

    @Override
	public Object visit(ASTCompilationUnit node, Object data) {
        createSourceFileScope(node);
        cont(node);
        return data;
    }

    @Override
	public Object visit(ASTClassOrInterfaceDeclaration node, Object data) {    	
        createClassScope(node);
        Scope s = ((SimpleNode) node.jjtGetParent()).getScope();
        s.addDeclaration(new ClassNameDeclaration(node));
        cont(node);
        
        return data;
    }

    @Override
	public Object visit(ASTEnumDeclaration node, Object data) {
        createClassScope(node);
        cont(node);
        return data;
    }

    /*public Object visit(ASTClassOrInterfaceBodyDeclaration node, Object data) {
        if (node.isAnonymousInnerClass() || node.isEnumChild()) {
            createClassScope(node);
            cont(node);
        } else {
            super.visit(node, data);
        }
        return data;
    }*/
    
    @Override
	public Object visit(ASTClassOrInterfaceBody node, Object data) {
        if (node.jjtGetParent() instanceof ASTAllocationExpression ){//|| node.jjtGetParent() instanceof ASTEnumConstant) {        	
            createClassScope(node);
            cont(node);
        } else {
            super.visit(node, data);
        }
        return data;
    }

    @Override
	public Object visit(ASTBlock node, Object data) {
        createLocalScope(node);
        cont(node);
        return data;
    }

    @Override
	public Object visit(ASTCatchStatement node, Object data) {
        createLocalScope(node);
        cont(node);
        return data;
    }

    @Override
	public Object visit(ASTFinallyStatement node, Object data) {
        createLocalScope(node);
        cont(node);
        return data;
    }

    @Override
	public Object visit(ASTConstructorDeclaration node, Object data) {
        createMethodScope(node);
        cont(node);
        return data;
    }
    
    @Override
	public Object visit(ASTConstructorBlock node, Object data){
        createLocalScope(node);
        cont(node);
        return data;  	
    }

    @Override
	public Object visit(ASTMethodDeclaration node, Object data) {
        createMethodScope(node);
        ASTMethodDeclarator md = (ASTMethodDeclarator) node.getFirstChildOfType(ASTMethodDeclarator.class);
        node.getScope().getEnclosingClassScope().addDeclaration(new MethodNameDeclaration(md));
        cont(node);
        return data;
    }

    @Override
	public Object visit(ASTTryStatement node, Object data) {
        createLocalScope(node);
        cont(node);
        return data;
    }

    // TODO - what about while loops and do loops?
    @Override
	public Object visit(ASTForStatement node, Object data) {
        createLocalScope(node);
        cont(node);
        return data;
    }

    @Override
	public Object visit(ASTIfStatement node, Object data) {
        createLocalScope(node);
        cont(node);
        return data;
    }

    @Override
	public Object visit(ASTVariableDeclaratorId node, Object data) {
        VariableNameDeclaration decl = new VariableNameDeclaration(node);
        node.getScope().addDeclaration(decl);
        node.setNameDeclaration(decl);
        return super.visit(node, data);
    }

    @Override
	public Object visit(ASTSwitchStatement node, Object data) {
        createLocalScope(node);
        cont(node);
        return data;
    }

    private void cont(SimpleJavaNode node) {
        super.visit(node, null);
        scopes.pop();
    }
    
    //added by xqing
    @Override
	public Object visit(ASTDoStatement node , Object data) {
        createLocalScope(node);
        cont(node);
        return data;
    }
    
    @Override
	public Object visit(ASTWhileStatement node , Object data) {
        createLocalScope(node);
        cont(node);
        return data;
    }
    
    @Override
	public Object visit(ASTSynchronizedStatement node , Object data) {
        createLocalScope(node);
        cont(node);
        return data;
    }
}
