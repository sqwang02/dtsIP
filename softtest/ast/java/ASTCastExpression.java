/* Generated By:JJTree: Do not edit this line. ASTCastExpression.java */

package softtest.ast.java;

public class ASTCastExpression extends ExpressionBase {
    public ASTCastExpression(int id) {
        super(id);
    }

    public ASTCastExpression(JavaParser p, int id) {
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
