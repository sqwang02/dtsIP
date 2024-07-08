package softtest.rules.java.question.PFMC;

import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;


/**
 * UWCIStateMachine
 * ���ʹ���˲���Ҫ�İ�װ��ʵ����
 * ������Ϊ�˵���toString()�������Ѽ�����ת���ɰ�װ�࣬���ʹ��������Ч�ʽϵͣ���ֱ�ӵ��øð�װ��ľ�̬����toString()
 ������
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
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("ʹ���˲���Ҫ�İ�װ���ʼ��: %d ����ʹ���˲���Ҫ�İ�װ���ʼ��,��ֱ�ӵ��øð�װ��ľ�̬����toString()", errorline);
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
