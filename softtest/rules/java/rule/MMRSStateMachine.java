package softtest.rules.java.rule;

import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;

import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;
import softtest.rules.java.AbstractStateMachine;

/**
 * MMRSStateMachine
 * 每个函数含有多条条return指令(MMRS) 
 * 说明：在每个函数中只能有一条return指令，以增加程序的可维护性。
 * 举例：  
 * public int f(int i) {
 *     if(i==0)
 *        return 10;
 *     else 
 *        return 100;
 * }
 * 
 * 
 * @author cjie
 * 
 */
public class MMRSStateMachine extends AbstractStateMachine {
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline,
			int errorline) {
		Formatter f = new Formatter();
		if (softtest.config.java.Config.LANGUAGE == 0) {
			f.format("函数含有多条return语句: 在每个函数中只能有一条return指令，以增加程序的可维护性");
		} else {
			f.format("Mutliple Return Statement: Used mutliple return statement on line %d", errorline);
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
     * 匹配多return语句函数
     */
	private final static String XPATH = ".//MethodDeclaration[count(.//ReturnStatement)>1]";
	
	/**
	 * 创建状态机
	 * @param node
	 * @param fsm
	 * @return
	 */
	public static List<FSMMachineInstance> createMMRSStateMachine(SimpleJavaNode node,
			FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result = null;

		result = node.findXpath(XPATH);
		for (Object o : result) {
			ASTMethodDeclaration as = (ASTMethodDeclaration) o;

			FSMMachineInstance fsminstance = fsm.creatInstance();
			fsminstance
					.setResultString("mutliple return statement.");
			fsminstance.setRelatedObject(new FSMRelatedCalculation(as));
			list.add(fsminstance);

		}

		return list;
	}
}
