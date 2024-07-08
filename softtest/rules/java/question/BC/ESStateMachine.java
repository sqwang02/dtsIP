package softtest.rules.java.question.BC;

import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;

import softtest.rules.java.AbstractStateMachine;
/**
 * ESStateMachine
 * 检查空的同步块语句
 * 描述:空的同步块很不稳定且难于正确应用。
 举例：
   1   	synchronized(this) {}


 * @author cjie
 * 
 */

public class ESStateMachine extends AbstractStateMachine{
	
	
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("空的同步化语句块: %d 行包含空的同步化语句块", beginline);
		}else{
			f.format("Empty Synchronized Statement: " +
					"at line %d the statement contains empty synchronized block. ", beginline);
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
	/**匹配空的synchronize代码块*/
	private static String XPATH=".//SynchronizedStatement/Block[1][count(*) = 0]";
		
	public static List<FSMMachineInstance> createESStateMachine(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result=null;
		
		result=node.findXpath(XPATH);
		for(Object o:result){
			ASTBlock block=(ASTBlock)o;
			FSMMachineInstance fsminstance = fsm.creatInstance();
			fsminstance.setResultString("Empty synchronize block");
			fsminstance.setRelatedObject(new FSMRelatedCalculation(block));
			list.add(fsminstance);	
		}
		return list;
	}
}
