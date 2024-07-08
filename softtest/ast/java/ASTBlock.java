/* Generated By:JJTree: Do not edit this line. ASTBlock.java */

package softtest.ast.java;

public class ASTBlock extends SimpleJavaNode {
    public ASTBlock(int id) {
        super(id);
    }

    public ASTBlock(JavaParser p, int id) {
        super(p, id);
    }


    /**
     * Accept the visitor. *
     */
    @Override
	public Object jjtAccept(JavaParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }

    private boolean containsComment;

    public boolean containsComment() {
        return this.containsComment;
    }

    public void setContainsComment() {
        this.containsComment = true;
    }

}
