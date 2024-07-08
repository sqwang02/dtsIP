package softtest.rules.java.question.PFMC;

import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;

public class PCMMStateMachine extends AbstractStateMachine {
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline,
			int errorline) {
		Formatter f = new Formatter();
		if (softtest.config.java.Config.LANGUAGE == 0) {
			f.format("参数为常数的数学方法: 当调用Math类的静态方法时，如果其参数是常量，则其值编译时就可计算出，因此为了提高性能应直接使用运算结果。");
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
