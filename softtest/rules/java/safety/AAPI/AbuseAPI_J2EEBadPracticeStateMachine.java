/**
 * 
 */
package softtest.rules.java.safety.AAPI;


import java.util.Formatter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTPrimarySuffix;
import softtest.ast.java.ASTType;
import softtest.ast.java.SimpleJavaNode;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;

/**
 * @author pengpinglei
 *
 */
public class AbuseAPI_J2EEBadPracticeStateMachine
{
	private static int count = 0;
	
	private static boolean isJ2EEprog = false;
	
	/**
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("����APIģʽ:J2EE�������ʵ������ %d �п������һ��©����Υ����J2EE�淶�������˲���ʹ�õ�����߷�����ȫ�Խ��͡�", errorline);
		}else{
			f.format("Abuse Application Program Interface: J2EE Bad Practice on line %d.Break the rules of J2EE, use the wrong classes or method.",errorline);
		}
		fsmmi.setDescription(f.toString());
		f.close();
	}

	public static List<FSMMachineInstance> createJ2EEBadPracticeStateMachines(SimpleJavaNode node, FSMMachine fsm)
	{
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		
		/*
		 * �����û��������ļ���
		 * ������һ�Ρ�
		 */
		if(count == 0)
		{
			UserConfig userConfig = new UserConfig();
			
			isJ2EEprog = userConfig.isJ2EEprogram();
			
			count++ ;
		}
		
		/*
		 * ���ΪJ2EE���� 
		 */
		if(isJ2EEprog)
		{
			String xpath = "";
			List evalRlts = null;
			Iterator i = null;
			
			/**
			 * 1 ��J2EE�����е���getConnection()
			 * o.getConnection();
			 */
			xpath = " .//PrimaryExpression/PrimaryPrefix/Name" +
			"[@TypeString='public static java.sql.Connection java.sql.DriverManager.getConnection(java.lang.String) throws java.sql.SQLException' | " +
			"@TypeString='public static java.sql.Connection java.sql.DriverManager.getConnection(java.lang.String,java.util.Properties) throws java.sql.SQLException' | " +
			"@TypeString='private static java.sql.Connection java.sql.DriverManager.getConnection(java.lang.String,java.util.Properties,java.lang.ClassLoader) throws java.sql.SQLException' ]";
			
			evalRlts = node.findXpath(xpath);
			i = evalRlts.iterator();
			
			while(i.hasNext())
			{
				ASTName name = (ASTName) i.next();
				FSMMachineInstance fsmInst = fsm.creatInstance();
				fsmInst.setRelatedObject(new FSMRelatedCalculation(name));
				fsmInst.setResultString("J2EE program uses getConnction()");
				list.add(fsmInst);
			}	
			
			/**
			 * 2 ��J2EE�����е���getConnection()
			 * o.f().getConnection();
			 */
			xpath = " .//PrimaryExpression/PrimarySuffix" +
			"[@TypeString='public static java.sql.Connection java.sql.DriverManager.getConnection(java.lang.String) throws java.sql.SQLException' | " +
			"@TypeString='public static java.sql.Connection java.sql.DriverManager.getConnection(java.lang.String,java.util.Properties) throws java.sql.SQLException' | " +
			"@TypeString='private static java.sql.Connection java.sql.DriverManager.getConnection(java.lang.String,java.util.Properties,java.lang.ClassLoader) throws java.sql.SQLException']";
			
			evalRlts = node.findXpath(xpath);
			i = evalRlts.iterator();
			
			while(i.hasNext())
			{
				ASTPrimarySuffix suffix = (ASTPrimarySuffix) i.next();
				FSMMachineInstance fsmInst = fsm.creatInstance();
				fsmInst.setRelatedObject(new FSMRelatedCalculation(suffix));
				fsmInst.setResultString("J2EE program uses getConnction()");
				list.add(fsmInst);
			}	
			
			/**
			 * 3 ��J2EE������ʹ��Socket
			 * Socket s = new Socket();
			 */
			xpath = ".//LocalVariableDeclaration/Type[@TypeImage='Socket']";
			evalRlts = node.findXpath(xpath);
			i = evalRlts.iterator();
			
			while(i.hasNext())
			{
				ASTType type = (ASTType) i.next();
				FSMMachineInstance fsmInst = fsm.creatInstance();
				fsmInst.setRelatedObject(new FSMRelatedCalculation(type));
				fsmInst.setResultString("J2EE program uses Socket");
				list.add(fsmInst);
			}
			
			/**
			 * 4 ��J2EE������ʹ��Socket
			 * g(Socket s)
			 */
			xpath = ".//FormalParameter/Type[@TypeImage='Socket']";
			evalRlts = node.findXpath(xpath);
			i = evalRlts.iterator();
			
			while(i.hasNext())
			{
				ASTType type = (ASTType) i.next();
				FSMMachineInstance fsmInst = fsm.creatInstance();
				fsmInst.setRelatedObject(new FSMRelatedCalculation(type));
				fsmInst.setResultString("J2EE program uses Socket");
				list.add(fsmInst);
			}
			
			/**
			 * 5 ��J2EE������ʹ��ServerSocket
			 * ServerSocket s = new ServerSocket();
			 */
			xpath = ".//LocalVariableDeclaration/Type[@TypeImage='ServerSocket']";
			evalRlts = node.findXpath(xpath);
			i = evalRlts.iterator();
			
			while(i.hasNext())
			{
				ASTType type = (ASTType) i.next();
				FSMMachineInstance fsmInst = fsm.creatInstance();
				fsmInst.setRelatedObject(new FSMRelatedCalculation(type));
				fsmInst.setResultString("J2EE program uses ServerSocket");
				list.add(fsmInst);
			}	
			
			/**
			 * 6 ��J2EE������ʹ��ServerSocket
			 * f(ServerSocket s)
			 */
			xpath = ".//FormalParameter/Type[@TypeImage='ServerSocket']";
			evalRlts = node.findXpath(xpath);
			i = evalRlts.iterator();
			
			while(i.hasNext())
			{
				ASTType type = (ASTType) i.next();
				FSMMachineInstance fsmInst = fsm.creatInstance();
				fsmInst.setRelatedObject(new FSMRelatedCalculation(type));
				fsmInst.setResultString("J2EE program uses ServerSocket");
				list.add(fsmInst);
			}
		}

		
		return list;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{

	}

}
