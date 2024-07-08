package softtest.rules.java.safety.CQ;
import java.util.Formatter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import softtest.ast.java.ASTSynchronizedStatement;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;
import softtest.rules.java.AbstractStateMachine;
public class CQ_EmSynStateMachine extends AbstractStateMachine{
	/**
	 * ���ù�������
	 * ���յ�synchronized��
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("��������CQ: %d �пյ�synchronized�飬�������һ��©����һ�� empty synchronized block ͨ��ֻ��־����Ա���ڴ���ͬ�����⣬����û�дﵽԤ�ڵĽ����", errorline);
		}else{
			f.format("Code Quality: program uses empty synchronized block on line %d",errorline);
		}
		fsmmi.setDescription(f.toString());
		f.close();
	}
	@Override
	public  void registerPrecondition(PreconditionListenerSet listeners) {
	}
	
	@Override
	public  void registerFeature(FeatureListenerSet listenerSet) {
	}
	public static List<FSMMachineInstance> createEmSynStateMachines(SimpleJavaNode node, FSMMachine fsm)
	{
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		String xpath = "";
		List evalRlts = null;
		Iterator i = null;
		xpath = ".//SynchronizedStatement[count(Block/BlockStatement) = 0]";
		evalRlts = node.findXpath(xpath);
		i = evalRlts.iterator();	
		while(i.hasNext())
		{
			ASTSynchronizedStatement name = (ASTSynchronizedStatement) i.next();
			FSMMachineInstance fsmInst = fsm.creatInstance();
			fsmInst.setRelatedObject(new FSMRelatedCalculation(name));
			fsmInst.setResultString("Program uses empty synchronized block");
			list.add(fsmInst);
		}	
		return list;
	}

}
