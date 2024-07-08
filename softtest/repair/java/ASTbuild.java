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

		// ���ű����
		// 1. ���������������
		// 2. ���ʽ���ͷ���
		// 3. ��Ƿ����ַ���
		ScopeAndDeclarationFinder sc = new ScopeAndDeclarationFinder();
		astRoot.jjtAccept(sc, null);
		astRoot.jjtAccept(new PakageAndImportVisitor(), TypeSet
				.getCurrentTypeSet());		
		astRoot.getScope().resolveTypes(TypeSet.getCurrentTypeSet());
		//astRoot.jjtaccept(new );
		/*
		 * �ڶ�������﷨��:���������ö�Ӧ����ȷ�ĺ����汾(�ܹ���������Ƿ��ĳ��ֶ�Ӧ����ȷ��������),
		 * ��Ҫ�ȶԺ����βκ�ʵ�ε����ͽ��з��������ж�Ӧ
		 */
		astRoot.jjtAccept(new ExpressionTypeFinder(), TypeSet
				.getCurrentTypeSet());
		/*
		 * ����������﷨��:�����б�Ƿ����ֵĵط����д�����ÿһ����Ƿ��ĳ��ֺ���Ӧ����ȷ����������ϵ
		 */
		OccurrenceFinder of = new OccurrenceFinder();
		astRoot.jjtAccept(of, null);
		//SimpleJavaNode rootNode = new SimpleJavaNode();
		//RelationNodeFinder relationNode = new RelationNodeFinder();
		//astRoot.jjtAccept(relationNode,null);
		//astRoot.jjtAccept(rootNode,null);
		// ����������ͼ
		astRoot.jjtAccept(new ControlFlowVisitor(), null); 

				// ������ʹ��
		astRoot.jjtAccept(new DUAnaysisVistor(), null);

				// ��ʼ������
		astRoot.getScope().initDomains();
		
		// ��������ù�ϵ����
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
