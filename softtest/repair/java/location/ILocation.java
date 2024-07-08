package softtest.repair.java.location;

import softtest.ast.java.ASTCompilationUnit;
import softtest.repair.java.ReadDefect;

public interface ILocation {
	public int[] getRepairLine(ASTCompilationUnit astroot,ReadDefect defectFile);

}
