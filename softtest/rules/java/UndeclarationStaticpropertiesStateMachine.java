package softtest.rules.java;

import java.util.*;
import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;

import softtest.symboltable.java.VariableNameDeclaration;

public class UndeclarationStaticpropertiesStateMachine extends
		AbstractStateMachine {
	@Override
	public void registerPrecondition(PreconditionListenerSet listeners) {
	}

	@Override
	public void registerFeature(FeatureListenerSet listenerSet) {
	}

	private static String XPATH1 = ".//ClassOrInterfaceBodyDeclaration/FieldDeclaration[@Final=\'true\' and @Static=\'false\']";

	public static List<FSMMachineInstance> createUndeclarationStaticpropertiess(
			SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result = null;

		result = node.findXpath(XPATH1);
		for (Object o : result) {
			ASTFieldDeclaration fieldde = (ASTFieldDeclaration) o;
			FSMMachineInstance fsminstance = fsm.creatInstance();
			fsminstance.setResultString("Undeclaration Static properties.");
			fsminstance.setRelatedObject(new FSMRelatedCalculation(fieldde));
			list.add(fsminstance);
		}

		return list;
	}
}
