/* Generated By:JJTree: Do not edit this line. ASTTypeBound.java */

package softtest.ast.java;

public class ASTTypeBound extends SimpleJavaNode {
    public ASTTypeBound(int id) {
        super(id);
    }

    public ASTTypeBound(JavaParser p, int id) {
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
