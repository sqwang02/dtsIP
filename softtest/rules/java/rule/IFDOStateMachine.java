package softtest.rules.java.rule;

import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;

import softtest.ast.java.ASTFieldDeclaration;
import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;
import softtest.rules.java.AbstractStateMachine;

/**
 * IFDOStateMachine
 * ����ȷ����������˳��(IFDO) 
 * ˵���������е�����������ѭ�ض���˳�����ԣ����캯����������
 * 
 * @author cjie
 * 
 */
public class IFDOStateMachine extends AbstractStateMachine {
	/**
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline,
			int errorline) {
		Formatter f = new Formatter();
		if (softtest.config.java.Config.LANGUAGE == 0) {
			f.format("����ȷ����������˳��: �����е�����������ѭ�ض���˳�����ԣ����캯����������");
		} else {
			f.format("Incorrect type declaration order. Declaration in the class must follow a specific order: " +
					"attributes, constructors, methods.");
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
    /**
     * ƥ�䲻��ȷ����������
     */
	private final static String FIELD_XPATH = ".//ClassOrInterfaceBodyDeclaration[preceding::ClassOrInterfaceBodyDeclaration[ConstructorDeclaration or MethodDeclaration ]]/FieldDeclaration";
	
    /**
     * ƥ�䲻��ȷ�Ĺ��캯������
     */
	private final static String CONSTRUCTOR_XPATH = ".//ClassOrInterfaceBodyDeclaration[following::ClassOrInterfaceBodyDeclaration/FieldDeclaration or preceding::ClassOrInterfaceBodyDeclaration/MethodDeclaration]/ConstructorDeclaration";
	
    /**
     * ƥ�䲻��ȷ�ķ�������
     */
	private final static String METHOD_XPATH = ".//ClassOrInterfaceBodyDeclaration[following::ClassOrInterfaceBodyDeclaration[ConstructorDeclaration or FieldDeclaration ]]/MethodDeclaration";
	
	/**
	 * ����״̬��
	 * @param node
	 * @param fsm
	 * @return
	 */
	public static List<FSMMachineInstance> createIFDOStateMachine(SimpleJavaNode node,
			FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result = null;

		result = node.findXpath(FIELD_XPATH);
		for (Object o : result) {
			ASTFieldDeclaration as = (ASTFieldDeclaration) o;

			FSMMachineInstance fsminstance = fsm.creatInstance();
			fsminstance
					.setResultString("Incorrect filed declaration location.");
			fsminstance.setRelatedObject(new FSMRelatedCalculation(as));
			list.add(fsminstance);

		}
		result = node.findXpath(METHOD_XPATH);
		for (Object o : result) {
			ASTMethodDeclaration as = (ASTMethodDeclaration) o;

			FSMMachineInstance fsminstance = fsm.creatInstance();
			fsminstance
					.setResultString("Incorrect method declaration location.");
			fsminstance.setRelatedObject(new FSMRelatedCalculation(as));
			list.add(fsminstance);

		}
		return list;
	}
}
