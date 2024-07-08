package softtest.rules.java.question.PFMC;

import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;

public class IGIRStateMachine extends
		AbstractStateMachine {
	
	/**
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("��Ч�����������: ����Ϊ�˲���һ�����������ȴ������nextDouble()������Ȼ��ǿ��ת��Ϊ���Σ�����Ч�ʽϵͣ�������ֱ�ӵ���nextInt������");
		}else{
			f.format("Inefficient Generating Integer Random: the code has generated integer random inefficiently on line %d,that belongs to Low Performance Code.",
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

	private static String XPATH1 = ".//Expression/CastExpression[./Type[./PrimitiveType[@Image=\'int\']and following-sibling::PrimaryExpression[1]/PrimaryPrefix/Expression[.//PrimaryExpression/PrimaryPrefix/Name[matches(@Image,\'^.+\\.nextDouble$\']]]";

	public static List<FSMMachineInstance> createIGIRs(
			SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result = null;

		result = node.findXpath(XPATH1);
		for (Object o : result) {
			ASTCastExpression castexp = (ASTCastExpression) o;
			FSMMachineInstance fsminstance = fsm.creatInstance();
			fsminstance
					.setResultString("Inefficient Generating Integer Random.");
			fsminstance.setRelatedObject(new FSMRelatedCalculation(castexp));
			list.add(fsminstance);
		}

		return list;
	}
}
