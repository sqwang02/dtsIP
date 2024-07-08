package softtest.rules.java.question.BC;

import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;



/**
 * BGCLStateMachine
 * ���ϴ�������˽��ת����long��
 * �������漰���Ƚϴ��������������㣬���ת����long�ͣ����ܷ������
 * ������
   1   long convertDaysToMilliseconds(int days) { 
   2   		return 1000*3600*24*days;
   3   }   //This is bad



 * @author cjie
 * 
 */
public class BGCLStateMachine extends AbstractStateMachine{
	
	/**
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("�ϴ�������˽��ת����long��: ��%d �������ϴ�������ˣ����ܷ������", errorline);
		}else{
			f.format("Big Integer Multiple Convert To Long �� Line %d two big integer multiply ,the result convert long may overflow.", errorline);
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
	/**��������ֵ�ͱ�����ʼ��*/
	private static String XPATH=".//MethodDeclaration[ResultType/Type/PrimitiveType[@Image='long']]//ReturnStatement/Expression/MultiplicativeExpression" +
			"|.//LocalVariableDeclaration[Type/PrimitiveType[@Image='long']]//Expression/MultiplicativeExpression";
	
	/**
	 * 
	 *
	 * @param 
	 * @return List<FSMMachineInstance> ״̬��ʵ���б�
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
					/**�жϱ��ʽ�ǲ���long�ͱ��ʽ*/
					if(left!=null &&(left.getType()==long.class||left.getType()==Long.class)) {
						notLong = false;
						continue;
					} else {
						/**�жϱ��ʽ�ǲ���long�ͳ���*/ 
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
