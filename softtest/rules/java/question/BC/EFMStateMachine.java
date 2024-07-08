package softtest.rules.java.question.BC;

import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;

import softtest.rules.java.AbstractStateMachine;
/**
 * EFMStateMachine
 * 检查空的finalize方法
 * 描述:finalize方法最好不要为空。
 举例：
   1   public void test3() {
   2   		new Example_004() {
   3   			protected void finalize() throws Throwable {
   4             // empty finalize() function
   5   			}
   6   		};
   7   }
   8   // fixed code
   9   public void test1() {
   10   	new Example_004() {
   11   	};
   12   }




 * @author cjie
 * 
 */

public class EFMStateMachine extends AbstractStateMachine{
	
	
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("空的catch块: %d 行包含了空的finalize方法", errorline);
		}else{
			f.format("Empty Finalize Method:at line %d it contains a empty finalize method.",errorline);
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

	private static String XPATH=".//MethodDeclaration[MethodDeclarator[@Image='finalize'][not(FormalParameters/*)]]/Block[count(*)=0]";
		
	public static List<FSMMachineInstance> createEFMStateMachine(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result=null;
		
		result=node.findXpath(XPATH);
		for(Object o:result){
			ASTBlock st=(ASTBlock)o;
			
			FSMMachineInstance fsminstance = fsm.creatInstance();
			fsminstance.setResultString("Empty Finalize Method.");
			fsminstance.setRelatedObject(new FSMRelatedCalculation(st));
			list.add(fsminstance);
				
		}

		return list;
	}
}
