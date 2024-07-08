package softtest.rules.java.safety.CQ;
import java.util.Formatter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import softtest.ast.java.ASTPrimarySuffix;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;
import softtest.rules.java.AbstractStateMachine;
public class CQ_ClsComByNameStateMachine extends AbstractStateMachine{
	/**
	 * 设置故障描述
	 * 通过名字来对类进行比较
	 * 对类进行比较的时候，同时判断名字是否相同，
	 * 应该检查类是否相同
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("代码质量CQ: %d 行通过名字对类进行比较，可能造成一个漏洞.通过名字的方式进行类比较，可能会使用错误的类。攻击者可以提供一个与TrustedClassName相同名字的类，对应用程序进行攻击。", errorline);
		}else{
			f.format("Code Quality: program compares class by name on line %d",errorline);
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
	public static List<FSMMachineInstance> createClsComByNameStateMachines(SimpleJavaNode node, FSMMachine fsm)
	{
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		String xpath = "";
		List evalRlts = null;
		Iterator i = null;
		xpath = ".//PrimaryExpression[PrimaryPrefix[matches(@TypeString,\"java.lang.Object.getClass\")]][PrimarySuffix[matches(@TypeString,\"java.lang.Class.getName\")]]/PrimarySuffix[matches(@TypeString,\"java.lang.String.equals\")]";
		evalRlts = node.findXpath(xpath);
		i = evalRlts.iterator();	
		while(i.hasNext())
		{
			ASTPrimarySuffix name = (ASTPrimarySuffix) i.next();
			FSMMachineInstance fsmInst = fsm.creatInstance();
			fsmInst.setRelatedObject(new FSMRelatedCalculation(name));
			fsmInst.setResultString("Program compares class by name");
			list.add(fsmInst);
		}	
		return list;
	}

}
