/* Generated By:JJTree: Do not edit this line. ASTExpression.java */

package softtest.ast.java;

public class ASTExpression extends ExpressionBase {
    public ASTExpression(int id) {
        super(id);
    }

    public ASTExpression(JavaParser p, int id) {
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
