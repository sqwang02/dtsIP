package softtest.rules.java.safety.ED;
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
public class ED_ThrowGenericExcpStateMachine extends AbstractStateMachine{
	/**
	 * ���ù�������
	 * �����Throw��	 
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("������:Throws�˹����Exception�� %d �У��������һ��©��", errorline);
		}else{
			f.format("Catch Generic Exception:Declaration of Throws for Generic Exception on line %d",errorline);
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
	public static List<FSMMachineInstance> createThrowGenericExcpStateMachines(SimpleJavaNode node, FSMMachine fsm)
	{
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		String xpath = "";
		List evalRlts = null;
		Iterator i = null;
		/**
		 *1.Throw���в�׽Exception 
		 * */
		xpath = ".//NameList/Name[@Image='Exception']";
		evalRlts = node.findXpath(xpath);
		i = evalRlts.iterator();		
		while(i.hasNext())
		{
			ASTName name = (ASTName) i.next();
			FSMMachineInstance fsmInst = fsm.creatInstance();
			fsmInst.setRelatedObject(new FSMRelatedCalculation(name));
			fsmInst.setResultString("Throws Generic Exception ");
			list.add(fsmInst);
		}	
		/**
		 *2.Throw���в�׽Throwable 
		 * */
		xpath = ".//NameList/Name[@Image='Throwable']";
		evalRlts = node.findXpath(xpath);
		i = evalRlts.iterator();		
		while(i.hasNext())
		{
			ASTName name = (ASTName) i.next();
			FSMMachineInstance fsmInst = fsm.creatInstance();
			fsmInst.setRelatedObject(new FSMRelatedCalculation(name));
			fsmInst.setResultString("Throws Generic Throwable");
			list.add(fsmInst);
		}	
		return list;
		
	}
}
