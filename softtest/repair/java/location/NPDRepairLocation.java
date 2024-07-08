package softtest.repair.java.location;

import com.sun.corba.se.impl.orbutil.graph.Node;

import softtest.ast.java.ASTCompilationUnit;
import softtest.ast.java.ASTForStatement;
import softtest.ast.java.ASTIfStatement;
import softtest.ast.java.ASTWhileStatement;
import softtest.ast.java.SimpleJavaNode;
import softtest.repair.java.ASTraverse;
import softtest.repair.java.ReadDefect;

public class NPDRepairLocation implements ILocation {
	public int[] getRepairLine(ASTCompilationUnit astroot,ReadDefect defectFile){
		int a[];
		a= new int[2];
		int defectstartLine=defectFile.getIPLine();
		int defectendline=defectFile.getIPLine();
		int id = defectFile.getId();
		SimpleJavaNode defectnode = ASTraverse.asTraverse(astroot, id);
		if(defectnode instanceof ASTIfStatement||defectnode instanceof ASTWhileStatement || defectnode instanceof ASTForStatement){
			int childnum=defectnode.jjtGetNumChildren();
			SimpleJavaNode defectchild = (SimpleJavaNode)defectnode.jjtGetChild(childnum-1);
			defectendline=defectchild.getEndLine();
		}
		
		a[0] = defectstartLine;
		a[1]= defectendline;
		return a;
		
		
		
	}
	

}
