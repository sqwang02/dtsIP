package softtest.rules.java.question.PFMC;

import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;

public class IGIRStateMachine extends
		AbstractStateMachine {
	
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("低效产生随机整数: 程序为了产生一个随机整数，却调用了nextDouble()方法，然后强制转换为整形，这样效率较低，建议您直接调用nextInt方法！");
		}else{
			f.format("Inefficient Generating Integer Random: the code has generated integer random inefficiently on line %d,that belongs to Low Performance Code.",
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

	private static String XPATH1 = ".//Expression/CastExpression[./Type[./PrimitiveType[@Image=\'int\']and following-sibling::PrimaryExpression[1]/PrimaryPrefix/Expression[.//PrimaryExpression/PrimaryPrefix/Name[matches(@Image,\'^.+\\.nextDouble$\']]]";

	public static List<FSMMachineInstance> createIGIRs(
			SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result = null;

		result = node.findXpath(XPATH1);
		for (Object o : result) {
			ASTCastExpression castexp = (ASTCastExpression) o;
			FSMMachineInstance fsminstance = fsm.creatInstance();
			fsminstance
					.setResultString("Inefficient Generating Integer Random.");
			fsminstance.setRelatedObject(new FSMRelatedCalculation(castexp));
			list.add(fsminstance);
		}

		return list;
	}
}
