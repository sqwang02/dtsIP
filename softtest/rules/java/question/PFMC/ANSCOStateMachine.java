package softtest.rules.java.question.PFMC;

import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;


/**
 * ANSCOStateMachine
 * 检查逻辑表达式中使用了非短路运算符性能问题
 * 描述：逻辑表达式中不要用binary（&和|）运算符，而是short-circuit运算符（&&和||）。在性能上看用短路运算符更好。
 举例：
   1   static void check(int arr[]) {
   2   		if (arr!=null & arr.length!=0) {
   3   			foo();
   4   		}
   5   		return;
   6   }

 * @author cjie
 * 
 */
public class ANSCOStateMachine extends AbstractStateMachine{
	
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("非短路运算符: %d 行应用了非短路运算符 \'%s\',在性能上看用短路运算符更好 ", errorline,fsmmi.getResultString());
		}else{
			f.format("Not Short-circuit Operator: the line  %d used the not short-circuit operator \'%s\'.", errorline,fsmmi.getResultString());
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
	
	private static String XPATH1=".//AndExpression";
	private static String XPATH2=".//InclusiveOrExpression";
	
	public static List<FSMMachineInstance> createANSCOStateMachine(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result=null;		
		result=node.findXpath(XPATH1);
		for(Object o:result){
			ASTAndExpression andExpression=(ASTAndExpression)o;
			/**如果是布尔表达式,则说明是逻辑与短路运算符而不是位运算符（逻辑与短路运算符(&)和位运算符与(&)都匹配AndExpression），则创建状态机实例*/
			if(andExpression.getType()==boolean.class)
			{
				FSMMachineInstance fsminstance = fsm.creatInstance();
		        fsminstance.setResultString("&");
			    fsminstance.setRelatedObject(new FSMRelatedCalculation(andExpression));
			    list.add(fsminstance);
			}
		}		
		result=node.findXpath(XPATH2);
		for(Object o:result){
			ASTInclusiveOrExpression orExpression=(ASTInclusiveOrExpression)o;
			/**如果是布尔表达式,则说明是逻辑或短路运算符而不是位运算符（逻辑或短路运算符(|)和位或运算符(|)都匹配InclusiveOrExpression），则创建状态机实例*/
			if(orExpression.getType()==boolean.class)
			{
				FSMMachineInstance fsminstance = fsm.creatInstance();
		        fsminstance.setResultString("|");
			    fsminstance.setRelatedObject(new FSMRelatedCalculation(orExpression));
			    list.add(fsminstance);
			}
		}		
		return list;
	}
}
