package softtest.deadlock.java.Alias;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import softtest.ast.java.ASTCompilationUnit;
import softtest.ast.java.JavaCharStream;
import softtest.ast.java.JavaParser;
import softtest.cfg.java.ControlFlowVisitor;
import softtest.symboltable.java.ExpressionTypeFinder;
import softtest.symboltable.java.OccurrenceFinder;
import softtest.symboltable.java.PakageAndImportVisitor;
import softtest.symboltable.java.ScopeAndDeclarationFinder;
import softtest.symboltable.java.TypeSet;

public class TestAlias {

	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		String parsefilename="test_temp\\test.java";
		
		//编译该文件
		//DTSJavaCompiler compiler = new DTSJavaCompiler(null,null, null);
		//boolean b=compiler.compileProject("temp", "temp");
		//if(!b){
		//	DTSJavaCompiler.printCompileInfo(compiler.getDiagnostics());
		//}	
		
		//产生抽象语法树
		System.out.println("生成抽象语法树...");
		JavaParser parser = new JavaParser(new JavaCharStream(new FileInputStream(parsefilename)));
		parser.setJDK15();
		ASTCompilationUnit astroot = parser.CompilationUnit();
		
		//产生符号表
		//new SymbolFacade().initializeWith(astroot);
		ScopeAndDeclarationFinder sc = new ScopeAndDeclarationFinder();
		astroot.jjtAccept(sc, null);	
		
		//处理类型信息
		//System.out.println("处理类型...");
		new TypeSet("temp");
		astroot.jjtAccept(new PakageAndImportVisitor(), TypeSet.getCurrentTypeSet());
		astroot.getScope().resolveTypes(TypeSet.getCurrentTypeSet());
//		astroot.jjtAccept(new ExpressionTypeFinder(), TypeSet.getCurrentTypeSet());
		
		//处理出现和声明关联
		OccurrenceFinder of = new OccurrenceFinder();
		astroot.jjtAccept(of, null);
		
		//产生控制流图
		astroot.jjtAccept(new ControlFlowVisitor(), null);
		
		//别名分析
		
		astroot.jjtAccept(new NodeFinder(), null);
		astroot.jjtAccept(new SecondFinder(), null);
		astroot.jjtAccept(new SecondFinder(), null);
		astroot.jjtAccept(new TestFinder(), null);
	}

}
