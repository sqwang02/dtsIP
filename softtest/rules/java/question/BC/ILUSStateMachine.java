package softtest.rules.java.question.BC;

import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;

import softtest.rules.java.AbstractStateMachine;
/**
 * ILUSStateMachine
 * 检查实现Lock()时使用了同步
 * 描述:两个类当中，分别有对对方类的实例初始化的代码。
 举例：
   1   public class lockTest implements Lock{
   2   		public synchronized Condition newCondition(); 
   3   }


 * @author cjie
 * 
 */

public class ILUSStateMachine extends AbstractStateMachine{
	
	
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("实现lock时使用了同步: %d 行实现了Lock接口，但同时也包含了同步方法", errorline);
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
