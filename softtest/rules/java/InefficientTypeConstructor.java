package softtest.rules.java;

import java.util.*;
import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;

import softtest.symboltable.java.VariableNameDeclaration;

public class InefficientTypeConstructor extends AbstractStateMachine{
	@Override
	public  void registerPrecondition(PreconditionListenerSet listeners) {
	}
	
	@Override
	public  void registerFeature(FeatureListenerSet listenerSet) {
	}
	
	//private static String XPATH1=".//AllocationExpression/ClassOrInterfaceType[@Image=\'Boolean\' or @Image=\'String\' or @Image=\'Double\' or @Image=\'Integer\' or @Image=\'Float\' or @Image=\'Character\' or @Image=\'Byte\' or @Image=\'Long\'or @Image=\'Short\']";
	private static String XPATH1=".//AllocationExpression/ClassOrInterfaceType[matches(@Image,\'^Boolean|String|Double|Integer|Float|Character|Byte|Long|Short$\')]";		
	public static List<FSMMachineInstance> createInefficientTypeConstructors(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result=null;
		
		result=node.findXpath(XPATH1);
		for(Object o:result){
			ASTClassOrInterfaceType prefix=(ASTClassOrInterfaceType)o;
			FSMMachineInstance fsminstance = fsm.creatInstance();
			fsminstance.setResultString("inefficient type constructor.");
			fsminstance.setRelatedObject(new FSMRelatedCalculation(prefix));
			list.add(fsminstance);
		}
		
		return list;
	}
}