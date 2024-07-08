package softtest.rules.java;

import softtest.fsm.java.*;
import softtest.jaxen.java.DocumentNavigator;

import java.util.*;

import org.jaxen.*;
import softtest.ast.java.*;
import softtest.jaxen.java.*;
import softtest.symboltable.java.NameDeclaration;
import softtest.symboltable.java.VariableNameDeclaration;
import softtest.ast.java.ASTBlockStatement;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTPrimaryExpression;
import softtest.ast.java.ASTVariableDeclarator;
import softtest.ast.java.ASTVariableDeclaratorId;
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

public class RiskOperStateMachine {

	public static List<FSMMachineInstance> createRiskOperStateMachines(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		/* Find out the pattern " File xx = File.createTempFile(...) " */
		String  xpathStr1 = ".//BlockStatement[./LocalVariableDeclaration/Type/ReferenceType/ClassOrInterfaceType[matches(@Image, 'File')]  and ./LocalVariableDeclaration/VariableDeclarator/VariableInitializer/Expression/PrimaryExpression/PrimaryPrefix/Name[matches(@Image, '.*File\\.createTempFile')]";
		/* Find out the pattern " xx = File.createTempFile(...) " */
		String  xpathStr2 = ".//BlockStatement[./Statement/StatementExpression/Expression/PrimaryExpression/PrimaryPrefix/Name[matches(@Image, '.*File\\.createTempFile')]  ]";
		String  xpathStrs [] = new String[2];
		xpathStrs[0] = xpathStr1;
		xpathStrs[1] = xpathStr2;

		for( int istr = 0; istr < 2; istr++ ) { // deal with all the xpath string

			String xpathStr = xpathStrs[ istr ];
			List evaluationResults = null;
			try {
				XPath xpath = new BaseXPath(xpathStr, new DocumentNavigator());
				evaluationResults = xpath.selectNodes(node);
			} catch (JaxenException e) {
				e.printStackTrace();
				throw new RuntimeException("xpath error",e);
			}

			if(evaluationResults.size() > 0) {
				FSMMachineInstance fsmInstance;
				Hashtable<VariableNameDeclaration, FSMMachineInstance> table = new Hashtable<VariableNameDeclaration, FSMMachineInstance>();
				for( int i = 0; i < evaluationResults.size(); i++ ) {
					if( istr == 0 ) { // LocalVariableDeclaration
						ASTVariableDeclarator vdecltor = (ASTVariableDeclarator) ( (ASTBlockStatement)evaluationResults.get( i ) ).jjtGetChild(0).jjtGetChild(1);
						VariableNameDeclaration varNameDecl = (( ASTVariableDeclaratorId ) vdecltor.jjtGetChild(0)).getNameDeclaration();
						fsmInstance = fsm.creatInstance();
						fsmInstance.setRelatedVariable( varNameDecl );
						table.put( varNameDecl, fsmInstance);
						
						logc1("put " + varNameDecl);
					}
					else if ( istr == 1 ) { // Name                         BlockStatement      /Statement  /StatementExpression /PrimaryExpression/PrimaryPrefix/Name
						ASTName name = (ASTName)((ASTBlockStatement)evaluationResults.get(i)).jjtGetChild(0).jjtGetChild(0).jjtGetChild(0).jjtGetChild(0).jjtGetChild(0);
						List<NameDeclaration> declList = name.getNameDeclarationList();
						Iterator<NameDeclaration> declIter = declList.iterator();
						while( declIter.hasNext() ) {
							NameDeclaration nameDecl = declIter.next();
							if( nameDecl instanceof VariableNameDeclaration ) {
								VariableNameDeclaration v = (VariableNameDeclaration) nameDecl;
								if( ! table.containsKey(v) ) {
									fsmInstance = fsm.creatInstance();
									fsmInstance.setRelatedVariable( v );
									table.put(v, fsmInstance);
									
									logc1("put " + v);
								}
							} else {
								logc1("NameDeclaration not instanceof VariableNameDeclaration :" + nameDecl);
							}
						}
						logc1("declList:" + declList.size());
					}
				}
				for( Enumeration<FSMMachineInstance> e = table.elements(); e.hasMoreElements(); ) {
					list.add( e.nextElement() );
				}
			} else {
				logc1("No RiskOperStateMachine created.");
			}

		}

		return list;
	}

	public static boolean checkRiskOperation(List nodes, FSMMachineInstance fsmInst) {
		boolean found = false;
		if(nodes == null) {
			System.out.println("nodes is null, return");				
		}
		VariableNameDeclaration  v = fsmInst.getRelatedVariable();
		/**  tmpXpathHead + tempFileVarName + tmpXpathTail ->  xpath statement  */
		String tmpXpathHead = ".//PrimaryExpression[./PrimarySuffix/Arguments/ArgumentList/Expression//Name[matches(@Image, '";
		String tmpXpathTail = "')] ]";
		String tempFileVarName;
		
		int size = nodes.size();
		logc2("nodes.size = " + size);
		try{
			for(int i = 0; i < size; i++) {
				tempFileVarName = v.getImage();
				String strXpath = tmpXpathHead + tempFileVarName + tmpXpathTail;
				XPath xpath = new BaseXPath( strXpath, new DocumentNavigator());
				logc2("Applying ||" + strXpath + " to " + nodes.get(i));
				List  evalRes = xpath.selectNodes( nodes.get(i) );
				if( evalRes.size() > 0 ) { // Should equal to 1 
					ASTPrimaryExpression expr = (ASTPrimaryExpression)evalRes.get(0);
					
					logc2("+--------------------------+");
					logc2("| TempFileOutput RiskOper  | var:" +  tempFileVarName );
					logc2("+--------------------------+");
					found = true;
					fsmInst.setRelatedObject( new FSMRelatedCalculation((SimpleJavaNode)nodes.get(i)) );
				}
			}
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		return found;
	}


	public static void logc1(String str) {
		logc("createRiskOperStateMachines(..) - " + str);
	}
	public static void logc2(String str) {
		logc("checkRiskOperation(..) - " + str);
	}
	
	public static void logc(String str) {
		if( Config.DEBUG ) {
			System.out.println("RiskOperStateMachine::" + str);
		}
	}
}
