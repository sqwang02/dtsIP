package softtest.IntervalAnalysis.java;

import softtest.ast.java.*;
import softtest.symboltable.java.*;
import java.io.*;
import java.util.*;

import softtest.ast.java.ASTCompilationUnit;
import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.JavaCharStream;
import softtest.ast.java.JavaParser;
import softtest.callgraph.java.CGraph;
import softtest.callgraph.java.CVexNode;
import softtest.cfg.java.*;
import softtest.symboltable.java.PakageAndImportVisitor;
import softtest.symboltable.java.SymbolFacade;
import softtest.symboltable.java.TypeSet;


public class TestInterval {
	public static void main(String args[]) throws IOException, ClassNotFoundException {
		//编译被测程序以获得类型信息
		/*System.out.println("编译被测程序...");
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler != null) {
			int result = compiler.run(null, null, null, "Test.java");
			if (result != 0) {
				System.out.println("编译被测程序失败");
			}
		}
		else{
			System.out.println("编译被测程序失败");
		}*/
		//产生抽象语法树
		System.out.println("生成抽象语法树...");
		JavaParser parser = new JavaParser(new JavaCharStream(new FileInputStream("D:\\workspacejava\\demo\\src\\bugPro\\IAOFault.java")));
		parser.setJDK15();
		ASTCompilationUnit astroot = parser.CompilationUnit();
		//产生控制流图
		System.out.println("生成控制流图...");
		astroot.jjtAccept(new ControlFlowVisitor(), null);
		//产生符号表
		System.out.println("生成符号表...");
		new SymbolFacade().initializeWith(astroot);
		//处理类型信息
		System.out.println("处理类型...");
		TypeSet typeset = new TypeSet("temp");
		astroot.jjtAccept(new PakageAndImportVisitor(), typeset);
		astroot.getScope().resolveTypes(typeset);
		//初始化区间
		System.out.println("初始化变量区间...");
		astroot.getScope().initDomains();
		
		System.out.println("生成函数调用图...");
		CGraph g = new CGraph();
		astroot.getScope().resolveCallRelation(g);
		
		System.out.println("计算区间...");
		List<CVexNode> list=g.getTopologicalOrderList();
		Collections.reverse(list);
		
		//按照拓扑逆序先计算普通函数区间
		ControFlowDomainVisitor tempvisitor=new ControFlowDomainVisitor();
		for(CVexNode n:list){
			ASTMethodDeclaration method=(ASTMethodDeclaration)n.getMethodNameDeclaration().getMethodNameDeclaratorNode().jjtGetParent();
			tempvisitor.visit(method, null);
		}

		//DomainData exprdata=new DomainData();
		//astroot.jjtAccept(new ExpressionDomainVisitor(),exprdata);
		astroot.jjtAccept(new ControFlowDomainVisitor(), null);
	}
}
