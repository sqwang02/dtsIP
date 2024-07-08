/* Generated By:JJTree: Do not edit this line. ASTImportDeclaration.java */

package softtest.ast.java;

public class ASTImportDeclaration extends SimpleJavaNode {

    private boolean isImportOnDemand;
    private boolean isStatic;

    public ASTImportDeclaration(int id) {
        super(id);
    }

    public ASTImportDeclaration(JavaParser p, int id) {
        super(p, id);
    }

    public void setImportOnDemand() {
        isImportOnDemand = true;
    }

    public boolean isImportOnDemand() {
        return isImportOnDemand;
    }

    public void setStatic() {
        isStatic = true;
    }

    public boolean isStatic() {
        return isStatic;
    }

    // TODO - this should go away
    public ASTName getImportedNameNode() {
        return (ASTName) jjtGetChild(0);
    }

    public String getImportedName() {
        return ((ASTName) jjtGetChild(0)).getImage();
    }

    public String getPackageName() {
        String importName = getImportedName();
        if (isImportOnDemand) {
            return importName;
        }
        if (importName.indexOf('.') == -1) {
            return "";
        }
        int lastDot = importName.lastIndexOf('.');
        return importName.substring(0, lastDot);
    }


    @Override
	public void dump(String prefix) {
        String out = "";
        if (isStatic()) {
            out += "(static)";
        }
        System.out.println(toString(prefix) + out);
        dumpChildren(prefix);
    }

    /**
     * Accept the visitor. *
     */
    @Override
	public Object jjtAccept(JavaParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
