package softtest.rules.java.question.PFMC;

import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;

public class NECFStateMachine extends AbstractStateMachine {
	/**
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("��ʽ����finalize ()����: ��Ȼ��ʽ�ĵ��������������ȷ�����ã����ǵ���������ռ����Ժ������ռ������ռ�һ�Ρ�Ϊ���Ч�ʣ���������Ҫ���д˲�����");
		}else{
			f.format("Not Explicitly Call Finalize(): the code has not called finalize() explicitly on line %d,that belongs to Low Performance Code.",
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

	private static String XPATH1 = ".//StatementExpression/PrimaryExpression[./PrimaryPrefix/Name[matches(@Image,'^.+\\.finalize$')]]";

	public static List<FSMMachineInstance> createNECFinalizes(SimpleJavaNode node,
			FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result = null;

		result = node.findXpath(XPATH1);
		for (Object o : result) {
			ASTPrimaryExpression prim = (ASTPrimaryExpression) o;
			FSMMachineInstance fsminstance = fsm.creatInstance();
			fsminstance
					.setResultString("Not Explicitly Call Finalize().");
			fsminstance.setRelatedObject(new FSMRelatedCalculation(prim));
			list.add(fsminstance);
		}

		return list;
	}
}
