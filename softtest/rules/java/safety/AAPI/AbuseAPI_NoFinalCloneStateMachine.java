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
public class AbuseAPI_NoFinalCloneStateMachine extends AbstractStateMachine{
	/**
	 * ���ù�������
	 */
	//	CWE-580������
	//2009.9.2
	//baigele
	/**���� clone() Ӧ���� super.clone() ��ȡ�µĶ���
	 * ����clone����������Object��ʱ��������super.clone()��
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
			f.format("����APIģʽ: %d ��ʵ����clone����ȴû������Ϊfinal���������һ��©�������һ�������������Ϣ����û��final��ʹ��super.clone()�����ݿ���ͨ����¡�����ʡ�", errorline);
		}else{
			f.format("Abuse Application Program Interface: no final when implementing clone on line %d. Without final and super.clone(),the insentive information may be accessed by clone. ",errorline);
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
	
	
	
	
	public static List<FSMMachineInstance> createNoFinalCloneStateMachines(SimpleJavaNode node, FSMMachine fsm)
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
			String xpathStr=".//MethodDeclaration[@Final='false']";
			List result = findTreeNodes(node , xpathStr);
			if( result == null || result.size() == 0) {
				logc1("No final clone()");
				FSMMachineInstance fsmInst = fsm.creatInstance();
				fsmInst.setRelatedObject(new FSMRelatedCalculation(astMD));
				fsmInst.setResultString("no final clone()");
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
			System.out.println("AAPI_NoSuperCloneStateMachine::" + str);
		}
	}
}
