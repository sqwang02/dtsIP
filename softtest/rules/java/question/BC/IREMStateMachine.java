package softtest.rules.java.question.BC;

import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;

public class IREMStateMachine extends AbstractStateMachine {
	/**
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("��equals()��������д����: ��equals()��������дʱ��ͨ���б�Ҫ��д hashCode ��������ά�� hashCode �����ĳ���Э����");
		}else{
			f.format("Incorrect ReWriting equals Method: equals() has been rewriten on line %d,but hashCode() has not been rewriten,that may cause Bad Code.",
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

	private static String XPATH1 = ".//ClassOrInterfaceDeclaration[./ClassOrInterfaceBody/ClassOrInterfaceBodyDeclaration/MethodDeclaration/MethodDeclarator[@Image='equals']]";

	public static List<FSMMachineInstance> createIREMs(SimpleJavaNode node,
			FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result = null;

		result = node.findXpath(XPATH1);
		for (Object o : result) {
			ASTClassOrInterfaceDeclaration cls = (ASTClassOrInterfaceDeclaration) o;
			String XPATH2 = ".//ClassOrInterfaceBodyDeclaration/MethodDeclaration/MethodDeclarator[@Image='hashCode']";

			List result2 = null;
			result2 = cls.findXpath(XPATH2);

			if (result2.size() < 1) {
				FSMMachineInstance fsminstance = fsm.creatInstance();
				fsminstance
						.setResultString("Incorrect ReWriting equals Method and hashCode Method Should be Rewriten Together.");
				fsminstance.setRelatedObject(new FSMRelatedCalculation(cls));
				list.add(fsminstance);

			}

		}

		return list;
	}
}
