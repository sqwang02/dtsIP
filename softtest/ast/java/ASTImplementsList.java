/* Generated By:JJTree: Do not edit this line. ASTImplementsList.java */

package softtest.ast.java;

public class ASTImplementsList extends SimpleJavaNode {
    public ASTImplementsList(int id) {
        super(id);
    }

    public ASTImplementsList(JavaParser p, int id) {
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
