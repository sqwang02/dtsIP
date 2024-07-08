/**
 * 
 */
package softtest.rules.java.safety.AAPI;

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
import softtest.symboltable.java.VariableNameDeclaration;

/**
 * @author pengpinglei
 *
 */
public class AbuseAPI_ArrayToStringStateMachine extends AbstractStateMachine
{
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("滥用API模式:在 %d 行使用Array对象的toString()方法。对数组使用toString()方法，返回的不是数组的内容，而是数组的类型和哈希码。", errorline);
		}else{
			f.format("Abuse Application Program Interface: User toString() for Array on line %d.The contents returned are not the content of Array, them are the Types and Hash code of the Array ",errorline);
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
	public static List<FSMMachineInstance> createArrayToStringStateMachines(SimpleJavaNode node, FSMMachine fsm)
	{
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		
		/**
		 * String [] s = new String[100];
		 * s.toString();
		 */
		String xpath = " .//PrimaryExpression/PrimaryPrefix/Name[@TypeString='public java.lang.String java.lang.Object.toString()'] ";
		
		List evalRlts = node.findXpath(xpath);
		Iterator i = evalRlts.iterator();
		
		while(i.hasNext())
		{
			ASTName name = (ASTName) i.next();
			if (!(name.getNameDeclaration() instanceof VariableNameDeclaration)) {
				continue;
			}
			VariableNameDeclaration v = (VariableNameDeclaration) name.getNameDeclaration();
			if(v.isArray())
			{
				FSMMachineInstance fsmInst = fsm.creatInstance();
				fsmInst.setRelatedObject(new FSMRelatedCalculation(name));
				fsmInst.setResultString("use array's toString(): " + name.getTypeString());
				list.add(fsmInst);
			}
		}
		
		return list;
	}

}
