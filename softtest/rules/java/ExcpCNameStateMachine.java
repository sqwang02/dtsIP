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
五   (一)命名规则不规范
1. 定义了形如继承异常类命名方式的类。
【例5-1】 下列程序：
   1   public class MyException{} 
定义的类名形如要扩展异常类，但是代码却没有那么做。

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
