package softtest.rules.java.safety.SF;

import softtest.ast.java.ASTName;
import softtest.ast.java.SimpleJavaNode;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;
import java.util.*;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;

public class SF_AccessConStateMachine extends AbstractStateMachine{
	/**
	 * ���ù�������
	 * ���û���ʵ��� access control ��ϵͳ�ͻ�ִ��һ�������û�����ֵ�� LDAP ������
	 * �Ӷ��������߷���δ����Ȩ�ļ�¼��
	 * ��δ�� authentication ��������������Ч��ִ�� LDAP ��ѯ��
	 * �ᵼ�¹��������õͶ����õ� LDAP ������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("��ȫ���ܲ���ģʽ: %d ��,�Է��ʿ�����ȨΪ��none�������ڰ�ȫ��������LDAP�����󶨺��û����Բ�������Ȩ�Ϳ���ִ�в�ѯ��", errorline);
		}else{
			f.format("SF: Impropriety access control on line %d",errorline);
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
	public static List<FSMMachineInstance> createIACStateMachine(SimpleJavaNode node, FSMMachine fsm) {
  
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();    
        String xpath = "";
		List evalRlts = null;
		Iterator i = null;
        /*xpath����ƥ�����2��������.put����
         * */
        xpath = ".//PrimaryExpression[PrimaryPrefix[matches(@TypeString,\"java.util.Hashtable.put\")]]/PrimarySuffix/Arguments[@ArgumentCount=2]"+
        "/ArgumentList[Expression[2]//PrimaryExpression//Literal[@Image='\"none\"']]"+
        "/Expression[1]//PrimaryExpression//Name[@Image='Context.SECURITY_AUTHENTICATION']"; 
        evalRlts=node.findXpath(xpath);
        i=evalRlts.iterator();
        while(i.hasNext())
		{
			ASTName name = (ASTName) i.next();
			FSMMachineInstance fsmInst = fsm.creatInstance();
			fsmInst.setRelatedObject(new FSMRelatedCalculation(name));
			fsmInst.setResultString("Impropriety access control");
			list.add(fsmInst);
		}	
        return list;
    }
}
