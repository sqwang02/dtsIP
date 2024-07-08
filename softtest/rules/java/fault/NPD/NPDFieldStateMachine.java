package softtest.rules.java.fault.NPD;
import softtest.ast.java.ASTMethodDeclarator;
import softtest.ast.java.SimpleJavaNode;
import softtest.ast.java.SimpleNode;
import softtest.fsm.java.*;
import softtest.jaxen.java.DocumentNavigator;

import java.util.*;

import org.jaxen.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.NpdPreconditionListener;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.jaxen.java.*;
import softtest.rules.java.AbstractStateMachine;

public class NPDFieldStateMachine  extends AbstractStateMachine {

	/**
	 * ���ù������� added by yang 2011-11-21
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("�ڵ�\'%s\'�г��ֵķ���ֵ���������͵ķ��� \'%s\'����ΪNULL",
					errorline,fsmmi.getResultString());
		}else{
			f.format("Null Pointer Dereference on FILED: the method declared on line %d may cause a  NullPointerException ",
					errorline,fsmmi.getResultString());
		}
		fsmmi.setDescription(f.toString());
		f.close();
		
			}
	@Override
	public  void registerPrecondition(PreconditionListenerSet listeners) {
		listeners.addListener(NpdPreconditionListener.getInstance());
	}
	
	@Override
	public  void registerFeature(FeatureListenerSet listenerSet) {
	}
	

	
	/** �ڽڵ�node�ϲ���xPath */
	private static List findTreeNodes(SimpleNode node, String xPath) {
		List evaluationResults = new ArrayList();
		try {
			XPath xpath = new BaseXPath(xPath, new DocumentNavigator());
			evaluationResults = xpath.selectNodes(node);
		} catch (JaxenException e) {
			// e.printStackTrace();
			throw new RuntimeException("xpath error",e);
		}
		return evaluationResults;
	}
	
	public static List<FSMMachineInstance> createNPDFieldStateMachines(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		String xPath = ".//MethodDeclaration[./ResultType[@returnsArray=\'true\'] and ./Block//ReturnStatement/Expression/PrimaryExpression/PrimaryPrefix/Literal/NullLiteral]/MethodDeclarator";
		List evaluationResults = null;

		evaluationResults = findTreeNodes(node, xPath);
		Iterator i = evaluationResults.iterator();
		while (i.hasNext()) {
			ASTMethodDeclarator decl=(ASTMethodDeclarator)i.next();
			FSMMachineInstance fsminstance = fsm.creatInstance();
			
			fsminstance.setRelatedObject(new FSMRelatedCalculation(decl));
			
			fsminstance.setResultString(decl.getImage());
			list.add(fsminstance);
		}
		return list;
	}
	
}
