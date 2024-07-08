package softtest.rules.java.question.BC;

import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import softtest.ast.java.ASTName;
import softtest.ast.java.ASTPrimaryPrefix;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;
import softtest.rules.java.AbstractStateMachine;

public class NWLMFStateMachine extends AbstractStateMachine {
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline,
			int errorline) {
		Formatter f = new Formatter();
		if (softtest.config.java.Config.LANGUAGE == 0) {
			f.format("不要在finalize ()中注销listeners: 不要再finalize ()方法中注销listeners，finalize ()只有再没有对象引用的时候调用，如果listeners从finalize()方法中去除了，被finalize的对象将不会在垃圾收集中去除");
		} else {
			f.format("Not to Write-off Listeners Method in Finalize Method: the listeners ared writed-off on line %d in the finalize(),that may cause Bad Code.",
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

	private static String XPATH1 = ".//MethodDeclaration[./MethodDeclarator[@Image='finalize']]/Block/BlockStatement/Statement/StatementExpression/PrimaryExpression/PrimaryPrefix";

	public static List<FSMMachineInstance> createNWLMFs(SimpleJavaNode node,
			FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result = null;

		result = node.findXpath(XPATH1);
		for (Object o : result) {
			ASTPrimaryPrefix prim = (ASTPrimaryPrefix) o;

			ASTName name1 = (ASTName) prim.getSingleChildofType(ASTName.class);
			if (name1 == null)
				continue;
			else {
				String str = name1.getImage();
				String str1 = ".remove[a-zA-z]+?Listener$";

				Pattern pattern = Pattern.compile(str1);
				Matcher matcher = pattern.matcher(str);

				while (matcher.find()) {
					FSMMachineInstance fsminstance = fsm.creatInstance();
					fsminstance
							.setResultString("Not to Write-off Listeners Method in Finalize Method.");
					fsminstance
							.setRelatedObject(new FSMRelatedCalculation(prim));
					list.add(fsminstance);
				}

			}

		}

		return list;
	}
}
