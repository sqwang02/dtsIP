package softtest.rules.java.question.BC;

import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;

public class CAIStateMachine extends AbstractStateMachine {
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline,
			int errorline) {
		Formatter f = new Formatter();
		if (softtest.config.java.Config.LANGUAGE == 0) {
			f.format("容器添加自己: 在container上进行操作，如addAll，removeAll，retainAll，containsAll等，若参数是container自己，提醒您在此进行检查，不见得是错误但要确保您非误编码。");
		} else {
			f.format("Container Adds Itself: the container operates itself on line %d,that may cause Bad Code.",
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

	private static String XPATH1 = ".//PrimaryExpression/PrimaryPrefix/Name[matches(@Image,'^.+\\.addAll$|.+\\.removeAll$|.+\\.retainAll$|.+\\.containsAll$')]";

	public static List<FSMMachineInstance> createCAIs(SimpleJavaNode node,
			FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result = null;

		result = node.findXpath(XPATH1);
		for (Object o : result) {
			ASTName namen = (ASTName) o;
			String name2 = null;

			name2 = namen.getImage();
			if (namen.jjtGetParent() instanceof ASTPrimaryPrefix) {
				ASTPrimaryPrefix prim1 = (ASTPrimaryPrefix) (namen.jjtGetParent());
				if (prim1.getNextSibling() instanceof ASTPrimarySuffix) {
					ASTPrimarySuffix prim2 = (ASTPrimarySuffix) prim1.getNextSibling();
					if (prim2.getSingleChildofType(ASTName.class) != null)

					{
						ASTName namen1 = (ASTName) prim2.getSingleChildofType(ASTName.class);
						String str = namen1.getImage();
						if (str != null) {
							if (name2.endsWith(".addAll")) {
								String str1 = name2.substring(0, name2.indexOf(".addAll"));
								if (str1.equals(str)) {
									FSMMachineInstance fsminstance = fsm.creatInstance();
									fsminstance.setResultString("Container Adds Itself.");
									fsminstance.setRelatedObject(new FSMRelatedCalculation(namen));
									list.add(fsminstance);
								}
								continue;
							}
							if (name2.endsWith(".retainAll")) {
								String str2 = name2.substring(0, name2.indexOf(".retainAll"));
								if (str2.equals(str)) {
									FSMMachineInstance fsminstance = fsm.creatInstance();
									fsminstance.setResultString("Container Adds Itself.");
									fsminstance.setRelatedObject(new FSMRelatedCalculation(namen));
									list.add(fsminstance);
								}
								continue;
							}
							if (name2.endsWith(".removeAll")) {String str3 = name2.substring(0, name2.indexOf(".removeAll"));
								if (str3.equals(str)) {
									FSMMachineInstance fsminstance = fsm.creatInstance();
									fsminstance.setResultString("Container Adds Itself.");
									fsminstance.setRelatedObject(new FSMRelatedCalculation(namen));
									list.add(fsminstance);
								}
								continue;
							}
							if (name2.endsWith(".containsAll")) {
								String str4 = name2.substring(0, name2.indexOf(".containsAll"));
								if (str4.equals(str)) {
									FSMMachineInstance fsminstance = fsm.creatInstance();
									fsminstance.setResultString("Container Add Itself.");
									fsminstance.setRelatedObject(new FSMRelatedCalculation(namen));
									list.add(fsminstance);
								}
								continue;
							}
						}
					}
				}
			}
		}
		return list;
	}
}
