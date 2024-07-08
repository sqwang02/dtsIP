package softtest.repair.java.location;

import softtest.ast.java.ASTCompilationUnit;
import softtest.repair.java.ReadDefect;
import softtest.repair.java.conSynthesis.ConSysFactory;
import softtest.repair.java.conSynthesis.IConditionSynthesis;

public class LocationSetFacade {
	public int[] getRepairLine(ASTCompilationUnit astroot,ReadDefect defectFile){
		LocSetFactory ls=new LocSetFactory();
		ILocation lst=ls.getRepairLocation(defectFile.getCategory());
		return lst.getRepairLine(astroot, defectFile);
		
		
	}
	

}
