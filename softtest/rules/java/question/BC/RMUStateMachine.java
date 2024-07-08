package softtest.rules.java.question.BC;

import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;

import softtest.ast.java.ASTWhileStatement;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;
import softtest.rules.java.AbstractStateMachine;



/**
 * RMUStateMachine
 * ���readLine()����������
 * ������readLine()�����У���������ø÷����ж��Ƿ�Ϊ�գ���ʹ��readLine()�õ�����ֵ����ʱ�Ѿ�������һ���ˣ��Ǵ����.
 * ������
   1   try {
   2   		BufferedReader in = new BufferedReader(new FileReader(��test.txt") );
   3   		while(in.readLine()!=null){//��ʱ�Ѿ�������ǰ��ĩβ
   4   			System.out.println(in.readLine());//��ʱ�Ѿ�������һ��
   5   		}
   6   		in.close();
   7   } catch (FileNotFoundException e) {
   8   		e.printStackTrace();
   9   }catch(IOException e){
   10   	e.printStackTrace();
   11  }


 * @author cjie
 * 
 */
public class RMUStateMachine extends AbstractStateMachine{
	
	/**
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("readLine()����������: ��%d ��readLine()����������.\n" +
					"readLine()�����У���������ø÷����ж��Ƿ�Ϊ�գ���ʹ��readLine()�õ�����ֵ����ʱ�Ѿ�������һ���ˣ��Ǵ����", errorline);
		}else{
			f.format("ReadLine() Method Misuse �� Line %d readLine() method misuse.", errorline);
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
	/**��������ֵ�ͱ�����ʼ��*/
	private static String XPATH=".//WhileStatement[Expression//EqualityExpression[@Image='!=']/PrimaryExpression/PrimaryPrefix/Name[matches(@Image,'.*\\.readLine$')] and Statement//Name[matches(@Image,'.*\\.readLine$')]]";
	
	/**
	 * 
	 *
	 * @param 
	 * @return List<FSMMachineInstance> ״̬��ʵ���б�
	 * @throws
	 */
	
	public static List<FSMMachineInstance> createRMUStateMachine(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result=null;		
		result=node.findXpath(XPATH);
		for(Object o:result){
			ASTWhileStatement name=(ASTWhileStatement) o;
			FSMMachineInstance fsminstance = fsm.creatInstance();
	        fsminstance.setResultString("ReadLine() Method Misuse");
		    fsminstance.setRelatedObject(new FSMRelatedCalculation(name));
		    list.add(fsminstance);
  
		}			
		return list;
	}

}
