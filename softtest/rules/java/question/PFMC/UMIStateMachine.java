package softtest.rules.java.question.PFMC;

import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;


/**
 * UMIStateMachine
 * ���ʹ���˲���Ҫ�ķ�������
 * ���������쳣������У�����System.exit()��û�б�Ҫ�ģ�Ӧ�׳��쳣��
 ������
1   String square(String x) {
   2   		try {
   3   			int y = Integer.parseInt(x.toLowerCase());
   4   			return y * y + "";
   5   		} catch (NumberFormatException e) {
   6   			e.printStackTrace();
   7   			System.exit(1);
   8   			return "";
   9   		}
   10   }


 * @author cjie
 * 
 */
public class UMIStateMachine extends AbstractStateMachine{
	
	/**
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("ʹ���˲���Ҫ�ĺ���: %d ����ʹ���˲���Ҫ�ĺ���System.exit,Ӧ�׳��쳣", errorline);
		}else{
			f.format("Used Unnecessary Method: " +
					" line %d Used unnecessary methods System.exit().", errorline);
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
	
	private static String XPATH=".//CatchStatement/Block/BlockStatement/Statement//StatementExpression/PrimaryExpression/PrimaryPrefix/Name[@Image='System.exit']";
	
	public static List<FSMMachineInstance> createUMIStateMachine(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result=null;		
		result=node.findXpath(XPATH);
		for(Object o:result){
			ASTName name=(ASTName)o;
			FSMMachineInstance fsminstance = fsm.creatInstance();
	        fsminstance.setResultString("Unnecessary Method Invoke");
		    fsminstance.setRelatedObject(new FSMRelatedCalculation(name));
		    list.add(fsminstance);
  
		}			
		return list;
	}
}
