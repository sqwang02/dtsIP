package softtest.rules.java.safety.ED;
import java.util.Formatter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import softtest.ast.java.ASTReturnStatement;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;
import softtest.rules.java.AbstractStateMachine;
public class ED_FinallyReturnStateMachine extends AbstractStateMachine{
	/**
	 * ���ù�������
	 * Finally�������return
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("������:Finally���е�����return�� %d �У��������һ��©��", errorline);
		}else{
			f.format("Catch Generic Exception:Return Inside Finally Block on line %d",errorline);
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
	public static List<FSMMachineInstance> createFinallyReturnStateMachines(SimpleJavaNode node, FSMMachine fsm)
	{
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		String xpath = "";
		List evalRlts = null;
		Iterator i = null;
		xpath = ".//FinallyStatement//ReturnStatement";
		evalRlts = node.findXpath(xpath);
		i = evalRlts.iterator();
		
		while(i.hasNext())
		{
			ASTReturnStatement name = (ASTReturnStatement) i.next();
			FSMMachineInstance fsmInst = fsm.creatInstance();
			fsmInst.setRelatedObject(new FSMRelatedCalculation(name));
			fsmInst.setResultString("Return Inside Finally Block");
			list.add(fsmInst);
		}	
		return list;
		
	}
}
