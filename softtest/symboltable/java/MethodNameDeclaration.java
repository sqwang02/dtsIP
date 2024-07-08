/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */
package softtest.symboltable.java;


//added by xqing
import softtest.ast.java.ASTFormalParameter;
import softtest.ast.java.ASTFormalParameters;
import softtest.ast.java.ASTMethodDeclarator;
import softtest.ast.java.ASTPrimitiveType;
import softtest.ast.java.SimpleNode;
import softtest.callgraph.java.*;

public class MethodNameDeclaration extends AbstractNameDeclaration {

    public MethodNameDeclaration(ASTMethodDeclarator node) {
        super(node);
        //added by xqing
        node.setMethodNameDeclaration(this);
    }

    public int getParameterCount() {
        return ((ASTMethodDeclarator) node).getParameterCount();
    }

    public ASTMethodDeclarator getMethodNameDeclaratorNode() {
        return (ASTMethodDeclarator) node;
    }

    public String getParameterDisplaySignature() {
        StringBuffer sb = new StringBuffer("(");
        ASTFormalParameters params = (ASTFormalParameters) node.jjtGetChild(0);
        // TODO - this can be optimized - add [0] then ,[n] in a loop.
        //        no need to trim at the end
        for (int i = 0; i < ((ASTMethodDeclarator) node).getParameterCount(); i++) {
            ASTFormalParameter p = (ASTFormalParameter) params.jjtGetChild(i);
            sb.append(p.getTypeNode().getTypeImage());
            sb.append(',');
        }
        if (sb.charAt(sb.length() - 1) == ',') {
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append(')');
        return sb.toString();
    }
    //added by xqing
    public Class[] getParameterClasses(){
    	Class[] ret=new Class[((ASTMethodDeclarator) node).getParameterCount()];
    	ASTFormalParameters params = (ASTFormalParameters) node.jjtGetChild(0);
    	 for (int i = 0; i < ((ASTMethodDeclarator) node).getParameterCount(); i++) {
             ASTFormalParameter p = (ASTFormalParameter) params.jjtGetChild(i);
             String name=p.getTypeNode().getTypeImage();
             int arrayDepth=p.getArrayDepth();
             ret[i]=findClassWithArray(name,arrayDepth);
    	 }
    	return ret;
    }

    @Override
	public boolean equals(Object o) {
        MethodNameDeclaration other = (MethodNameDeclaration) o;

        // compare name
        if (!other.node.getImage().equals(node.getImage())) {
            return false;
        }

        // compare parameter count - this catches the case where there are no params, too
        if (((ASTMethodDeclarator) (other.node)).getParameterCount() != ((ASTMethodDeclarator) node).getParameterCount()) {
            return false;
        }

        // compare parameter types
        ASTFormalParameters myParams = (ASTFormalParameters) node.jjtGetChild(0);
        ASTFormalParameters otherParams = (ASTFormalParameters) other.node.jjtGetChild(0);
        for (int i = 0; i < ((ASTMethodDeclarator) node).getParameterCount(); i++) {
            ASTFormalParameter myParam = (ASTFormalParameter) myParams.jjtGetChild(i);
            ASTFormalParameter otherParam = (ASTFormalParameter) otherParams.jjtGetChild(i);

            SimpleNode myTypeNode = (SimpleNode) myParam.getTypeNode().jjtGetChild(0);
            SimpleNode otherTypeNode = (SimpleNode) otherParam.getTypeNode().jjtGetChild(0);

            // compare primitive vs reference type
            if (myTypeNode.getClass() != otherTypeNode.getClass()) {
                return false;
            }

            // simple comparison of type images
            // this can be fooled by one method using "String"
            // and the other method using "java.lang.String"
            // once we get real types in here that should get fixed
            String myTypeImg;
            String otherTypeImg;
            if (myTypeNode instanceof ASTPrimitiveType) {
                myTypeImg = myTypeNode.getImage();
                otherTypeImg = otherTypeNode.getImage();
            } else {
                myTypeImg = ((SimpleNode) (myTypeNode.jjtGetChild(0))).getImage();
                otherTypeImg = ((SimpleNode) (otherTypeNode.jjtGetChild(0))).getImage();
            }

            if (!myTypeImg.equals(otherTypeImg)) {
                return false;
            }

            // if type is ASTPrimitiveType and is an array, make sure the other one is also
        }
        return true;
    }

    @Override
	public int hashCode() {
        return node.getImage().hashCode() + ((ASTMethodDeclarator) node).getParameterCount();
    }

    @Override
	public String toString() {
        return "Method " + node.getImage() + ", line " + node.getBeginLine() + ", params = " + ((ASTMethodDeclarator) node).getParameterCount();
    }
    
    //added by xqing
    CVexNode cvex =null;
    
    public void setCallGraphVex(CVexNode cvex){
    	this.cvex=cvex;
    }
    
    public CVexNode getCallGraphVex(){
    	return cvex;
    }
    
	private Class findClassWithArray(String typeimage,int arrayDepth){
		Class type=null;
		
		/*java 类型命名规则
		 * Element Type  Encoding  
		 * boolean  Z  
		 * byte  B  
		 * char  C  
		 * class or interface  Lclassname;  
		 * double  D  
		 * float  F  
		 * int  I  
		 * long  J  
		 * short  S  
		 * 
		 * The class or interface name classname is the binary name of the class specified above. 
		 * 
		 * Examples: 
		 * String.class.getName()
		 * 		returns "java.lang.String"
		 * byte.class.getName()
		 * 		returns "byte"
		 * (new Object[3]).getClass().getName()
		 * 		returns "[Ljava.lang.Object;"
		 * (new int[3][4][5][6][7][8][9]).getClass().getName()
		 * 		returns "[[[[[[[I"
		 */
		TypeSet typeset = TypeSet.getCurrentTypeSet();
		try {
			StringBuffer sb = new StringBuffer("");
			if (arrayDepth > 0) {
				for (int index = 0; index < arrayDepth; index++) {
					sb.append("[");
				}
				if (typeimage.equals("boolean")) {
					sb.append("Z");
				} else if (typeimage.equals("byte")) {
					sb.append("B");
				} else if (typeimage.equals("char")) {
					sb.append("C");
				} else if (typeimage.equals("double")) {
					sb.append("D");
				} else if (typeimage.equals("float")) {
					sb.append("F");
				} else if (typeimage.equals("int")) {
					sb.append("I");
				} else if (typeimage.equals("long")) {
					sb.append("J");
				} else if (typeimage.equals("short")) {
					sb.append("S");
				} else {
					Class t = typeset.findClass(typeimage);
					sb.append("L" + t.getName() + ";");
				}
				typeimage = sb.toString();
			}
			type = typeset.findClass(typeimage);
		} catch (ClassNotFoundException e) {
		}
		return type;
	}
}
