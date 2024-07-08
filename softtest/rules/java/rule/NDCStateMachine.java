package softtest.rules.java.rule;

import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;

import softtest.ast.java.ASTClassOrInterfaceDeclaration;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;
import softtest.rules.java.AbstractStateMachine;

/**
 * NDCStateMachine ȱʡ���캯��(NDC)
 * 
 * No default constructor(NDC) 
 * ˵����ÿ������뺬��һ����ʽ�����Ĺ��캯����
 * ʾ���� class aClass { ...
 * aClass(); ... }
 * 
 * 
 * @author cjie
 * 
 */
public class NDCStateMachine extends AbstractStateMachine {
	/**
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline,
			int errorline) {
		Formatter f = new Formatter();
		if (softtest.config.java.Config.LANGUAGE == 0) {
			f.format("ȱʡ���캯��: ����ȱʡ�˲��������Ĺ��캯����Υ���˴����̹淶", errorline);
		} else {
			f.format("No Default Constructor: there is no default constructor in the class,that violates Code Conventions.",
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

	private static String XPATH = ".//ClassOrInterfaceDeclaration[@Interface='false' and @Abstract='false' and count(.//ConstructorDeclaration/FormalParameters[@ParameterCount='0']) = 0]";

	public static List<FSMMachineInstance> createNDCs(SimpleJavaNode node,
			FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result = null;

		result = node.findXpath(XPATH);
		for (Object o : result) {
			ASTClassOrInterfaceDeclaration decl=(ASTClassOrInterfaceDeclaration)o;
			FSMMachineInstance fsminstance = fsm.creatInstance();
			fsminstance
					.setResultString("No default constructor.");
			fsminstance.setRelatedObject(new FSMRelatedCalculation(decl));
			list.add(fsminstance);

		}
		return list;
	}
}
