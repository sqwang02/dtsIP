/* Generated By:JJTree: Do not edit this line. ASTMultiplicativeExpression.java */

package softtest.ast.java;

public class ASTMultiplicativeExpression extends ExpressionBase {
    public ASTMultiplicativeExpression(int id) {
        super(id);
    }

    public ASTMultiplicativeExpression(JavaParser p, int id) {
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
