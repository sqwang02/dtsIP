/* Generated By:JJTree: Do not edit this line. ASTConditionalOrExpression.java */

package softtest.ast.java;

public class ASTConditionalOrExpression extends ExpressionBase {
    public ASTConditionalOrExpression(int id) {
        super(id);
    }

    public ASTConditionalOrExpression(JavaParser p, int id) {
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
