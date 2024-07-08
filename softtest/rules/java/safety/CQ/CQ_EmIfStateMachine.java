package softtest.rules.java.safety.CQ;
import java.util.Formatter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import softtest.ast.java.ASTStatement;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;
import softtest.rules.java.AbstractStateMachine;
public class CQ_EmIfStateMachine extends AbstractStateMachine{
	/**
	 * 设置故障描述
	 * 检测空的synchronized块
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("代码质量CQ: %d 行空的if块，可能造成一个漏洞。这可能是人为的疏漏。", errorline);
		}else{
			f.format("Code Quality: program uses empty if block on line %d.It may be caused by people's oversight. ",errorline);
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
	public static List<FSMMachineInstance> createEmIfStateMachines(SimpleJavaNode node, FSMMachine fsm)
	{
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		String xpath = "";
		List evalRlts = null;
		List evalRlts1 = null;
		Iterator i = null;
		Iterator j = null;
		xpath = ".//IfStatement/Statement[count(Block/BlockStatement) = 0]";
		evalRlts = node.findXpath(xpath);
		i = evalRlts.iterator();	
		if(i.hasNext()){
			xpath=".//IfStatement[count(Statement)=0]";
			evalRlts1=node.findXpath(xpath);
			j = evalRlts1.iterator();	
			while(j.hasNext())
			{
				ASTStatement name = (ASTStatement) j.next();
				FSMMachineInstance fsmInst = fsm.creatInstance();
				fsmInst.setRelatedObject(new FSMRelatedCalculation(name));
				fsmInst.setResultString("Program uses empty If block");
				list.add(fsmInst);
			}
		}	
		return list;
	}

}
