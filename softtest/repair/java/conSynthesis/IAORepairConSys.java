package softtest.repair.java.conSynthesis;

import softtest.ast.java.ASTCompilationUnit;
import softtest.ast.java.SimpleJavaNode;
import softtest.repair.java.ASTraverse;
import softtest.repair.java.ReadDefect;
import softtest.symboltable.java.NameDeclaration;

public class IAORepairConSys  implements IConditionSynthesis{

	@Override
	public String getConditionSynthesis(ASTCompilationUnit astroot,
			ReadDefect defect) {	
		int defectId = defect.getId();
		String defectType=defect.getDefect();
		String defectVar=defect.getVariable();
		SimpleJavaNode defectNode = new SimpleJavaNode();
		
		defectNode=ASTraverse.asTraverse(astroot,defectId,defectType,defectVar);
		
		String opt = defectNode.getImage();
		String con=RestrictedSet.restrictrule(opt, defectVar, defectVar);
		//String con =defect.getVariable();
					 	

		//defectNode.getSelf(defectId);
	   //NameDeclaration name = defectNode.getVexNode();
	//defectNode.findCurrentDomain((NameDeclaration)defect);
	//defectNode.getSelf(defectId);
		return con;
		
		
		
		
	}

}
