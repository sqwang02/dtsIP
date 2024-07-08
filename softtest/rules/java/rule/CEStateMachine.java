package softtest.rules.java.rule;

import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;

import softtest.ast.java.ASTExpression;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;
import softtest.rules.java.AbstractStateMachine;

/**
 * CEStateMachine
 * 复杂的表达式(CE) 
 * 说明：表达式的复杂性根据相关的语法树及其结点数计算，缺省情况下不超过10。
 * 
 * @author cjie
 * 
 */
public class CEStateMachine extends AbstractStateMachine {
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline,
			int errorline) {
		Formatter f = new Formatter();
		if (softtest.config.java.Config.LANGUAGE == 0) {
			f.format("复杂的表达式: 表达式的复杂性根据相关的语法树及其结点数计算，缺省情况下不超过10.");
		} else {
			f.format("The Complicated Expresss: The complexity of the expression is calculated according to the relevant syntax tree and nodes, " +
					"not excceed 10 by default.");
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
     * 匹配复杂的表达式
     */
	private final static String XPATH = ".//Expression[count(.//Name) >5]";
	
	/**
	 * 创建状态机
	 * @param node
	 * @param fsm
	 * @return
	 */
	public static List<FSMMachineInstance> createCEStateMachine(SimpleJavaNode node,
			FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result = null;

		result = node.findXpath(XPATH);
		for (Object o : result) {
			ASTExpression as = (ASTExpression) o;

			FSMMachineInstance fsminstance = fsm.creatInstance();
			fsminstance
					.setResultString("The complicated expresss.");
			fsminstance.setRelatedObject(new FSMRelatedCalculation(as));
			list.add(fsminstance);

		}

		return list;
	}
}
