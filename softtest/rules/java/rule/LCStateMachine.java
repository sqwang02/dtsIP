package softtest.rules.java.rule;

import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;

import softtest.ast.java.ASTLiteral;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;
import softtest.rules.java.AbstractStateMachine;

/**
 * LCStateMachine
 * 文字常量(LC) 
 * 说明：程序中使用的数字和字符串必须声明为常量，而不应直接使用。允许使用的文字常量为0、1、’\0’和字符串文字。
 * 
 * @author cjie
 * 
 */
public class LCStateMachine extends AbstractStateMachine {
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline,
			int errorline) {
		Formatter f = new Formatter();
		if (softtest.config.java.Config.LANGUAGE == 0) {
			f.format("文字常量: 程序中使用的数字和字符串必须声明为常量，而不应直接使用。允许使用的文字常量为0、1、'\\0'和字符串文字。", errorline);
		} else {
			f.format("Literal Constant on line %d: The number and string used in the program must be declared as constants, " +
					"rather than used directly. Allows the use of 0,1,'\\0' and string text.",
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
    /**
     * 匹配常量
     */
	private final static String XPATH = "//MethodDeclaration//Literal[@Image!=0 and @Image!=1]";
	
	/**
	 * 创建状态机
	 * @param node
	 * @param fsm
	 * @return
	 */
	public static List<FSMMachineInstance> createLCStateMachine(SimpleJavaNode node,
			FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result = null;

		result = node.findXpath(XPATH);
		for (Object o : result) {
			ASTLiteral as = (ASTLiteral) o;
            if (null != as.getImage() && !"'\\0'".equals(as.getImage())) {
            	FSMMachineInstance fsminstance = fsm.creatInstance();
    			fsminstance
    					.setResultString("Litral constant.");
    			fsminstance.setRelatedObject(new FSMRelatedCalculation(as));
    			list.add(fsminstance);

            }
		}

		return list;
	}
}
