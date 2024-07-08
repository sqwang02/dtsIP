package softtest.rules.java.rule;
import java.util.*;


import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.config.java.Config;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;


/**
 * SNESStateMachine
 * 检查switch语句中各分支无结束语句
 * 描述：switch语句中的各分支必须以break、return、System.exit()、Runtime.getRuntime().exit或Thread.currentThread.stop()结束。



 * @author cjie
 * 
 */
public class SNESStateMachine extends AbstractStateMachine{
	
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("switch语句中各分支无结束语句: switch语句中的各分支必须以break、return、System.exit()、Runtime.getRuntime().exit或Thread.currentThread.stop()结束。");
		}else{
			f.format("The Switch statement has No End Statement: Switch statement branches must be end with break、return、System.exit()、Runtime.getRuntime().exit or Thread.currentThread.stop()");
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
	
	/**查找switch节点*/
	private static String XPATH=".//SwitchStatement";
	/**查找结束语句节点*/
	private static String END_XPATH=".//Statement[BreakStatement or ContinueStatement or ReturnStatement or StatementExpression/PrimaryExpression/PrimaryPrefix/Name[@Image='System.exit']]";
	/**
	 * 功能： 创建开关语句各分支无结束语句的状态机
	 *
	 * @param 
	 * @return List<FSMMachineInstance> 状态机实例列表 
	 */
	public static List<FSMMachineInstance> createSNESStateMachine(SimpleJavaNode node, FSMMachine fsm)  {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result=null;	
		result=node.findXpath(XPATH);
		for(Object o:result){

			ASTSwitchStatement switchStatement=(ASTSwitchStatement)o;
			/**存放标签及其相对应的语句块*/
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
		    			fsminstance.setResultString("Switch语句 标签:"+labelName+"无结束语句.");
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
