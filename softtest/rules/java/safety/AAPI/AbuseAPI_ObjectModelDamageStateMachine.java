/**
 * 
 */
package softtest.rules.java.safety.AAPI;

import java.util.Formatter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.ASTName;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;
import softtest.rules.java.AbstractStateMachine;
import softtest.symboltable.java.VariableNameDeclaration;

/**
 * @author pengpinglei
 *
 */
public class AbuseAPI_ObjectModelDamageStateMachine extends AbstractStateMachine
{
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("滥用API模式:在 %d 行破坏了对象模型，可能造成一个漏洞。没有对关系密切的方法进行同步的操作，会导致相关类出错。", errorline);
		}else{
			f.format("Abuse Application Program Interface: Destroyed the object model on line %d.It may be incorrect when having no same operating on the related classes.",errorline);
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
	

	public static List<FSMMachineInstance> createObjectModelDamageStateMachines(SimpleJavaNode node, FSMMachine fsm)
	{
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		
		/**
		 * String [] s = new String[100];
		 * s.toString();
		 */
		String xpath1 = "";
		String xpath2 = "";
		List evalRlts1 = null;
		List evalRlts2 = null;
		Iterator i = null;
		Iterator i1 = null;
		Iterator i2 = null;
		int size1 = 0;
		int size2 = 0;
		/**
		 * 1 equals()与hashCode()的不同步
		 * 
		 */
//		xpath1 = ".//MethodDeclaration[@MethodName='equals' and ./ResultType/Type[@TypeImage='boolean'] and .//FormalParameters[@ParameterCount='1' and ./FormalParameter/Type[@TypeImage='Object']]]";	
		xpath1 = ".//MethodDeclaration[@MethodName='equals' and ./ResultType/Type[@TypeImage='boolean'] and .//FormalParameters[@ParameterCount='1']]";
		evalRlts1 = node.findXpath(xpath1);
		i1 = evalRlts1.iterator();
		size1 = evalRlts1.size();
		
		xpath2 = ".//MethodDeclaration[@MethodName='hashCode' and ./ResultType/Type[@TypeImage='int'] and .//FormalParameters[@ParameterCount='0']]";	
		evalRlts2 = node.findXpath(xpath2);
		i2 = evalRlts2.iterator();
		size2 = evalRlts2.size();
		
		
		if(size1 != size2)
		{
			if(size1 > size2) i = i1;
			else i = i2;
			
			ASTMethodDeclaration name = (ASTMethodDeclaration) i.next();
			FSMMachineInstance fsmInst = fsm.creatInstance();
			fsmInst.setRelatedObject(new FSMRelatedCalculation(name));
			fsmInst.setResultString("equals() and hashCode() are out of sync");
			list.add(fsmInst);
		}
		
		/**
		 * 2 saveState()与restoreState()的不同步
		 */
		xpath1 = ".//MethodDeclaration[@MethodName='saveState']";	
		evalRlts1 = node.findXpath(xpath1);
		i1 = evalRlts1.iterator();
		size1 = evalRlts1.size();
		
		xpath2 = ".//MethodDeclaration[@MethodName='restoreState']";	
		evalRlts2 = node.findXpath(xpath2);
		i2 = evalRlts2.iterator();
		size2 = evalRlts2.size();
		
		if(size1 != size2)
		{
			if(size1 > size2) i = i1;
			else i = i2;
			
			ASTMethodDeclaration name = (ASTMethodDeclaration) i.next();
			FSMMachineInstance fsmInst = fsm.creatInstance();
			fsmInst.setRelatedObject(new FSMRelatedCalculation(name));
			fsmInst.setResultString("saveState() and restoreState() are out of sync");
			list.add(fsmInst);
		}
		
		return list;
	}

}
