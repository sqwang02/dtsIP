/* Generated By:JJTree: Do not edit this line. ASTStatement.java */

package softtest.ast.java;

public class ASTStatement extends SimpleJavaNode {
    public ASTStatement(int id) {
        super(id);
    }

    public ASTStatement(JavaParser p, int id) {
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
