//added by xqing
package softtest.symboltable.java;

import softtest.ast.java.ASTCompilationUnit;
import softtest.ast.java.ASTImportDeclaration;
import softtest.ast.java.ASTPackageDeclaration;
import softtest.ast.java.JavaParserVisitorAdapter;
import softtest.ast.java.SimpleJavaNode;
import softtest.ast.java.SimpleNode;
import softtest.ast.java.*;

/** 处理包声明和import声明的抽象语法树访问者，用于类型处理 */
public class PakageAndImportVisitor extends JavaParserVisitorAdapter {
    @Override
	public Object visit(SimpleJavaNode node, Object data) {
    	//阻止孩子节点访问
        //node.childrenAccept(this, data);
        return null;
    }
    @Override
	public Object visit(ASTCompilationUnit node, Object data) {
    	node.childrenAccept(this, data);
        return null;
    }
    @Override
	public Object visit(ASTPackageDeclaration node, Object data) {
    	TypeSet typeset=(TypeSet)data;
    	SimpleNode treenode=(SimpleNode)node.jjtGetChild(0);
    	typeset.setASTCompilationUnitPackage(treenode.getImage());
        return null;
    }

    @Override
	public Object visit(ASTImportDeclaration node, Object data) {
    	TypeSet typeset=(TypeSet)data;
    	SimpleNode treenode=(SimpleNode)node.jjtGetChild(0);
    	if(node.isStatic()){
    		typeset.addStaticImport(treenode.getImage());
    	}
    	else {
    		if(node.isImportOnDemand()){
    			typeset.addImport(treenode.getImage()+".*");
    		}else{
    			typeset.addImport(treenode.getImage());
    		}
    	}
        return null;
    }
}
