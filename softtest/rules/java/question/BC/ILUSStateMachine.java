package softtest.rules.java.question.BC;

import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;

import softtest.rules.java.AbstractStateMachine;
/**
 * ILUSStateMachine
 * ���ʵ��Lock()ʱʹ����ͬ��
 * ����:�����൱�У��ֱ��жԶԷ����ʵ����ʼ���Ĵ��롣
 ������
   1   public class lockTest implements Lock{
   2   		public synchronized Condition newCondition(); 
   3   }


 * @author cjie
 * 
 */

public class ILUSStateMachine extends AbstractStateMachine{
	
	
	/**
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("ʵ��lockʱʹ����ͬ��: %d ��ʵ����Lock�ӿڣ���ͬʱҲ������ͬ������", errorline);
		}else{
			f.format("Used synchronized When It Implements Lock:at line %d Implements Lock interface but it contains a synchronzied method.",errorline);
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

	private static String XPATH=".//ClassOrInterfaceDeclaration[ImplementsList/ClassOrInterfaceType[@Image='Lock'] and  .//MethodDeclaration[@Synchronized='true']]";
		
	public static List<FSMMachineInstance> createILUSStateMachine(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result=null;
		
		result=node.findXpath(XPATH);
		for(Object o:result){
			ASTClassOrInterfaceDeclaration type=(ASTClassOrInterfaceDeclaration)o;
			
			FSMMachineInstance fsminstance = fsm.creatInstance();
			fsminstance.setResultString("Used synchronized When It Implements Lock.");
			fsminstance.setRelatedObject(new FSMRelatedCalculation(type));
			list.add(fsminstance);
				
		}

		return list;
	}
}
