/* Generated By:JJTree: Do not edit this line. ASTContinueStatement.java */

package softtest.ast.java;

public class ASTContinueStatement extends SimpleJavaNode {
    public ASTContinueStatement(int id) {
        super(id);
    }

    public ASTContinueStatement(JavaParser p, int id) {
        super(p, id);
    }


    /**
     * Accept the visitor. *
     */
    @Override
	public Object jjtAccept(JavaParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
