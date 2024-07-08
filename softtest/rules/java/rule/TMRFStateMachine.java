package softtest.rules.java.rule;

import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;

import softtest.ast.java.ASTTypeDeclaration;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;
import softtest.rules.java.AbstractStateMachine;

/**
 * 文件长度太长（TMRF）
 * Too many rows in the file 
 * 说明：源文件不应超过一定的行数，一般最大行数定义为1000.
 * 
 * @author cjie
 * 
 */
public class TMRFStateMachine extends AbstractStateMachine {
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline,
			int errorline) {
		Formatter f = new Formatter();
		if (softtest.config.java.Config.LANGUAGE == 0) {
			f.format("文件长度太长: 该文件的代码行数超过1000，违反了代码编程规范", errorline);
		} else {
			f.format("Too Many Rows in the File: this file has more than 1000 rows,that violates Code Conventions.",
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

	private static String XPATH = ".//TypeDeclaration";

	public static List<FSMMachineInstance> createTMRFs(SimpleJavaNode node,
			FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result = null;

		result = node.findXpath(XPATH);
		for (Object o : result) {
			ASTTypeDeclaration as = (ASTTypeDeclaration) o;
			if (as.getEndLine() > 2000) {

				FSMMachineInstance fsminstance = fsm.creatInstance();
				fsminstance.setResultString("Too many rows in the file.");
				fsminstance.setRelatedObject(new FSMRelatedCalculation(as));
				list.add(fsminstance);
			}

		}

		return list;
	}
}
