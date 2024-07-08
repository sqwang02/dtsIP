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
public class ED_CatchRuntimeExcpStateMachine extends AbstractStateMachine{
	/**
	 * 设置故障描述
	 * 捕获运行是异常
	 * public class RuntimeExceptionExample {
		public static void mysteryMethod(){
		try {
		        mysteryMethod();
		   	}
		    catch (NullPointerException npe) {
		    }
			}
		}
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("错误处理:显示调用了Runtime异常在 %d 行，可能造成一个漏洞", errorline);
		}else{
			f.format("Catch Generic Exception:Declaration of Catch for Generic Exception on line %d",errorline);
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
	public static List<FSMMachineInstance> createCatchRuntimeExcpStateMachines(SimpleJavaNode node, FSMMachine fsm)
	{
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		String xpath = "";
		List evalRlts = null;
		Iterator i = null;
		/**
		 *1.catch块中捕获了Runtime异常 
		 * */
		xpath = ".//TryStatement/CatchStatement/FormalParameter/VariableDeclaratorId";
		evalRlts = node.findXpath(xpath);
		i = evalRlts.iterator();
		while(i.hasNext())
		{
			ASTVariableDeclaratorId type = (ASTVariableDeclaratorId) i.next();
			VariableNameDeclaration v=type.getNameDeclaration();
			if(v.getType()==null||(v.getType().getSuperclass()==null)){
				return list;
			}
			if( v.getType().getSuperclass().toString().contains( "java.lang.RuntimeException")){
				FSMMachineInstance fsmInst = fsm.creatInstance();
				fsmInst.setRelatedObject(new FSMRelatedCalculation(type));
				fsmInst.setResultString("Catch Runtime Exception ");
				list.add(fsmInst);
			}
		}	
		
		return list;
		
	}

}
