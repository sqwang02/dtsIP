package softtest.rules.java.question.PFMC;

import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;
import softtest.symboltable.java.VariableNameDeclaration;


/**
 *PNUStateMachine
 * 检查参变量未被使用过
 * 描述：参变量未被使用过。
 举例：
	1    class A{
	2       void foo(int i){
	3			
	6     };
	7   }


 * @author cjie
 * 
 */
public class PNUStateMachine extends AbstractStateMachine{
	@Override
	public  void registerPrecondition(PreconditionListenerSet listeners) {
	}
	
	@Override
	public  void registerFeature(FeatureListenerSet listenerSet) {
	}
	
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("参变量未使用: %d 行上定义的方法参数  \'%s\',从未被使用过", beginline,fsmmi.getResultString());
		}else{
			f.format("Parameter Never Used: the parameter \'%s\' in line %d never be used.", fsmmi.getResultString(),beginline);
		}
		fsmmi.setDescription(f.toString());
		f.close();
	}
	/**查找含有代码块的方法（即不是接口）*/
	private static String XPATH=".//MethodDeclaration[Block[count(*) >0]]/MethodDeclarator/FormalParameters";
	
	/**
	 * 功能： 创建参变量未被使用过状态机
	 *
	 * @param 
	 * @return List<FSMMachineInstance> 状态机实例列表 
	 * @throws
	 */
	public static List<FSMMachineInstance> createPNUStateMachine(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result=null;	
		/**处理参变量变量定义*/
		result=node.findXpath(XPATH);
		for(Object o:result){
			ASTFormalParameters params=(ASTFormalParameters)o;
			int paramCnt = params.jjtGetNumChildren();
			for(int i = 0; i < paramCnt; i++) {
				/**取得变量声明*/
				ASTFormalParameter param = (ASTFormalParameter) params.jjtGetChild(i);
			    ASTVariableDeclaratorId astVDeclId = (ASTVariableDeclaratorId) param.getFirstChildOfType(ASTVariableDeclaratorId.class);
			    VariableNameDeclaration decl=astVDeclId.getNameDeclaration();
			    if (decl == null)
			    	continue;
			    List occrList=decl.getOccs();
			    boolean found=false;
			    if(occrList==null||occrList.size()==0)
			    {
			    	FSMMachineInstance fsminstance = fsm.creatInstance();
		    		fsminstance.setResultString(astVDeclId.getImage());
					fsminstance.setRelatedObject(new FSMRelatedCalculation(astVDeclId));
					list.add(fsminstance);
			    }	
			}				   

		}			
	   return list;
	}
}
