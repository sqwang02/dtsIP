package softtest.rules.java.safety.ECP;
import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;
import softtest.symboltable.java.VariableNameDeclaration;
public class ECP_ReturnPrivateStateMachine extends AbstractStateMachine{
	/**
	 * ���ù�������
	 * ��������������˽�е��������
	 * 2009.09.21@baigele
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("��װ����ģʽ: %d �еĹ��к���������˽���������", errorline);
		}else{
			f.format("ECP: Public function return private array on line %d",errorline);
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
	public static List<FSMMachineInstance> createReturnPrivateStateMachines(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		
		String xPath = ".//MethodDeclaration[@Public='true']//ReturnStatement//ConditionalExpression//Expression/PrimaryExpression//PrimaryPrefix/Name[@MethodName='false']";
		List result = node.findXpath(xPath);
		Iterator j = result.iterator();
		while(j.hasNext())
		{
			ASTName name = (ASTName) j.next();
			VariableNameDeclaration vName=(VariableNameDeclaration)name.getNameDeclaration();			
			if(name.getNameDeclaration()==null){
				return list;
			}
			if(vName.getAccessNodeParent()!=null&&vName.getAccessNodeParent().isPrivate()){
				if(vName.isArray()){
					FSMMachineInstance fsmInst = fsm.creatInstance();
					fsmInst.setRelatedObject(new FSMRelatedCalculation(name));
					fsmInst.setResultString("Public function return private array:"+name.getImage());
					list.add(fsmInst);
				}
			}
		}
//2-1	
		xPath = ".//MethodDeclaration[@Public='true']//ReturnStatement//ConditionalExpression/PrimaryExpression/PrimaryPrefix/Name[@MethodName='false']";
		result = node.findXpath(xPath);
		Iterator k = result.iterator();
		while(k.hasNext())
		{
			
			ASTName name = (ASTName) k.next();
			VariableNameDeclaration vName=(VariableNameDeclaration)name.getNameDeclaration();			
			if(name.getNameDeclaration()==null){
				return list;
			}
			if(vName.getAccessNodeParent()!=null&&vName.getAccessNodeParent().isPrivate()){
				if(vName.isArray()){
					FSMMachineInstance fsmInst = fsm.creatInstance();
					fsmInst.setRelatedObject(new FSMRelatedCalculation(name));
					fsmInst.setResultString("Public function return private array:"+name.getImage());
					list.add(fsmInst);
				}
			}
		}
		
			
//		������condition Expression
//		09.09.22 �޸���xPath����ֹNameΪ������
		xPath = ".//MethodDeclaration[@Public='true']//ReturnStatement//PrimaryExpression//PrimaryPrefix/Name[@MethodName='false']";
		List evaluationResults = node.findXpath(xPath);
		Iterator i = evaluationResults.iterator();
		while(i.hasNext())
		{
			ASTName name = (ASTName) i.next();
			Node np=name.jjtGetParent();
			while(!(np instanceof ASTMethodDeclaration)){
				np=np.jjtGetParent();
				if(np instanceof ASTConditionalExpression)
					return list;
			}				
			VariableNameDeclaration vName=(VariableNameDeclaration)name.getNameDeclaration();
			if(name.getNameDeclaration()!=null){
				if(vName.getAccessNodeParent()!=null&&vName.getAccessNodeParent().isPrivate()){
					if(vName.isArray()){
						FSMMachineInstance fsmInst = fsm.creatInstance();
						fsmInst.setRelatedObject(new FSMRelatedCalculation(name));
						fsmInst.setResultString("Public function return private array:"+name.getImage());
						list.add(fsmInst);
					}
				}
			}
		}
		
		
		
		return list;
	}
}
