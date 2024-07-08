package softtest.rules.java.rule;

import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;

import softtest.ast.java.ASTBlockStatement;
import softtest.ast.java.ASTWhileStatement;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;
import softtest.rules.java.AbstractStateMachine;

/**
 * LMSStateMachine
 * ÿ�к��������(LMS) 
 * ˵����ÿ�д��벻Ӧ���ж�����䣬�����ӿɶ��ԡ�
 * 
 * @author cjie
 * 
 */
public class LMSStateMachine extends AbstractStateMachine {
	/**
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline,
			int errorline) {
		Formatter f = new Formatter();
		if (softtest.config.java.Config.LANGUAGE == 0) {
			f.format("ÿ�к��������: ÿ�д��벻Ӧ���ж�����䣬�����ӿɶ���.");
		} else {
			f.format("The Line contains Multiple Statements: Every line of code should not contain multiple statements, " +
					"in order to increase readability.");
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
    /**
     * ƥ��һ�к��ж������
     */
	private final static String XPATH = ".//BlockStatement[@BeginLine = following-sibling::BlockStatement/@BeginLine]";
    /**
     * ƥ��һ�к��ж������
     */
	private final static String XPATH2 = ".//WhileStatement[@BeginLine = .//Statement/@BeginLine]";
	/**
	 * ����״̬��
	 * @param node
	 * @param fsm
	 * @return
	 */
	public static List<FSMMachineInstance> createLMSStateMachine(SimpleJavaNode node,
			FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result = null;

		result = node.findXpath(XPATH);
		for (Object o : result) {
			ASTBlockStatement as = (ASTBlockStatement) o;

			FSMMachineInstance fsminstance = fsm.creatInstance();
			fsminstance
					.setResultString("The line contains multiple statement.");
			fsminstance.setRelatedObject(new FSMRelatedCalculation(as));
			list.add(fsminstance);

		}
		result = node.findXpath(XPATH2);
		for (Object o : result) {
			ASTWhileStatement as = (ASTWhileStatement) o;

			FSMMachineInstance fsminstance = fsm.creatInstance();
			fsminstance
					.setResultString("The line contains multiple statement.");
			fsminstance.setRelatedObject(new FSMRelatedCalculation(as));
			list.add(fsminstance);

		}
		return list;
	}
}
