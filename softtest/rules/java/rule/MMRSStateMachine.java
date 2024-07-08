package softtest.rules.java.rule;

import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;

import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;
import softtest.rules.java.AbstractStateMachine;

/**
 * MMRSStateMachine
 * ÿ���������ж�����returnָ��(MMRS) 
 * ˵������ÿ��������ֻ����һ��returnָ������ӳ���Ŀ�ά���ԡ�
 * ������  
 * public int f(int i) {
 *     if(i==0)
 *        return 10;
 *     else 
 *        return 100;
 * }
 * 
 * 
 * @author cjie
 * 
 */
public class MMRSStateMachine extends AbstractStateMachine {
	/**
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline,
			int errorline) {
		Formatter f = new Formatter();
		if (softtest.config.java.Config.LANGUAGE == 0) {
			f.format("�������ж���return���: ��ÿ��������ֻ����һ��returnָ������ӳ���Ŀ�ά����");
		} else {
			f.format("Mutliple Return Statement: Used mutliple return statement on line %d", errorline);
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
	private final static String XPATH = ".//MethodDeclaration[count(.//ReturnStatement)>1]";
	
	/**
	 * ����״̬��
	 * @param node
	 * @param fsm
	 * @return
	 */
	public static List<FSMMachineInstance> createMMRSStateMachine(SimpleJavaNode node,
			FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result = null;

		result = node.findXpath(XPATH);
		for (Object o : result) {
			ASTMethodDeclaration as = (ASTMethodDeclaration) o;

			FSMMachineInstance fsminstance = fsm.creatInstance();
			fsminstance
					.setResultString("mutliple return statement.");
			fsminstance.setRelatedObject(new FSMRelatedCalculation(as));
			list.add(fsminstance);

		}

		return list;
	}
}
