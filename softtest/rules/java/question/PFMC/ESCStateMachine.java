package softtest.rules.java.question.PFMC;

import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;

import softtest.rules.java.AbstractStateMachine;


public class ESCStateMachine extends AbstractStateMachine{
	
	
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("低效的判断字符串为空: 行%d 使用了低效的字符串比较(var.equals(\"\")),应使用var.lenght==0", errorline);
		}else{
			f.format("UnEfficient String Compare: The line %d used unefficient String compare(var.equals(\"\")).", errorline);
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
	
	private static String XPATH1=".//PrimaryExpression/PrimaryPrefix[./Name[matches(@Image,\'^.+\\.equals$\')] and following-sibling::PrimarySuffix[1]/Arguments/ArgumentList/Expression/PrimaryExpression/PrimaryPrefix/Literal[@Image=\'\"\"\'] ]";
	private static String XPATH2=".//PrimaryExpression/PrimarySuffix[@Image=\'equals\' and following-sibling::PrimarySuffix[1]/Arguments/ArgumentList/Expression/PrimaryExpression/PrimaryPrefix/Literal[@Image=\'\"\"\']]";
	private static String XPATH3=".//PrimaryExpression/PrimaryPrefix[./Literal[@Image=\'\"\"\'] and following-sibling::PrimarySuffix[1][@Image=\'equals\'] ]";
		
	public static List<FSMMachineInstance> createESCs(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result=null;
		
		result=node.findXpath(XPATH1);
		for(Object o:result){
			ASTPrimaryPrefix prefix=(ASTPrimaryPrefix)o;
			FSMMachineInstance fsminstance = fsm.creatInstance();
			fsminstance.setResultString("Empty String Compare.");
			fsminstance.setRelatedObject(new FSMRelatedCalculation(prefix));
			list.add(fsminstance);
		}
		
		result=node.findXpath(XPATH2);
		for(Object o:result){
			ASTPrimarySuffix suffix=(ASTPrimarySuffix)o;
			FSMMachineInstance fsminstance = fsm.creatInstance();
			fsminstance.setResultString("Empty String Compare.");
			fsminstance.setRelatedObject(new FSMRelatedCalculation(suffix));
			list.add(fsminstance);
		}
		
		result=node.findXpath(XPATH3);
		for(Object o:result){
			ASTPrimaryPrefix prefix=(ASTPrimaryPrefix)o;
			FSMMachineInstance fsminstance = fsm.creatInstance();
			fsminstance.setResultString("Empty String Compare.");
			fsminstance.setRelatedObject(new FSMRelatedCalculation(prefix));
			list.add(fsminstance);
		}
		
		
		return list;
	}
}
