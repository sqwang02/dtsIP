package softtest.rules.java.safety.CQ;
import java.util.Formatter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import softtest.ast.java.ASTPrimarySuffix;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;
import softtest.rules.java.AbstractStateMachine;
public class CQ_ClsComByNameStateMachine extends AbstractStateMachine{
	/**
	 * ���ù�������
	 * ͨ��������������бȽ�
	 * ������бȽϵ�ʱ��ͬʱ�ж������Ƿ���ͬ��
	 * Ӧ�ü�����Ƿ���ͬ
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("��������CQ: %d ��ͨ�����ֶ�����бȽϣ��������һ��©��.ͨ�����ֵķ�ʽ������Ƚϣ����ܻ�ʹ�ô�����ࡣ�����߿����ṩһ����TrustedClassName��ͬ���ֵ��࣬��Ӧ�ó�����й�����", errorline);
		}else{
			f.format("Code Quality: program compares class by name on line %d",errorline);
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
	public static List<FSMMachineInstance> createClsComByNameStateMachines(SimpleJavaNode node, FSMMachine fsm)
	{
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		String xpath = "";
		List evalRlts = null;
		Iterator i = null;
		xpath = ".//PrimaryExpression[PrimaryPrefix[matches(@TypeString,\"java.lang.Object.getClass\")]][PrimarySuffix[matches(@TypeString,\"java.lang.Class.getName\")]]/PrimarySuffix[matches(@TypeString,\"java.lang.String.equals\")]";
		evalRlts = node.findXpath(xpath);
		i = evalRlts.iterator();	
		while(i.hasNext())
		{
			ASTPrimarySuffix name = (ASTPrimarySuffix) i.next();
			FSMMachineInstance fsmInst = fsm.creatInstance();
			fsmInst.setRelatedObject(new FSMRelatedCalculation(name));
			fsmInst.setResultString("Program compares class by name");
			list.add(fsmInst);
		}	
		return list;
	}

}
