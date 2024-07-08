package softtest.callgraph.java.method;

import java.lang.reflect.*;
import java.util.*;

import softtest.ast.java.*;
import softtest.symboltable.java.ExpressionTypeFinder;
import softtest.symboltable.java.TypeSet;

public class MethodNodeVisitor  extends JavaParserVisitorAdapter {
	private Object cur = null;
	
	@Override
	public Object visit(ASTMethodDeclaration node, Object data) {
		Class[] parameterTypes=node.getParameterTypes();
		String classname = node.getScope().getEnclosingClassScope().getClassName();
		Class type = TypeSet.getCurrentTypeSet().findClassWithoutEx(classname);
		
		if (type == null) {
			return null;
		}
		
		Method method = ExpressionTypeFinder.getMethodOfClass(node.getMethodName(), parameterTypes, type);
		MethodCallGraph mcg = (MethodCallGraph)data;
		new MethodNode(method,mcg.getCurrentFileName(),node.getBeginLine());
		
		if (method != null) {
			node.setType(method);
		}
						
		Object old = cur;
		cur = method;
		super.visit(node, data);
		cur = old;
		
		return null;
	}
	
	@Override
	public Object visit(ASTConstructorDeclaration node, Object data) {
		Class[] parameterTypes=node.getParameterTypes();
		String classname = node.getScope().getEnclosingClassScope().getClassName();
		Class type = TypeSet.getCurrentTypeSet().findClassWithoutEx(classname);
		
		if (type == null) {
			return null;
		}
		
		Constructor cons = ExpressionTypeFinder.getConstructorOfClass(parameterTypes, type);
		MethodCallGraph mcg = (MethodCallGraph)data;
		new MethodNode(cons,mcg.getCurrentFileName(),node.getBeginLine());
			
		if (cons != null) {
			node.setType(cons);
		}
			
		Object old = cur;
		cur = cons;
		super.visit(node, data);
		cur = old;

		return null;
	}
	
	@Override
	public Object visit(ASTClassOrInterfaceDeclaration node, Object data) {
		TypeSet.pushClassName(node.getImage());
		super.visit(node, data);
		TypeSet.popClassName();
		
		return null;
	}
}