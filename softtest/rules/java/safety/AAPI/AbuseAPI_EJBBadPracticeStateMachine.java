package softtest.rules.java.safety.AAPI;

import java.util.Formatter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import softtest.rules.java.AbstractStateMachine;
import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTImportDeclaration;
import softtest.ast.java.ASTPrimarySuffix;
import softtest.ast.java.ASTSynchronizedStatement;
import softtest.ast.java.ASTType;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;

public class AbuseAPI_EJBBadPracticeStateMachine extends AbstractStateMachine {

	private static int count = 0;
	private static boolean isEJBprog = false;
	/**
	 * 设置故障描述
	 * 2009.9.3
	 * baigele
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("滥用API模式:EJB程序错误实践，在 %d 行可能造成一个漏洞。违反了EJB规范，引入了不该使用的类或者方法安全性降低。", errorline);
		}else{
			f.format("Abuse Application Program Interface: EJB Bad Practice on line %d.Break the rules of EJB, use the wrong classes or method.",errorline);
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
	public static List<FSMMachineInstance> createEJBBadPracticeStateMachine(SimpleJavaNode node, FSMMachine fsm) {
		
		//ASTImportDeclaration parent = (ASTImportDeclaration) node.getSingleParentofType(ASTImportDeclaration.class);
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();	
		/*
		 * 解析用户的配置文件。
		 * 仅解析一次。
		 */
		if(count == 0)
		{
			UserConfig userConfig = new UserConfig();
			isEJBprog = userConfig.isEJBprogram();
			count++ ;
			
		}
		/*
		 * 如果为EJB程序
		 * */
		if(isEJBprog)
		{
			String xpath = "";
			List evalRlts = null;
			Iterator i = null;
			
			/**
			 * 1 在EJB程序中使用AWT
			 */
			xpath = ".//ImportDeclaration[@PackageName='java.awt']";
			evalRlts = node.findXpath(xpath);
			i = evalRlts.iterator();
			
			while(i.hasNext())
			{
				ASTImportDeclaration name = (ASTImportDeclaration) i.next();
				FSMMachineInstance fsmInst = fsm.creatInstance();
				fsmInst.setRelatedObject(new FSMRelatedCalculation(name));
				fsmInst.setResultString("EJB program uses AWT");
				list.add(fsmInst);
			}	
			
			/**
			 * 2 在EJB程序中使用swing
			 */
			xpath = ".//ImportDeclaration[@PackageName='javax.swing']";
			
			evalRlts = node.findXpath(xpath);
			i = evalRlts.iterator();
			
			while(i.hasNext())
			{
				ASTImportDeclaration name = (ASTImportDeclaration) i.next();
				FSMMachineInstance fsmInst = fsm.creatInstance();
				fsmInst.setRelatedObject(new FSMRelatedCalculation(name));
				fsmInst.setResultString("EJB program uses swing");
				list.add(fsmInst);
			}	
			
			/**
			 * 3 在EJB程序中使用Socket
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
				fsmInst.setResultString("EJB program uses Socket");
				list.add(fsmInst);
			}
			
			/**
			 * 4 在EJB程序中使用Socket
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
				fsmInst.setResultString("EJB program uses Socket");
				list.add(fsmInst);
			}
			
			/**
			 * 5 在EJB程序中使用ServerSocket
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
				fsmInst.setResultString("EJB program uses ServerSocket");
				list.add(fsmInst);
			}	
			
			/**
			 * 6 在EJB程序中使用ServerSocket
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
				fsmInst.setResultString("EJB program uses ServerSocket");
				list.add(fsmInst);
			}
			/**
			 * 7 在EJB程序中使用io
			 */
			xpath = ".//ImportDeclaration[@PackageName='java.io']";
			
			evalRlts = node.findXpath(xpath);
			i = evalRlts.iterator();
			
			while(i.hasNext())
			{
				ASTImportDeclaration name = (ASTImportDeclaration) i.next();
				FSMMachineInstance fsmInst = fsm.creatInstance();
				fsmInst.setRelatedObject(new FSMRelatedCalculation(name));
				fsmInst.setResultString("EJB program uses io");
				list.add(fsmInst);
			}	
			/**
			 * 8 在EJB程序中使用同步原语
			 * jdk1.5中使用import java.util.concurrent包
			 */
			xpath = ".//ImportDeclaration[@PackageName='java.util.concurrent']";
			
			evalRlts = node.findXpath(xpath);
			i = evalRlts.iterator();
			
			while(i.hasNext())
			{
				ASTImportDeclaration name = (ASTImportDeclaration) i.next();
				FSMMachineInstance fsmInst = fsm.creatInstance();
				fsmInst.setRelatedObject(new FSMRelatedCalculation(name));
				fsmInst.setResultString("EJB program uses Synchronization Primitives concurrent");
				list.add(fsmInst);
			}	
			/**
			 * 9 在EJB程序中使用同步原语
			 * 使用synchronized关键字声明方法
			 */
			xpath = ".//MethodDeclaration[@Synchronized='true']";
			evalRlts = node.findXpath(xpath);
			i = evalRlts.iterator();
			
			while(i.hasNext())
			{
				
				ASTMethodDeclaration type = (ASTMethodDeclaration) i.next();
				FSMMachineInstance fsmInst = fsm.creatInstance();
				fsmInst.setRelatedObject(new FSMRelatedCalculation(type));
				fsmInst.setResultString("EJB program uses synchronized declared the method");
				list.add(fsmInst);
			
			}	
			
			
			/**
			 * 10 在EJB程序中使用同步原语
			 * synchronized Statement
			 */
			xpath = ".//SynchronizedStatement";
			evalRlts = node.findXpath(xpath);
			i = evalRlts.iterator();
			
			while(i.hasNext())
			{
				ASTSynchronizedStatement type = (ASTSynchronizedStatement) i.next();
				FSMMachineInstance fsmInst = fsm.creatInstance();
				fsmInst.setRelatedObject(new FSMRelatedCalculation(type));
				fsmInst.setResultString("EJB program uses synchronized");
				list.add(fsmInst);
			}	
			/**
			 * 11-1 在EJB程序中使用同步原语
			 * wait()
			 */
			xpath = ".//PrimaryPrefix/Name[@Image='wait']";
			evalRlts = node.findXpath(xpath);
			i = evalRlts.iterator();
			
			while(i.hasNext())
			{
				ASTName name = (ASTName) i.next();
				FSMMachineInstance fsmInst = fsm.creatInstance();
				fsmInst.setRelatedObject(new FSMRelatedCalculation(name));
				fsmInst.setResultString("EJB program uses wait()");
				list.add(fsmInst);
			}
			/**
			 * 11-2 在EJB程序中使用同步原语
			 * this.wait()
			 */
			xpath = ".//PrimaryExpression/PrimarySuffix[@Image='wait']";
			evalRlts = node.findXpath(xpath);
			i = evalRlts.iterator();
			
			while(i.hasNext())
			{
				ASTPrimarySuffix type = (ASTPrimarySuffix) i.next();
				FSMMachineInstance fsmInst = fsm.creatInstance();
				fsmInst.setRelatedObject(new FSMRelatedCalculation(type));
				fsmInst.setResultString("EJB program uses this.wait()");
				list.add(fsmInst);
			}
			/**
			 * 11-3 在EJB程序中使用同步原语
			 * class.wait()
			 */
			xpath = ".//PrimaryExpression/PrimaryPrefix/Name[matches(@TypeString,\"java.lang.Object.wait()\")]";
			evalRlts = node.findXpath(xpath);
			i = evalRlts.iterator();
			while(i.hasNext())
			{
				ASTName name = (ASTName) i.next();
					FSMMachineInstance fsmInst = fsm.creatInstance();
					fsmInst.setRelatedObject(new FSMRelatedCalculation(name));
					fsmInst.setResultString("EJB program uses object.wait()");
					list.add(fsmInst);
			}
			
			/**
			 * 12-1 在EJB程序中使用同步原语
			 * notify()
			 */
			xpath = ".//PrimaryPrefix/Name[@Image='notify']";
			evalRlts = node.findXpath(xpath);
			i = evalRlts.iterator();
			
			while(i.hasNext())
			{
				ASTName name = (ASTName) i.next();
				FSMMachineInstance fsmInst = fsm.creatInstance();
				fsmInst.setRelatedObject(new FSMRelatedCalculation(name));
				fsmInst.setResultString("EJB program uses notify()");
				list.add(fsmInst);
			}
			/**
			 * 12-2 在EJB程序中使用同步原语
			 * this.notify()
			 */
			xpath = ".//PrimaryExpression/PrimarySuffix[@Image='notify']";
			evalRlts = node.findXpath(xpath);
			i = evalRlts.iterator();
			
			while(i.hasNext())
			{
				ASTPrimarySuffix type = (ASTPrimarySuffix) i.next();
				FSMMachineInstance fsmInst = fsm.creatInstance();
				fsmInst.setRelatedObject(new FSMRelatedCalculation(type));
				fsmInst.setResultString("EJB program uses x.notify()");
				list.add(fsmInst);
			}
			/**
			 * 12-3 在EJB程序中使用同步原语
			 * class.notify()
			 */
			xpath = ".//PrimaryExpression/PrimaryPrefix/Name[matches(@TypeString,\"java.lang.Object.notify()\")]";
			evalRlts = node.findXpath(xpath);
			i = evalRlts.iterator();
			while(i.hasNext())
			{
				ASTName name = (ASTName) i.next();
					FSMMachineInstance fsmInst = fsm.creatInstance();
					fsmInst.setRelatedObject(new FSMRelatedCalculation(name));
					fsmInst.setResultString("EJB program uses object.notify()");
					list.add(fsmInst);
			}
			/**
			 * 13-1 在EJB程序中使用同步原语
			 * notifyAll()
			 */
			xpath = ".//PrimaryPrefix/Name[@Image='notifyAll']";
			evalRlts = node.findXpath(xpath);
			i = evalRlts.iterator();
			
			while(i.hasNext())
			{
				ASTName name = (ASTName) i.next();
				FSMMachineInstance fsmInst = fsm.creatInstance();
				fsmInst.setRelatedObject(new FSMRelatedCalculation(name));
				fsmInst.setResultString("EJB program uses notifyAll()");
				list.add(fsmInst);
			}
			/**
			 * 13-2 在EJB程序中使用同步原语
			 * this.notifyAll()
			 */
			xpath = ".//PrimaryExpression/PrimarySuffix[@Image='notifyAll']";
			evalRlts = node.findXpath(xpath);
			i = evalRlts.iterator();
			
			while(i.hasNext())
			{
				ASTPrimarySuffix type = (ASTPrimarySuffix) i.next();
				FSMMachineInstance fsmInst = fsm.creatInstance();
				fsmInst.setRelatedObject(new FSMRelatedCalculation(type));
				fsmInst.setResultString("EJB program uses this.notifyAll()");
				list.add(fsmInst);
			}
			/**
			 * 13-3 在EJB程序中使用同步原语
			 * class.notifyAll()
			 */
			xpath = ".//PrimaryExpression/PrimaryPrefix/Name[matches(@TypeString,\"java.lang.Object.notifyAll()\")]";
			evalRlts = node.findXpath(xpath);
			i = evalRlts.iterator();
			while(i.hasNext())
			{
				ASTName name = (ASTName) i.next();
					FSMMachineInstance fsmInst = fsm.creatInstance();
					fsmInst.setRelatedObject(new FSMRelatedCalculation(name));
					fsmInst.setResultString("EJB program uses object.notifyAll()");
					list.add(fsmInst);
			}
			/**
			 * 14-1 在EJB程序中使用ClassLoader创建加载器
			 * 使用java.lang.Class中forName()方法
			 */
			xpath = ".//PrimaryExpression/PrimaryPrefix/Name[matches(@TypeString,\"java.lang.Class\")]";
			evalRlts = node.findXpath(xpath);
			i = evalRlts.iterator();
			while(i.hasNext())
			{
				ASTName name = (ASTName) i.next();
					FSMMachineInstance fsmInst = fsm.creatInstance();
					fsmInst.setRelatedObject(new FSMRelatedCalculation(name));
					fsmInst.setResultString("EJB program uses java.lang.Class load class");
					list.add(fsmInst);
			}
			/**
			 * 14-2 在EJB程序中使用ClassLoader创建加载器
			 * 使用java.lang.ClassLoader中loadClass()方法
			 */
			xpath = ".//PrimaryExpression/PrimaryPrefix/Name[matches(@TypeString,\"java.lang.ClassLoader\")]";
			evalRlts = node.findXpath(xpath);
			i = evalRlts.iterator();
			while(i.hasNext())
			{
				ASTName name = (ASTName) i.next();
					FSMMachineInstance fsmInst = fsm.creatInstance();
					fsmInst.setRelatedObject(new FSMRelatedCalculation(name));
					fsmInst.setResultString("EJB program uses java.lang.classLoader load class");
					list.add(fsmInst);
			}
			
		}
		return list;
	}
	
	public static void main(String[] args)
	{
	
	}

}
