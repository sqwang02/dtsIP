package softtest.rules.java.question.PFMC;

import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;


/**
 * ANSCOStateMachine
 * ����߼����ʽ��ʹ���˷Ƕ�·�������������
 * �������߼����ʽ�в�Ҫ��binary��&��|�������������short-circuit�������&&��||�����������Ͽ��ö�·��������á�
 ������
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
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("�Ƕ�·�����: %d ��Ӧ���˷Ƕ�·����� \'%s\',�������Ͽ��ö�·��������� ", errorline,fsmmi.getResultString());
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
			/**����ǲ������ʽ,��˵�����߼����·�����������λ��������߼����·�����(&)��λ�������(&)��ƥ��AndExpression�����򴴽�״̬��ʵ��*/
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
			/**����ǲ������ʽ,��˵�����߼����·�����������λ��������߼����·�����(|)��λ�������(|)��ƥ��InclusiveOrExpression�����򴴽�״̬��ʵ��*/
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
