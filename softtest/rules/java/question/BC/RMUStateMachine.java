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
 * 检查readLine()方法的误用
 * 描述：readLine()方法中，如果先利用该方法判断是否为空，再使用readLine()拿到返回值，这时已经读到下一行了，是错误的.
 * 举例：
   1   try {
   2   		BufferedReader in = new BufferedReader(new FileReader(“test.txt") );
   3   		while(in.readLine()!=null){//此时已经读到当前行末尾
   4   			System.out.println(in.readLine());//此时已经读到下一行
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
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("readLine()方法的误用: 第%d 行readLine()方法的误用.\n" +
					"readLine()方法中，如果先利用该方法判断是否为空，再使用readLine()拿到返回值，这时已经读到下一行了，是错误的", errorline);
		}else{
			f.format("ReadLine() Method Misuse ： Line %d readLine() method misuse.", errorline);
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
	/**方法返回值和变量初始化*/
	private static String XPATH=".//WhileStatement[Expression//EqualityExpression[@Image='!=']/PrimaryExpression/PrimaryPrefix/Name[matches(@Image,'.*\\.readLine$')] and Statement//Name[matches(@Image,'.*\\.readLine$')]]";
	
	/**
	 * 
	 *
	 * @param 
	 * @return List<FSMMachineInstance> 状态机实例列表
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
