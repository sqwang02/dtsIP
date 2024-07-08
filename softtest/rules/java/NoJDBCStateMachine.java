package softtest.rules.java;

import softtest.fsm.java.*;
import softtest.jaxen.java.DocumentNavigator;

import java.util.*;

import org.jaxen.*;
import softtest.ast.java.*;
import softtest.jaxen.java.*;
import softtest.ast.java.SimpleJavaNode;
import softtest.config.java.Config;


/**
9. JDBC连接问题     
应该避免调用DriverManager.getConnection(...)。
【例2-13】 下列程序
   1   protected void doPost(HttpServletRequest req,HttpServletResponse resp) throws ServletException {
   2   		String name = req.getParameter("userName");
   3   		String pswd = req.getParameter("passwd");
   4   		Connection conn = doGetConnection(name, pswd);
   5   }
   6   private Connection doGetConnection(String name,String pswd) {
   7   		Connection conn = null;
   8   		try {
   9   			return DriverManager.getConnection("localhost:3300",name, pswd);
   10   	} catch (SQLException e) {
   11   	}
   12   	return conn;
   13   }

2008-3-13
 */

public class NoJDBCStateMachine {

	public static List<FSMMachineInstance> createNoJDBCStateMachines(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		String  xpathStr = ".//Name[@Image='DriverManager.getConnection']";
		
		List evalRlts = null;
		try {
			XPath xpath = new BaseXPath(xpathStr, new DocumentNavigator());
			evalRlts = xpath.selectNodes(node);
		} catch (JaxenException e) {
			e.printStackTrace();
			throw new RuntimeException("xpath error",e);
		}
		if( evalRlts != null && evalRlts.size() > 0 ) {
			for(int i = 0; i < evalRlts.size(); i++) {
				FSMMachineInstance fsmInst = fsm.creatInstance();
				fsmInst.setRelatedObject(new FSMRelatedCalculation((SimpleJavaNode)evalRlts.get(i)));
				list.add(fsmInst);
			}
		}
		return list;
	}

	
	public static boolean checkNoJDBC(List nodes, FSMMachineInstance fsmInst) {
		boolean found = false;
		if(nodes == null) {
			System.out.println("nodes is null, return");				
		}
		
		logc2("+-------------+");
		logc2("| checkNoJDBC | :" + fsmInst.getRelatedObject() );
		logc2("+-------------+");
		found = true;
		fsmInst.setResultString("Using DriverManager.getConnection directly is not suggested.");
		
		return found;
	}

	

	public static void logc1(String str) {
		logc("createNoJDBCStateMachines(..) - " + str);
	}
	public static void logc2(String str) {
		logc("checkNoJDBC(..) - " + str);
	}
	
	public static void logc(String str) {
		if( Config.DEBUG ) {
			System.out.println("NoJDBCStateMachine::" + str);
		}
	}
}
