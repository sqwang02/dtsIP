package softtest.DefUseAnalysis.java;

import java.io.*;

import softtest.ast.java.*;
import softtest.symboltable.java.*;
import softtest.ast.java.ASTCompilationUnit;
import softtest.ast.java.JavaCharStream;
import softtest.ast.java.JavaParser;
import softtest.cfg.java.*;
import softtest.symboltable.java.SymbolFacade;


public class TestDefUseAnalysis {
	public static void main(String args[])throws IOException, ClassNotFoundException{
		String parsefilename="F:\\javaio\\IAOFault.java";

		//产生抽象语法树
		System.out.println("生成抽象语法树...");
		JavaParser parser = new JavaParser(new JavaCharStream(new FileInputStream(parsefilename)));
		parser.setJDK15();
		ASTCompilationUnit astroot = parser.CompilationUnit();
		
		//产生控制流图
		System.out.println("生成控制流图...");
		astroot.jjtAccept(new ControlFlowVisitor(), null);
		
		//产生符号表
		System.out.println("生成符号表...");
		new SymbolFacade().initializeWith(astroot);
		
		//处理定义使用
		System.out.println("生成定义使用链...");
		astroot.jjtAccept(new DUAnaysisVistor() , null);
		
		
		System.out.println("分析完毕.");
	}
}
