package softtest.rules.java;

import softtest.fsm.java.*;

import java.util.*;
import softtest.ast.java.*;
import softtest.ast.java.ASTClassOrInterfaceDeclaration;
import softtest.ast.java.ASTClassOrInterfaceType;
import softtest.ast.java.ASTExtendsList;
import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.ASTMethodDeclarator;
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

public class WebMainStateMachine {

	public static List<FSMMachineInstance> createWebMainStateMachines(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		
		if( ! isServlet(node) ) {
			logc1("" + node + " is not in Servlet");
			return list;
		}
		
		String  xpathStr = ".//MethodDeclarator[@Image='main']";
		
		/*List evalRlts = null;
		try {
			XPath xpath = new BaseXPath(xpathStr, new DocumentNavigator());
			evalRlts = xpath.selectNodes(node);
		} catch (JaxenException e) {
			e.printStackTrace();
			throw new RuntimeException("xpath error");
		}*/
		SimpleJavaNode pt = node;
		while( pt != null && !(pt instanceof ASTMethodDeclaration) ) {
			pt = (SimpleJavaNode) pt.jjtGetParent();
		}
		if( pt == null ) {
			return list;
		}
		ASTMethodDeclaration astMDecl = (ASTMethodDeclaration) pt;
		ASTMethodDeclarator  astMDecltor = (ASTMethodDeclarator) pt.jjtGetChild(1);
		
		/**  if the method is not static, return  **/
		if( ! astMDecl.isStatic() ) {
			return list;
		}
		FSMMachineInstance fsmInst = fsm.creatInstance();
		fsmInst.setRelatedObject( new FSMRelatedCalculation( astMDecl ) );
		list.add( fsmInst );
		
		return list;
	}

	public static boolean checkMainMethod(List nodes, FSMMachineInstance fsmInst) {
		boolean found = false;
		if(nodes == null) {
			System.out.println("nodes is null, return");				
		}
		logc2("+-----------------+");
		logc2("|  WebMainMethod  |");
		logc2("+-----------------+");
		fsmInst.setResultString("There should be no main method in web application.");
		found = true;
		return found;
	}


	private static boolean isServlet(SimpleJavaNode  node) {
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
					if( type.getImage().matches(".*Servlet") ) {
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
		logc("createWebMainStateMachines(..) - " + str);
	}
	public static void logc2(String str) {
		logc("checkMainMethod(..) - " + str);
	}
	
	public static void logc(String str) {
		if( Config.DEBUG ) {
			System.out.println("WebMainStateMachine::" + str);
		}
	}
	
	public static void main(String args[] ) {
		//String [] re = getVarXpath("var");
		//for( String i : re ) {
			System.out.println( "abcde".matches(".*bc.*") );
		//}
		
	}
}
