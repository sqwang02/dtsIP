package softtest.rules.java.rule;

import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;

import softtest.ast.java.ASTTypeDeclaration;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;
import softtest.rules.java.AbstractStateMachine;

/**
 * �ļ�����̫����TMRF��
 * Too many rows in the file 
 * ˵����Դ�ļ���Ӧ����һ����������һ�������������Ϊ1000.
 * 
 * @author cjie
 * 
 */
public class TMRFStateMachine extends AbstractStateMachine {
	/**
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline,
			int errorline) {
		Formatter f = new Formatter();
		if (softtest.config.java.Config.LANGUAGE == 0) {
			f.format("�ļ�����̫��: ���ļ��Ĵ�����������1000��Υ���˴����̹淶", errorline);
		} else {
			f.format("Too Many Rows in the File: this file has more than 1000 rows,that violates Code Conventions.",
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

	private static String XPATH = ".//TypeDeclaration";

	public static List<FSMMachineInstance> createTMRFs(SimpleJavaNode node,
			FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result = null;

		result = node.findXpath(XPATH);
		for (Object o : result) {
			ASTTypeDeclaration as = (ASTTypeDeclaration) o;
			if (as.getEndLine() > 2000) {

				FSMMachineInstance fsminstance = fsm.creatInstance();
				fsminstance.setResultString("Too many rows in the file.");
				fsminstance.setRelatedObject(new FSMRelatedCalculation(as));
				list.add(fsminstance);
			}

		}

		return list;
	}
}
