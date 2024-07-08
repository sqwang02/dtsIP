package softtest.rules.java;

import softtest.fsm.java.*;
import softtest.jaxen.java.DocumentNavigator;

import java.util.*;

import org.jaxen.*;

import softtest.ast.java.*;
import softtest.symboltable.java.*;
import softtest.jaxen.java.*;
import softtest.symboltable.java.VariableNameDeclaration;
import softtest.ast.java.ASTFormalParameter;
import softtest.ast.java.ASTFormalParameters;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTPrimaryExpression;
import softtest.ast.java.ASTPrimaryPrefix;
import softtest.ast.java.ASTStatementExpression;
import softtest.ast.java.ASTVariableDeclaratorId;
import softtest.ast.java.SimpleJavaNode;
import softtest.cfg.java.*;
import softtest.config.java.Config;


public class PASStateMachine {

	public static List<FSMMachineInstance> createPASStateMachines(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		String xPath = ".//MethodDeclarator/FormalParameters";
		List evaluationResults = null;
		try {
			XPath xpath = new BaseXPath(xPath, new DocumentNavigator());
			evaluationResults = xpath.selectNodes(node);
		} catch (JaxenException e) {
			e.printStackTrace();
			throw new RuntimeException("xpath error",e);
		}

		if(evaluationResults.size() > 0) {
			ASTFormalParameters params = (ASTFormalParameters) evaluationResults.get(0);
			int paramCnt = params.jjtGetNumChildren();
			logc1("PASStateMachine: paramCnt = " + paramCnt);
			for(int i = 0; i < paramCnt; i++) {
				ASTFormalParameter param = (ASTFormalParameter) params.jjtGetChild(i);
				ASTVariableDeclaratorId astVDeclId = (ASTVariableDeclaratorId) param.jjtGetChild(1);
				VariableNameDeclaration vDecl = astVDeclId.getNameDeclaration();
				FSMMachineInstance fsmInstance = fsm.creatInstance();
				fsmInstance.setRelatedVariable( vDecl );
				fsmInstance.setRelatedObject( new FSMRelatedCalculation( astVDeclId ) );
				list.add(fsmInstance);
			}
		} else {
			logc1("No PASStateMachine created.");
		}
		return list;
	}

	public static boolean checkChildAndAssignOperation(List nodes, 	FSMMachineInstance fsmInst) {
		boolean found = false;
		if (nodes == null) {
			logc2("nodes is null, return");
		}
		int size = nodes.size();
		logc2("nodes.size = " + size);
		try {
			for (int i = 0; i < size; i++) {
				ASTStatementExpression st = (ASTStatementExpression) nodes.get(i);
				ASTPrimaryExpression left = (ASTPrimaryExpression) st.jjtGetChild(0);
				ASTPrimaryPrefix leftPrefix = (ASTPrimaryPrefix) left.jjtGetChild(0);
				ASTName leftName = (ASTName) leftPrefix.jjtGetChild(0);

				VariableNameDeclaration vDecl = fsmInst.getRelatedVariable();
				String param = vDecl.getImage();
				logc2("leftName:" + leftName.getImage() + "  param:" + param);
				if (leftName.getImage().compareTo(param) == 0) {
					logc2("+---------------------+");
					logc2("| Assign to Parameter |  param:" + param);
					logc2("+---------------------+");
					found = true;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return found;
	}
	
	public static void logc1(String str) {
		logc("createPASStateMachine(..) - " + str);
	}
	public static void logc2(String str) {
		logc("checkChildAndAssignOperation(..) - " + str);
	}
	public static void logc(String str) {
		if( Config.DEBUG ) {
			System.out.println("PASStateMachine::" + str);
		}
	}
}


class ParamAssignCal extends FSMRelatedCalculation {

	public ParamAssignCal(){
	}
	
	public ParamAssignCal(FSMRelatedCalculation o) {
		super(o);
		if(!(o instanceof ParamAssignCal)){
			return;
		}
		ParamAssignCal t=(ParamAssignCal)o;
	}
	
	/** 拷贝 */
	@Override
	public FSMRelatedCalculation copy(){
		FSMRelatedCalculation r = new AliasSet(this);
		return r;
	}

	
	/** 计算数据流方程中的IN */
	@Override
	public void calculateIN(FSMMachineInstance fsmin,VexNode n,Object data){

	}
	
	/** 计算数据流方程中的OUT */
	@Override
	public void calculateOUT(FSMMachineInstance fsmin,VexNode n,Object data){

	}
	
}