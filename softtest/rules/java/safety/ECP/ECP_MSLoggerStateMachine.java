package softtest.rules.java.safety.ECP;
import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;
public class ECP_MSLoggerStateMachine extends AbstractStateMachine{
	/**
	 * 设置故障描述
	 * 同一个实例声明了多个日志记录器
	 * 2009.09.14@baigele
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("封装问题模式: %d 行同一个Logger实例声明为多个记录器", errorline);
		}else{
			f.format("ECP: Not only one Logger for a class on line %d",errorline);
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
	public static Set<String> nodeSet=null;
	
	public static List<FSMMachineInstance> createMSLoggerStateMachines(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		
		String xPath = ".//PrimaryExpression[PrimaryPrefix/Name[@Image='Logger.getLogger']]/PrimarySuffix//PrimaryExpression//Name";
		List evaluationResults = node.findXpath(xPath);		
		nodeSet=new HashSet<String>();
		Iterator i = evaluationResults.iterator();
		while(i.hasNext())
		{
			ASTName name = (ASTName) i.next();
			if(nodeSet.contains(name.getImage())){
				FSMMachineInstance fsmInst = fsm.creatInstance();
				fsmInst.setRelatedObject(new FSMRelatedCalculation(name));
				fsmInst.setResultString("Not only one Logger for a class");
				list.add(fsmInst);
			}else{nodeSet.add(name.getImage());}
		
		}
		return list;
	}
}
