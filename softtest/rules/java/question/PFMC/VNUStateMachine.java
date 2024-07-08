package softtest.rules.java.question.PFMC;

import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;
import softtest.symboltable.java.NameOccurrence;
import softtest.symboltable.java.VariableNameDeclaration;


/**
 * VNUStateMachine
 * 检查没有被使用过的变量
 * 描述：变量没有被读取，有的变量在定义后就未被调用过，有的变量定义后重新赋值但未被使用过，这样的变量都属于不良的代码，会对软件系统的性能有不良的影响，应该避免这种现象发生。
 举例：
	1    class A{
	2       void foo(int i){
	3			……
	4 			int var;
	5  			……
	6     };
	7   }


 * @author cjie
 * 
 */
public class VNUStateMachine extends AbstractStateMachine{
	
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("变量未使用: %d 行上的变量  \'%s\' 从未被使用过", errorline,fsmmi.getResultString());
		}else{
			f.format("Variable Never Used: the variable \'%s\' in line %d never be used.", fsmmi.getResultString(),errorline);
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
	
	private static String XPATH=".//LocalVariableDeclaration/VariableDeclarator/VariableDeclaratorId";
	
	/**
	 * 功能： 创建没有被使用过的变量状态机
	 *
	 * @param 
	 * @return List<FSMMachineInstance> 状态机实例列表 
	 * @throws
	 */
	public static List<FSMMachineInstance> createVNUStateMachine(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result=null;	
		/**处理变量定义*/
		result=node.findXpath(XPATH);
		For1:
		for(Object o:result){
			ASTVariableDeclaratorId declNode=(ASTVariableDeclaratorId)o;
			/**如果是静态初始化代码则不进行处理*/
			List<ASTInitializer> staticBlock=declNode.getParentsOfType(ASTInitializer.class);
			for( ASTInitializer init:staticBlock)
			{
				if(init.isStatic())
					continue For1;
			}
			/**取得变量声明*/
			VariableNameDeclaration decl=declNode.getNameDeclaration();
		    List<NameOccurrence> occrList=decl.getOccs();
		    if(occrList==null||occrList.size()==0)
		    {
		    	FSMMachineInstance fsminstance = fsm.creatInstance();
	    		fsminstance.setResultString(declNode.getImage());
				fsminstance.setRelatedObject(new FSMRelatedCalculation(declNode));
				list.add(fsminstance);
		    }
		    else 
		    {
		    	NameOccurrence occ=null;
		    	boolean use=false;
		    	for(Object t:occrList){
		    		occ=(NameOccurrence)t;
		    		if(occ.getOccurrenceType()==NameOccurrence.OccurrenceType.USE||occ.isSelfAssignment()){
		    			use=true;
		    			break;
		    		}
		    	}
		    	if(!use){
					FSMMachineInstance fsminstance = fsm.creatInstance();
		    		fsminstance.setResultString(declNode.getImage());
					fsminstance.setRelatedObject(new FSMRelatedCalculation((SimpleJavaNode)occ.getLocation()));
		    	   //fsminstance.setRelatedObject(new FSMRelatedCalculation(declNode));
					list.add(fsminstance);
		    	}
		    }
		    	
		}			
	   return list;
	}
}
