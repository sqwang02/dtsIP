/* Generated By:JJTree: Do not edit this line. ASTConstructorDeclaration.java */

package softtest.ast.java;
//added by xqing
import softtest.cfg.java.*;

public class ASTConstructorDeclaration extends AccessNode {
    public ASTConstructorDeclaration(int id) {
        super(id);
    }

    public ASTConstructorDeclaration(JavaParser p, int id) {
        super(p, id);
    }

    public ASTFormalParameters getParameters() {
        return (ASTFormalParameters) (jjtGetChild(0) instanceof ASTFormalParameters?jjtGetChild(0):jjtGetChild(1));
    }

    public int getParameterCount() {
        return getParameters().getParameterCount();
    }
    
    /**
	 * Gets the name of the method.
	 *
	 * @return a String representing the name of the method
	 */
	public String getMethodName() {
		ASTClassOrInterfaceDeclaration classtype = (ASTClassOrInterfaceDeclaration) this.getFirstParentOfType(ASTClassOrInterfaceDeclaration.class);
		if (classtype != null) {
			return classtype.getImage();
		}
		return null;
	}

    /**
     * Accept the visitor. *
     */
    @Override
	public Object jjtAccept(JavaParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }

    private boolean containsComment;

    public boolean containsComment() {
        return this.containsComment;
    }

    public void setContainsComment() {
        this.containsComment = true;
    }

    @Override
	public void dump(String prefix) {
        System.out.println(collectDumpedModifiers(prefix));
        dumpChildren(prefix);
    }
    //added by xqing
    /** 控制流图 */
    private Graph graph=null;
    
    /** 设置控制流图 */
    public void setGraph(Graph graph){
    	this.graph=graph;
    }
    
    /** 获得控制流图 */
    public Graph getGraph(){
    	return graph;
    }

    //added by yangxiu
	public Class[] getParameterTypes() {
		// BUGFOUND 20090416
		return ((ASTFormalParameters)(this.getFirstDirectChildOfType(ASTFormalParameters.class))).getParameterTypes();
	}
	
	private Object type=null;

	public Object getType() {
		return type;
	}

	public void setType(Object type) {
		this.type = type;
	}
}
