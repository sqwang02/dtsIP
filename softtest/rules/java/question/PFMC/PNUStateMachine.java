package softtest.rules.java.question.PFMC;

import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;
import softtest.symboltable.java.VariableNameDeclaration;


/**
 *PNUStateMachine
 * ���α���δ��ʹ�ù�
 * �������α���δ��ʹ�ù���
 ������
	1    class A{
	2       void foo(int i){
	3			
	6     };
	7   }


 * @author cjie
 * 
 */
public class PNUStateMachine extends AbstractStateMachine{
	@Override
	public  void registerPrecondition(PreconditionListenerSet listeners) {
	}
	
	@Override
	public  void registerFeature(FeatureListenerSet listenerSet) {
	}
	
	/**
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("�α���δʹ��: %d ���϶���ķ�������  \'%s\',��δ��ʹ�ù�", beginline,fsmmi.getResultString());
		}else{
			f.format("Parameter Never Used: the parameter \'%s\' in line %d never be used.", fsmmi.getResultString(),beginline);
		}
		fsmmi.setDescription(f.toString());
		f.close();
	}
	/**���Һ��д����ķ����������ǽӿڣ�*/
	private static String XPATH=".//MethodDeclaration[Block[count(*) >0]]/MethodDeclarator/FormalParameters";
	
	/**
	 * ���ܣ� �����α���δ��ʹ�ù�״̬��
	 *
	 * @param 
	 * @return List<FSMMachineInstance> ״̬��ʵ���б� 
	 * @throws
	 */
	public static List<FSMMachineInstance> createPNUStateMachine(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result=null;	
		/**����α�����������*/
		result=node.findXpath(XPATH);
		for(Object o:result){
			ASTFormalParameters params=(ASTFormalParameters)o;
			int paramCnt = params.jjtGetNumChildren();
			for(int i = 0; i < paramCnt; i++) {
				/**ȡ�ñ�������*/
				ASTFormalParameter param = (ASTFormalParameter) params.jjtGetChild(i);
			    ASTVariableDeclaratorId astVDeclId = (ASTVariableDeclaratorId) param.getFirstChildOfType(ASTVariableDeclaratorId.class);
			    VariableNameDeclaration decl=astVDeclId.getNameDeclaration();
			    if (decl == null)
			    	continue;
			    List occrList=decl.getOccs();
			    boolean found=false;
			    if(occrList==null||occrList.size()==0)
			    {
			    	FSMMachineInstance fsminstance = fsm.creatInstance();
		    		fsminstance.setResultString(astVDeclId.getImage());
					fsminstance.setRelatedObject(new FSMRelatedCalculation(astVDeclId));
					list.add(fsminstance);
			    }	
			}				   

		}			
	   return list;
	}
}
