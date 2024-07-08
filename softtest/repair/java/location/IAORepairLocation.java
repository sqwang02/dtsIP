package softtest.repair.java.location;

import softtest.ast.java.ASTCompilationUnit;
import softtest.ast.java.ASTIfStatement;
import softtest.ast.java.SimpleJavaNode;
import softtest.repair.java.ASTraverse;
import softtest.repair.java.ReadDefect;

public class IAORepairLocation implements ILocation {
	public int[] getRepairLine(ASTCompilationUnit astroot,ReadDefect defectFile){
		int defectLine=defectFile.getIPLine();		
		int a[];
		a= new int[2];
		int defectstartLine=defectFile.getIPLine();
		int defectendline=defectFile.getIPLine();
		int id = defectFile.getId();
		SimpleJavaNode defectnode = ASTraverse.asTraverse(astroot, id);
		if(defectnode instanceof ASTIfStatement){
			int childnum=defectnode.jjtGetNumChildren();
			SimpleJavaNode defectchild = (SimpleJavaNode)defectnode.jjtGetChild(childnum-1);
			defectendline=defectchild.getEndLine();
		}
		a[0] = defectstartLine;
		a[1]= defectendline;
		return a;
	
		
		
		
	}
	
}
