/* Generated By:JJTree: Do not edit this line. ASTTypeParameters.java */

package softtest.ast.java;

public class ASTTypeParameters extends SimpleJavaNode {
    public ASTTypeParameters(int id) {
        super(id);
    }

    public ASTTypeParameters(JavaParser p, int id) {
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
