package softtest.rules.java.safety.ED;
//��ȫȱ��.����������
import java.util.Formatter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import softtest.ast.java.ASTClassOrInterfaceType;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;
import softtest.rules.java.AbstractStateMachine;
public class ED_CatchGenericExcpStateMachine extends AbstractStateMachine{
	/**
	 * ���ù�������
	 * �����catch��
	 * 	1.    try {
		2.        doExchange();
		3.    }
		4.    catch (Exception e) {
		5.        logger.error("doExchange failed", e);
		6.    }
		catch���ص��쳣̫��һ����Exception��Throwable
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("������:Catch ���е����˹����Exception�� %d �У��������һ��©��", errorline);
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
	public static List<FSMMachineInstance> createCatchGenericExcpStateMachines(SimpleJavaNode node, FSMMachine fsm)
	{
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		String xpath = "";
		List evalRlts = null;
		Iterator i = null;
		/**
		 *1.Catch���в�׽Exception 
		 * */
		xpath = ".//TryStatement/CatchStatement/FormalParameter/Type/ReferenceType/ClassOrInterfaceType[@Image='Exception']";
		evalRlts = node.findXpath(xpath);
		i = evalRlts.iterator();		
		while(i.hasNext())
		{
			ASTClassOrInterfaceType name = (ASTClassOrInterfaceType) i.next();
			FSMMachineInstance fsmInst = fsm.creatInstance();
			fsmInst.setRelatedObject(new FSMRelatedCalculation(name));
			fsmInst.setResultString("Catch Generic Exception ");
			list.add(fsmInst);
		}	
		/**
		 *2.Catch���в�׽Throwable 
		 * */
		xpath = ".//TryStatement/CatchStatement/FormalParameter/Type/ReferenceType/ClassOrInterfaceType[@Image='Throwable']";
		evalRlts = node.findXpath(xpath);
		i = evalRlts.iterator();		
		while(i.hasNext())
		{
			ASTClassOrInterfaceType name = (ASTClassOrInterfaceType) i.next();
			FSMMachineInstance fsmInst = fsm.creatInstance();
			fsmInst.setRelatedObject(new FSMRelatedCalculation(name));
			fsmInst.setResultString("Catch Generic Throwable");
			list.add(fsmInst);
		}	
		return list;
		
	}
}
