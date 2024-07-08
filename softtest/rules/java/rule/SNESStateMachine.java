package softtest.rules.java.rule;
import java.util.*;


import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.config.java.Config;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;


/**
 * SNESStateMachine
 * ���switch����и���֧�޽������
 * ������switch����еĸ���֧������break��return��System.exit()��Runtime.getRuntime().exit��Thread.currentThread.stop()������



 * @author cjie
 * 
 */
public class SNESStateMachine extends AbstractStateMachine{
	
	/**
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("switch����и���֧�޽������: switch����еĸ���֧������break��return��System.exit()��Runtime.getRuntime().exit��Thread.currentThread.stop()������");
		}else{
			f.format("The Switch statement has No End Statement: Switch statement branches must be end with break��return��System.exit()��Runtime.getRuntime().exit or Thread.currentThread.stop()");
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
	
	/**����switch�ڵ�*/
	private static String XPATH=".//SwitchStatement";
	/**���ҽ������ڵ�*/
	private static String END_XPATH=".//Statement[BreakStatement or ContinueStatement or ReturnStatement or StatementExpression/PrimaryExpression/PrimaryPrefix/Name[@Image='System.exit']]";
	/**
	 * ���ܣ� ��������������֧�޽�������״̬��
	 *
	 * @param 
	 * @return List<FSMMachineInstance> ״̬��ʵ���б� 
	 */
	public static List<FSMMachineInstance> createSNESStateMachine(SimpleJavaNode node, FSMMachine fsm)  {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result=null;	
		result=node.findXpath(XPATH);
		for(Object o:result){

			ASTSwitchStatement switchStatement=(ASTSwitchStatement)o;
			/**��ű�ǩ�������Ӧ������*/
			Map<ASTSwitchLabel,List<ASTBlockStatement>> switchMap=new HashMap<ASTSwitchLabel,List<ASTBlockStatement>>();
			ASTSwitchLabel label=null;
			for(int i=1;i<switchStatement.jjtGetNumChildren();i++)
			{
				
				if(switchStatement.jjtGetChild(i) instanceof ASTSwitchLabel)
				{
					label=(ASTSwitchLabel) switchStatement.jjtGetChild(i);
						
				}
				else if(switchStatement.jjtGetChild(i) instanceof ASTBlockStatement)
				{
					List<ASTBlockStatement> blockList =switchMap.get(label);
					if(blockList==null)
					{
						blockList=new ArrayList<ASTBlockStatement> ();
					}
					blockList.add((ASTBlockStatement) switchStatement.jjtGetChild(i));
					switchMap.put(label, blockList);
				} 
			}
			ASTSwitchLabel[] labels=new ASTSwitchLabel[switchMap.keySet().size()];
			labels=(ASTSwitchLabel[]) switchMap.keySet().toArray(labels);
			boolean isError;
			for(int i=0;i<labels.length;i++)
			{
				if (labels[i].isDefault()) {
					continue;
				}
				isError = true;
				String labelName=labels[i].getImage();
				List<ASTBlockStatement> listBlock= switchMap.get(labels[i]);
				for(ASTBlockStatement block :listBlock)
				{
					List ends =block.findXpath(END_XPATH);
				    if(null != ends && ends.size() >0) {
				    	isError = false;
				    	break;
				    }
					
				}
				if(isError)
		    	{
		    		FSMMachineInstance fsminstance = fsm.creatInstance();
		    		if(Config.LANGUAGE==0)
		    			fsminstance.setResultString("Switch��� ��ǩ:"+labelName+"�޽������.");
		    		else
		    			fsminstance.setResultString("Switch label:"+labelName+"has no end stament.");
					fsminstance.setRelatedObject(new FSMRelatedCalculation(labels[i]));
					list.add(fsminstance);
		    	}
			}
		}		
	   return list;
	}
}
