package softtest.rules.java.safety.AAPI;

import softtest.fsm.java.*;
import softtest.jaxen.java.DocumentNavigator;

import java.util.*;

import org.jaxen.*;
import softtest.rules.java.AbstractStateMachine;
import softtest.ast.java.ASTName;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.config.java.Config;


/**   ��  ����API
3 �������ƣ�����ȫ��DNS��ѯ
����������������ڹ�������DNS���Һ�����������IP��ַ����Ϊ�����߿��Ը���DNS�������������ڰ�ȫ���治Ҫ����DNS��ѯ�� 
������
String ip = request.getRemoteAddr ();
InetAddress addr = InetAddress.getByName(ip);
if (addr.getCanonicalHostName().endsWith("trustme.com")) {
 trusted = true;
}

ԭ�������
������ͨ��DNS��ѯ��ȷ��һ����վ�����Ƿ�����������������������������ܹ���ȾDNS��
�棨Ҳ��DNS�����ж�������ô���ǾͿ���ȡ�����Ρ�

2008-06-19
 */

public class AbuseAPI_DNSStateMachine extends AbstractStateMachine{
	/**
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("����APIģʽ:�� %d ����,��������DNS���Һ�����������IP��ַ���������һ��©������Ϊ�����߿��Ը���DNS�������������ڰ�ȫ���治Ҫ����DNS��ѯ��", errorline);
		}else{
			f.format("Abuse Application Program Interface: a potential danger of DNS query on line %d",errorline);
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
	
	public static List  getTreeNode(SimpleJavaNode node, String xpathStr) {
		List evalRlts = null;
		try {
			XPath xpath = new BaseXPath(xpathStr, new DocumentNavigator());
			evalRlts = xpath.selectNodes(node);
		} catch (JaxenException e) {
			e.printStackTrace();
			throw new RuntimeException("xpath error",e);
		}
		return evalRlts;
	}
	
	public static List<FSMMachineInstance> createAbuseDNSStateMachines(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		String  xpathStr = ".//Name[ matches(@Image, \'^((.+\\.getCanonicalHostName)|(getCanonicalHostName))$\') ]";
		
		List evalRlts = getTreeNode(node, xpathStr);
		
		for(int i = 0; i < evalRlts.size(); i++) {
			ASTName astNm = (ASTName) evalRlts.get(i);

			FSMMachineInstance fsmInst = fsm.creatInstance();
			fsmInst.setRelatedObject(new FSMRelatedCalculation(astNm));
			list.add(fsmInst);
			fsmInst.setResultString(astNm.getImage());
		}
		return list;
	}
	
	
	public static boolean checkAbuseAPI(List nodes, FSMMachineInstance fsmInst) {
		boolean found = false;
		found = true;
		return found;
	}
	
	
	public static void logc1(String str) {
		logc("createAbuseAPIStateMachines(..) - " + str);
	}
	
	public static void logc(String str) {
		if( Config.DEBUG ) {
			System.out.println("AbuseAPI_DNSStateMachine::" + str);
		}
	}
}
