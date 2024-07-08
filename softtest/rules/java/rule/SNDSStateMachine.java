package softtest.rules.java.rule;

import java.util.*;
import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;



/**
 * SNDSStateMachine
 * ���switch�����defaultȱ��
 * ������ÿ��switch�����Ӧ�ú���default��֧�����ұ��������
 * 
 * @author cjie
 * 
 */
public class SNDSStateMachine extends AbstractStateMachine{
	
	/**
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("Switch�����default��û��λ�����,��%d��.", errorline);
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
	
	
	/**ƥ����default����switch*/
	private static final String SWITCH_NO_DEFAULT=".//SwitchStatement[not(SwitchLabel[@Default='true'])]";
	
	/**ƥ��default��䲻������switch*/
	private static final String SWITCH_DEFAULT_NOLAST=
		"	.//SwitchStatement[SwitchLabel[@Default='true' and following-sibling::SwitchLabel[@Default!='true']]";


	
	/**
	 * 
	 *
	 * @param 
	 * @return List<FSMMachineInstance> ״̬��ʵ���б�
	 * @throws
	 */
	
	public static List<FSMMachineInstance> createSNDSStateMachine(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result=null;	
		/**��default����switch*/
		result=node.findXpath(SWITCH_NO_DEFAULT);
		for(Object o:result){
			ASTSwitchStatement st=(ASTSwitchStatement)o;
			
			FSMMachineInstance fsminstance = fsm.creatInstance();
	        fsminstance.setResultString("Swtch statement has not default.");
		    fsminstance.setRelatedObject(new FSMRelatedCalculation(st));
		    list.add(fsminstance);
  
		}

		/**default��䲻������switch*/
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
