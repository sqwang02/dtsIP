/* Generated By:JJTree: Do not edit this line. ASTPackageDeclaration.java */

package softtest.ast.java;

public class ASTPackageDeclaration extends SimpleJavaNode {
    public ASTPackageDeclaration(int id) {
        super(id);
    }

    public ASTPackageDeclaration(JavaParser p, int id) {
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
