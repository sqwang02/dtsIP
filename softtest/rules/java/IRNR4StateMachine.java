package softtest.rules.java;

import softtest.fsm.java.*;
import softtest.jaxen.java.DocumentNavigator;

import java.util.*;

import org.jaxen.*;

import softtest.ast.java.*;
import softtest.jaxen.java.*;
import softtest.ast.java.ASTClassOrInterfaceDeclaration;
import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.SimpleJavaNode;
import softtest.cfg.java.*;
import softtest.config.java.Config;
import softtest.rules.java.sensdt.*;


/**  五  (一)命名规则不规范
4. 方法的可疑命名
【例5-4】 下列程序：
   1   public class Example_313 {
   2   		// method name might contain a typing error
   3   		public String tostring() {
   4  	 		return "Some string";
   5   		}
   6   }
类中定义的方法的名字同Java标准库中常用的方法名除了大小写不同外其他相同，这意味
着程序员可能不小心写错了。

2008-3-31
 */

public class IRNR4StateMachine {
	
	private static Hashtable  db = new Hashtable();
	
	static {
		try{
			XMLUtil.fillSuspicious("softtest\\rules\\java\\IRNR-Data.xml", db);
		} catch(Exception ex) {
			if(Config.DEBUG) {
				ex.printStackTrace();
			}
		}
	}
	
	private static List getTreeNode(SimpleJavaNode node, String xStr) {
		List evalRlts = new ArrayList();
		try {
			XPath xpath = new BaseXPath(xStr, new DocumentNavigator());
			evalRlts = xpath.selectNodes(node);
		} catch (JaxenException e) {
			if (softtest.config.java.Config.DEBUG) {
				e.printStackTrace();
			}
			throw new RuntimeException("xpath error",e);
		}
		return evalRlts;
	}
	
	public static List<FSMMachineInstance> createIRNR4StateMachines(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();

		List evalRlts = new ArrayList();

		// /////////////  Process Method Declaration  //////////////

		String xclsStr = ".//MethodDeclaration";

		evalRlts = getTreeNode(node, xclsStr);
		
		Iterator i = evalRlts.iterator();
		while (i.hasNext()) {
			ASTMethodDeclaration astMDecl = (ASTMethodDeclaration)i.next(); 
			if( isSuspicious(astMDecl.getMethodName()) ) {
				FSMMachineInstance fsminstance = fsm.creatInstance();
				fsminstance.setRelatedObject(new FSMRelatedCalculation(astMDecl));
				fsminstance.setResultString("Method name is suspicious, may be " + db.get(astMDecl.getMethodName().toLowerCase()));
				list.add(fsminstance);
				logc1("add " + astMDecl.getImage());
			}
		}
		return list;
	}
	
	
	public static boolean checkIRNR4(VexNode vex,FSMMachineInstance fsmInst) {
		
		return true;
	}
	
	
	private static boolean  isSuspicious(String  name) {
		String lower = name.toLowerCase();
		if( db.containsKey(lower) ) {
			String dbstr = (String)db.get(lower);
			if( ! dbstr.equals(name) ) {
				return true;
			}
		}
		return  false;
	}
	
	private static String  getClassName(SimpleJavaNode cur) {
		SimpleJavaNode  parent = cur;
		while( parent != null && !(parent instanceof ASTClassOrInterfaceDeclaration) ) {
			parent = (SimpleJavaNode)parent.jjtGetParent();
		}
		if(parent == null) {
			throw  new RuntimeException(cur + " has no class contained");
		}
		return ((ASTClassOrInterfaceDeclaration)parent).getImage();
	}
		
	public static void logc1(String str) {
		logc("createStateMachines(..) - " + str);
	}
	public static void logc2(String str) {
		logc("checkIRNR4(..) - " + str);
	}
	
	public static void logc(String str) {
		if( Config.DEBUG ) {
			System.out.println("IRNR4StateMachine::" + str);
		}
	}
}
