/* Generated By:JJTree: Do not edit this line. ASTFieldDeclaration.java */

package softtest.ast.java;

public class ASTFieldDeclaration extends AccessNode implements Dimensionable {

    public ASTFieldDeclaration(int id) {
        super(id);
    }

    public ASTFieldDeclaration(JavaParser p, int id) {
        super(p, id);
    }

    /**
     * Accept the visitor. *
     */
    @Override
	public Object jjtAccept(JavaParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }

    public boolean isSyntacticallyPublic() {
        return super.isPublic();
    }

    @Override
	public boolean isPublic() {
        if (isInterfaceMember()) {
            return true;
        }
        return super.isPublic();
    }

    public boolean isSyntacticallyStatic() {
        return super.isStatic();
    }

    @Override
	public boolean isStatic() {
        if (isInterfaceMember()) {
            return true;
        }
        return super.isStatic();
    }

    public boolean isSyntacticallyFinal() {
        return super.isFinal();
    }

    @Override
	public boolean isFinal() {
        if (isInterfaceMember()) {
            return true;
        }
        return super.isFinal();
    }

    @Override
	public boolean isPrivate() {
        if (isInterfaceMember()) {
            return false;
        }
        return super.isPrivate();
    }

    @Override
	public boolean isPackagePrivate() {
        if (isInterfaceMember()) {
            return false;
        }
        return super.isPackagePrivate();
    }

    @Override
	public boolean isProtected() {
        if (isInterfaceMember()) {
            return false;
        }
        return super.isProtected();
    }

    public boolean isInterfaceMember() {
        if (jjtGetParent().jjtGetParent() instanceof ASTEnumBody) {
            return false;
        }
        ASTClassOrInterfaceDeclaration n = (ASTClassOrInterfaceDeclaration)getFirstParentOfType(ASTClassOrInterfaceDeclaration.class);
        return n instanceof ASTClassOrInterfaceDeclaration && n.isInterface();
    }

    public boolean isArray() {
        return checkType() + checkDecl() > 0;
    }

    public int getArrayDepth() {
        if (!isArray()) {
            return 0;
        }
        return checkType() + checkDecl();
    }

    private int checkType() {
        if (jjtGetNumChildren() == 0 || !(jjtGetChild(0) instanceof ASTType)) {
            return 0;
        }
        return ((ASTType) jjtGetChild(0)).getArrayDepth();
    }

    private int checkDecl() {
        if (jjtGetNumChildren() < 2 || !(jjtGetChild(1) instanceof ASTVariableDeclarator)) {
            return 0;
        }
        return ((ASTVariableDeclaratorId) (jjtGetChild(1).jjtGetChild(0))).getArrayDepth();
    }

    @Override
	public void dump(String prefix) {
        String out = collectDumpedModifiers(prefix);
        if (isArray()) {
            out += "(array";
            for (int i = 0; i < getArrayDepth(); i++) {
                out += "[";
            }
            out += ")";
        }
        System.out.println(out);
        dumpChildren(prefix);
    }

    /**
     * Gets the variable name of this field.
     * This method searches the first VariableDeclartorId node and returns it's image or <code>null</code> if the child node is not found.
     *
     * @return a String representing the name of the variable
     */
    public String getVariableName() {
        ASTVariableDeclaratorId decl = (ASTVariableDeclaratorId) getFirstChildOfType(ASTVariableDeclaratorId.class);
        if (decl != null) {
            return decl.getImage();
        }
        return null;
    }
}
