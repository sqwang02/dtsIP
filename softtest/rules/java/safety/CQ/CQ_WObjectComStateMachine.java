package softtest.rules.java.safety.CQ;
import java.util.*;

import softtest.ast.java.ASTName;
import softtest.ast.java.ASTPrimaryExpression;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;
import softtest.rules.java.AbstractStateMachine;
import softtest.symboltable.java.VariableNameDeclaration;
public class CQ_WObjectComStateMachine extends AbstractStateMachine{
	/**
	 * ���ù�������
	 * Object�Ƚ�
	 * Objectʹ�á�==�����бȽϣ��Ƚϵ�ΪObejct������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("��������CQ: %d �г������ == �� != ���Ƚ϶����Ƿ���ȣ��������һ��©����������equals�Ƚϡ�������� == �� != ���Ƚ����������Ƿ���ͬ����ʵ�ʱȽϵ����������ã������Ƕ�������ݡ�", errorline);
		}else{
			f.format("Code Quality: program uses == or != to compara two objects on line %d",errorline);
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
	public static List<FSMMachineInstance> createWObjectComStateMachines(SimpleJavaNode node, FSMMachine fsm)
	{
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		String xpath = "";
		String xp = "";
		List evalRlts = null;
		List result=null;
		Iterator i = null;
		Iterator j = null;
		xpath = ".//EqualityExpression/PrimaryExpression/PrimaryPrefix/Name[@MethodName='false']";
		
		evalRlts = node.findXpath(xpath);
		i = evalRlts.iterator();
		while(i.hasNext())
		{
			ASTName name = (ASTName) i.next();
			VariableNameDeclaration vName=(VariableNameDeclaration)name.getNameDeclaration();		
			if(vName==null||vName.getType()==null||vName.getType().toString().contains("java.lang.String"))continue;
			if(!vName.getTypeImage().contains("java.lang")){
				if(vName.getType().getSuperclass()!=null){
					if(vName.getType().getSuperclass().toString().contains("java.lang.Object")){
						xp=".//EqualityExpression[PrimaryExpression[1]/PrimaryPrefix[Name[@MethodName='false'][@Image='";
						xp=xp+name.getImage()+"']]]/PrimaryExpression[2]/PrimaryPrefix/Name[@MethodName='false']";
						result=node.findXpath(xp);
						j=result.iterator();
						while(j.hasNext()){	
							ASTName nj = (ASTName) j.next();
							VariableNameDeclaration vnj=(VariableNameDeclaration)nj.getNameDeclaration();
							if(vnj==null||vnj.getType()==null)continue;
							if(!vnj.getTypeImage().contains("java.lang")){
								if(vnj.getType().getSuperclass()!=null){
									if(vnj.getType().getSuperclass().toString().contains("java.lang.Object")){
										FSMMachineInstance fsmInst = fsm.creatInstance();
										fsmInst.setRelatedObject(new FSMRelatedCalculation(name));
										fsmInst.setResultString(" Program uses == or != to compara two Object");
										list.add(fsmInst);
									}
								}
							}
						}		
					}
				}	
			}	
		}
		return list;
	}
}
