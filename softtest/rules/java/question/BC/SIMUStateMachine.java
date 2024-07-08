package softtest.rules.java.question.BC;

import java.lang.reflect.Method;
import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;

import softtest.rules.java.AbstractStateMachine;
import softtest.symboltable.java.VariableNameDeclaration;

/**
 * SIMUStateMachine
 * ���String.indexOf()������
 * ����:��String.indexOf���ر����ֵķ��ŵ�λ�ã���0��ʼ����˵�Ҫ����жϽ���Ƿ����0ʱ���ܿ����ǲ���ȷ�ģ�Ӧ�ü��==-1��>=0������>0��
 ������
   1   ...
   2   public boolean checkFile(String file) {
   3   		if (file.indexOf("/")>0) {
   4   			return true;
   5   		}
   6   		return false;
   7   }
   8   ...



 * @author cjie
 * 
 */
public class SIMUStateMachine extends AbstractStateMachine{
	
	
	/**
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("Sting.indexOf()������: ��%d ���ֵ�Sting.indexOf()������һ������(>0),Ӧ��ʹ��>=0", errorline);
		}else{
			f.format("String.indexOf() MisUse: The line %d String.indexOf() maybe a misuse(>0).", errorline);
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
	
	private static String XPATH1=".//RelationalExpression[@Image='>']/PrimaryExpression[PrimaryPrefix/Name[matches(@Image,'^.+\\.indexOf$')] and following-sibling::PrimaryExpression/PrimaryPrefix/Literal[@Image='0'] ]";
	private static String XPATH2=".//RelationalExpression[@Image='>']/PrimaryExpression[PrimaryPrefix and PrimarySuffix[@Image='indexOf'] and following-sibling::PrimaryExpression/PrimaryPrefix/Literal[@Image='0'] ]";

	public static List<FSMMachineInstance> createSIMUStateMachine(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result=null;
		
		result=node.findXpath(XPATH1);
		for(Object o:result){
			ASTPrimaryExpression exp=(ASTPrimaryExpression)o;
			if(exp.jjtGetChild(0).jjtGetChild(0) instanceof ASTName)
			{
				ASTName name=(ASTName) exp.jjtGetChild(0).jjtGetChild(0);
				if(!(name.getNameDeclaration() instanceof VariableNameDeclaration))
				{
					continue;
				}
				VariableNameDeclaration v = (VariableNameDeclaration) name.getNameDeclaration();
				if(v.getTypeImage().matches("^String$"))
				{
					FSMMachineInstance fsminstance = fsm.creatInstance();
					fsminstance.setResultString("String.indexOf() MisUse.");
					fsminstance.setRelatedObject(new FSMRelatedCalculation(exp));
					list.add(fsminstance);
				}
			}
		}
		
		result=node.findXpath(XPATH2);
		for(Object o:result){
			ASTPrimaryExpression exp=(ASTPrimaryExpression)o;
			boolean isError=false;
			if(exp.jjtGetChild(0) instanceof ASTPrimaryPrefix)
			{
				ASTPrimaryPrefix prefix=(ASTPrimaryPrefix) exp.jjtGetChild(0);
				if(prefix.getType()==String.class)
				{
					isError=true;
				}
				else if(prefix.getType() instanceof Method){ 
				   Method m=(Method)prefix.getType();
				   if(m.getReturnType()==String.class)
				   {
					   isError=true;  
				   }
				}
			}
			if(isError)
			{
				FSMMachineInstance fsminstance = fsm.creatInstance();
				fsminstance.setResultString("String.indexOf() MisUse.");
				fsminstance.setRelatedObject(new FSMRelatedCalculation(exp));
				list.add(fsminstance);
			}
			
		}
		return list;
	}
}
