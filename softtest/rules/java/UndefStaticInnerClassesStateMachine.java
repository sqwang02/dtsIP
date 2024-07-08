

package softtest.rules.java;

import java.util.*;
import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;


public class UndefStaticInnerClassesStateMachine extends
		AbstractStateMachine {
	@Override
	public void registerPrecondition(PreconditionListenerSet listeners) {
	}

	@Override
	public void registerFeature(FeatureListenerSet listenerSet) {
	}

	private static String XPATH1 = ".//ClassOrInterfaceBody/ClassOrInterfaceBodyDeclaration[./ClassOrInterfaceDeclaration[@Static='false']]";

	public static List<FSMMachineInstance> createUndefStaticInnerClassess(
			SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result = null;

		result = node.findXpath(XPATH1);
		for (Object o : result) {
			ASTClassOrInterfaceBodyDeclaration classorinterface = (ASTClassOrInterfaceBodyDeclaration) o;
			FSMMachineInstance fsminstance = fsm.creatInstance();
			fsminstance.setResultString("Undefine Static Inner Classes.");
			fsminstance.setRelatedObject(new FSMRelatedCalculation(classorinterface));
			list.add(fsminstance);
		}

		return list;
	}
}
