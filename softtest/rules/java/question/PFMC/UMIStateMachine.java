package softtest.rules.java.question.PFMC;

import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;


/**
 * UMIStateMachine
 * 检查使用了不必要的方法调用
 * 描述：在异常处理块中，调用System.exit()是没有必要的，应抛出异常。
 举例：
1   String square(String x) {
   2   		try {
   3   			int y = Integer.parseInt(x.toLowerCase());
   4   			return y * y + "";
   5   		} catch (NumberFormatException e) {
   6   			e.printStackTrace();
   7   			System.exit(1);
   8   			return "";
   9   		}
   10   }


 * @author cjie
 * 
 */
public class UMIStateMachine extends AbstractStateMachine{
	
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("使用了不必要的函数: %d 行上使用了不必要的函数System.exit,应抛出异常", errorline);
		}else{
			f.format("Used Unnecessary Method: " +
					" line %d Used unnecessary methods System.exit().", errorline);
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
	
	private static String XPATH=".//CatchStatement/Block/BlockStatement/Statement//StatementExpression/PrimaryExpression/PrimaryPrefix/Name[@Image='System.exit']";
	
	public static List<FSMMachineInstance> createUMIStateMachine(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result=null;		
		result=node.findXpath(XPATH);
		for(Object o:result){
			ASTName name=(ASTName)o;
			FSMMachineInstance fsminstance = fsm.creatInstance();
	        fsminstance.setResultString("Unnecessary Method Invoke");
		    fsminstance.setRelatedObject(new FSMRelatedCalculation(name));
		    list.add(fsminstance);
  
		}			
		return list;
	}
}
