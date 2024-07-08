package softtest.rules.java;

import softtest.fsm.java.*;
import softtest.jaxen.java.DocumentNavigator;

import java.util.*;

import org.jaxen.*;

import softtest.ast.java.*;
import softtest.jaxen.java.*;
import softtest.ast.java.ASTClassOrInterfaceDeclaration;
import softtest.ast.java.ASTFieldDeclaration;
import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.ASTVariableDeclaratorId;
import softtest.ast.java.SimpleJavaNode;
import softtest.cfg.java.*;
import softtest.config.java.Config;


/**  五  (一)命名规则不规范
2. 分不清方法名字
【例5-2】 下列程序：
   1   public int getValue（）{}
   2   public int GetValue() {}
方法在名字上与其他方法的不同仅仅是以大写字母开头。

3. 方法名字与构造器名字相同
【例5-3】 下列程序：
   1   public class Example_314 {
   2   		String name;
   3   		// constructor
   4   		public Example_314() {
   5   		}
   6   		// this method has a constructor name, but not a constructor
   7  		public void Example_314(String name) {
   8   			this.name = name;
   9   		}
   10   } 
方法的名字与构造器的名字相同，但是这些方法不是构造器因为它们有返回值。

2008-3-29
 */

public class IRNRStateMachine {
	
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
	
	public static List<FSMMachineInstance> createIRNRStateMachines(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();

		List evalRlts = new ArrayList();
		Hashtable everyClsFields = new Hashtable();
		Hashtable everyClsMethds = new Hashtable();
		// ///////////// Process Field Member //////////////

		String xclsStr = ".//ClassOrInterfaceDeclaration";

		evalRlts = getTreeNode(node, xclsStr);
		
		Iterator i = evalRlts.iterator();
		while (i.hasNext()) {
			ASTClassOrInterfaceDeclaration astCDecl = (ASTClassOrInterfaceDeclaration) i.next();
			if( astCDecl.isInterface() ) {
				continue;
			}
			everyClsFields.put(astCDecl.getImage(), new Hashtable());
			everyClsMethds.put(astCDecl.getImage(), new Hashtable());
			logc1("add " + astCDecl.getImage() + "_" + astCDecl.getBeginLine()+"_");
		}
		String  fieldsStr = ".//FieldDeclaration";
		
		evalRlts = getTreeNode(node, fieldsStr);
		for(ASTFieldDeclaration astField : (List<ASTFieldDeclaration>)evalRlts) {
			int cnt = astField.jjtGetNumChildren();
			for(int j = 1; j < cnt; j++) {
				ASTVariableDeclaratorId astVId = (ASTVariableDeclaratorId)astField.jjtGetChild(j).jjtGetChild(0);
				//VariableNameDeclaration ndecl = astVId.getNameDeclaration();
				String clsname = getClassName(astVId);
				Hashtable  fields = (Hashtable)everyClsFields.get(clsname);
				if( fields == null ) {
					throw new RuntimeException("fields is null, strange very much.");
				}
				String fieldName = astVId.getImage();
				String lowerCase = fieldName.toLowerCase();
				
				if( fields.containsValue(lowerCase) ) {
					FSMMachineInstance fsminstance = fsm.creatInstance();
					fsminstance.setRelatedObject(new FSMRelatedCalculation(astVId));
					fsminstance.setResultString("Fields' name is similar");
					list.add(fsminstance);
				} else {
					fields.put(lowerCase, fieldName);
				}
			}
		}
		
		String  methdStr  = ".//MethodDeclaration";
		evalRlts = getTreeNode(node, methdStr);
		for(ASTMethodDeclaration astM : (List<ASTMethodDeclaration>)evalRlts) {
			//  ////////    xxXx(); xxxx; xxxx();   ////////
			//  Methd name is similar to each other, and 
			String clsName = getClassName(astM);
			Hashtable  methds = (Hashtable)everyClsMethds.get(clsName);
			if( methds == null ) {
				throw new RuntimeException("fields is null, strange very much.");
			}
			String mthdName = astM.getMethodName();
			String lowerCase = mthdName.toLowerCase();
			
			if( methds.containsKey(lowerCase) ) {
				String mthdnm = (String)methds.get(lowerCase);
				if(mthdnm.equals(mthdName)) {
					//  /////  This is a overload method
					continue;
				}
				FSMMachineInstance fsminstance = fsm.creatInstance();
				fsminstance.setRelatedObject(new FSMRelatedCalculation(astM));
				fsminstance.setResultString("Method name is similar but not the same");
				list.add(fsminstance);
			} else {
				methds.put(lowerCase, mthdName);
			}
			Hashtable  fields = (Hashtable)everyClsFields.get(clsName);
			if( fields.containsKey(lowerCase) ) {
				FSMMachineInstance fsminstance = fsm.creatInstance();
				fsminstance.setRelatedObject(new FSMRelatedCalculation(astM));
				fsminstance.setResultString("Method' name is similar to field");
				list.add(fsminstance);
			}
			
			if( clsName.toLowerCase().equals( mthdName.toLowerCase() ) ) {
				FSMMachineInstance fsminstance = fsm.creatInstance();
				fsminstance.setRelatedObject(new FSMRelatedCalculation(astM));
				fsminstance.setResultString("Method name is similar to class");
				list.add(fsminstance);
				logc1("add " + astM.getImage());
			}
		}
		
		
		return list;
	}
	
	
	public static boolean checkIRNR(VexNode vex,FSMMachineInstance fsmInst) {
		
		return true;
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
		logc("createIRNRStateMachines(..) - " + str);
	}
	public static void logc2(String str) {
		logc("checkIRNR(..) - " + str);
	}
	
	public static void logc(String str) {
		if( Config.DEBUG ) {
			System.out.println("IRNRStateMachine::" + str);
		}
	}
}
