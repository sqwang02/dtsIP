package softtest.rules.java.safety.AAPI;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;

import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.config.java.Config;


/**
类的比较问题
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
public class AbuseAPI_ClsCmpStateMachine extends AbstractStateMachine{
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("滥用API模式:在 %d 行上试图通过比较两个对象的类名，去查看两个类是否相等，可能造成一个漏洞。通过名字的方式进行类比较，可能会使用错误的类。攻击者可以提供一个与TrustedClassName相同名字的类，对应用程序进行攻击。", errorline);
		}else{
			f.format("Abuse Application Program Interface: try to compare two classes by name on line %d.Comparing two class by name may get the wrong class.",errorline);
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
	public static List<FSMMachineInstance> createClsCmpStateMachines(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		String  xpathStr = ".//PrimaryExpression[./PrimaryPrefix/Name[matches(@Image,'\\.getClass'] and ./PrimarySuffix[1][@Arguments='true'] and ./PrimarySuffix[2][@Image='getName'] and ./PrimarySuffix[3][@Arguments='true'] and ./PrimarySuffix[4][@Image='equals'] and ./PrimarySuffix[5][@Arguments='true'] ";
		
		//ASTMethodDeclaration mthdDecl;
		
		List evalRlts = node.findXpath(xpathStr);
		Iterator i=evalRlts.iterator();
		while(i.hasNext()){
			ASTPrimaryExpression astPrimExpr = ( ASTPrimaryExpression )i.next();
			FSMMachineInstance fsmInst = fsm.creatInstance();
			fsmInst.setRelatedObject(new FSMRelatedCalculation(astPrimExpr));
			fsmInst.setResultString("class compare");
			list.add(fsmInst);
		}

		/*
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
					logc1("no equals :" + astSufix.printNode());
					continue;
				}
				astSufix = (ASTPrimarySuffix) astPrimExpr.jjtGetChild(num - 4);
				if( astSufix.getImage() == null || ! astSufix.getImage().equals("getName") ) {
					logc1("no equals :" + astSufix.printNode());
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
		}*/
		return list;
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
