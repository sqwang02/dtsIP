package softtest.rules.java.safety.CQ;
import java.util.Formatter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import softtest.ast.java.ASTName;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;
import softtest.rules.java.AbstractStateMachine;
import softtest.symboltable.java.NameDeclaration;
import softtest.symboltable.java.VariableNameDeclaration;
public class CQ_RunOfThreadStateMachine extends AbstractStateMachine{
	/**
	 * 设置故障描述
	 * Thread类的实例调用了th.run()方法
	 * 应该使用start（）方法来启动线程
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("代码质量CQ: 程序%d 行调用了Thread实例的run方法，应该调用start()方法来启动线程。", errorline);
		}else{
			f.format("Code Quality: Program calls to Thread run() instead of start() on line %d",errorline);
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
	public static List<FSMMachineInstance> createRunOfThreadStateMachines(SimpleJavaNode node, FSMMachine fsm)
	{
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		String xpath = "";
		List evalRlts = null;
		Iterator i = null;
		xpath = ".//PrimaryExpression/PrimaryPrefix/Name[@MethodName='true'][matches(@Image,\".run\")]";
		evalRlts = node.findXpath(xpath);
		i = evalRlts.iterator();	
		while(i.hasNext())
		{
			ASTName name = (ASTName) i.next();
			List<NameDeclaration> l =  name.getNameDeclarationList();
			Iterator iter = l.iterator();
//			while(iter.hasNext())
//			{
//				NameDeclaration a = (NameDeclaration)iter.next();			
//				if( !(a instanceof VariableNameDeclaration)) continue;				
//				VariableNameDeclaration vnd = (VariableNameDeclaration)a;
//				System.out.println(vnd.getImage());
//				if(vnd.getType()==null||vnd.getType().getSuperclass()==null)continue;
				
//				if(name.getTypeString().contains("Thread.run")){
					FSMMachineInstance fsmInst = fsm.creatInstance();
					fsmInst.setRelatedObject(new FSMRelatedCalculation(name));
					fsmInst.setResultString("Program calls to Thread run() instead of start()");
					list.add(fsmInst);
//				}
//			}
			
		}	
		return list;
	}
}
