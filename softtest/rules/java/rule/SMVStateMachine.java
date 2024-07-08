package softtest.rules.java.rule;

import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;

import softtest.ast.java.ASTFieldDeclaration;
import softtest.ast.java.ASTLocalVariableDeclaration;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;
import softtest.rules.java.AbstractStateMachine;

/**
 * SMVStateMachine
 * 每个声明含多个变量(SMV) 
 * 说明：变量声明遵循如下格式：type variable_name。
 * 示例：
 * //  满足规则的声明
 * int width;
 * int length;
 * // 不满足规则的声明
 * int width, length;

 * @author cjie
 * 
 */
public class SMVStateMachine extends AbstractStateMachine {
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline,
			int errorline) {
		Formatter f = new Formatter();
		if (softtest.config.java.Config.LANGUAGE == 0) {
			f.format("每个声明含有多个变量: 在%d 行的声明含有多个变量", errorline);
		} else {
			f.format("The Statement have Multiple Variable: The declaration have multiple variable on line %d", errorline);
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
	private final static String XPATH = ".//LocalVariableDeclaration[count(VariableDeclarator)>1]";
    
	private final static String XPATH2 = ".//FieldDeclaration[count(VariableDeclarator)>1]";
	/**
	 * 创建状态机
	 * @param node
	 * @param fsm
	 * @return
	 */
	public static List<FSMMachineInstance> createSMVStateMachine(SimpleJavaNode node,
			FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result = null;

		result = node.findXpath(XPATH);
		for (Object o : result) {
			ASTLocalVariableDeclaration as = (ASTLocalVariableDeclaration) o;

			FSMMachineInstance fsminstance = fsm.creatInstance();
			fsminstance
					.setResultString("The declaration have multiple variable.");
			fsminstance.setRelatedObject(new FSMRelatedCalculation(as));
			list.add(fsminstance);

		}
		result = node.findXpath(XPATH2);
		for (Object o : result) {
			ASTFieldDeclaration as = (ASTFieldDeclaration) o;

			FSMMachineInstance fsminstance = fsm.creatInstance();
			fsminstance
					.setResultString("The declaration have multiple variable.");
			fsminstance.setRelatedObject(new FSMRelatedCalculation(as));
			list.add(fsminstance);

		}
		return list;
	}
}
