package softtest.rules.java.safety.ECP;
import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;
public class ECP_WFinalLoggerStateMachine extends AbstractStateMachine{
	/**
	 * ���ù�������
	 * LoggerδFinal&Static
	 * 2009.09.14@baigele
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("��װ����ģʽ: %d �е�Loggerʵ��δ������ΪStatic final", errorline);
		}else{
			f.format("ECP: Logger is not declared with static final on line %d",errorline);
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
	public static List<FSMMachineInstance> createWFinalLoggerStateMachines(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		
		String xPath = ".//ClassOrInterfaceBodyDeclaration/FieldDeclaration[@Final='true'][@Static='false']/Type//ClassOrInterfaceType[@Image='Logger']";
		List evaluationResults = node.findXpath(xPath);
		Iterator i = evaluationResults.iterator();
		while(i.hasNext())
		{
			ASTClassOrInterfaceType name = (ASTClassOrInterfaceType) i.next();
			FSMMachineInstance fsmInst = fsm.creatInstance();
			fsmInst.setRelatedObject(new FSMRelatedCalculation(name));
			fsmInst.setResultString("Logger is not declared with static final");
			list.add(fsmInst);
		}
		return list;
	}
}
