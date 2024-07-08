package softtest.rules.java.rule;

import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;

import softtest.ast.java.ASTLiteral;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;
import softtest.rules.java.AbstractStateMachine;

/**
 * LCStateMachine
 * ���ֳ���(LC) 
 * ˵����������ʹ�õ����ֺ��ַ�����������Ϊ����������Ӧֱ��ʹ�á�����ʹ�õ����ֳ���Ϊ0��1����\0�����ַ������֡�
 * 
 * @author cjie
 * 
 */
public class LCStateMachine extends AbstractStateMachine {
	/**
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline,
			int errorline) {
		Formatter f = new Formatter();
		if (softtest.config.java.Config.LANGUAGE == 0) {
			f.format("���ֳ���: ������ʹ�õ����ֺ��ַ�����������Ϊ����������Ӧֱ��ʹ�á�����ʹ�õ����ֳ���Ϊ0��1��'\\0'���ַ������֡�", errorline);
		} else {
			f.format("Literal Constant on line %d: The number and string used in the program must be declared as constants, " +
					"rather than used directly. Allows the use of 0,1,'\\0' and string text.",
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
    /**
     * ƥ�䳣��
     */
	private final static String XPATH = "//MethodDeclaration//Literal[@Image!=0 and @Image!=1]";
	
	/**
	 * ����״̬��
	 * @param node
	 * @param fsm
	 * @return
	 */
	public static List<FSMMachineInstance> createLCStateMachine(SimpleJavaNode node,
			FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result = null;

		result = node.findXpath(XPATH);
		for (Object o : result) {
			ASTLiteral as = (ASTLiteral) o;
            if (null != as.getImage() && !"'\\0'".equals(as.getImage())) {
            	FSMMachineInstance fsminstance = fsm.creatInstance();
    			fsminstance
    					.setResultString("Litral constant.");
    			fsminstance.setRelatedObject(new FSMRelatedCalculation(as));
    			list.add(fsminstance);

            }
		}

		return list;
	}
}
