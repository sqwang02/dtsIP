package softtest.DefUseAnalysis.java;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import softtest.ast.java.*;
import softtest.ast.java.ASTCompilationUnit;
import softtest.ast.java.ASTConstructorDeclaration;
import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.JavaParserVisitorAdapter;
import softtest.ast.java.SimpleJavaNode;
import softtest.cfg.java.*;


public class DUAnaysisVistor extends JavaParserVisitorAdapter {
	@Override
	public Object visit(ASTCompilationUnit treenode, Object data) {
		//初始化变量出现的定义使用类型
		treenode.getScope().initDefUse();
		return visit((SimpleJavaNode) treenode, data);
	}

	@Override
	public Object visit(ASTConstructorDeclaration treenode, Object data) {
		Graph g = treenode.getGraph();
		if (g == null) {
			return null;
		}
		g.numberOrderVisit(new DUControlFlowVisitor(), null);
		
		// 输出到文件，测试用
		if (softtest.config.java.Config.CFGTRACE) {
			SimpleJavaNode simplejavanode = (SimpleJavaNode) treenode.jjtGetChild(1);
			String name = null;
			if (treenode.getType()!=null) {
				name = softtest.config.java.Config.DEBUGPATH + (treenode.getType());
			} else {
				name = softtest.config.java.Config.DEBUGPATH + simplejavanode.getImage();
			}
			name = name.replace(' ', '_')+"_DU";
			g.accept(new DumpEdgesVisitor(), name + ".dot");
			System.out.println("控制流图(定义引用)输出到了文件" + name + ".dot");
			try {
				java.lang.Runtime.getRuntime().exec("dot -Tjpg -o " + name + ".jpg " + name + ".dot").waitFor();
			} catch (IOException e1) {
				System.out.println(e1);
			} catch (InterruptedException e2) {
				System.out.println(e2);
			}
			System.out.println("控制流图(定义引用)打印到了文件" + name + ".jpg");
		}
		return null;
	}

	@Override
	public Object visit(ASTMethodDeclaration treenode, Object data) {
		Graph g = treenode.getGraph();
		if (g == null) {
			return null;
		}
		g.numberOrderVisit(new DUControlFlowVisitor(), null);
		
		// 输出到文件，测试用
		if (softtest.config.java.Config.CFGTRACE) {
			SimpleJavaNode simplejavanode = (SimpleJavaNode) treenode.jjtGetParent().jjtGetParent().jjtGetParent();
			String name = null;
			if (treenode.getType()!=null) {
				name = softtest.config.java.Config.DEBUGPATH + (treenode.getType());
			} else {
				name = softtest.config.java.Config.DEBUGPATH + simplejavanode.getImage();
			}
			name = name.replace(' ', '_')+"_DU";
			g.accept(new DumpEdgesVisitor(), name + ".dot");
			System.out.println("控制流图(定义引用)输出到了文件" + name + ".dot");
			try {
				java.lang.Runtime.getRuntime().exec("dot -Tjpg -o " + name + ".jpg " + name + ".dot").waitFor();
			} catch (IOException e1) {
				System.out.println(e1);
			} catch (InterruptedException e2) {
				System.out.println(e2);
			}
			System.out.println("控制流图(定义引用)打印到了文件" + name + ".jpg");
		}
		return null;
	}
}
