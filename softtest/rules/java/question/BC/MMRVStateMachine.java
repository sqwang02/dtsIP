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
 * ��鶪ʧ�����ķ���ֵ
 * ����:��Щ�����ķ���ֵ��Ҫ����飬�������һ�����ɱ����ķ����������������ķ���ֵ������ö����ܱ��ı䡣
 * �����¼��֣��κη�������ΪString�ķ�����һ��StringBuffer���е�toString()������
 * �κ�InetAddress, BigInteger, BigDecimal���еķ�����MessageDigest ���е�digest(byte[])������
 * ���߳����еĹ��췽����
 ������
   1   String str = "  text  ";
   2   str.trim();
   3   System.out.println("before: @"+ str +"@");//�˴�str��û�иı�ֵ
   4   str = str.trim();
   5   System.out.println("after: @"+ str +"@");//�˴�str�Ѹı�ֵ




 * @author cjie
 * 
 */
public class MMRVStateMachine extends AbstractStateMachine{
	
	
	/**
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("��ʧ�����ķ���ֵ: ��%d \'%s\'", errorline,fsmmi.getResultString());
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
				/**����ֵ����ΪString*/
				if(m.getReturnType()==String.class)
				{
					find=true;
				}
				/**StringBuffer���е�toString()����*/
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
				/**MessageDigest ���е�digest(byte[])����*/
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
				/**�κ�InetAddress, BigInteger, BigDecimal���еķ���*/
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
	    			fsminstance.setResultString("�������ã�"+name.getImage()+"��ʧ�˷����ķ���ֵ");
	    		else
	    			fsminstance.setResultString("Method Invoke"+name.getImage()+" missed method's return value");
				fsminstance.setRelatedObject(new FSMRelatedCalculation(name));
				list.add(fsminstance);
			}
			
		}	
		/**�����߳����еĹ��췽��*/
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
	    			fsminstance.setResultString("���췽��"+type.getImage()+"��ʧ�˷����ķ���ֵ");
	    		else
	    			fsminstance.setResultString("Constructor��"+type.getImage()+" missed method's return value");
				fsminstance.setRelatedObject(new FSMRelatedCalculation(type));
				list.add(fsminstance);
			}
		}
		return list;
	}
}
