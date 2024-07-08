package softtest.rules.java.safety.ECP;
import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;
import softtest.symboltable.java.NameDeclaration;
import softtest.symboltable.java.VariableNameDeclaration;
public class ECP_InnerPubStateMachine extends AbstractStateMachine{
	/**
	 * 设置故障描述
	 * 内部类返回了外部类的私有的变量
	 * 2009.09.21@baigele
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("封装问题模式: %d 行的内部类中函数返回了私有变量", errorline);
		}else{
			f.format("ECP: Public function in inner class return private array on line %d",errorline);
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
	public static List<FSMMachineInstance> createInnerPubStateMachines(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		String xPath = ".//ClassOrInterfaceDeclaration[@Nested='true']//MethodDeclaration[@Public='true']//PrimaryPrefix/Name[@MethodName='false']";
		List evaluationResults = node.findXpath(xPath);
		Iterator i = evaluationResults.iterator();
		while(i.hasNext())
		{
			ASTName name = (ASTName) i.next();
			NameDeclaration a = (NameDeclaration)name.getNameDeclaration();					
			VariableNameDeclaration vnd = (VariableNameDeclaration)a;	
			Node np=name.jjtGetParent();
			while(!(np instanceof ASTClassOrInterfaceDeclaration))
				np=np.jjtGetParent();
			ASTClassOrInterfaceDeclaration ac=(ASTClassOrInterfaceDeclaration)np;
			if(ac.isNested()){
				if(vnd==null||vnd.getDeclareScope()==null||vnd.getDeclareScope().isSelfOrAncestor(ac.getScope()))continue;				
				if(vnd.getAccessNodeParent()!=null){
					if(vnd.getAccessNodeParent().isPrivate()){
						FSMMachineInstance fsmInst = fsm.creatInstance();
						fsmInst.setRelatedObject(new FSMRelatedCalculation(name));
						fsmInst.setResultString("Public function in inner class return private variable:"+name.getImage());
						list.add(fsmInst);
					}
				}
			}
		}
		return list;
	}
}
