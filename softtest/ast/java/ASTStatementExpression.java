/* Generated By:JJTree: Do not edit this line. ASTStatementExpression.java */

package softtest.ast.java;

public class ASTStatementExpression extends SimpleJavaNode {
    public ASTStatementExpression(int id) {
        super(id);
    }

    public ASTStatementExpression(JavaParser p, int id) {
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
