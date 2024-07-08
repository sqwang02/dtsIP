package softtest.rules.java.rule;

import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;

import softtest.ast.java.ASTAssignmentOperator;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;
import softtest.rules.java.AbstractStateMachine;

/**
 * EIAStateMachine
 * 表达式内部赋值(EIA) 
 * 说明：避免使用容易引起误解的赋值方式。
 * 
 * @author cjie
 * 
 */
public class EIAStateMachine extends AbstractStateMachine {
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline,
			int errorline) {
		Formatter f = new Formatter();
		if (softtest.config.java.Config.LANGUAGE == 0) {
			f.format("表达式内部赋值: 避免使用容易引起误解的赋值方式");
		} else {
			f.format("The Expression Internal Assignment: Avoid using easily misunderstanded assignment method.");
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
     * 匹配表达式内部赋值
     */
	private final static String XPATH = ".//AssignmentOperator[preceding-sibling::PrimaryExpression/PrimaryPrefix/Name/@Image=following-sibling::Expression//Name/@Image]";
	
	/**
	 * 创建状态机
	 * @param node
	 * @param fsm
	 * @return
	 */
	public static List<FSMMachineInstance> createEIAStateMachine(SimpleJavaNode node,
			FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result = null;

		result = node.findXpath(XPATH);
		for (Object o : result) {
			ASTAssignmentOperator as = (ASTAssignmentOperator) o;

			FSMMachineInstance fsminstance = fsm.creatInstance();
			fsminstance
					.setResultString("The expression internal assignment.");
			fsminstance.setRelatedObject(new FSMRelatedCalculation(as));
			list.add(fsminstance);

		}

		return list;
	}
}
