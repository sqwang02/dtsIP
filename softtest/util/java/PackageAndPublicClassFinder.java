package softtest.util.java;

import softtest.ast.java.ASTAnnotationTypeDeclaration;
import softtest.ast.java.ASTClassOrInterfaceDeclaration;
import softtest.ast.java.ASTCompilationUnit;
import softtest.ast.java.ASTEnumDeclaration;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTPackageDeclaration;
import softtest.ast.java.ASTTypeDeclaration;
import softtest.ast.java.JavaParserVisitorAdapter;
import softtest.ast.java.SimpleJavaNode;
import softtest.ast.java.*;;

public class PackageAndPublicClassFinder extends JavaParserVisitorAdapter {
	private String pakagename="";
	private String filename="";
	private boolean haspublicclss=false;
	
	@Override
	public Object visit(ASTPackageDeclaration node, Object data) {
		pakagename=((ASTName)node.jjtGetChild(0)).getImage();
		return null;
	}

	@Override
	public Object visit(ASTAnnotationTypeDeclaration node, Object data) {
		if(!haspublicclss){
			filename=node.getImage();
		}
		return null;
	}

	@Override
	public Object visit(ASTClassOrInterfaceDeclaration node, Object data) {
		if(!haspublicclss){
			filename=node.getImage();
		}
		if(node.isPublic()){
			haspublicclss=true;
		}
		return null;
	}

	@Override
	public Object visit(ASTCompilationUnit node, Object data) {
		return super.visit((SimpleJavaNode)node, data);
	}

	@Override
	public Object visit(ASTEnumDeclaration node, Object data) {
		if(!haspublicclss){
			filename=node.getImage();
		}
		return null;
	}

	@Override
	public Object visit(ASTTypeDeclaration node, Object data) {
		return super.visit((SimpleJavaNode)node, data);
	}

	@Override
	public Object visit(SimpleJavaNode node, Object data) {
		return null;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getPakagename() {
		return pakagename;
	}

	public void setPakagename(String pakagename) {
		this.pakagename = pakagename;
	}

}
