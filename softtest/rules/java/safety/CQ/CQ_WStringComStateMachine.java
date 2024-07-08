package softtest.rules.java.safety.CQ;
import java.util.Formatter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import softtest.ast.java.ASTPrimaryExpression;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;
import softtest.rules.java.AbstractStateMachine;
public class CQ_WStringComStateMachine extends AbstractStateMachine{
	/**
	 * 设置故障描述
	 * 字符串比较
	 * 字符串使用“==”进行比较，比较的为字符串的引用
	 * 建议使用equals来进行比较
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("代码质量CQ: %d 行程序采用 == 或 != 来比较字符串是否相等，可能造成一个漏洞，建议用equals比较", errorline);
		}else{
			f.format("Code Quality: program uses == or != to compara two strings on line %d",errorline);
		}
		fsmmi.setDescription(f.toString());
		f.close();
	}
	@Override
	public  void registerPrecondition(PreconditionListenerSet listeners) {
	}
	
	@Override
	public  void registerFeature(FeatureListenerSet listenerSet) {
	}
	public static List<FSMMachineInstance> createWStringComStateMachines(SimpleJavaNode node, FSMMachine fsm)
	{
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		String xpath = "";
		List evalRlts = null;
		Iterator i = null;
		xpath = ".//Expression/EqualityExpression[PrimaryExpression[1][matches(@TypeString,\"java.lang.String\")]]/PrimaryExpression[2][matches(@TypeString,\"java.lang.String\")]";
		evalRlts = node.findXpath(xpath);
		i = evalRlts.iterator();
		
		while(i.hasNext())
		{
			ASTPrimaryExpression name = (ASTPrimaryExpression) i.next();
			FSMMachineInstance fsmInst = fsm.creatInstance();
			fsmInst.setRelatedObject(new FSMRelatedCalculation(name));
			fsmInst.setResultString("Program uses == or != to compara two strings");
			list.add(fsmInst);
		}	
		return list;
	}
}
