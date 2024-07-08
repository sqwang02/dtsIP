package softtest.repair.java.conSynthesis;

import softtest.ast.java.ASTCompilationUnit;
import softtest.repair.java.ReadDefect;

public class ConditionSynthesisFacade {
	public String getConditionSynthesis(ASTCompilationUnit astroot,ReadDefect defect){
		ConSysFactory csf = new ConSysFactory();
		IConditionSynthesis ics = csf.getRepairConSys(defect.getCategory());
		return ics.getConditionSynthesis(astroot, defect);
	}
}
