package softtest.rules.java.rule;

import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;

import softtest.ast.java.ASTBlock;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;
import softtest.rules.java.AbstractStateMachine;

/**
 * IMDOStateMachine
 * ����ȷ��ģ����������(IMDO) 
 * ˵����ģ���е�����Ӧ����ģ��ǰ���������ӿɶ��ԡ�
 * 
 * @author cjie
 * 
 */
public class IMDOStateMachine extends AbstractStateMachine {
	/**
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline,
			int errorline) {
		Formatter f = new Formatter();
		if (softtest.config.java.Config.LANGUAGE == 0) {
			f.format("����ȷ��ģ����������: ģ���е�����Ӧ����ģ��ǰ���������ӿɶ���.");
		} else {
			f.format("Incorrect Module Declaration Order: Declaration in the module should be placed on the front, " +
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
     * ����ǰ�������ĺ���
     */
	private final static String XPATH = ".//MethodDeclaration/Block[BlockStatement[LocalVariableDeclaration and preceding::BlockStatement/Statement]]";
	
	/**
	 * ����״̬��
	 * @param node
	 * @param fsm
	 * @return
	 */
	public static List<FSMMachineInstance> createIMDOStateMachine(SimpleJavaNode node,
			FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result = null;

		result = node.findXpath(XPATH);
		for (Object o : result) {
			ASTBlock as = (ASTBlock) o;

			FSMMachineInstance fsminstance = fsm.creatInstance();
			fsminstance
					.setResultString("Incorrect statement declaration sequence.");
			fsminstance.setRelatedObject(new FSMRelatedCalculation(as));
			list.add(fsminstance);

		}

		return list;
	}
}
