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


/**   二  滥用API
3 故障名称：不安全的DNS查询
描述：这类错误发生在过于相信DNS查找函数的域名或IP地址。因为攻击者可以更改DNS服务器，所以在安全方面不要过于DNS查询。 
举例：
String ip = request.getRemoteAddr ();
InetAddress addr = InetAddress.getByName(ip);
if (addr.getCanonicalHostName().endsWith("trustme.com")) {
 trusted = true;
}

原因分析：
代码想通过DNS查询来确定一个入站请求是否来自于信任主机，但如果攻击者能够感染DNS缓
存（也称DNS缓存中毒），那么他们就可以取得信任。

2008-06-19
 */

public class AbuseAPI_DNSStateMachine extends AbstractStateMachine{
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("滥用API模式:在 %d 行上,过于相信DNS查找函数的域名或IP地址，可能造成一个漏洞。因为攻击者可以更改DNS服务器，所以在安全方面不要过于DNS查询。", errorline);
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
