package softtest.rules.java.question.PFMC;

import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;

public class ExGCStateMachine extends AbstractStateMachine {
	/**
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("��ʽ��������: ��Java�У����������Ǻܺķ���Դ�ģ���ʾ�ص����������ջ��ƻᵼ��Ӧ�õ����ܼ����½����������Դ˲��������ã�");
		}else{
			f.format("Explicit Garbage Collection: the code has collected garbage on line %d,that belongs to Low Performance Code.",
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

	private static String XPATH1 = ".//StatementExpression/PrimaryExpression[./PrimaryPrefix/Name[@Image='System.gc']]";

	public static List<FSMMachineInstance> createExGCs(SimpleJavaNode node,
			FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result = null;

		result = node.findXpath(XPATH1);
		for (Object o : result) {
			ASTPrimaryExpression prim = (ASTPrimaryExpression) o;
			FSMMachineInstance fsminstance = fsm.creatInstance();
			fsminstance
					.setResultString("Explicit Garbage Collection.");
			fsminstance.setRelatedObject(new FSMRelatedCalculation(prim));
			list.add(fsminstance);
		}

		return list;
	}
}
