package softtest.ccd.java;

import softtest.ast.java.SimpleJavaNode;

//ASTBlockStatement
//ASTStatement under if, while, do, for
//ASTMethodDeclaration
public class  Statement implements Comparable {
	public SimpleJavaNode  astNode;
	
	// Karp Rabin hash code
	private int	 KRHashcode;
	private int	 index;
	private String	 src;
	
	public Statement(SimpleJavaNode  astNode) {
		this.astNode = astNode;
		this.astNode.setStmt(this);
	}
	

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getKRHashcode() {
		return KRHashcode;
	}

	public void setKRHashcode(int hashcode) {
		KRHashcode = hashcode;
	}
	
	public int getHashCode() {
		return astNode.getHashCode();
	}
	
	public int getBeginLine() {
		return astNode.getBeginLine();
	}
	
	public int getBeginColumn() {
		return astNode.getBeginColumn();
	}
	
	public String getImage() {
		return astNode.getImage();
	}
	
	public String getStmtSrcId() {
		if( src == null ) {
			return "" + index / 10000;
		}
		return src;
	}
	
	// comparable
    public int compareTo(Object o) {
    	Statement other = (Statement) o;
        return index - other.getIndex();
    }
}