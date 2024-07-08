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
	 * 设置故障描述
	 * 如果没有适当的 access control ，系统就会执行一个包含用户控制值的 LDAP 声明，
	 * 从而允许攻击者访问未经授权的记录。
	 * 若未经 authentication 便在匿名绑定下有效地执行 LDAP 查询，
	 * 会导致攻击者滥用低端配置的 LDAP 环境。
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("安全功能部件模式: %d 行,对访问控制授权为“none”，存在安全隐患。对LDAP匿名绑定后，用户可以不经过授权就可以执行查询。", errorline);
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
        /*xpath用于匹配具有2个参数的.put调用
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
