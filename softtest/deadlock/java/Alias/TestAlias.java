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
		
		//������ļ�
		//DTSJavaCompiler compiler = new DTSJavaCompiler(null,null, null);
		//boolean b=compiler.compileProject("temp", "temp");
		//if(!b){
		//	DTSJavaCompiler.printCompileInfo(compiler.getDiagnostics());
		//}	
		
		//���������﷨��
		System.out.println("���ɳ����﷨��...");
		JavaParser parser = new JavaParser(new JavaCharStream(new FileInputStream(parsefilename)));
		parser.setJDK15();
		ASTCompilationUnit astroot = parser.CompilationUnit();
		
		//�������ű�
		//new SymbolFacade().initializeWith(astroot);
		ScopeAndDeclarationFinder sc = new ScopeAndDeclarationFinder();
		astroot.jjtAccept(sc, null);	
		
		//����������Ϣ
		//System.out.println("��������...");
		new TypeSet("temp");
		astroot.jjtAccept(new PakageAndImportVisitor(), TypeSet.getCurrentTypeSet());
		astroot.getScope().resolveTypes(TypeSet.getCurrentTypeSet());
//		astroot.jjtAccept(new ExpressionTypeFinder(), TypeSet.getCurrentTypeSet());
		
		//������ֺ���������
		OccurrenceFinder of = new OccurrenceFinder();
		astroot.jjtAccept(of, null);
		
		//����������ͼ
		astroot.jjtAccept(new ControlFlowVisitor(), null);
		
		//��������
		
		astroot.jjtAccept(new NodeFinder(), null);
		astroot.jjtAccept(new SecondFinder(), null);
		astroot.jjtAccept(new SecondFinder(), null);
		astroot.jjtAccept(new TestFinder(), null);
	}

}
