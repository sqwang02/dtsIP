/* Generated By:JJTree: Do not edit this line. ASTMemberValue.java */

package softtest.ast.java;

public class ASTMemberValue extends SimpleJavaNode {
    public ASTMemberValue(int id) {
        super(id);
    }

    public ASTMemberValue(JavaParser p, int id) {
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
