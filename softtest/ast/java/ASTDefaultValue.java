/* Generated By:JJTree: Do not edit this line. ASTDefaultValue.java */

package softtest.ast.java;

public class ASTDefaultValue extends SimpleJavaNode {
    public ASTDefaultValue(int id) {
        super(id);
    }

    public ASTDefaultValue(JavaParser p, int id) {
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
