package softtest.rules.java;

import java.util.*;
import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;

public class InefficientGeneratingIntegerRandomStateMachine extends
		AbstractStateMachine {
	@Override
	public void registerPrecondition(PreconditionListenerSet listeners) {
	}

	@Override
	public void registerFeature(FeatureListenerSet listenerSet) {
	}

	private static String XPATH1 = ".//Expression/CastExpression[./Type[./PrimitiveType[@Image=\'int\']and following-sibling::PrimaryExpression[1]/PrimaryPrefix/Expression[.//PrimaryExpression/PrimaryPrefix/Name[matches(@Image,\'^.+\\.nextDouble$\']]]";

	public static List<FSMMachineInstance> createInefficientGeneratingIntegerRandoms(
			SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result = null;

		result = node.findXpath(XPATH1);
		for (Object o : result) {
			ASTCastExpression castexp = (ASTCastExpression) o;
			FSMMachineInstance fsminstance = fsm.creatInstance();
			fsminstance
					.setResultString("inefficient Generating Integer Random.");
			fsminstance.setRelatedObject(new FSMRelatedCalculation(castexp));
			list.add(fsminstance);
		}

		return list;
	}
}
