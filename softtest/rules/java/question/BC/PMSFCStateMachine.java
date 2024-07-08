package softtest.rules.java.question.BC;


import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;

public class PMSFCStateMachine extends
		AbstractStateMachine {
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("声明为final的类声明中proteced方法: 声明为final的类是不能被继承的，但是却在里面声明了protected方法迷惑人，因为protected方法一般是用作给子类留有访问权限的。提醒您不要书写这样具有迷惑性的代码！");
		}else{
			f.format("Proteced Method Statement in the Final Class: the proteced has declared on line %d in final class,that may cause Bad Code.",
				errorline);
		}
		fsmmi.setDescription(f.toString());
		f.close();
	}
	@Override
	public void registerPrecondition(PreconditionListenerSet listeners) {
	}

	@Override
	public void registerFeature(FeatureListenerSet listenerSet) {
	}

	private static String XPATH1 = ".//ClassOrInterfaceDeclaration[@Final='true']/ClassOrInterfaceBody/ClassOrInterfaceBodyDeclaration/MethodDeclaration[@Protected='true']";

	public static List<FSMMachineInstance> createPMSFCs(
			SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result = null;

		result = node.findXpath(XPATH1);
		for (Object o : result) {
			ASTMethodDeclaration met = (ASTMethodDeclaration) o;
			FSMMachineInstance fsminstance = fsm.creatInstance();
			fsminstance
					.setResultString("Proteced Method Statement in the Final Class.");
			fsminstance.setRelatedObject(new FSMRelatedCalculation(met));
			list.add(fsminstance);
		}

		return list;
	}
}
