/* Generated By:JJTree: Do not edit this line. ASTWhileStatement.java */

package softtest.ast.java;

public class ASTWhileStatement extends SimpleJavaNode {
    public ASTWhileStatement(int id) {
        super(id);
    }

    public ASTWhileStatement(JavaParser p, int id) {
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
