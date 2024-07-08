package softtest.rules.java.question.BC;

import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;

import softtest.rules.java.AbstractStateMachine;
/**
 * OCStateMachine
 * 检查运算符的混淆
 * 描述:在if或while语句中进行条件判定时，有时候会把= = 与 = 混淆。当代码很复杂时，这种错误一般很难检查出来。
 举例：
   1   in boolean b ;
   2   if (b=false){ 
   3   		……….;
   4   }


 * @author cjie
 * 
 */

public class OCStateMachine extends AbstractStateMachine{
	
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("运算符的混淆: %d 行可能包括混淆的运算符", beginline);
		}else{
			f.format("Operator Confuse: at line %d There maybe a confused operator.", beginline);
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
	/**匹配IF语句条件*/
	private static String XPATH1=".//IfStatement/Expression[.//AssignmentOperator[matches(@Image,'^=$']]";
	/**匹配While语句条件*/
	private static String XPATH2=".//WhileStatement/Expression[.//AssignmentOperator[matches(@Image,'^=$']]";
	/**匹配DoWhile语句条件*/
	private static String XPATH3=".//DoStatement/Expression[.//AssignmentOperator[matches(@Image,'^=$']]";
		
	public static List<FSMMachineInstance> createOCStateMachine(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result=null;
		
		result=node.findXpath(XPATH1);
		for(Object o:result){
			ASTExpression exp=(ASTExpression)o;
			List eqExpList=exp.findChildrenOfType(ASTAssignmentOperator.class);
			for(Object co:eqExpList)
			{
				if(co instanceof ASTAssignmentOperator)
				{
					ASTAssignmentOperator eqExp=(ASTAssignmentOperator) co;
					ASTPrimaryExpression leftExp=null;
					if(eqExp.jjtGetParent().jjtGetChild(0) instanceof ASTPrimaryExpression)
					{
						 leftExp=(ASTPrimaryExpression)eqExp.jjtGetParent().jjtGetChild(0);
					}
					ASTPrimaryExpression rightExp=null;
					if(eqExp.jjtGetParent().jjtGetChild(2) instanceof ASTPrimaryExpression)
					{
						rightExp=(ASTPrimaryExpression) eqExp.jjtGetParent().jjtGetChild(2);
					}
					if(leftExp.getType()==boolean.class
							||leftExp.getType()==boolean.class)
					{
						FSMMachineInstance fsminstance = fsm.creatInstance();
						fsminstance.setResultString("if conditions has confused operator.");
						fsminstance.setRelatedObject(new FSMRelatedCalculation(exp));
						list.add(fsminstance);
						break;
					}
				}
			}
		}
		
		result=node.findXpath(XPATH2);
		for(Object o:result){
			ASTExpression exp=(ASTExpression)o;
			List eqExpList=exp.findChildrenOfType(ASTAssignmentOperator.class);
			for(Object co:eqExpList)
			{
				if(co instanceof ASTAssignmentOperator)
				{
					ASTAssignmentOperator eqExp=(ASTAssignmentOperator) co;
					ASTPrimaryExpression leftExp=null;
					if(eqExp.jjtGetParent().jjtGetChild(0) instanceof ASTPrimaryExpression)
					{
						 leftExp=(ASTPrimaryExpression)eqExp.jjtGetParent().jjtGetChild(0);
					}
					ASTPrimaryExpression rightExp=null;
					if(eqExp.jjtGetParent().jjtGetChild(2) instanceof ASTPrimaryExpression)
					{
						rightExp=(ASTPrimaryExpression) eqExp.jjtGetParent().jjtGetChild(2);
					}
					if(leftExp.getType()==boolean.class
							||leftExp.getType()==boolean.class)
					{
						FSMMachineInstance fsminstance = fsm.creatInstance();
						fsminstance.setResultString("While conditions has confused operator.");
						fsminstance.setRelatedObject(new FSMRelatedCalculation(exp));
						list.add(fsminstance);
						break;
					}
				}
			}
		}
		
		result=node.findXpath(XPATH3);
		for(Object o:result){
			ASTExpression exp=(ASTExpression)o;
			List eqExpList=exp.findChildrenOfType(ASTAssignmentOperator.class);
			for(Object co:eqExpList)
			{
				if(co instanceof ASTAssignmentOperator)
				{
					ASTAssignmentOperator eqExp=(ASTAssignmentOperator) co;
					ASTPrimaryExpression leftExp=null;
					if(eqExp.jjtGetParent().jjtGetChild(0) instanceof ASTPrimaryExpression)
					{
						 leftExp=(ASTPrimaryExpression)eqExp.jjtGetParent().jjtGetChild(0);
					}
					ASTPrimaryExpression rightExp=null;
					if(eqExp.jjtGetParent().jjtGetChild(2) instanceof ASTPrimaryExpression)
					{
						rightExp=(ASTPrimaryExpression) eqExp.jjtGetParent().jjtGetChild(2);
					}
					if(leftExp.getType()==boolean.class
							||leftExp.getType()==boolean.class)
					{
						FSMMachineInstance fsminstance = fsm.creatInstance();
						fsminstance.setResultString("DoWhile conditions has confused operator.");
						fsminstance.setRelatedObject(new FSMRelatedCalculation(exp));
						list.add(fsminstance);
						break;
					}
				}
			}
		}
		
		return list;
	}
}
