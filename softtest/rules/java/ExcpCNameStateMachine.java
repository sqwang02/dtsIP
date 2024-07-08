package softtest.rules.java;

import softtest.fsm.java.*;
import softtest.jaxen.java.DocumentNavigator;

import java.util.*;

import org.jaxen.*;

import softtest.ast.java.*;
import softtest.jaxen.java.*;
import softtest.ast.java.ASTClassOrInterfaceDeclaration;
import softtest.ast.java.ASTClassOrInterfaceType;
import softtest.ast.java.ASTExtendsList;
import softtest.ast.java.SimpleJavaNode;
import softtest.cfg.java.*;
import softtest.config.java.Config;


/**
��   (һ)�������򲻹淶
1. ����������̳��쳣��������ʽ���ࡣ
����5-1�� ���г���
   1   public class MyException{} 
�������������Ҫ��չ�쳣�࣬���Ǵ���ȴû����ô����

2008-3-28
 */

public class ExcpCNameStateMachine {
	
	public static List<FSMMachineInstance> createExcpCNameStateMachines(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();

		List evalRlts = new ArrayList();

		// ///////////// Process Field Member //////////////

		String fieldstr = ".//ClassOrInterfaceDeclaration";

		try {
			XPath xpath = new BaseXPath(fieldstr, new DocumentNavigator());
			evalRlts = xpath.selectNodes(node);
		} catch (JaxenException e) {
			if (softtest.config.java.Config.DEBUG) {
				e.printStackTrace();
			}
			throw new RuntimeException("xpath error",e);
		}
		Iterator i = evalRlts.iterator();
		while (i.hasNext()) {
			ASTClassOrInterfaceDeclaration astCDecl = (ASTClassOrInterfaceDeclaration) i.next();
			String cname = astCDecl.getImage();
			if( ! cname.contains("Exception") ) {
				continue;
			}
			boolean  extExcep = false;
			if ( astCDecl.jjtGetChild(0) instanceof ASTExtendsList ) {
				ASTExtendsList extList = (ASTExtendsList) astCDecl.jjtGetChild(0);
				ASTClassOrInterfaceType extCls = (ASTClassOrInterfaceType)extList.jjtGetChild(0);
				if( extCls.getImage().contains("Exception") ) {
					extExcep = true;
				}
			}
			if( ! extExcep ) {
				FSMMachineInstance fsminstance = fsm.creatInstance();
				fsminstance.setRelatedObject(new FSMRelatedCalculation(astCDecl));
				fsminstance.setResultString( cname );
				list.add(fsminstance);
			}
		}

		return list;
	}
	
	
	public static boolean checkExcpCName(VexNode vex,FSMMachineInstance fsmInst) {

		return true;
	}
	
		
	public static void logc1(String str) {
		logc("createFieldNoUseStateMachines(..) - " + str);
	}
	public static void logc2(String str) {
		logc("checkFieldNoUse(..) - " + str);
	}
	
	public static void logc(String str) {
		if( Config.DEBUG ) {
			System.out.println("FieldNoUseStateMachine::" + str);
		}
	}
}
