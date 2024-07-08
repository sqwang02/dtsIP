package softtest.rules.java.question.PFMC;

import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;
import softtest.symboltable.java.NameDeclaration;


/**
 * UMIGStateMachine
 * �������˲���Ҫ��getClass()����
 * ������Ϊ�˵��ö����ĳ�����Ի򷽷���������getClass()������û�б�Ҫ��Ӧֱ��ʹ�����Ի��߷�����
 ������
   1   object.getClass().xx; // This is bad
   2   object.xx; // This is better



 * @author cjie
 * 
 */
public class UMIGStateMachine extends AbstractStateMachine{
	
	/**
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("ʹ���˲���Ҫ�ĺ���: %d ����ʹ���˲���Ҫ�ĺ���getClass", errorline);
		}else{
			f.format("Used Unnecessary Method: " +
					" line %d Used unnecessary methods getClass().", errorline);
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
	
	private static String XPATH=".//PrimaryExpression/PrimaryPrefix[child::Name[matches(@Image,'.*\\.getClass') and following::PrimarySuffix[2][@Image='getMethod' or @Image='getField' ]]";
	
	public static List<FSMMachineInstance> createUMIGStateMachine(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result=null;		
		result=node.findXpath(XPATH);
		for(Object o:result){
			ASTPrimaryPrefix prefix=(ASTPrimaryPrefix) o;
			if(prefix.getFirstChildOfType(ASTName.class) instanceof ASTName)
			{
				ASTName name=(ASTName) prefix.getFirstChildOfType(ASTName.class);
			    NameDeclaration decl=name.getNameDeclaration();
			    if(!((ASTVariableDeclaratorId)decl.getNode()).getTypeNode().getTypeImage().equals("Class"))
			    {
					FSMMachineInstance fsminstance = fsm.creatInstance();
			        fsminstance.setResultString("Unnecessary Method Invoke:getClass()");
				    fsminstance.setRelatedObject(new FSMRelatedCalculation(name));
				    list.add(fsminstance);
			    }

			}
		}			
		return list;
	}
}
