package softtest.callgraph.java.method;

import java.lang.reflect.*;
import java.util.*;

import softtest.ast.java.ASTClassOrInterfaceDeclaration;
import softtest.ast.java.ASTConstructorDeclaration;
import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.ExpressionBase;
import softtest.ast.java.JavaParserVisitorAdapter;

import softtest.ast.java.*;
import softtest.symboltable.java.ExpressionTypeFinder;
import softtest.symboltable.java.TypeSet;

public class MethodCallVisitor extends JavaParserVisitorAdapter {
	private Object cur = null;
	
	@Override
	public Object visit(ASTMethodDeclaration node, Object data) {
		Class[] parameterTypes=node.getParameterTypes();
		String classname = node.getScope().getEnclosingClassScope().getClassName();
		Class type = TypeSet.getCurrentTypeSet().findClassWithoutEx(classname);
		Method method = ExpressionTypeFinder.getMethodOfClass(node.getMethodName(), parameterTypes, type);
		MethodCallGraph mcg = (MethodCallGraph)data;
		
		if (method != null && !MethodNode.getMethodTable().containsKey(method)) {
		  new MethodNode(method,mcg.getCurrentFileName(),node.getBeginLine());
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
		if (cons != null && !MethodNode.getMethodTable().containsKey(cons)) {				  
			new MethodNode(cons,mcg.getCurrentFileName(),node.getBeginLine());
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
	
	@Override
	public Object visit(ExpressionBase node, Object data) {
		if(cur==null){
			return null;
		}
		MethodNode mncur=MethodNode.findMethodNode(cur);
		if(mncur==null){
			return null;
		}
		
		if (node.getType() instanceof Method || node.getType() instanceof Constructor) {
			MethodNode mn=MethodNode.findMethodNode(node.getType());
			if(mn!=null){
				mncur.addCalling(mn);
				mn.addCalled(mncur);
				return null;
			}
		}		
		return super.visit(node, data);
	}
}
