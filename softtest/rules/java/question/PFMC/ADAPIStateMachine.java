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
 * 检查逻辑表达式中使用了不推荐（过期）的API
 * 在类和方法或者java组件里有很多方法是陈旧的或者是可以选择的。有一些方法SUN用了"deprecated“标记。见JDK6中Deprecated部分，最好不要使用.
 举例：
   private List t_list = new List (); 
   t_list.addItem (str); 
   如果查一下javadoc的话，会发现建议用add()来代替addItem()。


 * @author cjie
 * 
 */
public class ADAPIStateMachine extends AbstractStateMachine{
	
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("使用了不推荐的API: %d 行上调用的方法  \'%s\'是过期或不推荐使用的函数", errorline,fsmmi.getResultString());
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
	 * 功能： 创建使用了不推荐方法状态机
	 *
	 * @param 
	 * @return List<FSMMachineInstance> 状态机实例列表 
	 * @throws
	 */
	public static List<FSMMachineInstance> createADAPIStateMachine(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result=null;	
		/**处理前缀表达式中的方法调用*/
		result=node.findXpath(XPATH1);
		for(Object o:result){
			ASTPrimaryPrefix prefix=(ASTPrimaryPrefix)o;
			if(prefix.isMethodName()){
				/**如果是方法，则检查方法是否包含@Deprecated Annotation，如果包括则创建状态机实例*/
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
		/**处理后缀表达式中的方法调用*/
		result=node.findXpath(XPATH2);
		for(Object o:result){
			ASTPrimarySuffix suffix=(ASTPrimarySuffix)o;
			if(suffix.isMethodName()){
				/**如果是方法，则检查方法是否包含@Deprecated Annotation，如果包括则创建状态机实例*/
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
		/**处理构造函数*/
		result=node.findXpath(XPATH3);
		for(Object o:result){
			ASTAllocationExpression expression=(ASTAllocationExpression)o;
			if(expression.getType() instanceof Constructor){
				/**如果是构造函数，则检查方法是否包含@Deprecated Annotation，如果包括则创建状态机实例*/
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
