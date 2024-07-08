package softtest.IntervalAnalysis.java;

import softtest.ast.java.*;
import softtest.symboltable.java.*;
import softtest.ast.java.ASTClassOrInterfaceDeclaration;
import softtest.ast.java.ASTConstructorDeclaration;
import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.JavaParserVisitorAdapter;
import softtest.ast.java.SimpleJavaNode;
import softtest.cfg.java.*;
import softtest.domain.java.*;
import softtest.symboltable.java.TypeSet;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;



/** 驱动区间运算的抽象语法树访问者 */
public class ControFlowDomainVisitor extends JavaParserVisitorAdapter {
	/** 迭代次数控制 */
	static int LOOP_NUM = 1;
	/** 驱动构造函数的控制流上的区间运算 */
	@Override
	public Object visit(ASTConstructorDeclaration treenode, Object data) {
		Graph g = treenode.getGraph();
		if(g==null){
			return null;
		}
		
		if (treenode.getType() == null) {			
			Class[] parameterTypes=treenode.getParameterTypes();
			String classname = treenode.getScope().getEnclosingClassScope().getClassName();
			Class type = TypeSet.getCurrentTypeSet().findClassWithoutEx(classname);
			Constructor cons = null;
			if (type != null) {
				cons = ExpressionTypeFinder.getConstructorOfClass(parameterTypes, type);
				treenode.setType(cons);
			}	
		}		
		
		// 迭代控制流图
		g.clearEdgeContradict();
		g.clearVexNodeContradict();
		for (int i = 0; i < LOOP_NUM; i++) {
			//g.dfs(new DomainVexVisitor(), null);
			g.numberOrderVisit(new DomainVexVisitor(), null);
			g.clearVisited();
		}

		// 输出到文件，测试用
		if (softtest.config.java.Config.CFGTRACE) {
			SimpleJavaNode simplejavanode = (SimpleJavaNode) treenode.jjtGetParent().jjtGetParent().jjtGetParent();
			String name = null;
			if (treenode.getType()!=null) {
				name = softtest.config.java.Config.DEBUGPATH + (treenode.getType());
			} else {
				name = softtest.config.java.Config.DEBUGPATH + simplejavanode.getImage();
			}
			name = name.replace(' ', '_');
			g.accept(new DumpEdgesVisitor(), name + ".dot");
			System.out.println("控制流图(区间运算)输出到了文件" + name + ".dot");
			try {
				java.lang.Runtime.getRuntime().exec("dot -Tjpg -o " + name + ".jpg " + name + ".dot").waitFor();
			} catch (IOException e1) {
				System.out.println(e1);
			} catch (InterruptedException e2) {
				System.out.println(e2);
			}
			System.out.println("控制流图(区间运算)打印到了文件" + name + ".jpg");
		}
		return null;
	}

	/** 驱动普通成员函数的控制流上的区间运算 */
	@Override
	public Object visit(ASTMethodDeclaration treenode, Object data) {
		Graph g = treenode.getGraph();
		if(g==null){
			return null;
		}
		if(treenode.getDomain()!=null){
			//已经计算过区间了
			return null;
		}else{
			if(treenode.getDomainType()==ClassType.ARBITRARY){
				treenode.setDomain(new ArbitraryDomain());
			}
		}
		
		if (treenode.getType() == null) {
			Class[] parameterTypes = treenode.getParameterTypes();
			String classname = treenode.getScope().getEnclosingClassScope().getClassName();
			Class type = TypeSet.getCurrentTypeSet().findClassWithoutEx(classname);
			Method method = ExpressionTypeFinder.getMethodOfClass(treenode.getMethodName(), parameterTypes, type);

			treenode.setType(method);
		}
			
		// 迭代控制流图
		g.clearEdgeContradict();
		g.clearVexNodeContradict();
		for (int i = 0; i < LOOP_NUM; i++) {
			//g.dfs(new DomainVexVisitor(), null);
			g.numberOrderVisit(new DomainVexVisitor(), null);
			g.clearVisited();
		}

		// 输出到文件，测试用
		if (softtest.config.java.Config.CFGTRACE) {
			SimpleJavaNode simplejavanode = (SimpleJavaNode) treenode.jjtGetChild(1);
			String name = null;
			if (treenode.getType()!=null) {
				name = softtest.config.java.Config.DEBUGPATH + (treenode.getType());
			} else {
				name = softtest.config.java.Config.DEBUGPATH + simplejavanode.getImage();
			}
			name = name.replace(' ', '_');
			g.accept(new DumpEdgesVisitor(), name + ".dot");
			System.out.println("控制流图(区间运算)输出到了文件" + name + ".dot");
			try {
				java.lang.Runtime.getRuntime().exec("dot -Tjpg -o " + name + ".jpg " + name + ".dot").waitFor();
			} catch (IOException e1) {
				System.out.println(e1);
			} catch (InterruptedException e2) {
				System.out.println(e2);
			}
			System.out.println("控制流图(区间运算)打印到了文件" + name + ".jpg");
		}
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
