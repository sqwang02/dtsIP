package softtest.rules.java.safety.ICE;

import softtest.fsm.java.*;
import softtest.jaxen.java.DocumentNavigator;

import java.util.*;

import org.jaxen.*;
import softtest.ast.java.*;
import softtest.jaxen.java.*;
import softtest.rules.java.AbstractStateMachine;
import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.ASTMethodDeclarator;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.config.java.Config;


/**
5. 垃圾回收器访问权限问题
对于一个类来说，其垃圾回收器应该是保护访问的，如果是public的，可能会被攻击，导致不正确的释放空间。
 public void finalize() throws Throwable {
  try {
   close();
  } finally {
   super.finalize();
  }
 }
 */

public class UnsafeFinalizeStateMachine extends AbstractStateMachine{
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("封装不当模式: %d 行垃圾回收器应该是protected", errorline);
		}else{
			f.format("Incorrect Encapsule:method finalize on line %d should be protected",errorline);
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

	public static List<FSMMachineInstance> createUnsafeFinalizeStateMachines(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		/* Find out the pattern " File xx = File.createTempFile(...) " */
		String  xpathStr = ".//MethodDeclarator[ @Image = 'finalize']";
		
		ASTMethodDeclaration mthdDecl;
		List evaluationResults = null;
		try {
			XPath xpath = new BaseXPath(xpathStr, new DocumentNavigator());
			evaluationResults = xpath.selectNodes(node);
		} catch (JaxenException e) {
			e.printStackTrace();
			throw new RuntimeException("xpath error",e);
		}
		if( evaluationResults != null && evaluationResults.size() == 1 ) {
			ASTMethodDeclarator pnode = (ASTMethodDeclarator)evaluationResults.get(0);
			ASTMethodDeclaration astMdecl = (ASTMethodDeclaration)pnode.jjtGetParent();
			if( astMdecl.isPublic() && !astMdecl.isStatic()  ) {
				FSMMachineInstance fsmInst = fsm.creatInstance();
				fsmInst.setRelatedObject( new FSMRelatedCalculation((SimpleJavaNode) evaluationResults.get(0) ) );
				list.add(fsmInst);
			}
			return list;
		}
		else {
			logc1(" ??????????? " + evaluationResults.size());
		}
		
		return list;
	}

	public static boolean checkQualifierPublic(List nodes, FSMMachineInstance fsmInst) {
		boolean found = false;
		if(nodes == null) {
			System.out.println("nodes is null, return");				
		}
		
		logc2("+---------------------------+");
		logc2("| public finalize() method  | method:" + fsmInst.getRelatedObject() );
		logc2("+---------------------------+");
		found = true;
		fsmInst.setResultString("public finalize() " + fsmInst.getRelatedObject());
		
		return found;
	}


	public static void logc1(String str) {
		logc("createUnsafeFinalizeStateMachines(..) - " + str);
	}
	public static void logc2(String str) {
		logc("checkQualifierPublic(..) - " + str);
	}
	
	public static void logc(String str) {
		if( Config.DEBUG ) {
			System.out.println("UnsafeFinalizeStateMachine::" + str);
		}
	}
}
