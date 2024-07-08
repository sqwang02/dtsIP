package softtest.rules.java.question.BC;


import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;

public class PMSFCStateMachine extends
		AbstractStateMachine {
	/**
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("����Ϊfinal����������proteced����: ����Ϊfinal�����ǲ��ܱ��̳еģ�����ȴ������������protected�����Ի��ˣ���Ϊprotected����һ�����������������з���Ȩ�޵ġ���������Ҫ��д���������Ի��ԵĴ��룡");
		}else{
			f.format("Proteced Method Statement in the Final Class: the proteced has declared on line %d in final class,that may cause Bad Code.",
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

	private static String XPATH1 = ".//ClassOrInterfaceDeclaration[@Final='true']/ClassOrInterfaceBody/ClassOrInterfaceBodyDeclaration/MethodDeclaration[@Protected='true']";

	public static List<FSMMachineInstance> createPMSFCs(
			SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result = null;

		result = node.findXpath(XPATH1);
		for (Object o : result) {
			ASTMethodDeclaration met = (ASTMethodDeclaration) o;
			FSMMachineInstance fsminstance = fsm.creatInstance();
			fsminstance
					.setResultString("Proteced Method Statement in the Final Class.");
			fsminstance.setRelatedObject(new FSMRelatedCalculation(met));
			list.add(fsminstance);
		}

		return list;
	}
}
