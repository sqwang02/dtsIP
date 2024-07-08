package softtest.rules.java.question.PFMC;

import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;

public class PCMMStateMachine extends AbstractStateMachine {
	/**
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline,
			int errorline) {
		Formatter f = new Formatter();
		if (softtest.config.java.Config.LANGUAGE == 0) {
			f.format("����Ϊ��������ѧ����: ������Math��ľ�̬����ʱ�����������ǳ���������ֵ����ʱ�Ϳɼ���������Ϊ���������Ӧֱ��ʹ����������");
		} else {
			f.format("Parameter Constant Mathematical Method: the code has called mathematical method whose parameter is constant on line %d,that belongs to Low Performance Code.",
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

	private static String XPATH1 = ".//PrimaryExpression/PrimarySuffix[preceding-sibling::PrimaryPrefix/Name[matches(@Image,'^Math..+$')]and ./Arguments/ArgumentList/Expression/PrimaryExpression/PrimaryPrefix/Literal]";

	public static List<FSMMachineInstance> createPCMMs(SimpleJavaNode node,
			FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result = null;

		result = node.findXpath(XPATH1);
		for (Object o : result) {
			ASTPrimarySuffix prim = (ASTPrimarySuffix) o;
			if (prim.jjtGetChild(0).jjtGetChild(0) instanceof ASTArgumentList) {
				ASTArgumentList arg = (ASTArgumentList) prim.jjtGetChild(0)
						.jjtGetChild(0);
				String XPATH2 = ".//Expression/PrimaryExpression/PrimaryPrefix/Literal";
				String XPATH3 = ".//Expression";
				List list1 = arg.findXpath(XPATH2);
				List list2 = arg.findXpath(XPATH3);
				if (list1.size() == list2.size()) {
					FSMMachineInstance fsminstance = fsm.creatInstance();
					fsminstance
							.setResultString("Parameter Constant Mathematical Method.");
					fsminstance
							.setRelatedObject(new FSMRelatedCalculation(prim));
					list.add(fsminstance);

				}

			}
			continue;
		}

		return list;
	}
}
