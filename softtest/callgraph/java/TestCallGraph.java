package softtest.callgraph.java;

import java.io.*;
import softtest.ast.java.*;
import softtest.symboltable.java.*;
import java.util.*;

import softtest.ast.java.ASTCompilationUnit;
import softtest.ast.java.JavaCharStream;
import softtest.ast.java.JavaParser;
import softtest.symboltable.java.SymbolFacade;


public class TestCallGraph {
	public static void main(String args[]) throws IOException{
		// 产生抽象语法树
		System.out.println("产生抽象语法树...");
		JavaParser parser = new JavaParser(new JavaCharStream(new FileInputStream("D:\\Day11.java")));
		parser.setJDK15();
		ASTCompilationUnit astroot = parser.CompilationUnit();

		// 产生符号表
		System.out.println("生成符号表...");
		new SymbolFacade().initializeWith(astroot);

		CGraph g = new CGraph();
		astroot.getScope().resolveCallRelation(g);
		
		List<CVexNode> list=g.getTopologicalOrderList();
		Collections.reverse(list);
		System.out.println("调用关系拓扑逆序：");
		for(CVexNode n:list){
			System.out.print(n.getName()+"  ");
		}
		System.out.println();
		
		String name ="D:\\Day11";
		g.accept(new DumpCGraphVisitor(), name + ".dot");
		System.out.println("调用图图输出到了文件" + name + ".dot");
		try {
			java.lang.Runtime.getRuntime().exec("dot -Tjpg -o " + name + ".jpg " + name + ".dot").waitFor();
		} catch (IOException e1) {
			System.out.println(e1);
		} catch (InterruptedException e2) {
			System.out.println(e2);
		}
		System.out.println("调用图打印到了文件" + name + ".jpg");
	}
}
