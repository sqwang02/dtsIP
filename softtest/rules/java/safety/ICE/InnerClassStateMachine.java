package softtest.rules.java.safety.ICE;

import softtest.fsm.java.*;
import softtest.jaxen.java.DocumentNavigator;

import java.util.*;

import org.jaxen.*;
import softtest.ast.java.*;
import softtest.jaxen.java.*;
import softtest.rules.java.AbstractStateMachine;
import softtest.symboltable.java.MethodNameDeclaration;
import softtest.symboltable.java.NameDeclaration;
import softtest.symboltable.java.Scope;
import softtest.symboltable.java.VariableNameDeclaration;
import softtest.ast.java.ASTClassOrInterfaceDeclaration;
import softtest.ast.java.ASTConstructorDeclaration;
import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.ASTMethodDeclarator;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTPrimaryExpression;
import softtest.ast.java.ASTPrimarySuffix;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.config.java.Config;


/**
4. �ڲ��ඨ������    
Java�ֽ���û�ж����ڲ���ĸ�����������ڲ���ת������ͬһ���п��Է������д���
����ͨ�ࡣ��������ʹ��������˽�еģ��ڲ���Ҳ����˶Է�װ�����ķ���Ȩ��������
����ʹ���ڲ������������Ӧ���ⲿ�ࡣ
����2-8�� ���г���
   1   public class innerClsDef {
   2      private String data;
   3   	  class MyInnerClass {
   4   	    public void print() {
   5   		  System.out.println(data);
   6   	    }
   7  	  }
   8   }
 */

public class InnerClassStateMachine extends AbstractStateMachine{
	/**
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("��װ����ģʽ: %d �д����ڲ��ඨ�壬������©������ʹ��������˽�еģ��ڲ���Ҳ����˶Է�װ�����ķ���Ȩ�������߿���ʹ���ڲ������������Ӧ���ⲿ�ࡣ", errorline);
		}else{
			f.format("Incorrect Encapsule:inner class may cause a vulnerability on line %d",errorline);
		}
		fsmmi.setDescription(f.toString());
		f.close();
	}
	@Override
	public  void registerPrecondition(PreconditionListenerSet listeners) {
	}
	
	@Override
	public  void registerFeature(FeatureListenerSet listenerSet) {
	}

	public static List getTreeNode(SimpleJavaNode node, String str) {
		List evalRlts = null;
		try {
			XPath xpath = new BaseXPath(str, new DocumentNavigator());
			evalRlts = xpath.selectNodes(node);
		} catch (JaxenException e) {
			e.printStackTrace();
			throw new RuntimeException("xpath error",e);
		}
		return evalRlts;
	}
	
	public static List<FSMMachineInstance> createInnerClassStateMachines(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		
		String  xpathLeakOut = ".//PrimaryExpression[ ./PrimaryPrefix/Name[ matches(@Image, 'System.out.print') ] ]";
		String  xpathName    = ".//Name";
		
		
		FSMMachineInstance fsmInstance;
		Hashtable<VariableNameDeclaration, FSMMachineInstance> table = new Hashtable<VariableNameDeclaration, FSMMachineInstance>();
		
		List  primExprs = null;
		Scope curScope  = null;
		if( node instanceof ASTMethodDeclaration ) {
			ASTMethodDeclaration astMthd = (ASTMethodDeclaration) node;
			ASTMethodDeclarator  mthdtor = (ASTMethodDeclarator) astMthd.getFirstChildOfType(ASTMethodDeclarator.class);
			MethodNameDeclaration mdecl = mthdtor.getMethodNameDeclaration();
			curScope = mdecl.getScope().getEnclosingClassScope();
		}
		else if(node instanceof ASTConstructorDeclaration) {
			ASTConstructorDeclaration astCnstr = (ASTConstructorDeclaration) node;
			SimpleJavaNode parent = astCnstr;
			while( parent != null && !(parent instanceof ASTClassOrInterfaceDeclaration)) {
				parent = (SimpleJavaNode)parent.jjtGetParent();
			}
			if(parent == null) {
				return list;
			}
			ASTClassOrInterfaceDeclaration astClsDecl = (ASTClassOrInterfaceDeclaration)parent;
			curScope = astClsDecl.getScope().getEnclosingClassScope();
		}
		logc1(":" + curScope);
	
		// ��������ʹ����������ı��ʽ�ڵ�
		primExprs = getTreeNode( node, xpathLeakOut);
		for(int j = 0; j < primExprs.size(); j++) {
			ASTPrimaryExpression astPrimExpr = (ASTPrimaryExpression)primExprs.get(j);
			if(astPrimExpr.jjtGetNumChildren() != 2) {
				continue;
			}
			ASTPrimarySuffix astSuffix = (ASTPrimarySuffix)astPrimExpr.jjtGetChild(1);
			
			// �������г��ֵ�ASTName��Ӧ�ڵ������λ�ã��������ʹ�ð�Χ����ĳ�Ա���򱨴�
			List  astNames = getTreeNode(astSuffix, xpathName);
			for(int k = 0; k < astNames.size(); k++ ) {
				ASTName  astName = (ASTName) astNames.get(k);
				NameDeclaration ndecl = astName.getNameDeclaration();
				if( ndecl instanceof VariableNameDeclaration ) {
					VariableNameDeclaration vdecl = (VariableNameDeclaration) ndecl;
					Scope  symboldecl = vdecl.getDeclareScope();
					if( symboldecl != curScope && curScope.isSelfOrAncestor( symboldecl )) {
						fsmInstance = fsm.creatInstance();
						fsmInstance.setRelatedVariable( vdecl );
						fsmInstance.setRelatedObject(new FSMRelatedCalculation( astName ));
						table.put( vdecl, fsmInstance);
					}
				}
			}
		} 
		//logc1("cdecl: " + cdecl.printNode() + "  " + innerList.size());
		for( Enumeration<FSMMachineInstance> e = table.elements(); e.hasMoreElements(); ) {
			list.add( e.nextElement() );
		}
		return list;
	}

	public static boolean checkInnerClassOperation(List nodes, FSMMachineInstance fsmInst) {
		boolean found = false;
		if(nodes == null) {
			System.out.println("nodes is null, return");				
		}	
		logc2("+--------------+");
		logc2("| InnerClass   |" + fsmInst.getRelatedVariable());
		logc2("+--------------+");
		found = true;
		
		return found;
	}


	public static void logc1(String str) {
		logc("createInnerClassFSM(..) - " + str);
	}
	public static void logc2(String str) {
		logc("checkInnerClassOper(..) - " + str);
	}
	
	public static void logc(String str) {
		if( Config.DEBUG ) {
			System.out.println("ICFSM::" + str);
		}
	}
}
