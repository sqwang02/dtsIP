package softtest.cfg.java;

import java.lang.reflect.*;
import java.util.HashSet;
import java.util.Set;

import softtest.ast.java.ASTAllocationExpression;
import softtest.ast.java.ASTPrimaryPrefix;
import softtest.ast.java.ASTPrimarySuffix;
import softtest.ast.java.JavaParserVisitorAdapter;

import softtest.ast.java.*;

public class ImplicitExceptionFinder extends JavaParserVisitorAdapter {
	private boolean hasimplicitexception=false;
	private Set<Class> exceptionSet = new HashSet<Class>();
	
	public Set<Class> getExceptions() {
		return exceptionSet;
	}
	
	@Override
	public Object visit(ASTPrimaryPrefix node, Object data) {
		if(node.isMethodName()){
			if(node.getType() instanceof Method){
				Method m=(Method)node.getType();
				if(m.getExceptionTypes().length>0){
					hasimplicitexception=true;
				}
				for (Class clz : m.getExceptionTypes()) {
					exceptionSet.add(clz);
				}
			}
		}
		super.visit(node, data);
		return null;
	}
	
	@Override
	public Object visit(ASTAllocationExpression node, Object data) {
		if (node.getType() instanceof Constructor) {
			Constructor m = (Constructor) node.getType();
			if (m.getExceptionTypes().length > 0) {
				hasimplicitexception = true;
			}
			for (Class clz : m.getExceptionTypes()) {
				exceptionSet.add(clz);
			}
		}
		super.visit(node, data);
		return null;
	}

	@Override
	public Object visit(ASTPrimarySuffix node, Object data) {
		if(node.isMethodName()){
			if(node.getType() instanceof Method){
				Method m=(Method)node.getType();
				if(m.getExceptionTypes().length>0){
					hasimplicitexception=true;
				}
				for (Class clz : m.getExceptionTypes()) {
					exceptionSet.add(clz);
				}
			}
		}
		super.visit(node, data);
		return null;
	}

	public boolean isHasImplicitException() {
		return hasimplicitexception;
	}

	public void setHasImplicitException(boolean hasimplicitexception) {
		this.hasimplicitexception = hasimplicitexception;
	}
	

}
