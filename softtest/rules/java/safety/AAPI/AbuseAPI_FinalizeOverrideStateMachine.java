package softtest.rules.java.safety.AAPI;

import java.util.Formatter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.ASTPrimaryPrefix;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;
import softtest.rules.java.AbstractStateMachine;

public class AbuseAPI_FinalizeOverrideStateMachine extends AbstractStateMachine{
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("滥用API模式: %d 行实现了finalize方法却没有调用super.finalize，可能造成一个漏洞。", errorline);
		}else{
			f.format("Abuse Application Program Interface: no super.finalize when implementing finalize on line %d. ",errorline);
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
	
	public static List<FSMMachineInstance> createFinalizeOverrideStateMachines(SimpleJavaNode node, FSMMachine fsm)
	{	
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		if(node instanceof ASTMethodDeclaration){
			if(((ASTMethodDeclaration) node).getMethodName().equals("finalize")){
				String xPath=".//PrimaryPrefix[@SuperModifier='true'][@Label='super.finalize']";
				List<ASTPrimaryPrefix> evalRlt=node.findXpath(xPath);
				if(evalRlt.size()==0){
					FSMMachineInstance fsmInst = fsm.creatInstance();
					fsmInst.setRelatedObject(new FSMRelatedCalculation(node));
					fsmInst.setResultString("no super.finalize()");
					list.add(fsmInst);
				}
			}
		}
		return list;
	}
}
