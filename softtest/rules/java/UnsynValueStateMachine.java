package softtest.rules.java;

import softtest.fsm.java.*;
import softtest.jaxen.java.DocumentNavigator;

import java.util.*;
import java.lang.StringBuffer;

import org.jaxen.*;
import softtest.ast.java.*;
import softtest.jaxen.java.*;
import softtest.symboltable.java.VariableNameDeclaration;
import softtest.ast.java.ASTClassOrInterfaceDeclaration;
import softtest.ast.java.ASTClassOrInterfaceType;
import softtest.ast.java.ASTExtendsList;
import softtest.ast.java.ASTFieldDeclaration;
import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.ASTMethodDeclarator;
import softtest.ast.java.ASTSynchronizedStatement;
import softtest.ast.java.ASTVariableDeclaratorId;
import softtest.ast.java.SimpleJavaNode;
import softtest.config.java.Config;


/**
2．值的未同步使用
在servlet中，当一些方法访问静态变量或者共享变量，而这些方法被servlet中的doPost和doGet方法调用，将造成潜在的多线程风险。

   public static class Counter extends HttpServlet {
  		static int count = 0;
   		protected void doGet(HttpServletRequest in,HttpServletResponse out) throws       ServletException,IOException {
   			out.setContentType("text/plain");
   			PrintWriter p = out.getWriter();
   			count++;
   			p.println(count + " hits so far!");
   		}
   }
 */

public class UnsynValueStateMachine {

	public static List getTreeNode(SimpleJavaNode node, String xstr) {
		List eval = null;
		try {
			XPath xpath = new BaseXPath(xstr, new DocumentNavigator());
			eval = xpath.selectNodes(node);
		} catch (JaxenException e) {
			e.printStackTrace();
			throw new RuntimeException("xpath error",e);
		}
		return eval;
	}
	
	public static List<FSMMachineInstance> createUnsynValueStateMachines(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		
		if( ! isHttpServlet(node) ) {
			logc1("" + node + " is not in Servlet");
			return list;
		}
		
		String  xpathStr = "//FieldDeclaration[@Static='true']";
		
		List evalRlts = null;
		try {
			XPath xpath = new BaseXPath(xpathStr, new DocumentNavigator());
			evalRlts = xpath.selectNodes(node);
		} catch (JaxenException e) {
			e.printStackTrace();
			throw new RuntimeException("xpath error",e);
		}
	
		if(evalRlts == null){
			logc1("No static field found");
			return list;
		}
		for( int i = 0; i < evalRlts.size(); i++ ) {
			ASTVariableDeclaratorId vDeclId = (ASTVariableDeclaratorId) ((ASTFieldDeclaration)evalRlts.get(i)).jjtGetChild(1).jjtGetChild(0);
			VariableNameDeclaration varNameDecl = vDeclId.getNameDeclaration();
			String xpathStrs [] = getVarXpath(vDeclId.getImage());
			List evalVar = null;
			for( int j = 0; j < xpathStrs.length; j++ ) {
				evalVar = getTreeNode(node, xpathStrs[j]);
				if( evalVar != null && evalVar.size() != 0 ) {
					logc1("xpathStr[" + j + "] get " + evalVar.size());
					for( int k = 0; k < evalVar.size(); k++) {
						if( ! isSynchronized( (SimpleJavaNode)evalVar.get(k)) ) {
							FSMMachineInstance fsmInst = fsm.creatInstance();
							fsmInst.setRelatedVariable( varNameDecl );
							fsmInst.setRelatedObject( new FSMRelatedCalculation((SimpleJavaNode)evalVar.get(k)) );
							list.add(fsmInst);
						} else {
							logc1("" + evalVar.get(k) + " is Synronized");
						}
					}
				} else {
					logc1("Nothing found, using ||" + xpathStrs[j]);
				}
			}
		}
		
		return list;
	}

