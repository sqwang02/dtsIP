package softtest.rules.java.rule;

import java.util.*;
import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;



/**
 * ITLStateMachine
 * 检查标识符太长缺陷
 * 描述：类、方法以及变量名等的长度必须有一定限制，一般在2到25之间。
 * 
 * @author cjie
 * 
 */
public class ITLStateMachine extends AbstractStateMachine{
	
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("变量或类名长度不合法,第%d行：类、方法以及变量名等的长度必须有一定限制，一般在2到25之间.", errorline);
		}else{
			f.format("The Identifier is Too Long: The length of variable or class name is invalid at Line %d .", errorline);
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
	
	
	/**匹配类或接口定义*/
	private static final String CLASS_XPATH=".//ClassOrInterfaceDeclaration[string-length(@Image) <2 or string-length(@Image) >25]";
	
	/**匹配变量或字段定义*/
	private static final String VARIABLE_XPATH=".//VariableDeclaratorId[string-length(@Image) <2 or string-length(@Image) >25]";
	
	/**匹配方法定义*/
	private static final String METHOD_XPATH=".//MethodDeclarator[string-length(@Image) <2 or string-length(@Image) >25]";
	
	/**匹配包名*/
	private static final String PACKAGE_XPATH=".//PackageDeclaration/Name";
	
	/**
	 * 
	 *
	 * @param 
	 * @return List<FSMMachineInstance> 状态机实例列表
	 * @throws
	 */
	
	public static List<FSMMachineInstance> createITLStateMachine(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result=null;	
		/**类和接口*/
		result=node.findXpath(CLASS_XPATH);
		for(Object o:result){
			ASTClassOrInterfaceDeclaration decl=(ASTClassOrInterfaceDeclaration)o;
			
			FSMMachineInstance fsminstance = fsm.creatInstance();
	        fsminstance.setResultString("Class name is invalid");
		    fsminstance.setRelatedObject(new FSMRelatedCalculation(decl));
		    list.add(fsminstance);
  
		}
        /**变量和字段*/		
		result=node.findXpath(VARIABLE_XPATH);
		for(Object o:result){
			ASTVariableDeclaratorId decl=(ASTVariableDeclaratorId)o;
			
			FSMMachineInstance fsminstance = fsm.creatInstance();
	        fsminstance.setResultString("Variable name is invalid");
		    fsminstance.setRelatedObject(new FSMRelatedCalculation(decl));
		    list.add(fsminstance);
  
		}	
		/**方法*/		
		result=node.findXpath(METHOD_XPATH);
		for(Object o:result){
			ASTMethodDeclarator decl=(ASTMethodDeclarator)o;
			
			FSMMachineInstance fsminstance = fsm.creatInstance();
	        fsminstance.setResultString("Method name is invalid");
		    fsminstance.setRelatedObject(new FSMRelatedCalculation(decl));
		    list.add(fsminstance);
  
		}	
		/**方法*/		
		result=node.findXpath(PACKAGE_XPATH);
		for(Object o:result){
			ASTName decl=(ASTName)o;
			boolean isInvalid = false;
			if (null != decl && decl.getImage() != null) {
				String[] packages =decl.getImage().split("\\.");
				for (String s: packages) {
					if (s.length()<2 || s.length()>25) {
						isInvalid = true;
						break;
					}
				}
			}
			if (isInvalid) {
				FSMMachineInstance fsminstance = fsm.creatInstance();
		        fsminstance.setResultString("Package name is invalid");
			    fsminstance.setRelatedObject(new FSMRelatedCalculation(decl));
			    list.add(fsminstance);
			}
		}	
		return list;
	}

}
