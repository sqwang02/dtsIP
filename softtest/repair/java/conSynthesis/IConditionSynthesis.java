package softtest.repair.java.conSynthesis;

import softtest.ast.java.ASTCompilationUnit;
import softtest.repair.java.ReadDefect;

public interface IConditionSynthesis {
	public String getConditionSynthesis(ASTCompilationUnit astroot,ReadDefect defect);
}
