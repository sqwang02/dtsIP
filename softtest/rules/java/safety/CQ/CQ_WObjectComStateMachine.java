package softtest.rules.java.safety.CQ;
import java.util.*;

import softtest.ast.java.ASTName;
import softtest.ast.java.ASTPrimaryExpression;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;
import softtest.rules.java.AbstractStateMachine;
import softtest.symboltable.java.VariableNameDeclaration;
public class CQ_WObjectComStateMachine extends AbstractStateMachine{
	/**
	 * 设置故障描述
	 * Object比较
	 * Object使用“==”进行比较，比较的为Obejct的引用
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("代码质量CQ: %d 行程序采用 == 或 != 来比较对象是否相等，可能造成一个漏洞，建议用equals比较。程序采用 == 或 != 来比较两个对象是否相同，其实质比较的是两个引用，而不是对象的内容。", errorline);
		}else{
			f.format("Code Quality: program uses == or != to compara two objects on line %d",errorline);
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
	public static List<FSMMachineInstance> createWObjectComStateMachines(SimpleJavaNode node, FSMMachine fsm)
	{
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		String xpath = "";
		String xp = "";
		List evalRlts = null;
		List result=null;
		Iterator i = null;
		Iterator j = null;
		xpath = ".//EqualityExpression/PrimaryExpression/PrimaryPrefix/Name[@MethodName='false']";
		
		evalRlts = node.findXpath(xpath);
		i = evalRlts.iterator();
		while(i.hasNext())
		{
			ASTName name = (ASTName) i.next();
			VariableNameDeclaration vName=(VariableNameDeclaration)name.getNameDeclaration();		
			if(vName==null||vName.getType()==null||vName.getType().toString().contains("java.lang.String"))continue;
			if(!vName.getTypeImage().contains("java.lang")){
				if(vName.getType().getSuperclass()!=null){
					if(vName.getType().getSuperclass().toString().contains("java.lang.Object")){
						xp=".//EqualityExpression[PrimaryExpression[1]/PrimaryPrefix[Name[@MethodName='false'][@Image='";
						xp=xp+name.getImage()+"']]]/PrimaryExpression[2]/PrimaryPrefix/Name[@MethodName='false']";
						result=node.findXpath(xp);
						j=result.iterator();
						while(j.hasNext()){	
							ASTName nj = (ASTName) j.next();
							VariableNameDeclaration vnj=(VariableNameDeclaration)nj.getNameDeclaration();
							if(vnj==null||vnj.getType()==null)continue;
							if(!vnj.getTypeImage().contains("java.lang")){
								if(vnj.getType().getSuperclass()!=null){
									if(vnj.getType().getSuperclass().toString().contains("java.lang.Object")){
										FSMMachineInstance fsmInst = fsm.creatInstance();
										fsmInst.setRelatedObject(new FSMRelatedCalculation(name));
										fsmInst.setResultString(" Program uses == or != to compara two Object");
										list.add(fsmInst);
									}
								}
							}
						}		
					}
				}	
			}	
		}
		return list;
	}
}
