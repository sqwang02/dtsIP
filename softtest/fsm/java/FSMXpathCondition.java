package softtest.fsm.java;

import softtest.jaxen.java.*;
import softtest.ast.java.*;

import org.jaxen.*;
import org.w3c.dom.Node;

import softtest.ast.java.SimpleJavaNode;
import softtest.ast.java.SimpleNode;
import softtest.config.java.*;
import softtest.cfg.java.*;
import java.util.*;
import softtest.fsmanalysis.java.*;
import softtest.jaxen.java.DocumentNavigator;

/** xpath���� */
public class FSMXpathCondition extends FSMCondition {
	/** xpath */
	private String xpath = "";

	/** ��ָ����xpath����ת������ */
	public FSMXpathCondition(String xpath) {
		this.xpath = xpath;
	}

	/** ����xpath */
	public void setXpath(String xpath) {
		this.xpath = xpath;
	}

	/** ���xpath */
	public String getXpath() {
		return xpath;
	}

	/** ���������м��㣬�ж����Ƿ����� */
	@Override
	public boolean evaluate(FSMMachineInstance fsm,FSMStateInstance state,VexNode vex) {
		boolean b = false;
		List evaluationResults = null;
				
		SimpleJavaNode treenode=null;
		if(vex!=null){
			treenode=vex.getTreeNode();
		}
		else{
			treenode=ProjectAnalysis.getCurrent_astroot();
		}

		// xpath��������Щβ�ڵ�
		if (/*treenode.getVexNode().get(0) != vex*/vex!=null&&vex.isBackNode()) {
			return b;
		}
		try {
			XPath xpath = new BaseXPath(this.xpath, new DocumentNavigator());
			
			treenode=(SimpleJavaNode)treenode.getConcreteNode();
			if(treenode==null){
				return false;
			}
				
			evaluationResults = xpath.selectNodes(treenode);
			
		} catch (JaxenException e) {
			if(softtest.config.java.Config.DEBUG){
				e.printStackTrace();
			}
			throw new RuntimeException("xpath error",e);
		}
		
		List todelete=new ArrayList();
		Iterator i=evaluationResults.iterator();
		while(i.hasNext()){
			SimpleNode simplenode=(SimpleNode)i.next();
			if(simplenode.hasLocalMethod(treenode)){
				todelete.add(simplenode);
			}
		}
		
		evaluationResults.removeAll(todelete);
		
		if (evaluationResults!=null&&evaluationResults.size() > 0) {
			if (relatedmethod == null) {
				b = true;
			} else {
				Object[] args = new Object[2];
				args[0] = evaluationResults;
				args[1] = fsm;
				try {
					Boolean r = (Boolean) relatedmethod.invoke(null, args);
					b = r;
				} catch (Exception e) {
					if( Config.DEBUG ) {
						e.printStackTrace();
					}
					throw new RuntimeException("action error",e);
				}
			}
		}
		return b;
	}

	/** ����xml */
	@Override
	public void loadXML(Node n) {
		Node value = n.getAttributes().getNamedItem("Value");
		if (value == null) {
			throw new RuntimeException("Xpath condition must have a value.");
		}
		xpath = value.getNodeValue();
		if (fsm != null && fsm.getRelatedClass() != null) {
			loadAction(n, fsm.getRelatedClass());
		}
	}
}
