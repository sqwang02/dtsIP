package softtest.rules.java;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Hashtable;


import org.jaxen.BaseXPath;
import org.jaxen.JaxenException;
import org.jaxen.XPath;

import softtest.ast.java.ASTAdditiveExpression;
import softtest.ast.java.ASTAllocationExpression;
import softtest.ast.java.ASTExpression;
import softtest.ast.java.ASTLiteral;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTPrimaryExpression;
import softtest.ast.java.SimpleJavaNode;
import softtest.config.java.Config;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.jaxen.java.DocumentNavigator;
import softtest.rules.java.sensdt.SensClasses;
import softtest.rules.java.sensdt.SensInfo;
import softtest.rules.java.sensdt.XMLUtil;
import softtest.rules.java.sensdt.ExtendAlias;
import softtest.symboltable.java.VariableNameDeclaration;

/**
11. 设计信息反馈到web接口的问题  d
【例2-15】 下列程序
   1   protected void doPost(HttpServletRequest req,HttpServletResponse resp) throws ServletException,IOException {
   2   		String name = req.getParameter("userName");
   3   		File file = new File(System.getProperty("web.root"), name + ".dat");
   4   		try {
   5   			FileOutputStream str = new FileOutputStream(file);
   6   			str.close();
   7   		} catch (IOException e) {
   8   			throw new ServletException("Cannot open file " + file + ":" + e.getMessage());
   9   		}
   10   }
*/
public class DsnInfoLeakStateMachine {

	public static SensInfo        sInfo = null;
	public static SensClasses     sensClasses = null;

	static {
		try{
			sInfo = new SensInfo();
			/** Sensitive Design Info is writen in DsnInfoLeak-Data.xml */
			XMLUtil.getSensInfo("softtest\\rules\\java\\DsnInfoLeak-Data".replace('\\', File.separatorChar).replace('/', File.separatorChar), sInfo);
			
			//sInfo.dump();
		}catch(Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("Get SensInfo failed.",ex);
		}
		try{
			sensClasses = new SensClasses();
			XMLUtil.getSensClasses("softtest\\rules\\java\\DsnInfoLeak-Data".replace('\\', File.separatorChar).replace('/', File.separatorChar), sensClasses);
			
			//sensClasses.dump();
		} catch(Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("Get SensClassInfo failed.",ex);
		}
	}

	public static List<FSMMachineInstance> createSensInfoLeakStateMachines(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();

		String  xpathStr = ".//Literal[ @StringLiteral='true' ]";//".//Name[ matches(@Image, 'System') ]";
		
		List evalRlts = findTreeNodes(node , xpathStr);

		if( evalRlts == null || evalRlts.size() == 0) {
			logc1("No ASTLiter[@StringLiteral='true'] found");
		}
		Hashtable<String, String>  literals = new Hashtable<String, String>();
		for( int i = 0; i < evalRlts.size(); i++ ) {
			ASTLiteral  astLiter = (ASTLiteral) evalRlts.get(i);
			String      liter = astLiter.getImage();
			/**  "xxxx" to  xxxx   **/
			String      noquota = liter.substring(1, liter.length()-1);
			if( ! sInfo.isSensInfo( noquota ) ) {
				continue;
			}
			if( literals.containsKey(noquota) ) {
				continue;
			} else {
				literals.put(noquota, noquota);
			}
			FSMMachineInstance fsmInst;
			ExtendAlias ealias = new ExtendAlias();
			ealias.setResource( astLiter );
			ealias.setResouceName( noquota );
			
			logc1("create ExtendAlias :" + astLiter.getImage());
			
			fsmInst = fsm.creatInstance();
			fsmInst.setRelatedObject( ealias );
			list.add( fsmInst );
		}
		return list;
	}

	/** throw statements, which throws sensinfo
	 *  throw new XXXException( "" + sensivar + "" );
	 * */
	public static boolean checkSensInfoLeak(List nodes, FSMMachineInstance fsmInst) {
		logc2("------ Begin\n" + fsmInst.getStates() );
		boolean found = false;
		if(nodes == null || nodes.size() == 0) {
			System.out.println("nodes has nothing, return");
		}
		
		ExtendAlias  ealias = (ExtendAlias) fsmInst.getRelatedObject();
		ASTAllocationExpression astAlloc = (ASTAllocationExpression) nodes.get(0);
		ASTExpression astExpr = (ASTExpression)astAlloc.jjtGetChild(1).jjtGetChild(0).jjtGetChild(0);
		VariableNameDeclaration vdecl = null;
		
		if( astExpr.jjtGetChild(0) instanceof ASTAdditiveExpression ) {
			ASTAdditiveExpression  astAdd = (ASTAdditiveExpression)astExpr.jjtGetChild(0);
			for( int i = 0; i < astAdd.jjtGetNumChildren(); i++) {
				ASTPrimaryExpression astpe = (ASTPrimaryExpression)astAdd.jjtGetChild(i);
				ASTName astName = (ASTName)astpe.getSingleChildofType( ASTName.class );
				if( astName != null ) {
					if( ealias.getSensEntityTable().containsKey( astName.getNameDeclaration())) {
						vdecl = (VariableNameDeclaration)astName.getNameDeclaration();
						found = true;
					}
				}
				else if(SensClasses.isSensitive(astpe, sensClasses, ealias)) {
					found = true;
				}
			}
		} else if( astExpr.jjtGetChild(0) instanceof ASTPrimaryExpression ) {
			ASTPrimaryExpression astPrim = (ASTPrimaryExpression) astExpr.jjtGetChild(0);
			if(SensClasses.isSensitive(astPrim, sensClasses, ealias)) {
				found = true;
			}
		}
		if( found ) {
			logc2("+----------------+");
			logc2("|   DsnInfoLeak  | " + fsmInst.getRelatedObject());
			logc2("+----------------+");
			fsmInst.setResultString("SensInfoLeak:" + fsmInst.getRelatedObject().toString());
		} else {
			logc2("-_- found nothing");
		}
		logc2("------- End");
		return found;
	}




	/**  check the ExtendAlias' sensEntity is not null
	 * **/
	public static boolean checkSensEntityNotNull(List nodes, FSMMachineInstance fsmInst) {
		boolean  infect = false;
		
		return  true;
	}

	
	private static List findTreeNodes(SimpleJavaNode node, String xPath) {
		List evaluationResults = null;
		try {
			XPath xpath = new BaseXPath(xPath, new DocumentNavigator());
			evaluationResults = xpath.selectNodes(node);
		} catch (JaxenException e) {
			// e.printStackTrace();
			throw new RuntimeException("xpath error",e);
		}
		return evaluationResults;
	}
	
	/**   "Xxxx.staticMethod"
	 * */
	private static boolean  maybeClass(String str) {
		int  p = str.indexOf('.');
		if( p > 0 ) {
			char first = str.substring(0, p).charAt(0);
			if( Character.isUpperCase( first ) ) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	public static void logc1(String str) {
		logc("createSensInfoLeakStateMachines(..) - " + str);
	}
	public static void logc2(String str) {
		logc("checkSensInfoLeak(..) - " + str);
	}
	public static void logc5(String str) {
		logc("checkSensEntityNotNull(..) - " + str);
	}

	public static void logc(String str) {
		if( Config.DEBUG ) {
			System.out.println("DsnInfoLeakStateMachine::" + str);
		}
	}

	public static void main(String args[] ) {
		String  str = "System.out.println;";
		String strs[] = str.split("\\.");
		for( String s : strs ) {
			System.out.println(s);
		}
		str = "\"ab\"";
		System.out.println( str.substring(1, str.length() - 1)  );
	}
}
