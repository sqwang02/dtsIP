package softtest.rules.java.question.BC;

import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;

public class INOStateMachine extends AbstractStateMachine {
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline,
			int errorline) {
		Formatter f = new Formatter();
		if (softtest.config.java.Config.LANGUAGE == 0) {
			f.format("忽略新建对象: 执行语句调用了一个构造函数的方法，又忽略了此方法的返回值，这样这个创建的对象就又被回收，这样的代码是无意义的，也许是失误错误，提醒您对此进行检查确认。");
		} else {
			f.format("Ignoring the New Object: the new object is not used on line %d,that may cause Bad Code.",
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

	private static String XPATH1 = ".//PrimaryExpression[./PrimaryPrefix/AllocationExpression]";

	public static List<FSMMachineInstance> createINOs(SimpleJavaNode node,
			FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result = null;

		result = node.findXpath(XPATH1);
		for (Object o : result) {
			ASTPrimaryExpression prim = (ASTPrimaryExpression) o;

			int allocNum = 0;
			Node node1 = prim.jjtGetParent();
			String XPATH2 = ".//PrimarySuffix";
			List list1 = prim.findXpath(XPATH2);
			String XPATH3 = ".//ClassOrInterfaceBody";
			if (prim.jjtGetChild(0).jjtGetChild(0) instanceof ASTAllocationExpression) {
				ASTAllocationExpression alloc = (ASTAllocationExpression) prim
						.jjtGetChild(0).jjtGetChild(0);
				allocNum = alloc.findXpath(XPATH3).size();
			}
			if (node1 instanceof ASTStatementExpression && list1.size() == 0
					&& allocNum == 0) {
				FSMMachineInstance fsminstance = fsm.creatInstance();
				fsminstance.setResultString("Ignoring the New Object .");
				fsminstance.setRelatedObject(new FSMRelatedCalculation(prim));
				list.add(fsminstance);
			}
		}
		return list;
	}
}
