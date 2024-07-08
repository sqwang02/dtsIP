package softtest.rules.java.question.BC;

import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;

import softtest.rules.java.AbstractStateMachine;
/**
 * ECOLStateMachine
 * 检查条件、循环语句的空分支 
 * 描述:由于误写了一个;号造成if，while,for语句有一个空的分支。
 举例：
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
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("条件循环空分支: %d 行的条件或循环语句包括空语句", beginline);
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
	/**匹配IF语句空分支*/
	private static String XPATH1=".//IfStatement/Statement[EmptyStatement or Block[count(*) = 0]]";
	/**匹配While语句空分支*/
	private static String XPATH2=".//WhileStatement/Statement[Block[count(*) = 0]  or EmptyStatement]";
	/**匹配DoWhile语句空分支*/
	private static String XPATH3=".//DoStatement/Statement[Block[count(*) = 0]]";
	
	/**匹配For语句空分支*/
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
