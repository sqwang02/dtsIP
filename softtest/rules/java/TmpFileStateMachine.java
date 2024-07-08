package softtest.rules.java;

import softtest.fsm.java.*;
import softtest.fsmanalysis.java.ProjectAnalysis;
import softtest.jaxen.java.DocumentNavigator;

import java.util.*;

import org.jaxen.*;

import softtest.ast.java.*;
import softtest.jaxen.java.*;
import softtest.symboltable.java.NameDeclaration;
import softtest.symboltable.java.VariableNameDeclaration;
import softtest.ast.java.ASTExpression;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTPrimarySuffix;
import softtest.ast.java.ASTStatementExpression;
import softtest.ast.java.ASTVariableDeclaratorId;
import softtest.ast.java.ASTVariableInitializer;
import softtest.ast.java.SimpleJavaNode;
import softtest.config.java.Config;


/**
 * 1．内部或临时文件的文件名问题
当文件名为内部文件名或者临时文件名并打印给用户。
1   protected void doPost(HttpServletRequest req,HttpServletResponse resp) throws ServletException,IOException {
2   	// use user Name as uniq prefix
3   	String debug = req.getParameter("debug");
4   	File file = File.createTempFile("aaa", ".tmp");
5   	if (debug.equals("true"))
6   		resp.getOutputStream().print("Using " + file.toString());
                                                     ^-- ( Error )
7  	 	FileOutputStream st = new FileOutputStream(file);
8   	st.close();
9   	file.delete();
10   }
 */

public class TmpFileStateMachine {

	private static List getTreeNode(SimpleJavaNode node, String xpathStr) {
		List evalRlts = null;
		try {
			XPath xpath = new BaseXPath(xpathStr, new DocumentNavigator());
			evalRlts = xpath.selectNodes(node);
		} catch (JaxenException e) {
			e.printStackTrace();
			throw new RuntimeException("xpath error",e);
		}
		return evalRlts;
	}
	
	public static List<FSMMachineInstance> createTmpFileStateMachines(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		String  xstr = ".//Expression[ ./PrimaryExpression/PrimaryPrefix/Name[ matches(@Image, '\\.createTempFile') ] ]";
		List evalRlts = getTreeNode(node, xstr);

		for(int i = 0; i < evalRlts.size(); i++) {
			FSMMachineInstance fsmInstance;
			ASTExpression astExpr = (ASTExpression)evalRlts.get(i);
			fsmInstance = fsm.creatInstance();
			AliasSet alias = new AliasSet();
			alias.setResource(astExpr);
			alias.setResouceName( ((SimpleJavaNode)astExpr.jjtGetChild(0).jjtGetChild(1).jjtGetChild(0)).printNode(ProjectAnalysis.getCurrent_file()) );
			fsmInstance.setRelatedObject(alias);
			list.add(fsmInstance);
		}
		logc1("  " + list.size());
		return list;
	}

	public static boolean checkTheSameResource(List nodes, FSMMachineInstance fsmInst) {
		boolean found = false;
		if(nodes == null) {
			logc2("nodes is null, return");				
		}
		AliasSet alias = (AliasSet)fsmInst.getRelatedObject();
		ASTExpression astExpr = null;
		for(int i = 0; i < nodes.size(); i++) {
			if(alias.getResource().equals(nodes.get(i))) {
				found = true;
				astExpr = (ASTExpression)nodes.get(i);
				break;
			}
		}
		if( ! found ) {
			return false;
		}
		VariableNameDeclaration  v = fsmInst.getRelatedVariable();
		
		int size = nodes.size();
		
		logc2("" + nodes.size());
		while(astExpr instanceof ASTExpression && astExpr.jjtGetParent() instanceof ASTExpression) {
			if(astExpr.jjtGetParent().jjtGetNumChildren() > 1) {
				if(astExpr.jjtGetParent().jjtGetChild(0).jjtGetChild(0) instanceof ASTName) {
					ASTName astN = (ASTName)astExpr.jjtGetChild(0).jjtGetChild(0).jjtGetChild(0);
					NameDeclaration ndecl = astN.getNameDeclaration();
					if(ndecl instanceof VariableNameDeclaration) {
						alias.add((VariableNameDeclaration)ndecl);
					}
				}
			}
			astExpr = (ASTExpression) astExpr.jjtGetParent();
		}		
		if(astExpr.jjtGetParent() instanceof ASTVariableInitializer) {
			ASTVariableDeclaratorId astVid = (ASTVariableDeclaratorId)astExpr.jjtGetParent().jjtGetParent().jjtGetChild(0);
			alias.add( astVid.getNameDeclaration() );
		}
		else if(astExpr.jjtGetParent() instanceof ASTStatementExpression) {
			if(astExpr.jjtGetParent().jjtGetNumChildren() > 1) {
				if(astExpr.jjtGetChild(0).jjtGetChild(0).jjtGetChild(0) instanceof ASTName) {
					ASTName astN = (ASTName)astExpr.jjtGetChild(0).jjtGetChild(0).jjtGetChild(0);
					NameDeclaration ndecl = astN.getNameDeclaration();
					if(ndecl instanceof VariableNameDeclaration) {
						alias.add((VariableNameDeclaration)ndecl);
					}
				}
			}
		}
		
		return true;
	}

	public static boolean checkNameInAliasSet(List nodes, FSMMachineInstance fsmInst) {
		AliasSet alias = (AliasSet)fsmInst.getRelatedObject();
		ASTExpression astExpr = (ASTExpression)alias.getResource();
		for(int i = 0; i < nodes.size(); i++) {
			SimpleJavaNode node = (SimpleJavaNode)nodes.get(i);
			if(node.jjtGetNumChildren() < 2) {
				continue;
			}
			if(node.jjtGetChild(node.jjtGetNumChildren()-1) instanceof ASTPrimarySuffix) {
				ASTPrimarySuffix astSuf = (ASTPrimarySuffix)node.jjtGetChild(node.jjtGetNumChildren()-1);
				List eval = getTreeNode(astSuf, ".//Name");
				for(int j = 0; j < eval.size(); j++ ) {
					ASTName name = (ASTName)eval.get(j);
					if( name.getNameDeclaration() instanceof VariableNameDeclaration ) {
						if(alias.contains((VariableNameDeclaration)name.getNameDeclaration())) {
							return true;
						}
					}
				}
			}
		}
		return  false;
	}

	public static void logc1(String str) {
		logc("createTmpFileStateMachines(..) - " + str);
	}
	public static void logc2(String str) {
		logc("checkTheSameResource(..) - " + str);
	}
	public static void logc3(String str) {
		logc("checkNameInAliasSet(..) - " + str);
	}
	public static void logc(String str) {
		if( Config.DEBUG ) {
			System.out.println("TmpFileStateMachine::" + str);
		}
	}
}
