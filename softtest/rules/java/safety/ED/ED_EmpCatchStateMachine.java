package softtest.rules.java.safety.ED;
//安全缺陷.错误处理问题
import java.util.Formatter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import softtest.ast.java.ASTBlock;
import softtest.ast.java.ASTClassOrInterfaceType;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;
import softtest.rules.java.AbstractStateMachine;
public class ED_EmpCatchStateMachine extends AbstractStateMachine{
	
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("错误处理:Catch 块为空在 %d 行，可能造成一个漏洞", errorline);
		}else{
			f.format("Empty Catch Exception:Declaration of Empty Catch on line %d",errorline);
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
	public static List<FSMMachineInstance> createEmpCatchStateMachines(SimpleJavaNode node, FSMMachine fsm)
	{
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		String xpath = "";
		List evalRlts = null;
		Iterator i = null;
		xpath = ".//CatchStatement/Block";
		evalRlts = node.findXpath(xpath);
		i = evalRlts.iterator();		
		while(i.hasNext())
		{
			ASTBlock name = (ASTBlock) i.next();
			if(name.jjtGetNumChildren()==0){
				FSMMachineInstance fsmInst = fsm.creatInstance();
				fsmInst.setRelatedObject(new FSMRelatedCalculation(name));
				fsmInst.setResultString("Empty Catch Exception ");
				list.add(fsmInst);
			}
		}	
		
		return list;
		
	}
}
