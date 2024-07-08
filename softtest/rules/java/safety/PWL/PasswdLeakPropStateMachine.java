package softtest.rules.java.safety.PWL;

import softtest.fsm.java.*;
import softtest.jaxen.java.DocumentNavigator;

import java.util.*;

import org.jaxen.*;
import softtest.ast.java.*;
import softtest.jaxen.java.*;
import softtest.rules.java.AbstractStateMachine;
import softtest.ast.java.ASTArgumentList;
import softtest.ast.java.ASTExpression;
import softtest.ast.java.ASTLiteral;
import softtest.ast.java.ASTPrimaryExpression;
import softtest.ast.java.Node;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.config.java.Config;


/**
7����Ϊ������ַ����ɱ���������   
�����ַ�������ͨ���Դ��ļ��洢������������и��١�
����2-10�� ���г���
   1   public static void main(String[] args)throws SQLException {
   2   		Properties info = new Properties();
   3   		info.setProperty("user", "root");
   4   		info.setProperty("password", "^6nR$%_");  <----X
   5   		DriverManager.getConnection("jdbc:mysql://localhost:3307", info);
   6   }
 */

public class PasswdLeakPropStateMachine extends AbstractStateMachine{
	/**
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("��ȫ����ȱ��ģʽ: %d ��,Ӳ�������Ӳ�����һ������Ϊ������߼���ժҪ��Ӳ������ܻᱻ��ΪժҪ��һ���֣���Ϊ��Կ���ɵ���ʾ��", errorline);
		}else{
			f.format("Password Leak: Password Leak on line %d",errorline);
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

	public static List<FSMMachineInstance> createPasswdLeakStateMachines(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		String  xpathStr = ".//PrimaryExpression[./PrimaryPrefix/Name[@Image = '\\.setProperty']  and .//Literal[@Image='\"password\"'] ]";
		
		List evalRlts = null;
		try {
			XPath xpath = new BaseXPath(xpathStr, new DocumentNavigator());
			evalRlts = xpath.selectNodes(node);
		} catch (JaxenException e) {
			e.printStackTrace();
			throw new RuntimeException("xpath error",e);
		}
		if( evalRlts != null && evalRlts.size() > 0 ) {
			for(int i = 0; i < evalRlts.size(); i++ ) {
				logc1("evalRlts.size :" + evalRlts.size() );
				ASTPrimaryExpression pexpr = (ASTPrimaryExpression) evalRlts.get(i);
				ASTArgumentList args = (ASTArgumentList) pexpr.jjtGetChild(1).jjtGetChild(0).jjtGetChild(0);
				if( args.jjtGetNumChildren() == 2 ) {
					ASTExpression arg0 = (ASTExpression) args.jjtGetChild(0);
					ASTExpression arg1 = (ASTExpression) args.jjtGetChild(1);
					Node liter0 = arg0.jjtGetChild(0).jjtGetChild(0).jjtGetChild(0);
					if( liter0 instanceof ASTLiteral && ((ASTLiteral)liter0).getImage().equals("\"password\"") ) {
						Node  liter1 = arg1.jjtGetChild(0).jjtGetChild(0).jjtGetChild(0);
						if ( liter1 instanceof ASTLiteral ) {
							FSMMachineInstance fsmInst = fsm.creatInstance();
							fsmInst.setRelatedObject( new FSMRelatedCalculation((SimpleJavaNode)liter1) );
							list.add(fsmInst);
						} else {
							logc1("???????????  2ed arg is " + liter1 );
						}
					} else {
						logc1("liter0:" + liter0);
					}
				} else {
					logc1("????????????  args.size != 2");
				}
			}
		}
		return list;
	}
	
	
	/**
	 * This method is used to match :DriverManager.getConnection("", user, "pwd"); 
	 * this method may do nothing except return true.
	 */
	public static boolean checkPasswdLeakProp(List nodes, FSMMachineInstance fsmInst) {
		boolean found = false;
		if(nodes == null) {
			System.out.println("nodes is null, return");				
		}
		
		logc2("+-----------------+");
		logc2("|  passwdLeakProp | pwd_MingWen :" + fsmInst.getRelatedObject() );
		logc2("+-----------------+");
		found = true;
		fsmInst.setResultString("pwd text :" + fsmInst.getRelatedObject());
		
		return found;
	}

	
	public static void logc1(String str) {
		logc("createPasswdLeakStateMachines(..) - " + str);
	}
	public static void logc2(String str) {
		logc("checkPasswdLeakProp(..) - " + str);
	}

	public static void logc(String str) {
		if( Config.DEBUG ) {
			System.out.println("PasswdLeakPropStateMachine::" + str);
		}
	}
}
