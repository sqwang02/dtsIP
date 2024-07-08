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
 * CSAStateMachine
 *  ��������а�����ֵ(CSA) 
 * ˵�����������ʽ�ڲ�Ӧʹ��=��+=��-=��*=��/=��%=��>>=��<<=��&=��|=��^=��++��--�ȸ�ֵ��������
 * ʾ������Ӧʹ�����´��� if (x -= 5) { ... 
 * for (i=j=n; --i > 0; j--)
 *  { ..  
 * ��Ӧ��дΪ�� x -= * 5; 
 * if (x) { ... 
 * for (i=j=n; i > 0; i--, j--) 
 * { ...
 * 
 * 
 * @author cjie
 * 
 */
public class CSAStateMachine extends AbstractStateMachine {
	/**
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline,
			int errorline) {
		Formatter f = new Formatter();
		if (softtest.config.java.Config.LANGUAGE == 0) {
			f.format("��������а�����ֵ: ���������%d ���жԱ������и�ֵ��Υ���˴����̹淶", errorline);
		} else {
			f.format("Conditional Statement including Assignment: Conditional statement includes assignment on line %d," +
					"that violates Code Conventions.",errorline);
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

	private static String XPATH = ".//IfStatement/Expression/RelationalExpression/PrimaryExpression/PrimaryPrefix/Expression[./AssignmentOperator|./PostfixExpression]|"
		+".//WhileStatement/Expression/RelationalExpression/PrimaryExpression/PrimaryPrefix/Expression[./AssignmentOperator|./PostfixExpression]";

	public static List<FSMMachineInstance> createCSAs(SimpleJavaNode node,
			FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result = null;

		result = node.findXpath(XPATH);
		for (Object o : result) {
			ASTExpression as = (ASTExpression) o;

			FSMMachineInstance fsminstance = fsm.creatInstance();
			fsminstance
					.setResultString("Conditional statement including assignment.");
			fsminstance.setRelatedObject(new FSMRelatedCalculation(as));
			list.add(fsminstance);

		}

		return list;
	}
}
