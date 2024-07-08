package softtest.rules.java.rule;

import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;

import softtest.ast.java.ASTFieldDeclaration;
import softtest.ast.java.ASTVariableDeclarator;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;
import softtest.rules.java.AbstractStateMachine;

/**
 * VDULStateMachine
 * ��������ʱδ��ʼ��(VDUL) 
 * ˵��������Ӧ����������ʱ��ʼ����ȷ����ʹ��֮ǰ�����˳�ʼ����
 * 
 * @author cjie
 * 
 */
public class VDULStateMachine extends AbstractStateMachine {
	/**
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline,
			int errorline) {
		Formatter f = new Formatter();
		if (softtest.config.java.Config.LANGUAGE == 0) {
			f.format("��������ʱδ��ʼ��: ����Ӧ����������ʱ��ʼ����ȷ����ʹ��֮ǰ�����˳�ʼ��", errorline);
		} else {
			f.format("The Variable in Declaration is Uninitialized : The variavle is not initalized in delcaration.",
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
    /**
     * ƥ��XPath
     */
	private final static String XPATH = ".//LocalVariableDeclaration/VariableDeclarator[count(VariableInitializer)=0]";
	private final static String XPATH2 = ".//FieldDeclaration/VariableDeclarator[count(VariableInitializer)=0]";
	
	/**
	 * ����״̬��
	 * @param node
	 * @param fsm
	 * @return
	 */
	public static List<FSMMachineInstance> createVDULStateMachine(SimpleJavaNode node,
			FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result = null;

		result = node.findXpath(XPATH);
		for (Object o : result) {
			ASTVariableDeclarator as = (ASTVariableDeclarator) o;

			FSMMachineInstance fsminstance = fsm.creatInstance();
			fsminstance
					.setResultString("The variavle is not iniitalize when delcaration.");
			fsminstance.setRelatedObject(new FSMRelatedCalculation(as));
			list.add(fsminstance);

		}
		result = node.findXpath(XPATH2);
		for (Object o : result) {
			ASTVariableDeclarator as = (ASTVariableDeclarator) o;

			FSMMachineInstance fsminstance = fsm.creatInstance();
			fsminstance
					.setResultString("The variavle is not iniitalize when delcaration.");
			fsminstance.setRelatedObject(new FSMRelatedCalculation(as));
			list.add(fsminstance);

		}
		return list;
	}
}
