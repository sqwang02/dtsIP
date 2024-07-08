package softtest.cfg.java;

import softtest.ast.java.*;
//import softtest.symboltable.java.*;
import java.io.*;

import softtest.ast.java.ASTCompilationUnit;
import softtest.ast.java.ASTConstructorDeclaration;
import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.JavaCharStream;
import softtest.ast.java.JavaParser;
import softtest.ast.java.JavaParserVisitorAdapter;
import softtest.ast.java.SimpleJavaNode;
import softtest.symboltable.java.ExpressionTypeFinder;
import softtest.symboltable.java.OccurrenceFinder;
import softtest.symboltable.java.PakageAndImportVisitor;
import softtest.symboltable.java.ScopeAndDeclarationFinder;
import softtest.symboltable.java.TypeSet;

class PrintGraphVisitor extends JavaParserVisitorAdapter {
	/** 驱动构造函数的控制流上的区间运算 */
	@Override
	public Object visit(ASTConstructorDeclaration treenode, Object data) {
		Graph g = treenode.getGraph();
		if(g==null){
			return null;
		}
		SimpleJavaNode simplejavanode = (SimpleJavaNode) treenode.jjtGetParent().jjtGetParent().jjtGetParent();
		String name = "test_temp\\" + simplejavanode.getImage();
		g.accept(new DumpEdgesVisitor(), name + ".dot");
		System.out.println("控制流图输出到了文件" + name + ".dot");
		try {
			java.lang.Runtime.getRuntime().exec("dot -Tjpg -o " + name + ".jpg " + name + ".dot").waitFor();
		} catch (IOException e1) {
			System.out.println(e1);
		} catch (InterruptedException e2) {
			System.out.println(e2);
		}
		System.out.println("控制流图打印到了文件" + name + ".jpg");
		return null;
	}

	/** 驱动普通成员函数的控制流上的区间运算 */
	@Override
	public Object visit(ASTMethodDeclaration treenode, Object data) {
		Graph g = treenode.getGraph();
		if(g==null){
			return null;
		}
		// 输出到文件，测试用
		SimpleJavaNode simplejavanode = (SimpleJavaNode) treenode.jjtGetChild(1);
		String name = "test_temp\\" + simplejavanode.getImage();
		g.accept(new DumpEdgesVisitor(), name + ".do" +
				"");
		System.out.println("控制流图输出到了文件" + name + ".dot");
		try {
			java.lang.Runtime.getRuntime().exec("dot -Tjpg -o " + name + ".jpg " + name + ".dot").waitFor();
		} catch (IOException e1) {
			System.out.println(e1);
		} catch (InterruptedException e2) {
			System.out.println(e2);
		}
		System.out.println("控制流图打印到了文件" + name + ".jpg");
		return null;
	}
}
public class TestGraph {
	public static void main(String args[]) throws IOException, ClassNotFoundException {
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
		astroot.jjtAccept(new ExpressionTypeFinder(), TypeSet.getCurrentTypeSet());
		
		//处理出现和声明关联
		OccurrenceFinder of = new OccurrenceFinder();
		astroot.jjtAccept(of, null);
		
		//产生控制流图
		astroot.jjtAccept(new ControlFlowVisitor(), null);
		
		astroot.jjtAccept(new PrintGraphVisitor(), null);

	}
}