package softtest.rules.java.fault.NPD;

import java.lang.reflect.Method;
import java.util.Formatter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import softtest.IntervalAnalysis.java.*;
import softtest.ast.java.ASTPrimarySuffix;
import softtest.ast.java.ExpressionBase;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.MethodNode;
import softtest.callgraph.java.method.NpdPrecondition;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.domain.java.*;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;
import softtest.rules.java.AbstractStateMachine;

public class NPDRetStateMachine extends AbstractStateMachine{
	/**
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("��ָ������: %d ���ϣ� \'%s\'����Ϊnull�����ܵ��¿�ָ���쳣.", errorline,fsmmi.getResultString());
		}else{
			f.format("Null Pointer Dereference: \'%s\' on line %d may be a null pointer.", fsmmi.getResultString(),errorline);
		}
		fsmmi.setDescription(f.toString());
		f.close();
	}
	
	@Override
	public  void registerPrecondition(PreconditionListenerSet listeners) {
		//listeners.addListener(NpdPreconditionListener.getInstance());
	}
	
	@Override
	public  void registerFeature(FeatureListenerSet listenerSet) {
	}

	

	public static List<FSMMachineInstance> createNPDRetStateMachines(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		String xPath = ".//PrimarySuffix[Arguments and following-sibling::PrimarySuffix]";
		List evaluationResults = null;
		evaluationResults = node.findXpath(xPath);
		
		Iterator i = evaluationResults.iterator();
		while (i.hasNext()) {
			ASTPrimarySuffix suffix=(ASTPrimarySuffix)i.next();
			ExpressionBase e=suffix.getLastExpression();
			if(!(e.getType() instanceof Method)){
				continue;
			}
			
			MethodNode mn=MethodNode.findMethodNode(e.getType());
			if(mn==null){
				continue;
			}
						
			Object domain=mn.getDomain();
			ClassType classtype = DomainSet.getDomainType(domain);
					
			if(classtype==ClassType.REF){
				ReferenceDomain ref=(ReferenceDomain)domain;
				if(!ref.getUnknown()&&
						(ref.getValue()==ReferenceValue.NULL||ref.getValue()==ReferenceValue.NULL_OR_NOTNULL)){
					Method method=(Method)e.getType();
					if(!NpdPrecondition.checkNPDRetNotGuard(e, method)){
						continue;
					}
					String methodname=method.getName();
					int temp=methodname.lastIndexOf('.');
					if(temp>=0&&temp<methodname.length()-1){
						methodname=methodname.substring(temp+1);
					}
					String traceinfo="";
					if(softtest.config.java.Config.LANGUAGE==0){
						traceinfo="�ļ�:"+mn.getFileName()+" ��:"+mn.getLinenum()+" ����:"+methodname;
					}else{
						traceinfo="file:"+mn.getFileName()+" line:"+mn.getLinenum()+" Method:"+methodname;
					}
					
					FSMMachineInstance fsminstance = fsm.creatInstance();
					list.add(fsminstance);
					fsminstance.setRelatedObject(new FSMRelatedCalculation(e));
					if(softtest.config.java.Config.LANGUAGE==0){
						fsminstance.setResultString("���� "+((Method)e.getType()).getName()+" �ķ���ֵ");
					}else{
						fsminstance.setResultString("the returen value of method "+((Method)e.getType()).getName());
					}
					fsminstance.setTraceinfo(traceinfo);
				}
			}
		    else if (classtype==ClassType.ARRAY) {
			    ArrayDomain arr = (ArrayDomain)domain; 
			    
			    if("NULL".equals(arr.getvalue().toString())){
					Method method=(Method)e.getType();
					if(!NpdPrecondition.checkNPDRetNotGuard(e, method)){
						continue;
					}
					String methodname=method.getName();
					int temp=methodname.lastIndexOf('.');
					if(temp>=0&&temp<methodname.length()-1){
						methodname=methodname.substring(temp+1);
					}
					String traceinfo="";
					if(softtest.config.java.Config.LANGUAGE==0){
						traceinfo="�ļ�:"+mn.getFileName()+" ��:"+mn.getLinenum()+" ����:"+methodname;
					}else{
						traceinfo="file:"+mn.getFileName()+" line:"+mn.getLinenum()+" Method:"+methodname;
					}
					
					FSMMachineInstance fsminstance = fsm.creatInstance();
					list.add(fsminstance);
					fsminstance.setRelatedObject(new FSMRelatedCalculation(e));
					if(softtest.config.java.Config.LANGUAGE==0){
						fsminstance.setResultString("���� "+((Method)e.getType()).getName()+" �ķ���ֵ");
					}else{
						fsminstance.setResultString("the returen value of method "+((Method)e.getType()).getName());
					}
					fsminstance.setTraceinfo(traceinfo);
				}
			
			} 
		}
		return list;
	}

}
