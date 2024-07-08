package softtest.rules.java.safety.AAPI;

import java.io.File;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import org.jaxen.BaseXPath;
import org.jaxen.JaxenException;
import org.jaxen.XPath;

import softtest.ast.java.ASTClassOrInterfaceDeclaration;
import softtest.ast.java.ASTExtendsList;
import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.ASTMethodDeclarator;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.config.java.Config;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;
import softtest.jaxen.java.DocumentNavigator;
import softtest.rules.java.AbstractStateMachine;
import softtest.rules.java.sensdt.XMLUtil;

/**       安全缺陷模型  （二） 滥用API
（2）	故障名称：不正确的finalize()方法
描述：这里指实现了finalize方法却没有调用super.finalize().如果一个超类实现器重载了超类finalizer但是忘记了调用超类finalizer，超类finalizer将永远不会被调用。这意味着对于超类的资源清除不会执行而导致资源泄漏。
举例：
public class Example_308 {
 no super.finalize() was called

 protected  void finalize() {
   System.err.println("finalized");
 }
}

2008-06-25

检测方法：
*/
public class AbuseAPI_NoSuperFinalizeStateMachine extends AbstractStateMachine{
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("滥用API模式: %d 行实现了finalize方法却没有调用super.finalize()，可能造成一个漏洞。如果一个超类实现器重载了超类finalizer但是忘记了调用超类finalizer，超类finalizer将永远不会被调用。这意味着对于超类的资源清除不会执行而导致资源泄漏", errorline);
		}else{
			f.format("Abuse Application Program Interface: missing super.finalize() when implementing finalize on line %d.",errorline);
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
	private static String escapeFilePath(String path) {
		String ret1 = path.replace('/', File.separatorChar);
		String ret2 = ret1.replace('\\', File.separatorChar);
		return ret2;
	}
	/**
	 * 保存不需要报错的父类名称，例如 Object
	 */
	public static Set<String>  clses = null;

	static {
		try{
			clses = new HashSet<String>();
			/**  get string name of classes, which can do read(char[]) operation  **/
			List<String>  ret = XMLUtil.getStrsFromFile(escapeFilePath("softtest\\rules\\java\\safety\\AAPI\\AAPI-Data"), "AAPI-Data", "ExcepClass");
			clses.addAll(ret);
		}catch(Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("Get SensInfo failed.",ex);
		}
	}

	/**
	 * 
	 * @param node
	 * @param fsm
	 * @return
	 */
	public static List<FSMMachineInstance> createNoSuperFinalizeStateMachines(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();

		if( ! (node instanceof ASTMethodDeclaration) ) {
			return list;
		}
		/**  如果没有继承某个类，则不再继续处理  **/
		SimpleJavaNode parent = (SimpleJavaNode)node.jjtGetParent(); 
		while(parent != null && ! (parent instanceof ASTClassOrInterfaceDeclaration)) {
			parent = (SimpleJavaNode)parent.jjtGetParent();
		}
		if(parent == null) {
			return list;
		} else {
			if(parent.jjtGetNumChildren() > 0 && parent.jjtGetChild(0) instanceof ASTExtendsList ) {
				ASTExtendsList  astExt = (ASTExtendsList)parent.jjtGetChild(0);
				String cname = ((SimpleJavaNode)astExt.jjtGetChild(0)).getImage();
				/**    **/
				if(clses.contains(cname)) {
					return list;
				}
			} else {
				/**  没有继承，直接返回  **/
				return list;
			}
		}
		if(node.jjtGetNumChildren() > 2 && node.jjtGetChild(1) instanceof ASTMethodDeclarator) {
			ASTMethodDeclarator astMD = (ASTMethodDeclarator)node.jjtGetChild(1);
			if ( ! astMD.getImage().equals("finalize") ) {
				return list;
			}
			String  xpathStr = ".//PrimaryPrefix[@Image=\'finalize\' and @SuperModifier=\'true\']";
			List evalRlts = findTreeNodes(node , xpathStr);
			if( evalRlts == null || evalRlts.size() == 0) {
				logc1("No .finalize() found");
				FSMMachineInstance fsmInst = fsm.creatInstance();
				fsmInst.setRelatedObject(new FSMRelatedCalculation(astMD));
				fsmInst.setResultString("no super.finalize() in finalize method");
				list.add( fsmInst );
			}
		}
		return list;
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
	

	public static void logc1(String str) {
		logc("createStateMachines(..) - " + str);
	}
	
	public static void logc2(String str) {
		logc("check(..) - " + str);
	}

	public static void logc(String str) {
		if( Config.DEBUG ) {
			System.out.println("AAPI_NoSuperFinalizeStateMachine::" + str);
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
