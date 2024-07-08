package softtest.rules.java.question.PFMC;

import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;

import softtest.rules.java.AbstractStateMachine;
import softtest.symboltable.java.VariableNameDeclaration;

public class ITCStateMachine extends AbstractStateMachine {
	/**
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline,
			int errorline) {
		Formatter f = new Formatter();
		if (softtest.config.java.Config.LANGUAGE == 0) {
			f.format("ʹ�õ�Ч�����͹�����: ��Java�����ж���һЩ�����������͵İ�װ���String�࣬Ӧ��������ʹ���乹�췽������ö��󣬶����þ�̬��������ö��󣬳����Ժ�Ҫʹ�ð�װ��������;��");
		} else {
			f.format("Inefficient Type Constructor: the code has used type constructor inefficiently on line %d,that belongs to Low Performance Code.",
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

	// private static String
	// XPATH1=".//AllocationExpression/ClassOrInterfaceType[@Image=\'Boolean\'
	// or @Image=\'String\' or @Image=\'Double\' or @Image=\'Integer\' or
	// @Image=\'Float\' or @Image=\'Character\' or @Image=\'Byte\' or
	// @Image=\'Long\'or @Image=\'Short\']";
	private static String XPATH1 = ".//AllocationExpression/ClassOrInterfaceType[matches(@Image,'^\\bBoolean$|\\bString$|\\bDouble$|\\bInteger$|\\bFloat$|\\bCharacter$|\\bByte$|\\bLong$|\\bShort$')]";

	public static List<FSMMachineInstance> createITCs(SimpleJavaNode node,
			FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result = null;

		result = node.findXpath(XPATH1);
		for (Object o : result) {
			ASTClassOrInterfaceType prefix = (ASTClassOrInterfaceType) o;
			if (!(prefix.getNextSibling() instanceof ASTArrayDimsAndInits)
					&& !(prefix.jjtGetParent().jjtGetParent().jjtGetParent()
							.jjtGetParent().jjtGetParent() instanceof ASTArgumentList)) {
				FSMMachineInstance fsminstance = fsm.creatInstance();
				fsminstance.setResultString("Inefficient Type Constructor.");
				fsminstance.setRelatedObject(new FSMRelatedCalculation(prefix));
				list.add(fsminstance);

			} 
			continue;
		}

		return list;
	}
}