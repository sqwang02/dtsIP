package softtest.symboltable.java;

import softtest.ast.java.ASTClassOrInterfaceDeclaration;

public class ClassNameDeclaration extends AbstractNameDeclaration {

    public ClassNameDeclaration(ASTClassOrInterfaceDeclaration node) {
        super(node);
    }

    @Override
	public String toString() {
        return "Class " + node.getImage();
    }

}
