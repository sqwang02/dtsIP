package softtest.rules.java.rule;

import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;

import softtest.ast.java.ASTFieldDeclaration;
import softtest.ast.java.ASTLocalVariableDeclaration;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;
import softtest.rules.java.AbstractStateMachine;

/**
 * SMVStateMachine
 * ÿ���������������(SMV) 
 * ˵��������������ѭ���¸�ʽ��type variable_name��
 * ʾ����
 * //  ������������
 * int width;
 * int length;
 * // ��������������
 * int width, length;

 * @author cjie
 * 
 */
public class SMVStateMachine extends AbstractStateMachine {
	/**
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline,
			int errorline) {
		Formatter f = new Formatter();
		if (softtest.config.java.Config.LANGUAGE == 0) {
			f.format("ÿ���������ж������: ��%d �е��������ж������", errorline);
		} else {
			f.format("The Statement have Multiple Variable: The declaration have multiple variable on line %d", errorline);
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
     * ƥ���return��亯��
     */
	private final static String XPATH = ".//LocalVariableDeclaration[count(VariableDeclarator)>1]";
    
	private final static String XPATH2 = ".//FieldDeclaration[count(VariableDeclarator)>1]";
	/**
	 * ����״̬��
	 * @param node
	 * @param fsm
	 * @return
	 */
	public static List<FSMMachineInstance> createSMVStateMachine(SimpleJavaNode node,
			FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result = null;

		result = node.findXpath(XPATH);
		for (Object o : result) {
			ASTLocalVariableDeclaration as = (ASTLocalVariableDeclaration) o;

			FSMMachineInstance fsminstance = fsm.creatInstance();
			fsminstance
					.setResultString("The declaration have multiple variable.");
			fsminstance.setRelatedObject(new FSMRelatedCalculation(as));
			list.add(fsminstance);

		}
		result = node.findXpath(XPATH2);
		for (Object o : result) {
			ASTFieldDeclaration as = (ASTFieldDeclaration) o;

			FSMMachineInstance fsminstance = fsm.creatInstance();
			fsminstance
					.setResultString("The declaration have multiple variable.");
			fsminstance.setRelatedObject(new FSMRelatedCalculation(as));
			list.add(fsminstance);

		}
		return list;
	}
}
