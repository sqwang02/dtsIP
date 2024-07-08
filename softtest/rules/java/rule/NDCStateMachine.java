package softtest.rules.java.rule;

import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;

import softtest.ast.java.ASTClassOrInterfaceDeclaration;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;
import softtest.rules.java.AbstractStateMachine;

/**
 * NDCStateMachine 缺省构造函数(NDC)
 * 
 * No default constructor(NDC) 
 * 说明：每个类必须含有一个显式声明的构造函数。
 * 示例： class aClass { ...
 * aClass(); ... }
 * 
 * 
 * @author cjie
 * 
 */
public class NDCStateMachine extends AbstractStateMachine {
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline,
			int errorline) {
		Formatter f = new Formatter();
		if (softtest.config.java.Config.LANGUAGE == 0) {
			f.format("缺省构造函数: 该类缺省了不含参数的构造函数，违反了代码编程规范", errorline);
		} else {
			f.format("No Default Constructor: there is no default constructor in the class,that violates Code Conventions.",
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

	private static String XPATH = ".//ClassOrInterfaceDeclaration[@Interface='false' and @Abstract='false' and count(.//ConstructorDeclaration/FormalParameters[@ParameterCount='0']) = 0]";

	public static List<FSMMachineInstance> createNDCs(SimpleJavaNode node,
			FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result = null;

		result = node.findXpath(XPATH);
		for (Object o : result) {
			ASTClassOrInterfaceDeclaration decl=(ASTClassOrInterfaceDeclaration)o;
			FSMMachineInstance fsminstance = fsm.creatInstance();
			fsminstance
					.setResultString("No default constructor.");
			fsminstance.setRelatedObject(new FSMRelatedCalculation(decl));
			list.add(fsminstance);

		}
		return list;
	}
}
