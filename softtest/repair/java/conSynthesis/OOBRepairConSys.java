package softtest.repair.java.conSynthesis;

import softtest.ast.java.ASTCompilationUnit;
import softtest.ast.java.SimpleJavaNode;
import softtest.repair.java.ASTraverse;
import softtest.repair.java.ReadDefect;
import softtest.symboltable.java.NameDeclaration;
import softtest.symboltable.java.Scope;

public class OOBRepairConSys implements IConditionSynthesis{
	public String getConditionSynthesis(ASTCompilationUnit astroot,
			ReadDefect defect) {	
			int defectId = defect.getId();
			String defectType=defect.getCategory();
			String defectVar=defect.getVariable();
			SimpleJavaNode defectNode = new SimpleJavaNode();
			defectNode=ASTraverse.asTraverse(astroot,defectId,defectType,defectVar);
			
			String opt = defectNode.getImage();
			String con=RestrictedSet.restrictrule(opt, defectVar, defectVar);
			//String con =defect.getVariable();
						 	

			//defectNode.getSelf(defectId);
		  NameDeclaration name = (NameDeclaration) defectNode.getVexNode();
		  Scope scope=name.getScope();
		 // defectNode.findCurrentDomain(scope.getVariableDeclarations());
		//defectNode.getSelf(defectId);
			return con;
			
			
			
			
		}
}
