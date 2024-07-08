package softtest.rules.java.question.PFMC;

import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;
import softtest.symboltable.java.VariableNameDeclaration;


/**
 * LSCStateMachine
 * 检查使用了循环字符串连接
 * 描述：如果要在循环中进行字符串连接，为了提高性能应将String类型的变量转换成StringBuffer（JDK1.4以前版本，以后版本变为StringBuilder），再调用其append()方法，最后再转换成String类型。 
 * 举例：
   1   // This is bad
   2   String s = "";
   3   for (int i = 0; i < field.length; ++i) {
   4   		s = s + field[i];
   5   }

 * @author cjie
 * 
 */
public class LSCStateMachine extends AbstractStateMachine{
	
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("循环字符串连接: 第%d 行使用了循环字符串连接为了提高性能应将String类型的变量转换成StringBuffer（JDK1.4以前版本，以后版本变为StringBuilder），再调用其append()方法，最后再转换成String类型", errorline);
		}else{
			f.format("Loop String Concatenate： Line %d applyed loop string concatenate.", errorline);
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
	
	private static String XPATH=".//*[self::ForStatement or self::WhileStatement or self::DoStatement]/Statement//AssignmentOperator[(@Image='+=')or(@Image='=' and preceding-sibling::PrimaryExpression/PrimaryPrefix/Name/@Image=following-sibling::Expression/AdditiveExpression/PrimaryExpression/PrimaryPrefix/Name/@Image)]/preceding-sibling::PrimaryExpression/PrimaryPrefix/Name";
	
	/**
	 * 
	 *
	 * @param 
	 * @return List<FSMMachineInstance> 状态机实例列表
	 * @throws
	 */
	
	public static List<FSMMachineInstance> createLSCStateMachine(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result=null;		
		result=node.findXpath(XPATH);
		for(Object o:result){
			ASTName name=(ASTName)o;
			if(!(name.getNameDeclaration() instanceof VariableNameDeclaration))
			{
				continue;
			}
			VariableNameDeclaration v = (VariableNameDeclaration) name.getNameDeclaration();
			if (!v.getTypeImage().matches("^(String)$")) {
				continue;
			}
		    if(v.getNode()!=null&&
		    		(v.getNode().getParentsOfType(ASTForStatement.class).size()>0
		    				||v.getNode().getParentsOfType(ASTWhileStatement.class).size()>0
		    				||v.getNode().getParentsOfType(ASTDoStatement.class).size()>0))
		    {
		    	continue;
		    }
			FSMMachineInstance fsminstance = fsm.creatInstance();
	        fsminstance.setResultString("Loop String Concatenate");
		    fsminstance.setRelatedObject(new FSMRelatedCalculation(name));
		    list.add(fsminstance);
  
		}			
		return list;
	}

	public void f()
	{
		
	}
}
