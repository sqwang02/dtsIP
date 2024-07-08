package softtest.callgraph.java.method;

import java.util.*;

import softtest.ast.java.*;
import softtest.symboltable.java.*;

/**
 * @author 肖庆
 *
 */
public class MapOfVariable {
	
	/**
	 * 数组大小（只对数组越界使用）
	 */
	private long arrayLimit;
	/**
	 * 摘要对象到成员变量声明的映射
	 */
	private static Hashtable<MapOfVariable,VariableNameDeclaration> table=new Hashtable<MapOfVariable,VariableNameDeclaration>();
	
	/**
	 * @param v 变量声明
	 */
	public MapOfVariable(VariableNameDeclaration v) {
		if (!(v.getDeclareScope() instanceof MethodScope)) {
			table.put(this, v);
		} else {
			ASTVariableDeclaratorId id = (ASTVariableDeclaratorId) v.getNode();
			ASTFormalParameter formalParameter = (ASTFormalParameter) id.jjtGetParent();
			index = formalParameter.getIndexOfParent();
		}
	}
	/**
	 * @param v 变量声明
	 */
	public MapOfVariable(VariableNameDeclaration v, int index) {
		table.put(this, v);
		this.index = index;
	}
	
	/**
	 * 变量次序，成员变量的次序为-1
	 */
	private int index=-1;
	
	/**
	 * @param node 具有Mehtod类型或Constructor类型的表达式节点，通常可能是ASTPrimarySuffix或者ASTPrimaryPrefix或者ASTAllocationExpression
	 * @return 变量声明,为null的话就是没有找到
	 */
	public VariableNameDeclaration findVariable(SimpleJavaNode node){
		VariableNameDeclaration ret=null;
		if(index!=-1){
			if(node!=null&&node.jjtGetParent() instanceof ASTPrimaryExpression){
				if(node.getNextSibling()!=null){//added by yang
				   ASTArguments arguements=(ASTArguments)((SimpleJavaNode)node.getNextSibling()).getSingleChildofType(ASTArguments.class);
				   if(arguements!=null&&arguements.jjtGetNumChildren()>0&&index<arguements.jjtGetChild(0).jjtGetNumChildren()){
					  ASTName name=(ASTName)((SimpleJavaNode)(arguements.jjtGetChild(0).jjtGetChild(index))).getSingleChildofType(ASTName.class);
					  if(name!=null&&name.getNameDeclaration() instanceof VariableNameDeclaration){
						ret=(VariableNameDeclaration)name.getNameDeclaration();
					  }
				  }
				}
			}else if(node!=null&&node instanceof ASTAllocationExpression){
				if(node.jjtGetNumChildren()>1 && node.jjtGetChild(1) instanceof ASTArguments){
					ASTArguments arguements=(ASTArguments)node.jjtGetChild(1);
					if(arguements!=null&&arguements.jjtGetNumChildren()>0&&index<arguements.jjtGetChild(0).jjtGetNumChildren()){
						ASTName name=(ASTName)((SimpleJavaNode)(arguements.jjtGetChild(0).jjtGetChild(index))).getSingleChildofType(ASTName.class);
						if(name!=null&&name.getNameDeclaration() instanceof VariableNameDeclaration){
							ret=(VariableNameDeclaration)name.getNameDeclaration();
						}
					}
				}
			}
		}else{
			ret=table.get(this);
		}
		return ret;
	}
	
	/**
	 * 清除映射表
	 */
	public static void clear(){
		table.clear();
	}

	@Override
	public String toString() {
		String str;
		if(index!=-1){
			str="No "+index+" Param.";
		}else{
			str=table.get(this).toString();
		}
		return str;
	}

	public int getIndex() {
		return index;
	}
	public long getArrayLimit() {
		return arrayLimit;
	}
	public void setArrayLimit(long arrayLimit) {
		this.arrayLimit = arrayLimit;
	}
}
