package softtest.rules.java.safety.CQ;
import java.util.Formatter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import softtest.ast.java.ASTPrimaryExpression;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;
import softtest.rules.java.AbstractStateMachine;
public class CQ_WStringComStateMachine extends AbstractStateMachine{
	/**
	 * ���ù�������
	 * �ַ����Ƚ�
	 * �ַ���ʹ�á�==�����бȽϣ��Ƚϵ�Ϊ�ַ���������
	 * ����ʹ��equals�����бȽ�
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("��������CQ: %d �г������ == �� != ���Ƚ��ַ����Ƿ���ȣ��������һ��©����������equals�Ƚ�", errorline);
		}else{
			f.format("Code Quality: program uses == or != to compara two strings on line %d",errorline);
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
	public static List<FSMMachineInstance> createWStringComStateMachines(SimpleJavaNode node, FSMMachine fsm)
	{
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		String xpath = "";
		List evalRlts = null;
		Iterator i = null;
		xpath = ".//Expression/EqualityExpression[PrimaryExpression[1][matches(@TypeString,\"java.lang.String\")]]/PrimaryExpression[2][matches(@TypeString,\"java.lang.String\")]";
		evalRlts = node.findXpath(xpath);
		i = evalRlts.iterator();
		
		while(i.hasNext())
		{
			ASTPrimaryExpression name = (ASTPrimaryExpression) i.next();
			FSMMachineInstance fsmInst = fsm.creatInstance();
			fsmInst.setRelatedObject(new FSMRelatedCalculation(name));
			fsmInst.setResultString("Program uses == or != to compara two strings");
			list.add(fsmInst);
		}	
		return list;
	}
}
