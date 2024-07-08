package softtest.rules.java.rule;

import java.util.*;
import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;



/**
 * SNDSStateMachine
 * 检查switch语句无default缺陷
 * 描述：每条switch语句中应该含有default分支，而且必须在最后。
 * 
 * @author cjie
 * 
 */
public class SNDSStateMachine extends AbstractStateMachine{
	
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("Switch语句无default或没有位于最后,第%d行.", errorline);
		}else{
			f.format("Switch has No Default Statement: Switch statement has not default statement or default is not in the last at Line %d .",
					errorline);
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
	
	
	/**匹配无default语句的switch*/
	private static final String SWITCH_NO_DEFAULT=".//SwitchStatement[not(SwitchLabel[@Default='true'])]";
	
	/**匹配default语句不是最后的switch*/
	private static final String SWITCH_DEFAULT_NOLAST=
		"	.//SwitchStatement[SwitchLabel[@Default='true' and following-sibling::SwitchLabel[@Default!='true']]";


	
	/**
	 * 
	 *
	 * @param 
	 * @return List<FSMMachineInstance> 状态机实例列表
	 * @throws
	 */
	
	public static List<FSMMachineInstance> createSNDSStateMachine(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result=null;	
		/**无default语句的switch*/
		result=node.findXpath(SWITCH_NO_DEFAULT);
		for(Object o:result){
			ASTSwitchStatement st=(ASTSwitchStatement)o;
			
			FSMMachineInstance fsminstance = fsm.creatInstance();
	        fsminstance.setResultString("Swtch statement has not default.");
		    fsminstance.setRelatedObject(new FSMRelatedCalculation(st));
		    list.add(fsminstance);
  
		}

		/**default语句不是最后的switch*/
		result=node.findXpath(SWITCH_DEFAULT_NOLAST);
		for(Object o:result){
			ASTSwitchStatement st=(ASTSwitchStatement)o;
			
			FSMMachineInstance fsminstance = fsm.creatInstance();
	        fsminstance.setResultString("Default is not in the last.");
		    fsminstance.setRelatedObject(new FSMRelatedCalculation(st));
		    list.add(fsminstance);
  
		}
		return list;
	}

}
