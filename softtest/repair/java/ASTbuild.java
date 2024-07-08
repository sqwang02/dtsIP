package softtest.repair.java;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;


import softtest.DefUseAnalysis.java.DUAnaysisVistor;
import softtest.ast.java.*;
import softtest.callgraph.java.method.MethodCallGraph;
import softtest.callgraph.java.method.MethodCallVisitor;
import softtest.cfg.java.ControlFlowVisitor;
import softtest.fsmanalysis.java.ProjectAnalysis;
import softtest.symboltable.java.ExpressionTypeFinder;
import softtest.symboltable.java.OccurrenceFinder;
import softtest.symboltable.java.PakageAndImportVisitor;
import softtest.symboltable.java.ScopeAndDeclarationFinder;
import softtest.symboltable.java.TypeSet;
import softtest.symboltable.java.VariableUsageFinderFunction;
import softtest.util.java.Applier;


public class ASTbuild extends ASTCompilationUnit{
	private static MethodCallGraph mcgraph = new MethodCallGraph();
	public static String current_file = null;
	public static ASTCompilationUnit current_astRoot = null;
	public static ASTCompilationUnit astBuild(String defectFile) throws FileNotFoundException{
		String classpath = "temp";
		new TypeSet(classpath + File.pathSeparator + "temp");
		TypeSet.clear();
		File sourceFile = new File(defectFile);
		
		JavaParser parser = new JavaParser(new JavaCharStream(
				new FileInputStream(sourceFile)));
		parser.setJDK15();
		ASTCompilationUnit astRoot = parser.CompilationUnit();

		ProjectAnalysis.setCurrent_file(sourceFile.getAbsolutePath());
		ProjectAnalysis.setCurrent_astroot(astRoot);

		// 符号表分析
		// 1. 作用域和声明分析
		// 2. 表达式类型分析
		// 3. 标记符出现分析
		ScopeAndDeclarationFinder sc = new ScopeAndDeclarationFinder();
		astRoot.jjtAccept(sc, null);
		astRoot.jjtAccept(new PakageAndImportVisitor(), TypeSet
				.getCurrentTypeSet());		
		astRoot.getScope().resolveTypes(TypeSet.getCurrentTypeSet());
		//astRoot.jjtaccept(new );
		/*
		 * 第二遍遍历语法树:将函数调用对应到正确的函数版本(能够将函数标记符的出现对应到正确的声明处),
		 * 需要先对函数形参和实参的类型进行分析并进行对应
		 */
		astRoot.jjtAccept(new ExpressionTypeFinder(), TypeSet
				.getCurrentTypeSet());
		/*
		 * 第三遍遍历语法树:对所有标记符出现的地方进行处理，将每一个标记符的出现和相应的正确声明进行联系
		 */
		OccurrenceFinder of = new OccurrenceFinder();
		astRoot.jjtAccept(of, null);
		//SimpleJavaNode rootNode = new SimpleJavaNode();
		//RelationNodeFinder relationNode = new RelationNodeFinder();
		//astRoot.jjtAccept(relationNode,null);
		//astRoot.jjtAccept(rootNode,null);
		// 产生控制流图
		astRoot.jjtAccept(new ControlFlowVisitor(), null); 

				// 处理定义使用
		astRoot.jjtAccept(new DUAnaysisVistor(), null);

				// 初始化区间
		astRoot.getScope().initDomains();
		
		// 方法间调用关系分析
		mcgraph.setCurrentFileName(sourceFile.getAbsolutePath());
		astRoot.jjtAccept(new MethodCallVisitor(), mcgraph);
		mcgraph.setCurrentFileName(null);
		//SynCondition.AstTraverse(astRoot);
		
		/*ASTCompilationUnit current_astRoot = null;

		
		String current_file = null;
		String filename = null;
		
		JavaParser parser = new JavaParser(new JavaCharStream(new FileInputStream(new File(args[0]))));
		parser.setJDK15();
		ASTCompilationUnit astRoot = parser.CompilationUnit();
		
		current_file = filename;
		
		current_astRoot = astRoot;
		
		ScopeAndDeclarationFinder sc = new ScopeAndDeclarationFinder();
		astRoot.jjtAccept(sc, null);
		astRoot.jjtAccept(new PakageAndImportVisitor(), TypeSet
				.getCurrentTypeSet());
		astRoot.getScope().resolveTypes(TypeSet.getCurrentTypeSet());
		astRoot.jjtAccept(new ExpressionTypeFinder(), TypeSet
				.getCurrentTypeSet());
		OccurrenceFinder of = new OccurrenceFinder();
		
		astRoot.jjtAccept(of, null);
		astRoot.jjtAccept(new DUAnaysisVistor(), null);
		
		astRoot.getScope().initDomains();*/
		return astRoot;
		
		
		
		
	}
	/*public Map getVariableDeclarations(String variableNames) {
		//ReadFile.ReadFile("D:\\workspacejava\\demo\\src\\bugPro\\IAOFault.java");
		
		
		VariableUsageFinderFunction f = new VariableUsageFinderFunction(variableNames);
		Applier.apply(f, variableNames.keySet().iterator());
		return f.getUsed();
	}*/
	

}
