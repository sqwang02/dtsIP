package softtest.rules.java.rule;

import java.util.*;
import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;



/**
 * ITLStateMachine
 * ����ʶ��̫��ȱ��
 * �������ࡢ�����Լ��������ȵĳ��ȱ�����һ�����ƣ�һ����2��25֮�䡣
 * 
 * @author cjie
 * 
 */
public class ITLStateMachine extends AbstractStateMachine{
	
	/**
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("�������������Ȳ��Ϸ�,��%d�У��ࡢ�����Լ��������ȵĳ��ȱ�����һ�����ƣ�һ����2��25֮��.", errorline);
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
	
	
	/**ƥ�����ӿڶ���*/
	private static final String CLASS_XPATH=".//ClassOrInterfaceDeclaration[string-length(@Image) <2 or string-length(@Image) >25]";
	
	/**ƥ��������ֶζ���*/
	private static final String VARIABLE_XPATH=".//VariableDeclaratorId[string-length(@Image) <2 or string-length(@Image) >25]";
	
	/**ƥ�䷽������*/
	private static final String METHOD_XPATH=".//MethodDeclarator[string-length(@Image) <2 or string-length(@Image) >25]";
	
	/**ƥ�����*/
	private static final String PACKAGE_XPATH=".//PackageDeclaration/Name";
	
	/**
	 * 
	 *
	 * @param 
	 * @return List<FSMMachineInstance> ״̬��ʵ���б�
	 * @throws
	 */
	
	public static List<FSMMachineInstance> createITLStateMachine(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		List result=null;	
		/**��ͽӿ�*/
		result=node.findXpath(CLASS_XPATH);
		for(Object o:result){
			ASTClassOrInterfaceDeclaration decl=(ASTClassOrInterfaceDeclaration)o;
			
			FSMMachineInstance fsminstance = fsm.creatInstance();
	        fsminstance.setResultString("Class name is invalid");
		    fsminstance.setRelatedObject(new FSMRelatedCalculation(decl));
		    list.add(fsminstance);
  
		}
        /**�������ֶ�*/		
		result=node.findXpath(VARIABLE_XPATH);
		for(Object o:result){
			ASTVariableDeclaratorId decl=(ASTVariableDeclaratorId)o;
			
			FSMMachineInstance fsminstance = fsm.creatInstance();
	        fsminstance.setResultString("Variable name is invalid");
		    fsminstance.setRelatedObject(new FSMRelatedCalculation(decl));
		    list.add(fsminstance);
  
		}	
		/**����*/		
		result=node.findXpath(METHOD_XPATH);
		for(Object o:result){
			ASTMethodDeclarator decl=(ASTMethodDeclarator)o;
			
			FSMMachineInstance fsminstance = fsm.creatInstance();
	        fsminstance.setResultString("Method name is invalid");
		    fsminstance.setRelatedObject(new FSMRelatedCalculation(decl));
		    list.add(fsminstance);
  
		}	
		/**����*/		
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
