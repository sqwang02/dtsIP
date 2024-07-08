package softtest.rules.java.question.BC;

import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;

import softtest.rules.java.AbstractStateMachine;
/**
 * ECOLStateMachine
 * ���������ѭ�����Ŀշ�֧ 
 * ����:������д��һ��;�����if��while,for�����һ���յķ�֧��
 ������
   1   private void foo(boolean debug) {
   2   		// ...
   3   		if (debug); { // print something
   4   			System.err.println("Enter foo");
   5   		}
   6   }
 * @author cjie
 * 
 */

public class ECOLStateMachine extends AbstractStateMachine{
	
	
	/**
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("����ѭ���շ�֧: %d �е�������ѭ�������������", beginline);
		}else{
			f.format("Condition and Loop Contains Empty Statement:at line %d the condition or loop contains empty branck.", beginline);
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
	/**ƥ��IF���շ�֧*/
	private static String XPATH1=".//IfStatement/Statement[EmptyStatement or Block[count(*) = 0]]";
	/**ƥ��While���շ�֧*/
	private static String XPATH2=".//WhileStatement/Statement[Block[count(*) = 0]  or EmptyStatement]";
	/**ƥ��DoWhile���շ�֧*/
	private static String XPATH3=".//DoStatement/Statement[Block[count(*) = 0]]";
	
	/**ƥ��For���շ�֧*/
	private static String XPATH4=".//ForStatement/Statement[Block[count(*) = 0]  or EmptyStatement]";
		
	public static List<FSMMachineInstance> createECOLStateMachine(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result=null;
		
		result=node.findXpath(XPATH1);
		for(Object o:result){
			ASTStatement st=(ASTStatement)o;
			FSMMachineInstance fsminstance = fsm.creatInstance();
			fsminstance.setResultString("empty if statement.");
			fsminstance.setRelatedObject(new FSMRelatedCalculation(st));
			list.add(fsminstance);
		}
		
		result=node.findXpath(XPATH2);
		for(Object o:result){
			ASTStatement st=(ASTStatement)o;
			FSMMachineInstance fsminstance = fsm.creatInstance();
			fsminstance.setResultString("empty while statement.");
			fsminstance.setRelatedObject(new FSMRelatedCalculation(st));
			list.add(fsminstance);
		}
		
		result=node.findXpath(XPATH3);
		for(Object o:result){
			ASTStatement st=(ASTStatement)o;
			FSMMachineInstance fsminstance = fsm.creatInstance();
			fsminstance.setResultString("empty dowhile statement.");
			fsminstance.setRelatedObject(new FSMRelatedCalculation(st));
			list.add(fsminstance);
		}
		
		result=node.findXpath(XPATH4);
		for(Object o:result){
			ASTStatement st=(ASTStatement)o;
			FSMMachineInstance fsminstance = fsm.creatInstance();
			fsminstance.setResultString("empty for statement.");
			fsminstance.setRelatedObject(new FSMRelatedCalculation(st));
			list.add(fsminstance);
		}
		
		return list;
	}
}
