package softtest.rules.java.rule;

import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;

import softtest.ast.java.ASTBlock;
import softtest.ast.java.ASTIfStatement;
import softtest.ast.java.ASTStatement;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;
import softtest.rules.java.AbstractStateMachine;

/**
 * NCSBStateMachine ��������еĿ�(NCSB) 
 * ˵�����ڿ�����䣨if��for��while��do����Ӧ������ʹ�ÿ���䡣
 * ʾ����
 * ��Ӧʹ��������ʽ�� if (x == 0) return; 
 * else while (x > min) x--;
 * ��Ӧ��дΪ: if (x == 0) {
 * return; } else { while (x > min) { x--; } }
 * 
 * 
 * @author cjie
 * 
 */
public class NCSBStateMachine extends AbstractStateMachine {
	/**
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline,
			int errorline) {
		Formatter f = new Formatter();
		if (softtest.config.java.Config.LANGUAGE == 0) {
			f.format("��������еĿ�: ���������в���\"{\"��\"}\"֮�䣬Υ���˴����̹淶", errorline);
		} else {
			f.format("No Control Statement Block: there is no block in the control statement on line %d,that violates Code Conventions.",
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

	private static String XPATH = ".//IfStatement/Statement";

	public static List<FSMMachineInstance> createNCSBs(SimpleJavaNode node,
			FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result = null;

		result = node.findXpath(XPATH);
		for (Object o : result) {
			ASTStatement st = (ASTStatement) o;
			if (st.jjtGetChild(0) instanceof ASTBlock||st.jjtGetChild(0) instanceof ASTIfStatement) {
				continue;
			} else {
				FSMMachineInstance fsminstance = fsm.creatInstance();
				fsminstance.setResultString("No control statement block.");
				fsminstance.setRelatedObject(new FSMRelatedCalculation(st));
				list.add(fsminstance);
			}

		}
		
		String xpath1=".//WhileStatement/Statement";
		result = node.findXpath(xpath1);
		for (Object o : result) {
			ASTStatement st = (ASTStatement) o;
			if (st.jjtGetChild(0) instanceof ASTBlock) {
				continue;
			} else {
				FSMMachineInstance fsminstance = fsm.creatInstance();
				fsminstance.setResultString("No control statement block.");
				fsminstance.setRelatedObject(new FSMRelatedCalculation(st));
				list.add(fsminstance);
			}

		}
		
		String xpath2=".//DoStatement/Statement";
		result = node.findXpath(xpath2);
		for (Object o : result) {
			ASTStatement st = (ASTStatement) o;
			if (st.jjtGetChild(0) instanceof ASTBlock) {
				continue;
			} else {
				FSMMachineInstance fsminstance = fsm.creatInstance();
				fsminstance.setResultString("No control statement block.");
				fsminstance.setRelatedObject(new FSMRelatedCalculation(st));
				list.add(fsminstance);
			}

		}
		
		String xpath3=".//ForStatement/Statement";
		result = node.findXpath(xpath3);
		for (Object o : result) {
			ASTStatement st = (ASTStatement) o;
			if (st.jjtGetChild(0) instanceof ASTBlock) {
				continue;
			} else {
				FSMMachineInstance fsminstance = fsm.creatInstance();
				fsminstance.setResultString("No control statement block.");
				fsminstance.setRelatedObject(new FSMRelatedCalculation(st));
				list.add(fsminstance);
			}

		}
		return list;
	}
}
