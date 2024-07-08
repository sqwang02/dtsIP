package softtest.rules.java.question.BC;

import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;

import softtest.rules.java.AbstractStateMachine;
/**
 * FNECStateMachine
 * ��鸡�����Ĵ���Ƚ�
 * ����:�������ļ�����в���ȷ�ԣ�Ӧʹ�ô��ڵ��ڣ�С�ڵ��ڻ������ľ���ֵ��С���жϣ���(Math.abs(x1-x2) < MIN_DIFF)��
 ������
      1   public static double integral(MyFunction f, double x1,double x2) {
   2   		double x = x1;
   3   		double result = 0;
   4   		double step = (x2 - x1) / 700;
   5   		while (x != x2) { // should use (x <= x2)
   6   			result = result + f.valueFor(x) * step;
   7   			x = x + step;
   8   		}
   9   		return result;
   10   }

 * @author cjie
 * 
 */

public class FNECStateMachine extends AbstractStateMachine{
	
	
	/**
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("�������Ĵ���Ƚ�: %d ��ʹ���˴���ıȽϣ������ļ�����в���ȷ�ԣ�Ӧʹ�ô��ڵ��ڣ�С�ڵ��ڻ������ľ���ֵ��С���ж�", beginline);
		}else{
			f.format("Float Number Error Compare: at line %d find a float number error comparision.", beginline);
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
	/**ƥ��IF�������*/
	private static String XPATH1=".//IfStatement/Expression[.//EqualityExpression[matches(@Image,'^!=|==$']]";
	/**ƥ��While�������*/
	private static String XPATH2=".//WhileStatement/Expression[.//EqualityExpression[matches(@Image,'^!=|==$']]";
	/**ƥ��DoWhile�������*/
	private static String XPATH3=".//DoStatement/Expression[.//EqualityExpression[matches(@Image,'^!=|==$']]";
		
	public static List<FSMMachineInstance> createFNECStateMachine(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result=null;
		
		result=node.findXpath(XPATH1);
		for(Object o:result){
			ASTExpression exp=(ASTExpression)o;
			List eqExpList=exp.findChildrenOfType(ASTEqualityExpression.class);
			for(Object co:eqExpList)
			{
				if(co instanceof ASTEqualityExpression)
				{
					ASTEqualityExpression eqExp=(ASTEqualityExpression) co;
					ASTPrimaryExpression leftExp=null;
					if(eqExp.jjtGetChild(0) instanceof ASTPrimaryExpression)
					{
						 leftExp=(ASTPrimaryExpression) eqExp.jjtGetChild(0);
					}
					ASTPrimaryExpression rightExp=null;
					if(eqExp.jjtGetChild(1) instanceof ASTPrimaryExpression)
					{
						rightExp=(ASTPrimaryExpression) eqExp.jjtGetChild(1);
					}
					if(leftExp!=null
							&&(leftExp.getType()==double.class||leftExp.getType()==float.class)
							||
							rightExp!=null
							&&(rightExp.getType()==double.class||rightExp.getType()==float.class))
					{
						FSMMachineInstance fsminstance = fsm.creatInstance();
						fsminstance.setResultString("if conditions has unproper floating numer compare.");
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
			List eqExpList=exp.findChildrenOfType(ASTEqualityExpression.class);
			for(Object co:eqExpList)
			{
				if(co instanceof ASTEqualityExpression)
				{
					ASTEqualityExpression eqExp=(ASTEqualityExpression) co;
					ASTPrimaryExpression leftExp=null;
					if(eqExp.jjtGetChild(0) instanceof ASTPrimaryExpression)
					{
						 leftExp=(ASTPrimaryExpression) eqExp.jjtGetChild(0);
					}
					ASTPrimaryExpression rightExp=null;
					if(eqExp.jjtGetChild(1) instanceof ASTPrimaryExpression)
					{
						rightExp=(ASTPrimaryExpression) eqExp.jjtGetChild(1);
					}
					if(leftExp!=null
							&&(leftExp.getType()==double.class||leftExp.getType()==float.class)
							||
							rightExp!=null
							&&(rightExp.getType()==double.class||rightExp.getType()==float.class))
					{
						FSMMachineInstance fsminstance = fsm.creatInstance();
						fsminstance.setResultString("while conditions has unproper floating numer compare.");
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
			List eqExpList=exp.findChildrenOfType(ASTEqualityExpression.class);
			for(Object co:eqExpList)
			{
				if(co instanceof ASTEqualityExpression)
				{
					ASTEqualityExpression eqExp=(ASTEqualityExpression) co;
					ASTPrimaryExpression leftExp=null;
					if(eqExp.jjtGetChild(0) instanceof ASTPrimaryExpression)
					{
						 leftExp=(ASTPrimaryExpression) eqExp.jjtGetChild(0);
					}
					ASTPrimaryExpression rightExp=null;
					if(eqExp.jjtGetChild(1) instanceof ASTPrimaryExpression)
					{
						rightExp=(ASTPrimaryExpression) eqExp.jjtGetChild(1);
					}
					if(leftExp!=null
							&&(leftExp.getType()==double.class||leftExp.getType()==float.class)
							||
							rightExp!=null
							&&(rightExp.getType()==double.class||rightExp.getType()==float.class))
					{
						FSMMachineInstance fsminstance = fsm.creatInstance();
						fsminstance.setResultString("dowhile conditions has unproper floating numer compare.");
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
