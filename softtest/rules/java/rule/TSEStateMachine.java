package softtest.rules.java.rule;

import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;

import softtest.rules.java.AbstractStateMachine;
/**
 * TSEStateMachine
 * 检查then为空
 * 描述:if语句的then部分不能为空。
 * @author cjie
 * 
 */

public class TSEStateMachine extends AbstractStateMachine{
	
	
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("then为空: if语句的then部分不能为空。");
		}else{
			f.format("Then Statement is Empty.", beginline);
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
	/**匹配IF语句空分支*/
	private static String XPATH1=".//IfStatement/Statement[EmptyStatement or Block[count(*) = 0]]";
		
	public static List<FSMMachineInstance> createTSEStateMachine(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result=null;
		
		result=node.findXpath(XPATH1);
		for(Object o:result){
			ASTStatement st=(ASTStatement)o;
			FSMMachineInstance fsminstance = fsm.creatInstance();
			fsminstance.setResultString("empty if statement.");
			fsminstance.setRelatedObject(new FSMRelatedCalculation(st));
			list.add(fsminstance);
		}
		return list;
	}
}
