package softtest.rules.java.safety.CQ;

import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;
import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.ASTMethodDeclarator;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;
import softtest.rules.java.AbstractStateMachine;

public class CQ_UExtCloneableStateMachine extends AbstractStateMachine{

	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("代码质量CQ: %d 行实现了clone方法，类却没有继承Cloneable，可能造成一个漏洞", errorline);
		}else{
			f.format("Code Quality: unextends Cloneable when implementing clone on line %d",errorline);
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

	public static List<FSMMachineInstance> createUExtCloneableStateMachines(SimpleJavaNode node, FSMMachine fsm)
	{	
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		if(! (node instanceof ASTMethodDeclaration)){
			return list;
		}
		if(! (node.jjtGetChild(1) instanceof ASTMethodDeclarator)){
			return list;
		}
		ASTMethodDeclarator astMD = (ASTMethodDeclarator)node.jjtGetChild(1);
		if ( ! astMD.getImage().equals("clone") ) {
			return list;
		}
		String xpath=".//ExtendsList/ClassOrInterfaceType[@Image='Cloneable']";
		while(!node.getClass().toString().contains("ASTClassOrInterfaceDeclaration")){
			node=(SimpleJavaNode)node.jjtGetParent();
		}
		List result =node.findXpath(xpath);
		if(result==null||result.size()==0) {			
			String xpath1=".//ImplementsList/ClassOrInterfaceType[@Image='Cloneable']";
			while(!node.getClass().toString().contains("ASTClassOrInterfaceDeclaration")){
				node=(SimpleJavaNode)node.jjtGetParent();
			}
			List result1 =node.findXpath(xpath1);
			if(result1==null||result1.size()==0) {			
				FSMMachineInstance fsmInst = fsm.creatInstance();
				fsmInst.setRelatedObject(new FSMRelatedCalculation(astMD));
				fsmInst.setResultString("unextends Cloneable when implementing clone");
				list.add( fsmInst );
			}
		}
		return list;
		
	}
}
