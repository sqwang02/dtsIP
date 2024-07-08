package softtest.rules.java.rule;

import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;

import softtest.ast.java.ASTImportDeclaration;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;
import softtest.rules.java.AbstractStateMachine;

/**
 * IIStateMachine
 * 隐式导入(II) 
 * 说明：总是使用显式引入，如import.io.basic，而不要使用一般引入，如java.io.*.
 * 
 * 
 * @author cjie
 * 
 */
public class IIStateMachine extends AbstractStateMachine {
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline,
			int errorline) {
		Formatter f = new Formatter();
		if (softtest.config.java.Config.LANGUAGE == 0) {
			f.format("隐式导入：总是使用显式引入，如import.io.basic，而不要使用一般引入，如java.io.*.");
		} else {
			f.format("Implicit Import: Used implicit important on line %d", errorline);
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
     * 匹配隐式导入xpath
     */
	private final static String XPATH = ".//ImportDeclaration[@ImportOnDemand='true']";
	
	/**
	 * 创建状态机
	 * @param node
	 * @param fsm
	 * @return
	 */
	public static List<FSMMachineInstance> createIIStateMachine(SimpleJavaNode node,
			FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result = null;

		result = node.findXpath(XPATH);
		for (Object o : result) {
			ASTImportDeclaration as = (ASTImportDeclaration) o;

			FSMMachineInstance fsminstance = fsm.creatInstance();
			fsminstance
					.setResultString("Implicit import.");
			fsminstance.setRelatedObject(new FSMRelatedCalculation(as));
			list.add(fsminstance);

		}

		return list;
	}
}
