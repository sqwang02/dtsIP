package softtest.rules.java.question.BC;

import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;

public class IICStateMachine extends AbstractStateMachine {
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline,
			int errorline) {
		Formatter f = new Formatter();
		if (softtest.config.java.Config.LANGUAGE == 0) {
			f.format("错误的增量运算: 程序使用了类似i = i++语句，会将i++的值覆盖掉，使得i的值永远为初始值，这种语句是无意义的，只会降低系统的效率。");
		} else {
			f.format("Incorrect Incremental Calculations: the variable has incorrect incremental calculations on line %d,that may cause Bad Code.",
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

	private static String XPATH1 = ".//StatementExpression[./Expression[preceding-sibling::AssignmentOperator[preceding-sibling::PrimaryExpression/PrimaryPrefix]]/PostfixExpression /PrimaryExpression/PrimaryPrefix]";

	public static List<FSMMachineInstance> createIICs(SimpleJavaNode node,
			FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result = null;

		result = node.findXpath(XPATH1);
		for (Object o : result) {
			ASTStatementExpression stat = (ASTStatementExpression) o;

			Node node1 = stat.jjtGetChild(0);
			String str1 = null;
			String str1_1 = null;
			List list1 = null;
			if (node1 instanceof ASTPrimaryExpression) {
				ASTPrimaryExpression prim1 = (ASTPrimaryExpression) node1;

				int i = prim1.jjtGetNumChildren();
				if (prim1.jjtGetChild(0) instanceof ASTPrimaryPrefix) {

					ASTName name1 = (ASTName) ((ASTPrimaryPrefix) (prim1
							.jjtGetChild(0)))
							.getSingleChildofType(ASTName.class);
					
					//jdh
					if(name1==null)
						continue;

					str1 = name1.getImage();
				}
				if (i > 1) {

					if (prim1.jjtGetChild(1) instanceof ASTPrimarySuffix) {

						ASTName name1_1 = (ASTName) ((ASTPrimarySuffix) (prim1
								.jjtGetChild(1)))
								.getSingleChildofType(ASTName.class);

						//jdh
						if(name1_1==null)
							continue;

						str1_1 = name1_1.getImage();
					}

				}
			}

			Node node2 = stat.jjtGetChild(2);
			String str2 = null;
			String str2_1 = null;
			List list2 = null;
			if (node2 instanceof ASTExpression) {
				ASTExpression exp1 = (ASTExpression) node2;

				ASTPrimaryExpression prim1 = (ASTPrimaryExpression) node2
						.jjtGetChild(0).jjtGetChild(0);

				//jdh
				if(prim1==null)
					continue;

				int i = prim1.jjtGetNumChildren();
				if (prim1.jjtGetChild(0) instanceof ASTPrimaryPrefix) {

					ASTName name1 = (ASTName) ((ASTPrimaryPrefix) (prim1
							.jjtGetChild(0)))
							.getSingleChildofType(ASTName.class);

					//jdh
					if(name1==null)
						continue;

					str2 = name1.getImage();
				}

				if (i > 1) {
					if (prim1.jjtGetChild(1) instanceof ASTPrimarySuffix) {

						ASTName name1_1 = (ASTName) ((ASTPrimarySuffix) (prim1
								.jjtGetChild(1)))
								.getSingleChildofType(ASTName.class);

						//jdh
						if(name1_1==null)
							continue;

						str2_1 = name1_1.getImage();
					}
				}

			}

			if (str1_1 != null) {

				if (str1.equals(str2) && str1_1.equals(str2_1)) {
					FSMMachineInstance fsminstance = fsm.creatInstance();
					fsminstance
							.setResultString("Incorrect Incremental Calculations.");
					fsminstance
							.setRelatedObject(new FSMRelatedCalculation(stat));
					list.add(fsminstance);
				}
			} else {
				if (str1.equals(str2)) {
					FSMMachineInstance fsminstance = fsm.creatInstance();
					fsminstance
							.setResultString("Incorrect Incremental Calculations.");
					fsminstance
							.setRelatedObject(new FSMRelatedCalculation(stat));
					list.add(fsminstance);
				}
			}
		}
		return list;
	}
}