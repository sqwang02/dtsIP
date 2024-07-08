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
	 * ���ù�������
	 * 2009.9.3
	 * baigele
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("����APIģʽ:EJB�������ʵ������ %d �п������һ��©����Υ����EJB�淶�������˲���ʹ�õ�����߷�����ȫ�Խ��͡�", errorline);
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
		 * �����û��������ļ���
		 * ������һ�Ρ�
		 */
		if(count == 0)
		{
			UserConfig userConfig = new UserConfig();
			isEJBprog = userConfig.isEJBprogram();
			count++ ;
			
		}
		/*
		 * ���ΪEJB����
		 * */
		if(isEJBprog)
		{
			String xpath = "";
			List evalRlts = null;
			Iterator i = null;
			
			/**
			 * 1 ��EJB������ʹ��AWT
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
			 * 2 ��EJB������ʹ��swing
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
			 * 3 ��EJB������ʹ��Socket
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
			 * 4 ��EJB������ʹ��Socket
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
			 * 5 ��EJB������ʹ��ServerSocket
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
			 * 6 ��EJB������ʹ��ServerSocket
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
			 * 7 ��EJB������ʹ��io
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
			 * 8 ��EJB������ʹ��ͬ��ԭ��
			 * jdk1.5��ʹ��import java.util.concurrent��
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
			 * 9 ��EJB������ʹ��ͬ��ԭ��
			 * ʹ��synchronized�ؼ�����������
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
			 * 10 ��EJB������ʹ��ͬ��ԭ��
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
			 * 11-1 ��EJB������ʹ��ͬ��ԭ��
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
			 * 11-2 ��EJB������ʹ��ͬ��ԭ��
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
			 * 11-3 ��EJB������ʹ��ͬ��ԭ��
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
			 * 12-1 ��EJB������ʹ��ͬ��ԭ��
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
			 * 12-2 ��EJB������ʹ��ͬ��ԭ��
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
			 * 12-3 ��EJB������ʹ��ͬ��ԭ��
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
			 * 13-1 ��EJB������ʹ��ͬ��ԭ��
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
			 * 13-2 ��EJB������ʹ��ͬ��ԭ��
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
			 * 13-3 ��EJB������ʹ��ͬ��ԭ��
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
			 * 14-1 ��EJB������ʹ��ClassLoader����������
			 * ʹ��java.lang.Class��forName()����
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
			 * 14-2 ��EJB������ʹ��ClassLoader����������
			 * ʹ��java.lang.ClassLoader��loadClass()����
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
