package softtest.rules.java.question.BC;

import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;

import softtest.rules.java.AbstractStateMachine;
/**
 * ECBStateMachine
 * 检查空的catch语句块
 * 描述:catch语句里没有内容，这是不太合乎习惯与语法的，遇到异常最好是处理，而不是忽略。
 举例：
   1   public void openFile(String name) {
   2   		try {
   3   			FileInputStream is = new FileInputStream(name);
   4   			// read file ...
   5   		} catch (FileNotFoundException e) {
   6   			// TODO Auto-generated catch block
   7   		}
   8   }



 * @author cjie
 * 
 */

public class ECBStateMachine extends AbstractStateMachine{
	
	
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("空的catch块: %d 行包含了空的catch语句块", errorline);
		}else{
			f.format("Empty Catch Block:at line %d it contains a empty catch block.",errorline);
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

	private static String XPATH=".//CatchStatement[count(Block/BlockStatement) = 0]";
		
	public static List<FSMMachineInstance> createECBStateMachine(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result=null;
		
		result=node.findXpath(XPATH);
		for(Object o:result){
			ASTCatchStatement st=(ASTCatchStatement)o;
			
			FSMMachineInstance fsminstance = fsm.creatInstance();
			fsminstance.setResultString("Empty Catch Block.");
			fsminstance.setRelatedObject(new FSMRelatedCalculation(st));
			list.add(fsminstance);
				
		}

		return list;
	}
}
