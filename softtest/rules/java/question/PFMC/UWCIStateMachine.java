package softtest.rules.java.question.PFMC;

import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;


/**
 * UWCIStateMachine
 * 检查使用了不必要的包装类实例化
 * 描述：为了调用toString()方法而把简单类型转换成包装类，则会使代码运行效率较低，可直接调用该包装类的静态方法toString()
 举例：
   1   // This is bad
   2   new Long(1).toString();
   3   new Float(1.0).toString();
   4   // This is better 
   5   Long.toString(1);
   6   Float.toString(1.0);
 * @author cjie
 * 
 */
public class UWCIStateMachine extends AbstractStateMachine{
	
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("使用了不必要的包装类初始化: %d 行上使用了不必要的包装类初始化,可直接调用该包装类的静态方法toString()", errorline);
		}else{
			f.format("Unnecessary Wrapped Class  Initialize :line %d used unnecessary wrapped class Initialize.",errorline);
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
	
	private static String XPATH=".//PrimaryExpression/PrimaryPrefix[child::AllocationExpression/ClassOrInterfaceType[matches(@Image,'^Double|Long|Boolean|Integer|Float|Byte|Short|Character|String$' )] and following-sibling::PrimarySuffix[1][@Image='toString']]";
	//private static String XPATH=".//PrimaryExpression/PrimaryPrefix[child::AllocationExpression/ClassOrInterfaceType[@Image='Double' or @Image='Long' or @Image='Integer' or @Image='Float' or @Image='Byte' or @Image='Short' or @Image='Character' or @Image='Boolean' or @Image='String'] and following-sibling::PrimarySuffix[1][@Image='toString']]";
	
	public static List<FSMMachineInstance> createUWCIStateMachine(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result=null;		
		result=node.findXpath(XPATH);
		for(Object o:result){
			ASTPrimaryPrefix prefix=(ASTPrimaryPrefix)o;
			FSMMachineInstance fsminstance = fsm.creatInstance();
	        fsminstance.setResultString("Unnecessary Wrapper Class Instantiation");
		    fsminstance.setRelatedObject(new FSMRelatedCalculation(prefix));
		    list.add(fsminstance);
  
		}			
		return list;
	}
}
