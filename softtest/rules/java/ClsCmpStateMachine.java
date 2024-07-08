package softtest.rules.java;

import softtest.fsm.java.*;
import softtest.fsmanalysis.java.ProjectAnalysis;
import softtest.jaxen.java.DocumentNavigator;

import java.util.*;

import org.jaxen.*;
import softtest.ast.java.*;
import softtest.jaxen.java.*;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTPrimaryExpression;
import softtest.ast.java.ASTPrimaryPrefix;
import softtest.ast.java.ASTPrimarySuffix;
import softtest.ast.java.SimpleJavaNode;
import softtest.config.java.Config;


/**
3．对象的比较问题
通过比较两个对象的类名，去查看两个类是否一样。
【例2-7】 下列程序
   1   public void privateMethod(Object object1, Object object2){
   2   		if(object1.getClass().getName().equals("anotherClass")) {// wrong
   3   		// do work based on the assumption we're dealing with the right object
   4   		}
   5   		if (object1.getClass() == object2.getClass()) { //correct
   6   		// do work based on the fact that the objects are the of the same class
   7   		}
   8   }

2008-3-13
 */

public class ClsCmpStateMachine {

	public static List<FSMMachineInstance> createClsCmpStateMachines(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		String  xpathStr = ".//PrimaryExpression[ ./PrimarySuffix[@Image='getName'] and ./PrimarySuffix[@Image='equals'] ]";
		
		//ASTMethodDeclaration mthdDecl;
		
		List evalRlts = null;
		try {
			XPath xpath = new BaseXPath(xpathStr, new DocumentNavigator());
			evalRlts = xpath.selectNodes(node);
		} catch (JaxenException e) {
			e.printStackTrace();
			throw new RuntimeException("xpath error",e);
		}
		if( evalRlts != null && evalRlts.size() > 0 ) {
			//Hashtable<VariableNameDeclaration, FSMMachineInstance> table = new Hashtable<VariableNameDeclaration, FSMMachineInstance>();
			for(int i = 0; i < evalRlts.size(); i++) {
				ASTPrimaryExpression  astPrimExpr = ( ASTPrimaryExpression ) evalRlts.get(i);
				ASTPrimaryPrefix astPrimPrefix = (ASTPrimaryPrefix) astPrimExpr.jjtGetChild(0);
				ASTName                astName = null;
				if( astPrimExpr.jjtGetNumChildren() < 6 ) {
					logc1("NumChildren :" + astPrimExpr.jjtGetNumChildren());
					continue;
				}
				int num = astPrimExpr.jjtGetNumChildren();
				
				ASTPrimarySuffix astSufix = (ASTPrimarySuffix) astPrimExpr.jjtGetChild(num - 2);
				if( astSufix.getImage() == null || ! astSufix.getImage().equals("equals") ) {
					logc1("no equals :" + astSufix.printNode(ProjectAnalysis.getCurrent_file()));
					continue;
				}
				astSufix = (ASTPrimarySuffix) astPrimExpr.jjtGetChild(num - 4);
				if( astSufix.getImage() == null || ! astSufix.getImage().equals("getName") ) {
					logc1("no equals :" + astSufix.printNode(ProjectAnalysis.getCurrent_file()));
					continue;
				}
				
				if( astPrimExpr.jjtGetChild(num-6) instanceof ASTPrimarySuffix ) {
					astSufix = (ASTPrimarySuffix) astPrimExpr.jjtGetChild(num-6);
					if( astSufix.getImage() == null || ! astSufix.getImage().equals("getClass") ) {
						continue;
					}
					FSMMachineInstance fsmInst = fsm.creatInstance();
					fsmInst.setRelatedObject(new FSMRelatedCalculation(astPrimExpr));
					list.add(fsmInst);
				}
				else if( astPrimExpr.jjtGetChild(num-6) instanceof ASTPrimaryPrefix ) {
					if( astPrimPrefix.jjtGetNumChildren() > 0 && astPrimPrefix.jjtGetChild(0) instanceof ASTName ) {
						astName = (ASTName) astPrimPrefix.jjtGetChild(0);
						String img = ((ASTName)astPrimPrefix.jjtGetChild(0)).getImage();
						if( ! img.contains("getClass")) {
							continue;
						}
						FSMMachineInstance fsmInst = fsm.creatInstance();
						fsmInst.setRelatedObject(new FSMRelatedCalculation(astPrimExpr));
						list.add(fsmInst);
					}
					else {
						String img = astPrimPrefix.getImage();
						if( ! img.contains("getClass")) {
							continue;
						}
						FSMMachineInstance fsmInst = fsm.creatInstance();
						fsmInst.setRelatedObject(new FSMRelatedCalculation(astPrimExpr));
						list.add(fsmInst);
					}
				}
			}
		}
		return list;
	}

	
	public static boolean checkClsCmp(List nodes, FSMMachineInstance fsmInst) {
		boolean found = false;
		if(nodes == null) {
			System.out.println("nodes is null, return");				
		}
		
		logc2("+--------------------+");
		logc2("| checkObjectCompare |" );
		logc2("+--------------------+");
		found = true;
		fsmInst.setResultString("comparing class with .getClass().getName().equals(..) method");
		
		return found;
	}

	

	public static void logc1(String str) {
		logc("createClsCmpStateMachines(..) - " + str);
	}
	public static void logc2(String str) {
		logc("checkClsCmp(..) - " + str);
	}
	
	public static void logc(String str) {
		if( Config.DEBUG ) {
			System.out.println("ClsCmpStateMachine::" + str);
		}
	}
}
