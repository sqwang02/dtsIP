/* Generated By:JJTree: Do not edit this line. ASTTypeArgument.java */

package softtest.ast.java;

public class ASTTypeArgument extends SimpleJavaNode {
    public ASTTypeArgument(int id) {
        super(id);
    }

    public ASTTypeArgument(JavaParser p, int id) {
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
