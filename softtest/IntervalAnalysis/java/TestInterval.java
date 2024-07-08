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
		//���뱻������Ի��������Ϣ
		/*System.out.println("���뱻�����...");
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler != null) {
			int result = compiler.run(null, null, null, "Test.java");
			if (result != 0) {
				System.out.println("���뱻�����ʧ��");
			}
		}
		else{
			System.out.println("���뱻�����ʧ��");
		}*/
		//���������﷨��
		System.out.println("���ɳ����﷨��...");
		JavaParser parser = new JavaParser(new JavaCharStream(new FileInputStream("D:\\workspacejava\\demo\\src\\bugPro\\IAOFault.java")));
		parser.setJDK15();
		ASTCompilationUnit astroot = parser.CompilationUnit();
		//����������ͼ
		System.out.println("���ɿ�����ͼ...");
		astroot.jjtAccept(new ControlFlowVisitor(), null);
		//�������ű�
		System.out.println("���ɷ��ű�...");
		new SymbolFacade().initializeWith(astroot);
		//����������Ϣ
		System.out.println("��������...");
		TypeSet typeset = new TypeSet("temp");
		astroot.jjtAccept(new PakageAndImportVisitor(), typeset);
		astroot.getScope().resolveTypes(typeset);
		//��ʼ������
		System.out.println("��ʼ����������...");
		astroot.getScope().initDomains();
		
		System.out.println("���ɺ�������ͼ...");
		CGraph g = new CGraph();
		astroot.getScope().resolveCallRelation(g);
		
		System.out.println("��������...");
		List<CVexNode> list=g.getTopologicalOrderList();
		Collections.reverse(list);
		
		//�������������ȼ�����ͨ��������
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
