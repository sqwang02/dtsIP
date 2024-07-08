package softtest.rules.java;

import java.util.LinkedList;
import java.util.List;

import org.jaxen.BaseXPath;
import org.jaxen.JaxenException;
import org.jaxen.XPath;

import softtest.ast.java.ASTName;
import softtest.ast.java.ASTPrimaryExpression;
import softtest.ast.java.ASTStatementExpression;
import softtest.ast.java.ASTVariableDeclarator;
import softtest.ast.java.ASTVariableDeclaratorId;
import softtest.ast.java.SimpleJavaNode;
import softtest.config.java.Config;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsmanalysis.java.ProjectAnalysis;
import softtest.jaxen.java.DocumentNavigator;
import softtest.symboltable.java.VariableNameDeclaration;

public class TmpFileDelStateMachine {


	public static List<FSMMachineInstance> createTmpFileDelStateMachines(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();

		String  xpathStr = ".//Name[ matches(@Image, '\\.+createTempFile') ]";
		List evalRlts = findTreeNodes(node , xpathStr);

		if( evalRlts == null || evalRlts.size() == 0) {
			logc1("No Name[ matches(@Image, '\\.+createTempFile')] found");
		}
		for( int i = 0; i < evalRlts.size(); i++ ) {
			ASTName  astName = (ASTName) evalRlts.get(i);

			FSMMachineInstance fsmInst;
			AliasSet alias = new AliasSet();
			alias.setResource( astName );
			alias.setResouceName( astName.printNode(ProjectAnalysis.getCurrent_file()) );
			
			logc1("create AliasSet :" + astName.printNode(ProjectAnalysis.getCurrent_file()));
			
			fsmInst = fsm.creatInstance();
			fsmInst.setRelatedObject( alias );
			list.add( fsmInst );
		}
		return list;
	}

	/**  File xx = File.createTempFile("", "");
	 * */
	public static boolean checkSameTmpFile(List nodes, FSMMachineInstance fsmInst) {
		logc2("------ Begin\n" + fsmInst.getStates() );
		boolean found = false;
		if(nodes == null || nodes.size() == 0) {
			System.out.println("nodes has nothing, return");
		}
		AliasSet  alias = (AliasSet) fsmInst.getRelatedObject();
		if( alias.getResource() != nodes.get(0) ) {
			return false;
		}
		ASTVariableDeclarator astVdecltor;
		SimpleJavaNode parent = (SimpleJavaNode)nodes.get(0);
		while( parent != null && ! (parent instanceof ASTVariableDeclarator) ) {
			parent = (SimpleJavaNode)parent.jjtGetParent();
		}
		if( parent != null ) {
			astVdecltor = (ASTVariableDeclarator) parent;
			ASTVariableDeclaratorId astVdeclId = (ASTVariableDeclaratorId) astVdecltor.jjtGetChild(0);
			alias.add( astVdeclId.getNameDeclaration() );
			found = true;
		} else {
			parent = (SimpleJavaNode)nodes.get(0);
			while( parent != null && ! (parent instanceof ASTStatementExpression) ) {
				parent = (SimpleJavaNode)parent.jjtGetParent();
			}
			if(parent != null) {
				ASTPrimaryExpression astPrimExpr = (ASTPrimaryExpression) parent.jjtGetChild(0);
				ASTName astName = (ASTName)astPrimExpr.jjtGetChild(0).jjtGetChild(0);
				alias.add((VariableNameDeclaration)astName.getNameDeclaration());
				found = true;
			}
		}

		logc2("------- End");
		return found;
	}


	/**
	 * **/
	public static boolean checkTmpFileDeleted(List nodes, FSMMachineInstance fsmInst) {
		boolean  deleted = false;
		
		if( nodes.size() > 1 ) {
			return false;
		}
		AliasSet  alias = (AliasSet) fsmInst.getRelatedObject();
	
		ASTName astName = (ASTName) nodes.get(0);
		VariableNameDeclaration vdecl = null;
		if( ! (astName.getNameDeclaration() instanceof VariableNameDeclaration)) {
			return false;
		}
		vdecl = (VariableNameDeclaration) astName.getNameDeclaration();
		if( ! alias.contains(vdecl) ) {
			return false;
		}
		deleted = true;
		logc3("");
		return  deleted;
	}
	
	/**
	 * 
	 */
	public static boolean checkTmpFileNotNull(List nodes, FSMMachineInstance fsmInst) {
		boolean  found = false;
		
		if( nodes.size() > 1 ) {
			return false;
		}
		AliasSet alias = (AliasSet) fsmInst.getRelatedObject();
		
		ASTName astName = (ASTName) nodes.get(0);
		VariableNameDeclaration vdecl = null;
		if( ! (astName.getNameDeclaration() instanceof VariableNameDeclaration)) {
			return false;
		}
		vdecl = (VariableNameDeclaration) astName.getNameDeclaration();
		if( alias.contains(vdecl) ) {
			found = true;
		}
		
		if( found ) {
			logc4("+-----------------+");
			logc4("|  TmpFileDelete  | " + fsmInst.getRelatedObject());
			logc4("+-----------------+");
			fsmInst.setResultString("TmpFileDelete:" + fsmInst.getRelatedObject().toString());
		}
		return  found;
	}
	
	
	private static List findTreeNodes(SimpleJavaNode node, String xPath) {
		List evaluationResults = null;
		try {
			XPath xpath = new BaseXPath(xPath, new DocumentNavigator());
			evaluationResults = xpath.selectNodes(node);
		} catch (JaxenException e) {
			// e.printStackTrace();
			throw new RuntimeException("xpath error",e);
		}
		return evaluationResults;
	}
	

	public static void logc1(String str) {
		logc("createTmpFileDelStateMachines(..) - " + str);
	}
	public static void logc2(String str) {
		logc("checkSameTmpFile(..) - " + str);
	}
	public static void logc3(String str) {
		logc("checkTmpFileDeleted(..) - " + str);
	}
	public static void logc4(String str) {
		logc("checkTmpFileNotNull(..) - " + str);
	}
	public static void logc(String str) {
		if( Config.DEBUG ) {
			System.out.println("TmpFileDelStateMachine::" + str);
		}
	}

	public static void main(String args[] ) {
		String  str = "System.out.println;";
		String strs[] = str.split("\\.");
		for( String s : strs ) {
			System.out.println(s);
		}
		str = "\"ab\"";
		System.out.println( str.substring(1, str.length() - 1)  );
	}
}
