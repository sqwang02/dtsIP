package softtest.rules.java.safety.ED;
import java.util.Formatter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.lang.String;

import softtest.ast.java.ASTVariableDeclaratorId;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;
import softtest.rules.java.AbstractStateMachine;
import softtest.symboltable.java.VariableNameDeclaration;
public class ED_UnhandledSSLExcpStateMachine extends AbstractStateMachine{
	/**
	 * 设置故障描述
	 *未处理的SSL异常
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("错误处理:未处理的SSL异常在 %d 行，可能造成一个漏洞", errorline);
		}else{
			f.format("Poor Error Handled:Unhandled SSL Exception on line %d",errorline);
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
	public static List<FSMMachineInstance> createUnhandledSSLExcpStateMachines(SimpleJavaNode node, FSMMachine fsm)
	{
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		String xpath = "";
		List evalRlts = null;
		Iterator i = null;
		/**
		 *1.javax.net.ssl.SSLHandshakeException 
		 * */
		xpath = ".//TryStatement/CatchStatement/FormalParameter/VariableDeclaratorId";
		evalRlts = node.findXpath(xpath);
		i = evalRlts.iterator();
		while(i.hasNext())
		{
			ASTVariableDeclaratorId type = (ASTVariableDeclaratorId) i.next();
			VariableNameDeclaration v=type.getNameDeclaration();			 
			if(v==null||v.getType()==null){
				return list;
			}
			if( v.getType().toString().contains( "javax.net.ssl.SSLHandshakeException")){
				FSMMachineInstance fsmInst = fsm.creatInstance();
				fsmInst.setRelatedObject(new FSMRelatedCalculation(type));
				fsmInst.setResultString("Unhandle SSL Exception ");
				list.add(fsmInst);
			}
		}	
		/**
		 *2.javax.net.ssl.SSLKeyException
		 * */
		xpath = ".//TryStatement/CatchStatement/FormalParameter/VariableDeclaratorId";
		evalRlts = node.findXpath(xpath);
		i = evalRlts.iterator();
		while(i.hasNext())
		{
			ASTVariableDeclaratorId type = (ASTVariableDeclaratorId) i.next();
			VariableNameDeclaration v=type.getNameDeclaration();	
			if(v==null){
				return list;
			}
			if( v.getType().toString().contains( "javax.net.ssl.SSLKeyException")){
				FSMMachineInstance fsmInst = fsm.creatInstance();
				fsmInst.setRelatedObject(new FSMRelatedCalculation(type));
				fsmInst.setResultString("Unhandle SSL Exception");
				list.add(fsmInst);
			}
		}	
		/**
		 *3.javax.net.ssl.SSLPeerUnverifiedException
		 * */
		xpath = ".//TryStatement/CatchStatement/FormalParameter/VariableDeclaratorId";
		evalRlts = node.findXpath(xpath);
		i = evalRlts.iterator();
		while(i.hasNext())
		{
			ASTVariableDeclaratorId type = (ASTVariableDeclaratorId) i.next();
			VariableNameDeclaration v=type.getNameDeclaration();			 
			if(v==null){
				return list;
			}
			if( v.getType().toString().contains( "javax.net.ssl.SSLPeerUnverifiedException")){
				FSMMachineInstance fsmInst = fsm.creatInstance();
				fsmInst.setRelatedObject(new FSMRelatedCalculation(type));
				fsmInst.setResultString("Unhandle SSL Exception");
				list.add(fsmInst);
			}
		}	
		return list;
		
	}

}
