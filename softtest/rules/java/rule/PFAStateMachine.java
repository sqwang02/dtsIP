package softtest.rules.java.rule;

import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;

import softtest.ast.java.ASTExpression;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;
import softtest.rules.java.AbstractStateMachine;

/**
 * PFAStateMachine
 * �������ò����а�����ֵ(PFA) 
 * ˵�������������ڲ�Ӧʹ��=��+=��-=��*=��/=��%=��>>=��<<=��&=��|=��^=��++��--�ȸ�ֵ��������
 * ʾ����
 * ��Ӧʹ�����´��� y=MyCount(x-=5) 
 * ��Ӧ��дΪ�� x = x-5; y=MyCount(x);
 * 
 * @author cjie
 * 
 */
public class PFAStateMachine extends AbstractStateMachine {
	/**
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline,
			int errorline) {
		Formatter f = new Formatter();
		if (softtest.config.java.Config.LANGUAGE == 0) {
			f.format("�������ò����а�����ֵ: �����Ĳ�����%d ���жԱ������и�ֵ��Υ���˴����̹淶", errorline);
		} else {
			f.format("Parameters in Function including Assignment: Parameters in function include assignment on line %d,that violates Code Conventions.",
						errorline);
		}
		fsmmi.setDescription(f.toString());
		f.close();
	}

	@Override
	public void registerPrecondition(PreconditionListenerSet listeners) {
	}

	@Override
	public void registerFeature(FeatureListenerSet listenerSet) {
	}

	private static String XPATH = ".//PrimaryExpression/PrimarySuffix/Arguments/ArgumentList/Expression[./AssignmentOperator|./PostfixExpression]";

	public static List<FSMMachineInstance> createPFAs(SimpleJavaNode node,
			FSMMachine fsm) {List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
			List result=null;
			
			result=node.findXpath(XPATH);
			for(Object o:result){
				ASTExpression as=(ASTExpression)o;
				
				FSMMachineInstance fsminstance = fsm.creatInstance();
				fsminstance.setResultString("Parameters in function including assignment.");
				fsminstance.setRelatedObject(new FSMRelatedCalculation(as));
				list.add(fsminstance);
					
			}

			return list;
	}
}
