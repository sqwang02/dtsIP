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



/** ������������ĳ����﷨�������� */
public class ControFlowDomainVisitor extends JavaParserVisitorAdapter {
	/** ������������ */
	static int LOOP_NUM = 1;
	/** �������캯���Ŀ������ϵ��������� */
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
		
		// ����������ͼ
		g.clearEdgeContradict();
		g.clearVexNodeContradict();
		for (int i = 0; i < LOOP_NUM; i++) {
			//g.dfs(new DomainVexVisitor(), null);
			g.numberOrderVisit(new DomainVexVisitor(), null);
			g.clearVisited();
		}

		// ������ļ���������
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
			System.out.println("������ͼ(��������)��������ļ�" + name + ".dot");
			try {
				java.lang.Runtime.getRuntime().exec("dot -Tjpg -o " + name + ".jpg " + name + ".dot").waitFor();
			} catch (IOException e1) {
				System.out.println(e1);
			} catch (InterruptedException e2) {
				System.out.println(e2);
			}
			System.out.println("������ͼ(��������)��ӡ�����ļ�" + name + ".jpg");
		}
		return null;
	}

	/** ������ͨ��Ա�����Ŀ������ϵ��������� */
	@Override
	public Object visit(ASTMethodDeclaration treenode, Object data) {
		Graph g = treenode.getGraph();
		if(g==null){
			return null;
		}
		if(treenode.getDomain()!=null){
			//�Ѿ������������
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
			
		// ����������ͼ
		g.clearEdgeContradict();
		g.clearVexNodeContradict();
		for (int i = 0; i < LOOP_NUM; i++) {
			//g.dfs(new DomainVexVisitor(), null);
			g.numberOrderVisit(new DomainVexVisitor(), null);
			g.clearVisited();
		}

		// ������ļ���������
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
			System.out.println("������ͼ(��������)��������ļ�" + name + ".dot");
			try {
				java.lang.Runtime.getRuntime().exec("dot -Tjpg -o " + name + ".jpg " + name + ".dot").waitFor();
			} catch (IOException e1) {
				System.out.println(e1);
			} catch (InterruptedException e2) {
				System.out.println(e2);
			}
			System.out.println("������ͼ(��������)��ӡ�����ļ�" + name + ".jpg");
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
