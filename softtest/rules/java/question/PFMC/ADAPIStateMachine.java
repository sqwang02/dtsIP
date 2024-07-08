package softtest.rules.java.question.PFMC;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;


/**
 * ADAPIStateMachine
 * ����߼����ʽ��ʹ���˲��Ƽ������ڣ���API
 * ����ͷ�������java������кܶ෽���ǳ¾ɵĻ����ǿ���ѡ��ġ���һЩ����SUN����"deprecated����ǡ���JDK6��Deprecated���֣���ò�Ҫʹ��.
 ������
   private List t_list = new List (); 
   t_list.addItem (str); 
   �����һ��javadoc�Ļ����ᷢ�ֽ�����add()������addItem()��


 * @author cjie
 * 
 */
public class ADAPIStateMachine extends AbstractStateMachine{
	
	/**
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("ʹ���˲��Ƽ���API: %d ���ϵ��õķ���  \'%s\'�ǹ��ڻ��Ƽ�ʹ�õĺ���", errorline,fsmmi.getResultString());
		}else{
			f.format("Applyed deprecated API: the method \'%s\' is a deprecated method in line %d.", fsmmi.getResultString(),errorline);
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
	
	private static String XPATH1=".//PrimaryPrefix";
	private static String XPATH2=".//PrimarySuffix";
	private static String XPATH3=".//AllocationExpression";
	
	/**
	 * ���ܣ� ����ʹ���˲��Ƽ�����״̬��
	 *
	 * @param 
	 * @return List<FSMMachineInstance> ״̬��ʵ���б� 
	 * @throws
	 */
	public static List<FSMMachineInstance> createADAPIStateMachine(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result=null;	
		/**����ǰ׺���ʽ�еķ�������*/
		result=node.findXpath(XPATH1);
		for(Object o:result){
			ASTPrimaryPrefix prefix=(ASTPrimaryPrefix)o;
			if(prefix.isMethodName()){
				/**����Ƿ��������鷽���Ƿ����@Deprecated Annotation����������򴴽�״̬��ʵ��*/
				if(prefix.getType() instanceof Method){ 
				    Method m=(Method)prefix.getType();
				    Annotation[] annotations=m.getAnnotations();
				    for(int i=0;i<annotations.length;i++)
				    {
				    	if(annotations[i]!=null&&annotations[i].annotationType()==Deprecated.class);
				    	{
				    		FSMMachineInstance fsminstance = fsm.creatInstance();
							fsminstance.setResultString(m.getName());
							fsminstance.setRelatedObject(new FSMRelatedCalculation(prefix));
							list.add(fsminstance);
							break;
				    	}
				    }
				}
			}

		}	
		/**�����׺���ʽ�еķ�������*/
		result=node.findXpath(XPATH2);
		for(Object o:result){
			ASTPrimarySuffix suffix=(ASTPrimarySuffix)o;
			if(suffix.isMethodName()){
				/**����Ƿ��������鷽���Ƿ����@Deprecated Annotation����������򴴽�״̬��ʵ��*/
				if(suffix.getType() instanceof Method){
					Method m=(Method)suffix.getType();
				    Annotation[] annotations=m.getAnnotations();
				    for(int i=0;i<annotations.length;i++)
				    {
				    	if(annotations[i]!=null&&annotations[i].annotationType()==Deprecated.class);
				    	{
				    		FSMMachineInstance fsminstance = fsm.creatInstance();
							fsminstance.setResultString(m.getName());
							fsminstance.setRelatedObject(new FSMRelatedCalculation(suffix));
							list.add(fsminstance);
							break;
				    	}
				    }
				}
			}

		}		
		/**�����캯��*/
		result=node.findXpath(XPATH3);
		for(Object o:result){
			ASTAllocationExpression expression=(ASTAllocationExpression)o;
			if(expression.getType() instanceof Constructor){
				/**����ǹ��캯�������鷽���Ƿ����@Deprecated Annotation����������򴴽�״̬��ʵ��*/
				Constructor c=(Constructor)expression.getType();
			    Annotation[] annotations=c.getDeclaredAnnotations();
			    for(int i=0;i<annotations.length;i++)
			    {
			    	if(annotations[i]!=null&&annotations[i].annotationType()==Deprecated.class);
			    	{
			    		FSMMachineInstance fsminstance = fsm.creatInstance();
						fsminstance.setResultString(""+expression.getType());
						fsminstance.setRelatedObject(new FSMRelatedCalculation(expression));
						list.add(fsminstance);
						break;
			    	}
			    }

		   }		
	   }
	   return list;
	}
}
