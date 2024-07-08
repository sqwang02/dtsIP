package softtest.ast.java;

/**所有表达式语句节点的抽象基类，提供类型信息接口支持，当前可能的具体表达式节点包括；
 * ASTExpression
 * ASTConditionalExpression
 * ASTConditionalOrExpression
 * ASTConditionalAndExpression
 * ASTInclusiveOrExpression
 * ASTExclusiveOrExpression
 * ASTAndExpression
 * ASTEqualityExpression
 * ASTInstanceOfExpression
 * ASTRelationalExpression
 * ASTShiftExpression
 * ASTAdditiveExpression
 * ASTMultiplicativeExpression
 * ASTUnaryExpression
 * ASTPreIncrementExpression
 * ASTPreDecrementExpression
 * ASTUnaryExpressionNotPlusMinus
 * ASTPostfixExpression
 * ASTCastExpression
 * ASTPrimaryExpression
 * ASTPrimaryPrefix
 * ASTPrimarySuffix
 * ASTAllocationExpression
 * ASTName
 * ASTLiteral
 * ASTBooleanLiteral
 * ASTNullLiteral
 * ASTArguments 存放的是返回值类型
 * */
public abstract class ExpressionBase extends SimpleJavaNode {

	/**表达式的类型*/
	protected Object type;

	public ExpressionBase(JavaParser p, int i) {
		super(p, i);
	}

	public ExpressionBase(int i) {
		super(i);
	}

	@Override
	public Object jjtAccept(JavaParserVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}

	/** 获得表达式的类型 */
	public Object getType() {
		return type;
	}

	/** 设置表达式类型*/
	public void setType(Object type) {
		this.type = type;
	}
	
	public String getTypeString(){
		return ""+type;
	}
}
