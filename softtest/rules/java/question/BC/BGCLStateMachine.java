package softtest.rules.java.question.BC;

import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;



/**
 * BGCLStateMachine
 * 检查较大整数相乘结果转换成long型
 * 描述：涉及到比较大的整数的相乘运算，结果转换成long型，可能发生溢出
 * 举例：
   1   long convertDaysToMilliseconds(int days) { 
   2   		return 1000*3600*24*days;
   3   }   //This is bad



 * @author cjie
 * 
 */
public class BGCLStateMachine extends AbstractStateMachine{
	
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("较大整数相乘结果转换成long型: 第%d 行两个较大整数相乘，可能发生溢出", errorline);
		}else{
			f.format("Big Integer Multiple Convert To Long ： Line %d two big integer multiply ,the result convert long may overflow.", errorline);
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
	/**方法返回值和变量初始化*/
	private static String XPATH=".//MethodDeclaration[ResultType/Type/PrimitiveType[@Image='long']]//ReturnStatement/Expression/MultiplicativeExpression" +
			"|.//LocalVariableDeclaration[Type/PrimitiveType[@Image='long']]//Expression/MultiplicativeExpression";
	
	/**
	 * 
	 *
	 * @param 
	 * @return List<FSMMachineInstance> 状态机实例列表
	 * @throws
	 */
	
	public static List<FSMMachineInstance> createBGCLStateMachine(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result=null;		
		result=node.findXpath(XPATH);
		for(Object o:result){
			ASTMultiplicativeExpression expression=(ASTMultiplicativeExpression)o;
			if(expression.jjtGetNumChildren()<2)
				continue;
			ASTPrimaryExpression left=null,right=null;
			boolean isError = false;
			boolean notLong = false;
			boolean isVar = false;
			for ( int i=0; i <expression.jjtGetNumChildren(); i ++) {
				if(expression.jjtGetChild(i) instanceof ASTPrimaryExpression)
				{
					left=(ASTPrimaryExpression) expression.jjtGetChild(i);
					/**判断表达式是不是long型表达式*/
					if(left!=null &&(left.getType()==long.class||left.getType()==Long.class)) {
						notLong = false;
						continue;
					} else {
						/**判断表达式是不是long型常量*/ 
						if(left!=null&&left.jjtGetChild(0) instanceof ASTPrimaryPrefix)
						{
							ASTPrimaryPrefix prefix=(ASTPrimaryPrefix) left.jjtGetChild(0);
							if(prefix!=null&&prefix.jjtGetChild(0) instanceof ASTLiteral)
							{
								ASTLiteral literal=(ASTLiteral) prefix.jjtGetChild(0);
								if(literal!=null&&literal.getImage()!=null)
								{
									String img=literal.getImage();
									if(img.charAt(img.length()-1)=='l'
										||img.charAt(img.length()-1)=='L') {
										notLong = false;
										continue;
									}
								}
							} else {
								isVar = true;
							}
						} 
						if(notLong && isVar) {
							isError = true;
							break;
						}
						notLong = true;
					}
					
				} 
							
			} 
			if(isError) {
				FSMMachineInstance fsminstance = fsm.creatInstance();
		        fsminstance.setResultString("Big Integer Multiple Convert To Long");
			    fsminstance.setRelatedObject(new FSMRelatedCalculation(expression));
			    list.add(fsminstance);
			}
		}			
		return list;
	}

}
