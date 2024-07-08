package softtest.repair.java;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import softtest.ast.java.ASTCompilationUnit;
import softtest.ast.java.JavaCharStream;
import softtest.ast.java.JavaParser;
import softtest.callgraph.java.method.MethodNodeVisitor;
import softtest.fsmanalysis.java.ProjectAnalysis;
import softtest.symboltable.java.ExpressionTypeFinder;
import softtest.symboltable.java.OccurrenceFinder;
import softtest.symboltable.java.PakageAndImportVisitor;
import softtest.symboltable.java.ScopeAndDeclarationFinder;
import softtest.symboltable.java.TypeSet;
public class Jparser {
	public static void reBuildAST() {
		try{
			//ASTCompililationUnit unit = JavaParser.parse(new File("F:\\javaio\\Test.java"));
			JavaParser parser = new JavaParser(new JavaCharStream(
					new FileInputStream("F:\\javaio\\Test.java")));
			parser.setJDK15();
			ASTCompilationUnit astroot = parser.CompilationUnit();
			ScopeAndDeclarationFinder sc = new ScopeAndDeclarationFinder();
			astroot.jjtAccept(sc, null);
			astroot.jjtAccept(new PakageAndImportVisitor(), TypeSet
					.getCurrentTypeSet());
			astroot.getScope().resolveTypes(TypeSet.getCurrentTypeSet());
			astroot.jjtAccept(new ExpressionTypeFinder(), TypeSet
					.getCurrentTypeSet());
			OccurrenceFinder of = new OccurrenceFinder();
			astroot.jjtAccept(of, null);

			// 方法间调用关系分析
			//mcgraph.setCurrentFileName(sourceFile.getAbsolutePath());
			//astroot.jjtAccept(new MethodNodeVisitor(), mcgraph);
			//mcgraph.setCurrentFileName(null);
		} catch (Exception e) {
			if (softtest.config.java.Config.TESTING
					|| softtest.config.java.Config.DEBUG) {
				e.printStackTrace();
			}
			//ProjectAnalysis.logger.fatal(sourceFile
					//+ " encounter an pre-analysis error!\n"
					//+ e.getMessage());
		}
	}

}
