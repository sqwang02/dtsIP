package softtest.rules.java.question.PFMC;

import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;
import softtest.symboltable.java.NameDeclaration;


/**
 * UMIGStateMachine
 * 检查调用了不必要的getClass()方法
 * 描述：为了调用对象的某个属性或方法而调用了getClass()方法，没有必要。应直接使用属性或者方法。
 举例：
   1   object.getClass().xx; // This is bad
   2   object.xx; // This is better



 * @author cjie
 * 
 */
public class UMIGStateMachine extends AbstractStateMachine{
	
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("使用了不必要的函数: %d 行上使用了不必要的函数getClass", errorline);
		}else{
			f.format("Used Unnecessary Method: " +
					" line %d Used unnecessary methods getClass().", errorline);
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
	
	private static String XPATH=".//PrimaryExpression/PrimaryPrefix[child::Name[matches(@Image,'.*\\.getClass') and following::PrimarySuffix[2][@Image='getMethod' or @Image='getField' ]]";
	
	public static List<FSMMachineInstance> createUMIGStateMachine(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result=null;		
		result=node.findXpath(XPATH);
		for(Object o:result){
			ASTPrimaryPrefix prefix=(ASTPrimaryPrefix) o;
			if(prefix.getFirstChildOfType(ASTName.class) instanceof ASTName)
			{
				ASTName name=(ASTName) prefix.getFirstChildOfType(ASTName.class);
			    NameDeclaration decl=name.getNameDeclaration();
			    if(!((ASTVariableDeclaratorId)decl.getNode()).getTypeNode().getTypeImage().equals("Class"))
			    {
					FSMMachineInstance fsminstance = fsm.creatInstance();
			        fsminstance.setResultString("Unnecessary Method Invoke:getClass()");
				    fsminstance.setRelatedObject(new FSMRelatedCalculation(name));
				    list.add(fsminstance);
			    }

			}
		}			
		return list;
	}
}
