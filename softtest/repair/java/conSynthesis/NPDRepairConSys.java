package softtest.repair.java.conSynthesis;

import softtest.ast.java.ASTCompilationUnit;
import softtest.ast.java.ASTForStatement;
import softtest.ast.java.ASTIfStatement;
import softtest.ast.java.ASTLocalVariableDeclaration;
import softtest.ast.java.ASTWhileStatement;
import softtest.ast.java.SimpleJavaNode;
import softtest.repair.java.ASTraverse;
import softtest.repair.java.ReadDefect;

public class NPDRepairConSys implements IConditionSynthesis{

	@Override
	public String getConditionSynthesis(ASTCompilationUnit astroot,
			ReadDefect defect) {
		// TODO Auto-generated method stub
		int id = defect.getId();
		String consyn=null;
		String rettype=null;
		SimpleJavaNode defectnode = ASTraverse.asTraverse(astroot, id);
		if(defectnode instanceof ASTLocalVariableDeclaration){
			consyn="if("+defect.getVariable()+"==null)"+"{"+"\n"+"return"+rettype+"}";
		}
		else
			consyn = "if("+defect.getVariable()+"!=null";
		return defect.getVariable()+"!=null";
	}

}
