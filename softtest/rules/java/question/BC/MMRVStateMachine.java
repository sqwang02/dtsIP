package softtest.rules.java.question.BC;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.config.java.Config;
import softtest.fsm.java.*;

import softtest.rules.java.AbstractStateMachine;
import softtest.symboltable.java.VariableNameDeclaration;

/**
 * MMRVStateMachine
 * 检查丢失方法的返回值
 * 描述:有些方法的返回值需要被检查，比如调用一个不可变对象的方法，必须引用它的返回值，否则该对象不能被改变。
 * 有如下几种：任何返回类型为String的方法，一个StringBuffer类中的toString()方法，
 * 任何InetAddress, BigInteger, BigDecimal类中的方法，MessageDigest 类中的digest(byte[])方法，
 * 多线程类中的构造方法。
 举例：
   1   String str = "  text  ";
   2   str.trim();
   3   System.out.println("before: @"+ str +"@");//此处str并没有改变值
   4   str = str.trim();
   5   System.out.println("after: @"+ str +"@");//此处str已改变值




 * @author cjie
 * 
 */
public class MMRVStateMachine extends AbstractStateMachine{
	
	
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("丢失方法的返回值: 行%d \'%s\'", errorline,fsmmi.getResultString());
		}else{
			f.format("Missing Method's Return Value: The line %d \'%s\'", errorline,fsmmi.getResultString());
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
	
	private static String XPATH1=".//Statement/StatementExpression[count(*)=1]/PrimaryExpression/PrimaryPrefix/Name";
	private static String XPATH2=".//Statement/StatementExpression/PrimaryExpression[count(*)=1]/PrimaryPrefix/AllocationExpression/ClassOrInterfaceType";
    
	private static Class[] classes={InetAddress.class, BigInteger.class, BigDecimal.class}; 
	public static List<FSMMachineInstance> createMMRVStateMachine(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result=null;
		
		result=node.findXpath(XPATH1);
		for(Object o:result){
			ASTName name=(ASTName) o;
			ASTPrimaryPrefix prefix=null;
			if(name.jjtGetParent() instanceof ASTPrimaryPrefix)
			   prefix=(ASTPrimaryPrefix) name.jjtGetParent();
			boolean find=false;
			if(prefix!=null&&prefix.getType() instanceof Method)
			{
				Method m=(Method)prefix.getType();
				VariableNameDeclaration  v=null;
				if(name.getNameDeclaration() instanceof VariableNameDeclaration)
				{
					v = (VariableNameDeclaration) name.getNameDeclaration();
				}
				/**返回值类型为String*/
				if(m.getReturnType()==String.class)
				{
					find=true;
				}
				/**StringBuffer类中的toString()方法*/
				else if(m.getName().equals("toString"))
				{
					if(v!=null)
					{
						if(v.getType()==StringBuffer.class)
						{
							find=true;
						}
					}
				}
				/**MessageDigest 类中的digest(byte[])方法*/
				else if(m.getName().equals("digest"))
				{
					if(v!=null)
					{
						Class[] params=m.getParameterTypes();
						if(params!=null
								&&params.length==1&&params[0].isArray()
								&&v.getType()==MessageDigest.class)
						{
							find=true;
						}
					}
				}
				/**任何InetAddress, BigInteger, BigDecimal类中的方法*/
				else
				{
					if(v!=null)
					{
						for(Class clazz:classes)
					    {
					    	if(v.getType()==clazz)
					    	{
					    		find=true;
					    		break;
					    	}
					    }
					}
					
				}
				
			}
			if(find)
			{
				FSMMachineInstance fsminstance = fsm.creatInstance();
				if(Config.LANGUAGE==0)
	    			fsminstance.setResultString("方法调用："+name.getImage()+"丢失了方法的返回值");
	    		else
	    			fsminstance.setResultString("Method Invoke"+name.getImage()+" missed method's return value");
				fsminstance.setRelatedObject(new FSMRelatedCalculation(name));
				list.add(fsminstance);
			}
			
		}	
		/**多线线程类中的构造方法*/
		result=node.findXpath(XPATH2);
		for(Object o:result){
			ASTClassOrInterfaceType type=(ASTClassOrInterfaceType) o;
			ASTPrimaryPrefix prefix=null;
			if(type.jjtGetParent().jjtGetParent() instanceof ASTPrimaryPrefix)
			   prefix=(ASTPrimaryPrefix) type.jjtGetParent().jjtGetParent();
			boolean find=false;
			if(prefix!=null&&prefix.getType() instanceof Class)
			{
				Class clazz=(Class) prefix.getType();
				while(!find&&clazz!=null)
				{
					if(clazz==Thread.class)
					{
						find=true;
						break;
					}
					Class[] inter=clazz.getInterfaces();
					for(Class c:inter)
					{
						if(c==Runnable.class)
						{
							find=true;
							break;
						}
					}
					
					clazz=clazz.getSuperclass();
				}
			}
			if(find)
			{
				FSMMachineInstance fsminstance = fsm.creatInstance();
				if(Config.LANGUAGE==0)
	    			fsminstance.setResultString("构造方法"+type.getImage()+"丢失了方法的返回值");
	    		else
	    			fsminstance.setResultString("Constructor："+type.getImage()+" missed method's return value");
				fsminstance.setRelatedObject(new FSMRelatedCalculation(type));
				list.add(fsminstance);
			}
		}
		return list;
	}
}
