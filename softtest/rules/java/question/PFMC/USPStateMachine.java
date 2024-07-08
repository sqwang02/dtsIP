package softtest.rules.java.question.PFMC;

import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;

import softtest.rules.java.AbstractStateMachine;
import softtest.symboltable.java.VariableNameDeclaration;

public class USPStateMachine extends
		AbstractStateMachine {
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("未声明为静态的属性: final类型的属性是某一编译时常量，如果该属性未声明为静态类型将降低效率，应将该属性声明为static，私有变量除外。");
		}else{
			f.format("Undeclaration Static Properties: the code has not declared final variable static on line %d,that belongs to Low Performance Code.",
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

	private static String XPATH1 = ".//ClassOrInterfaceBodyDeclaration/FieldDeclaration[@Final=\'true\' and @Static=\'false\']";

	public static List<FSMMachineInstance> createUSPs(
			SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result = null;

		result = node.findXpath(XPATH1);
		for (Object o : result) {
			ASTFieldDeclaration fieldde = (ASTFieldDeclaration) o;
			if (fieldde.jjtGetChild(1).jjtGetNumChildren()>1) {
				if (!fieldde.isPrivate()) {
					FSMMachineInstance fsminstance = fsm.creatInstance();
					fsminstance.setResultString("Undeclaration Static Properties.");
					fsminstance.setRelatedObject(new FSMRelatedCalculation(fieldde));
					list.add(fsminstance);
				}
			}
		}
		return list;
	}
}