	public static boolean checkUnsynAssign(List nodes, FSMMachineInstance fsmInst) {
		boolean found = false;
		if(nodes == null) {
			System.out.println("nodes is null, return");				
		}
		VariableNameDeclaration  v = fsmInst.getRelatedVariable();
		
		logc2("+--------------+");
		logc2("| UnsynValue   | var:" + fsmInst.getRelatedVariable() );
		logc2("+--------------+");
				
		found = true;
		return found;
	}


	private static String[]  getVarXpath(String varName) {
		final String xStrHead0 = ".//PostfixExpression[ ./PrimaryExpression/PrimaryPrefix/Name[@Image='";
		final String xStrTail0 = "' ] ]";
		final String xStrHead1 = ".//PreIncrementExpression[ ./PrimaryExpression/PrimaryPrefix/Name[@Image='";
		final String xStrTail1 = "' ] ]";
		final String xStrHead2 = ".//StatementExpression[ ./PrimaryExpression/PrimaryPrefix/Name[@Image='";
		final String xStrTail2 = "' ] and ./AssignmentOperator ]";
		String xpathStrs [] = new String[3];
		StringBuffer sb = new StringBuffer();
		sb.append(xStrHead0);
		sb.append(varName);
		sb.append(xStrTail0);
		xpathStrs[0] = sb.toString();
		
		sb = new StringBuffer();
		sb.append(xStrHead1);
		sb.append(varName);
		sb.append(xStrTail1);
		xpathStrs[1] = sb.toString();
		
		sb = new StringBuffer();
		sb.append(xStrHead2);
		sb.append(varName);
		sb.append(xStrTail2);
		xpathStrs[2] = sb.toString();
		
		return xpathStrs;
	}
	
	private static boolean   isSynchronized(SimpleJavaNode  node) {
		boolean isSyn = false;
		ASTMethodDeclaration astMethd;
		ASTSynchronizedStatement astSynStmnt;
		
		SimpleJavaNode parent = (SimpleJavaNode) node.jjtGetParent();
		while( ! isSyn ) {			
			if( parent instanceof ASTSynchronizedStatement ) {
				isSyn = true;
			} else 
			if( parent instanceof ASTMethodDeclaration ) {
				ASTMethodDeclaration mthDecl = (ASTMethodDeclaration) parent;
				if( mthDecl.isSynchronized() ) {
					isSyn = true;
				} else {
					logc("??????????  is not synchronized ::: " + ((ASTMethodDeclarator)mthDecl.jjtGetChild(1)).getImage());
					break;
				}
			}
			parent = (SimpleJavaNode) parent.jjtGetParent();
		}
		return isSyn;
	}
	
	private static boolean isHttpServlet(SimpleJavaNode  node) {
		boolean  isServlet = false;
		ASTClassOrInterfaceDeclaration clDecl;
		SimpleJavaNode parent = node;
		while( parent != null && ( ! (parent instanceof ASTClassOrInterfaceDeclaration)) ) {
			parent = (SimpleJavaNode) parent.jjtGetParent();
		}
		if( parent != null ) {
			if( parent.jjtGetChild(0) instanceof ASTExtendsList ) {
				ASTExtendsList extList = (ASTExtendsList) parent.jjtGetChild(0);
				for( int i = 0; i < extList.jjtGetNumChildren(); i++) {
					ASTClassOrInterfaceType type = (ASTClassOrInterfaceType)extList.jjtGetChild(i);
					if( type.getImage().equals("HttpServlet") ) {
						isServlet = true;
						break;
					}
				}
			} else {
				logc("isHttpServlet(..) - no ASTExtendsList");
			}
		} else {
			logc("isHttpServlet(..) - no ASTClassOrInterfaceDeclaration node :" + node);
		}
		return isServlet;
	}
	
	public static void logc1(String str) {
		logc("createUnsynValueStateMachines(..) - " + str);
	}
	public static void logc2(String str) {
		logc("checkUnsynAssign(..) - " + str);
	}
	
	public static void logc(String str) {
		if( Config.DEBUG ) {
			System.out.println("UnsynValueStateMachine::" + str);
		}
	}
	
	public static void main(String args[] ) {
		String [] re = getVarXpath("var");
		for( String i : re ) {
			System.out.println( i);
		}
	}
}
