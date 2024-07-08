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
 * CSAStateMachine
 *  条件语句中包含赋值(CSA) 
 * 说明：条件表达式内不应使用=、+=、-=、*=、/=、%=、>>=、<<=、&=、|=、^=、++、--等赋值操作符。
 * 示例：不应使用如下代码 if (x -= 5) { ... 
 * for (i=j=n; --i > 0; j--)
 *  { ..  
 * 而应该写为： x -= * 5; 
 * if (x) { ... 
 * for (i=j=n; i > 0; i--, j--) 
 * { ...
 * 
 * 
 * @author cjie
 * 
 */
public class CSAStateMachine extends AbstractStateMachine {
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline,
			int errorline) {
		Formatter f = new Formatter();
		if (softtest.config.java.Config.LANGUAGE == 0) {
			f.format("条件语句中包含赋值: 条件语句在%d 行有对变量进行赋值，违反了代码编程规范", errorline);
		} else {
			f.format("Conditional Statement including Assignment: Conditional statement includes assignment on line %d," +
					"that violates Code Conventions.",errorline);
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

	private static String XPATH = ".//IfStatement/Expression/RelationalExpression/PrimaryExpression/PrimaryPrefix/Expression[./AssignmentOperator|./PostfixExpression]|"
		+".//WhileStatement/Expression/RelationalExpression/PrimaryExpression/PrimaryPrefix/Expression[./AssignmentOperator|./PostfixExpression]";

	public static List<FSMMachineInstance> createCSAs(SimpleJavaNode node,
			FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result = null;

		result = node.findXpath(XPATH);
		for (Object o : result) {
			ASTExpression as = (ASTExpression) o;

			FSMMachineInstance fsminstance = fsm.creatInstance();
			fsminstance
					.setResultString("Conditional statement including assignment.");
			fsminstance.setRelatedObject(new FSMRelatedCalculation(as));
			list.add(fsminstance);

		}

		return list;
	}
}
