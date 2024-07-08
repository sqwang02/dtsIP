package softtest.rules.java.safety.CQ;
import java.util.Formatter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import softtest.IntervalAnalysis.java.DomainData;
import softtest.IntervalAnalysis.java.ExpressionDomainVisitor;
import softtest.ast.java.ASTExpression;
import softtest.ast.java.ASTIfStatement;
import softtest.ast.java.ASTRelationalExpression;
import softtest.ast.java.SimpleJavaNode;
import softtest.ast.java.ASTForStatement;
import softtest.ast.java.ASTWhileStatement;
import softtest.ast.java.ASTDoStatement;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;
import softtest.rules.java.AbstractStateMachine;
public class CQ_TrueOrFalseStateMachine extends AbstractStateMachine{
	/**
	 * ���ù�������
	 * ��������ʽ��������Ϊ false��������԰�һ�ָ�Ϊ�򵥵ķ�ʽ��д��
	 * ���丽������ĳ��ֿ����ǳ��ڵ���Ŀ�ģ����߿���û��������е���������һͬ����ά����
	 * �ñ��ʽ������Ϊ����ָ�������еĴ������ڡ�
	 * (û�м��ѭ���б��ʽ��ֵ�仯�����)
	 * 09.09.25@baigele
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("��������CQ: %d ��Booleanֵ��Ϊtrue����false���������һ��©��", errorline);
		}else{
			f.format("Code Quality: The value of boolean expression is alway true or false on line %d",errorline);
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
	public static List<FSMMachineInstance> createTrueOrFalseStateMachines(SimpleJavaNode node, FSMMachine fsm)
	{
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		
		DomainData exprdata = new DomainData();
		exprdata.sideeffect=false;
		ExpressionDomainVisitor exprvisitor = new ExpressionDomainVisitor();
		String xpath = "";
		List evalRlts = null;
		Iterator i = null;

//		if statement		
		xpath=".//IfStatement/Expression[1][@TypeString='boolean']";
		evalRlts = node.findXpath(xpath);
		i = evalRlts.iterator();
		while(i.hasNext())
		{
			ASTExpression ce = (ASTExpression) i.next();
//			ASTRelationalExpression ce = (ASTRelationalExpression) i.next();
			ce.jjtAccept(exprvisitor, exprdata);
			if(ce.getFirstParentOfType(ASTWhileStatement.class)!=null||ce.getFirstParentOfType(ASTForStatement.class)!=null||ce.getFirstParentOfType(ASTDoStatement.class)!=null)continue;		
			if(exprdata.domain.toString().equals("FALSE")||exprdata.domain.toString().equals("TRUE")){	
				FSMMachineInstance fsmInst = fsm.creatInstance();
				fsmInst.setRelatedObject(new FSMRelatedCalculation(ce));
				fsmInst.setResultString("The value of if statement is alway "+exprdata.domain);
				list.add(fsmInst);
			}
		}
//		
		
		return list;
	}
}
