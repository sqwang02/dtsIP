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

/**       ��ȫȱ��ģ��  ������ ����API
��2��	�������ƣ�����ȷ��finalize()����
����������ָʵ����finalize����ȴû�е���super.finalize().���һ������ʵ���������˳���finalizer���������˵��ó���finalizer������finalizer����Զ���ᱻ���á�����ζ�Ŷ��ڳ������Դ�������ִ�ж�������Դй©��
������
public class Example_308 {
 no super.finalize() was called

 protected  void finalize() {
   System.err.println("finalized");
 }
}

2008-06-25

��ⷽ����
*/
public class AbuseAPI_NoSuperFinalizeStateMachine extends AbstractStateMachine{
	/**
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("����APIģʽ: %d ��ʵ����finalize����ȴû�е���super.finalize()���������һ��©�������һ������ʵ���������˳���finalizer���������˵��ó���finalizer������finalizer����Զ���ᱻ���á�����ζ�Ŷ��ڳ������Դ�������ִ�ж�������Դй©", errorline);
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
	 * ���治��Ҫ����ĸ������ƣ����� Object
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
		/**  ���û�м̳�ĳ���࣬���ټ�������  **/
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
				/**  û�м̳У�ֱ�ӷ���  **/
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
