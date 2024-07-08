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
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("滥用API模式:J2EE程序错误实践，在 %d 行可能造成一个漏洞。违反了J2EE规范，引入了不该使用的类或者方法安全性降低。", errorline);
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
		 * 解析用户的配置文件。
		 * 仅解析一次。
		 */
		if(count == 0)
		{
			UserConfig userConfig = new UserConfig();
			
			isJ2EEprog = userConfig.isJ2EEprogram();
			
			count++ ;
		}
		
		/*
		 * 如果为J2EE程序 
		 */
		if(isJ2EEprog)
		{
			String xpath = "";
			List evalRlts = null;
			Iterator i = null;
			
			/**
			 * 1 在J2EE程序中调用getConnection()
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
			 * 2 在J2EE程序中调用getConnection()
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
			 * 3 在J2EE程序中使用Socket
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
			 * 4 在J2EE程序中使用Socket
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
			 * 5 在J2EE程序中使用ServerSocket
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
			 * 6 在J2EE程序中使用ServerSocket
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
