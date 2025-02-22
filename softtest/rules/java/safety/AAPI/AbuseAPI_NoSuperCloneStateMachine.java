package softtest.rules.java.safety.AAPI;

import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;

import org.jaxen.BaseXPath;
import org.jaxen.JaxenException;
import org.jaxen.XPath;

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
public class AbuseAPI_NoSuperCloneStateMachine extends AbstractStateMachine{
	/**
	 * 设置故障描述
	 */
	//	CWE-580中例子
	//2009.9.2
	//baigele
	/**方法 clone() 应调用 super.clone() 获取新的对象。
	 * 调用clone方法产生新Object的时候必须调用super.clone()。
	 * Example:
	 * public class cloneExample3 {
		public static class Kibitzer  {	
	    Kibitzer(){}
		public Object clone() { 
		        Object returnMe = new Kibitzer();
		        return returnMe;
			        }
			}	
		}										
	 * */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("滥用API模式: %d 行实现了clone方法却没有调用super.clone()，可能造成一个漏洞。", errorline);
		}else{
			f.format("Abuse Application Program Interface: missing super.clone() when implementing clone on line %d",errorline);
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
	
	
	
	
	public static List<FSMMachineInstance> createNoSuperCloneStateMachines(SimpleJavaNode node, FSMMachine fsm)
	{	
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		if(! (node instanceof ASTMethodDeclaration)){
			return list;
		}
		ASTMethodDeclaration astMD =(ASTMethodDeclaration) node;
		if ( ! astMD.getMethodName().equals("clone") ) {
			return list;
		}
		else{
			String xpathStr=".//PrimaryPrefix[@Image=\'clone\' and @SuperModifier=\'true\']";
			List result = findTreeNodes(node , xpathStr);
			if( result == null || result.size() == 0) {
				logc1("No super.clone() found");
				FSMMachineInstance fsmInst = fsm.creatInstance();
				fsmInst.setRelatedObject(new FSMRelatedCalculation(astMD));
				fsmInst.setResultString("no super.clone() in clone method");
				list.add( fsmInst );
			}
		return list;
		}
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
	public static void logc(String str) {
		if( Config.DEBUG ) {
			System.out.println("AAPI_NoSuperCloneStateMachine:" + str);
		}
	}
}
