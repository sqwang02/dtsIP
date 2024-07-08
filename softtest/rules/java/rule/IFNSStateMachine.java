package softtest.rules.java.rule;

import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;



/**
 * IFNSStateMachine
 * 检查标识符格式不规范
 * 描述：类、方法和变量应该遵循一定的格式,
 * 一般类名、接口名和常量名以大写字母开头，包名和变量名以小写字母开头。
 * 
 * @author cjie
 * 
 */
public class IFNSStateMachine extends AbstractStateMachine{
	
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("变量或类名命名不合法,第%d行：类、方法和变量应该遵循一定的格式，一般类名、接口名和常量名以大写字母开头，包名和变量名以小写字母开头.", errorline);
		}else{
			f.format("The Identifier Format is Not Standard: Variable or Class name is invalid at  Line %d .", errorline);
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
	/**类或接口名正则表达式*/
	private static final String CLASS_REGEX = "^[A-Z]{1}";
	
	/**变量或包名正则表达式*/
	private static final String VAR_AND_PACKAGE_REGEX = "^[a-z]+.*";
	
	/**变量正则表达式*/
	private static final String CONST_REGEX = "^[A-Z0-9_]+$";
	/**匹配类或接口定义*/
	private static final String CLASS_XPATH=".//ClassOrInterfaceDeclaration[not(matches(@Image,'" + CLASS_REGEX +"'))]";
	
	/**匹配变量或字段定义*/
	private static final String VARIABLE_XPATH=".//FieldDeclaration[@Final='false' or @Static='false']/VariableDeclarator/VariableDeclaratorId[not(matches(@Image,'" + VAR_AND_PACKAGE_REGEX +"'))]" +
	"|.//LocalVariableDeclaration/VariableDeclarator/VariableDeclaratorId[not(matches(@Image,'" + VAR_AND_PACKAGE_REGEX +"'))]";
	
	/**匹配方法定义*/
	private static final String METHOD_XPATH=".//MethodDeclarator[not(matches(@Image,'" + VAR_AND_PACKAGE_REGEX +"'))]";
	
	/**匹配包名*/
	private static final String PACKAGE_XPATH=".//PackageDeclaration/Name";
	/**匹配常量定义*/
	private static final String CONST_XPATH=".//FieldDeclaration[@Final='true' and @Static='true']/VariableDeclarator/VariableDeclaratorId[not(matches(@Image,'" + CONST_REGEX + "'))]";
	
	
	/**
	 * 
	 *
	 * @param 
	 * @return List<FSMMachineInstance> 状态机实例列表
	 * @throws
	 */
	
	public static List<FSMMachineInstance> createIFNSStateMachine(SimpleJavaNode node, FSMMachine fsm) {
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
		/**常量*/		
		result=node.findXpath(CONST_XPATH);
		for(Object o:result){
			ASTVariableDeclaratorId decl=(ASTVariableDeclaratorId)o;
			
			FSMMachineInstance fsminstance = fsm.creatInstance();
	        fsminstance.setResultString("constant name is invalid");
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
		/**包*/		
		result=node.findXpath(PACKAGE_XPATH);
		for(Object o:result){
			ASTName decl=(ASTName)o;
			boolean isInvalid = false;
			if (null != decl && decl.getImage() != null) {
				String[] packages =decl.getImage().split("\\.");
				for (String s: packages) {
					if (!s.matches(VAR_AND_PACKAGE_REGEX)) {
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
