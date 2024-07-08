package softtest.rules.java.rule;

import java.util.*;

import softtest.ast.java.*;
import softtest.callgraph.java.method.*;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;



/**
 * IFNSStateMachine
 * ����ʶ����ʽ���淶
 * �������ࡢ�����ͱ���Ӧ����ѭһ���ĸ�ʽ,
 * һ���������ӿ����ͳ������Դ�д��ĸ��ͷ�������ͱ�������Сд��ĸ��ͷ��
 * 
 * @author cjie
 * 
 */
public class IFNSStateMachine extends AbstractStateMachine{
	
	/**
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("�����������������Ϸ�,��%d�У��ࡢ�����ͱ���Ӧ����ѭһ���ĸ�ʽ��һ���������ӿ����ͳ������Դ�д��ĸ��ͷ�������ͱ�������Сд��ĸ��ͷ.", errorline);
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
	/**���ӿ���������ʽ*/
	private static final String CLASS_REGEX = "^[A-Z]{1}";
	
	/**���������������ʽ*/
	private static final String VAR_AND_PACKAGE_REGEX = "^[a-z]+.*";
	
	/**����������ʽ*/
	private static final String CONST_REGEX = "^[A-Z0-9_]+$";
	/**ƥ�����ӿڶ���*/
	private static final String CLASS_XPATH=".//ClassOrInterfaceDeclaration[not(matches(@Image,'" + CLASS_REGEX +"'))]";
	
	/**ƥ��������ֶζ���*/
	private static final String VARIABLE_XPATH=".//FieldDeclaration[@Final='false' or @Static='false']/VariableDeclarator/VariableDeclaratorId[not(matches(@Image,'" + VAR_AND_PACKAGE_REGEX +"'))]" +
	"|.//LocalVariableDeclaration/VariableDeclarator/VariableDeclaratorId[not(matches(@Image,'" + VAR_AND_PACKAGE_REGEX +"'))]";
	
	/**ƥ�䷽������*/
	private static final String METHOD_XPATH=".//MethodDeclarator[not(matches(@Image,'" + VAR_AND_PACKAGE_REGEX +"'))]";
	
	/**ƥ�����*/
	private static final String PACKAGE_XPATH=".//PackageDeclaration/Name";
	/**ƥ�䳣������*/
	private static final String CONST_XPATH=".//FieldDeclaration[@Final='true' and @Static='true']/VariableDeclarator/VariableDeclaratorId[not(matches(@Image,'" + CONST_REGEX + "'))]";
	
	
	/**
	 * 
	 *
	 * @param 
	 * @return List<FSMMachineInstance> ״̬��ʵ���б�
	 * @throws
	 */
	
	public static List<FSMMachineInstance> createIFNSStateMachine(SimpleJavaNode node, FSMMachine fsm) {
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
		result=node.findXpath(CONST_XPATH);
		for(Object o:result){
			ASTVariableDeclaratorId decl=(ASTVariableDeclaratorId)o;
			
			FSMMachineInstance fsminstance = fsm.creatInstance();
	        fsminstance.setResultString("constant name is invalid");
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
		/**��*/		
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
