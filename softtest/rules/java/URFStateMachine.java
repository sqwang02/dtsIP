package softtest.rules.java;

import softtest.fsm.java.*;
import softtest.jaxen.java.DocumentNavigator;

import java.util.*;

import org.jaxen.*;

import softtest.ast.java.*;
import softtest.jaxen.java.*;
import softtest.symboltable.java.MethodNameDeclaration;
import softtest.symboltable.java.NameDeclaration;
import softtest.symboltable.java.VariableNameDeclaration;
import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.ASTMethodDeclarator;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTPrimaryExpression;
import softtest.ast.java.SimpleJavaNode;
import softtest.cfg.java.*;
import softtest.config.java.Config;


/**
三 性能缺陷模型
{二}多余函数
2. 没有必要方法调用
【例3-12】 下列程序：
      String square(String x) {
      		try {
      			int y = Integer.parseInt(x.toLowerCase());
      			return y * y + "";
      		} catch (NumberFormatException e) {
      			e.printStackTrace();
      			System.exit(1);
      			return "";
      		}
      }
在异常处理块中，调用System.exit()是没有必要的，应抛出异常。


2008-4
 */

public class URFStateMachine {
	
	public static List<FSMMachineInstance> createURFStateMachines(SimpleJavaNode node, FSMMachine fsm) 
	{
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();

		
		////////////////////   没有必要方法调用    //////////////////
		ASTMethodDeclaration astMDec = null;
		if( !(node instanceof ASTMethodDeclaration) )
		{
			return list;
		} 
		astMDec = (ASTMethodDeclaration) node;
		
		List evalRlts = new ArrayList();
		String fieldstr = ".//CatchStatement//PrimaryExpression[ ./PrimaryPrefix/Name[@Image='System.exit' ] ]";
		try 
		{
			XPath xpath = new BaseXPath(fieldstr, new DocumentNavigator());
			evalRlts = xpath.selectNodes(node);
		} 
		catch (JaxenException e) {
			if (softtest.config.java.Config.DEBUG) 
			{
				e.printStackTrace();
			}
			throw new RuntimeException("xpath error",e);
		}
		Iterator i = evalRlts.iterator();
		while( i.hasNext())
		{
			
			FSMMachineInstance fsminstance = fsm.creatInstance();
			fsminstance.setRelatedObject(new FSMRelatedCalculation((SimpleJavaNode)i.next()));
			fsminstance.setResultString("call " + "URF-Catch call System.exit" + " in synchronized");
			list.add(fsminstance);
		}

		///////////////////////////////////////////////////
		
		if( !(node instanceof ASTMethodDeclaration) )
		{
			return list;
		} 
		astMDec = null;
		astMDec = (ASTMethodDeclaration) node;
		
		evalRlts = new ArrayList();
		//String fieldstr = ".//PrimaryExpression[ ./PrimaryPrefix/Name[@Image='Math.abs' ] ]";
		fieldstr = ".//PrimaryExpression[ ./PrimaryPrefix/Name[@Image='Math.abs' ] ]/PrimarySuffix[.//Literal]";
		
		try 
		{
			XPath xpath = new BaseXPath(fieldstr, new DocumentNavigator());
			evalRlts = xpath.selectNodes(node);
		} 
		catch (JaxenException e) {
			if (softtest.config.java.Config.DEBUG) 
			{
				e.printStackTrace();
			}
			throw new RuntimeException("xpath error",e);
		}
		i = evalRlts.iterator();
		while( i.hasNext())
		{
			FSMMachineInstance fsminstance = fsm.creatInstance();
			fsminstance.setRelatedObject(new FSMRelatedCalculation((SimpleJavaNode)i.next()));
			fsminstance.setResultString("call " + "URF-Parament is constance" );
			list.add(fsminstance);
			
		}

		///////////////////////////////////////////////////////
		
		if(!(node instanceof ASTMethodDeclaration) )
		{
			return list;
		} 
		
		ASTMethodDeclaration astMDecl = null;
		astMDecl = (ASTMethodDeclaration)node;
		
		
		evalRlts = new ArrayList();
		fieldstr = ".//PrimaryExpression/PrimaryPrefix[./Name[matches(@Image,'\\.toString$')] ]";
		try 
		{
			XPath xpath = new BaseXPath(fieldstr, new DocumentNavigator());
			evalRlts = xpath.selectNodes(node);
		} 
		catch (JaxenException e) {
			if (softtest.config.java.Config.DEBUG) 
			{
				e.printStackTrace();
			}
			throw new RuntimeException("xpath error",e);
		}
		i = evalRlts.iterator();
		while (i.hasNext()) 
		{
			SimpleJavaNode prefix = (SimpleJavaNode) i.next();
				
			ASTName name = (ASTName)prefix.jjtGetChild(0);
			List ndecls = name.getNameDeclarationList();
			
			NameDeclaration ndecl = (NameDeclaration)ndecls.get( ndecls.size() - 1 ); //declaration 符号表
			//a.b.c.d.toString() 判断.toString() 之前的变量是否是String类型的就好了。
			//System.out.println(":::" + ((VariableNameDeclaration)ndecl).getTypeImage());
				
			if(((VariableNameDeclaration)ndecl).getTypeImage().equals("String")); 
			{
				FSMMachineInstance fsminstance = fsm.creatInstance();
				fsminstance.setRelatedObject(new FSMRelatedCalculation(name));
				fsminstance.setResultString("call " +  " in URF StrtoStr");
				list.add(fsminstance);
			}
		}
		
		
		/////////////////////////////////////////////////////////
		
		astMDecl = null;
		if(!(node instanceof ASTMethodDeclaration) )
		{
			return list;
		} 
		astMDecl = (ASTMethodDeclaration)node;
		if( ! astMDecl.isSynchronized()) 
		{
			return list;
		}
		evalRlts = new ArrayList();
		fieldstr = ".//PrimaryExpression";
		try 
		{
			XPath xpath = new BaseXPath(fieldstr, new DocumentNavigator());
			evalRlts = xpath.selectNodes(node);
		} 
		catch (JaxenException e) {
			if (softtest.config.java.Config.DEBUG) 
			{
				e.printStackTrace();
			}
			throw new RuntimeException("xpath error",e);
		}
		i = evalRlts.iterator();
		while (i.hasNext()) 
		{
			ASTPrimaryExpression astPrimExpr = (ASTPrimaryExpression) i.next();
			if(astPrimExpr.jjtGetNumChildren() > 1) 
			{
				SimpleJavaNode prefix = (SimpleJavaNode)astPrimExpr.jjtGetChild(0);
				if(prefix.jjtGetNumChildren() > 0 && prefix.jjtGetChild(0) instanceof ASTName) 
				{
					ASTName name = (ASTName)prefix.jjtGetChild(0);
					List ndecls = name.getNameDeclarationList();
					for(int k = 0; k < ndecls.size(); k++)
					{
						NameDeclaration ndecl = (NameDeclaration)ndecls.get(k); //declaration 符号表
						if(ndecl instanceof MethodNameDeclaration ) 
						{
							MethodNameDeclaration mdecl = (MethodNameDeclaration)ndecl;
							ASTMethodDeclarator astMDtor = mdecl.getMethodNameDeclaratorNode();
							ASTMethodDeclaration astMD = (ASTMethodDeclaration)astMDtor.jjtGetParent();
							if(astMD.isSynchronized()) 
							{
								FSMMachineInstance fsminstance = fsm.creatInstance();
								fsminstance.setRelatedObject(new FSMRelatedCalculation(name));
								fsminstance.setResultString("call " + astMD.toString() + " in synchronized");
								list.add(fsminstance);
							}
						}
					}
				}
			}
			
		}
		
		return list;
	}
	
	
	public static boolean checkURF(VexNode vex,FSMMachineInstance fsmInst) {

		return true;
	}
	
		
	public static void logc1(String str) {
		logc("createURFStateMachines(..) - " + str);
	}
	public static void logc2(String str) {
		logc("checkURF(..) - " + str);
	}
	
	public static void logc(String str) {
		if( Config.DEBUG ) {
			System.out.println("URFStateMachine::" + str);
		}
	}
}
