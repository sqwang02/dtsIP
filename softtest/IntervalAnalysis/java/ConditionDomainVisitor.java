package softtest.IntervalAnalysis.java;

import softtest.ast.java.*;
import softtest.symboltable.java.*;
import softtest.domain.java.*;
import softtest.symboltable.java.VariableNameDeclaration;

import softtest.ast.java.ASTAdditiveExpression;
import softtest.ast.java.ASTAndExpression;
import softtest.ast.java.ASTAssignmentOperator;
import softtest.ast.java.ASTConditionalAndExpression;
import softtest.ast.java.ASTConditionalOrExpression;
import softtest.ast.java.ASTEqualityExpression;
import softtest.ast.java.ASTExclusiveOrExpression;
import softtest.ast.java.ASTExpression;
import softtest.ast.java.ASTInclusiveOrExpression;
import softtest.ast.java.ASTInstanceOfExpression;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTPrimaryExpression;
import softtest.ast.java.ASTPrimaryPrefix;
import softtest.ast.java.ASTPrimarySuffix;
import softtest.ast.java.ASTRelationalExpression;
import softtest.ast.java.ASTUnaryExpression;
import softtest.ast.java.ASTUnaryExpressionNotPlusMinus;
import softtest.ast.java.JavaNode;
import softtest.ast.java.JavaParserVisitorAdapter;
import softtest.ast.java.SimpleJavaNode;
import softtest.cfg.java.*;

/**
 * 生成条件判断节点的条件限定域集的抽象语法树访问者， data为ConditionData类型，在visit函数结束后，data将存有条件限定域集
 */
public class ConditionDomainVisitor extends JavaParserVisitorAdapter {
	/** 默认访问 */
	public Object visit(SimpleJavaNode node, Object data) {
		// 阻止访问向孩子传递
		// node.childrenAccept(this, data);
		return null;
	}

	/** 基本表达式节点，处理条件判断表达式 */
	public Object visit(ASTExpression node, Object data) {
		if (node.jjtGetNumChildren() == 1) {
			JavaNode javanode = (JavaNode) node.jjtGetChild(0);
			javanode.jjtAccept(this, data);
		}else if(node.jjtGetNumChildren() == 3){
			JavaNode javanode = (JavaNode) node.jjtGetChild(0);
			javanode.jjtAccept(this, data);
		}
		return null;
	}

	/** 处理大小关系表达式，目前只处理了（x op 表达式） 和（表达式 op x）,其中x为变量两种基本情况 */
	public Object visit(ASTRelationalExpression node, Object data) {
		// < > <= >=
		ConditionData condata = (ConditionData) data;
		SimpleJavaNode left = (SimpleJavaNode) node.jjtGetChild(0);
		SimpleJavaNode right = (SimpleJavaNode) node.jjtGetChild(1);

		VexNode vex = condata.getCurrentVex();
		if (vex == null) {
			vex = node.getCurrentVexNode();
		}
		
		String image = node.getImage();
		if (image == null) {
			throw new RuntimeException("This is not a legal RelationalExpression");
		}
		
		String[] operators = image.split("#");
		if (operators.length != (node.jjtGetNumChildren() - 1)) {
			throw new RuntimeException("This is not a legal RelationalExpression");
		}

		if (operators.length != 1) {
			// 语义合法的情况实际上只能有这一个，但是为了程序统一风格，还是和前面的一样模式处理了
			throw new RuntimeException("This is not a legal RelationalExpression");
		}

		ASTName name = (ASTName) left.getSingleChildofType(ASTName.class);
		String nameimage = null;
		if (name == null) {
			ASTExpression expression = (ASTExpression) (left.getSingleChildofType(ASTExpression.class));
			if (expression != null && expression.jjtGetNumChildren() == 3) {
				if (expression.jjtGetChild(1) instanceof ASTAssignmentOperator) {
					if (((SimpleJavaNode) (expression.jjtGetChild(1))).getImage().equals("=")) {
						name = (ASTName) ((SimpleJavaNode) (expression.jjtGetChild(0))).getSingleChildofType(ASTName.class);
					}
				}
			}
		}
		if (name != null) {
			nameimage = name.getImage();
		}
						
		if (name != null && (name.getNameDeclaration() instanceof VariableNameDeclaration) && nameimage != null && !nameimage.contains(".")) {
			// 左边为变量情况
			VariableNameDeclaration v = (VariableNameDeclaration) name.getNameDeclaration();

			DomainData expdata = new DomainData(vex);
			expdata.sideeffect = false;
			// 计算右边表达式
			right.jjtAccept(new ExpressionDomainVisitor(), expdata);
			Object rightdomain = expdata.domain;

			Object leftdomain = node.findCurrentDomain(v, vex);
			ClassType lefttype = DomainSet.getDomainType(leftdomain);

			if (lefttype == ClassType.DOUBLE ) {
				DoubleDomain dright = (DoubleDomain) ConvertDomain.DomainSwitch(rightdomain, ClassType.DOUBLE);
				DoubleDomain dleft = (DoubleDomain) ConvertDomain.DomainSwitch(leftdomain, ClassType.DOUBLE);

				DoubleInterval interval = dright.jointoOneInterval();
				String operator = operators[0];
				DoubleDomain may = null, must = null;
				if(dright.getUnknown()){
					may = dleft;
					condata.addMayDomain(v, may);
					must = DoubleDomain.getEmptyDomain();
					condata.addMustDomain(v, must);
				}else if (!interval.isEmpty()) {
					if (operator.equals(">")) {
						may = DoubleDomain.intersect(dleft, new DoubleDomain(DoubleMath.nextfp(interval.getMin()), Double.POSITIVE_INFINITY, false, false));
						condata.addMayDomain(v, may);
						must = DoubleDomain.intersect(dleft, new DoubleDomain(DoubleMath.nextfp(interval.getMax()), Double.POSITIVE_INFINITY, false, false));
						condata.addMustDomain(v, must);
					} else if (operator.equals(">=")) {
						may = DoubleDomain.intersect(dleft, new DoubleDomain(interval.getMin(), Double.POSITIVE_INFINITY, false, false));
						condata.addMayDomain(v, may);
						must = DoubleDomain.intersect(dleft, new DoubleDomain(interval.getMax(), Double.POSITIVE_INFINITY, false, false));
						condata.addMustDomain(v, must);
					} else if (operator.equals("<")) {
						may = DoubleDomain.intersect(dleft, new DoubleDomain(Double.NEGATIVE_INFINITY, DoubleMath.prevfp(interval.getMax()), false, false));
						condata.addMayDomain(v, may);
						must = DoubleDomain.intersect(dleft, new DoubleDomain(Double.NEGATIVE_INFINITY, DoubleMath.prevfp(interval.getMin()), false, false));
						condata.addMustDomain(v, must);
					} else if (operator.equals("<=")) {
						may = DoubleDomain.intersect(dleft, new DoubleDomain(Double.NEGATIVE_INFINITY, interval.getMax(), false, false));
						condata.addMayDomain(v, may);
						must = DoubleDomain.intersect(dleft, new DoubleDomain(Double.NEGATIVE_INFINITY, interval.getMin(), false, false));
						condata.addMustDomain(v, must);
					} else {
						throw new RuntimeException("This is not a legal RelationalExpression");
					}
				}
			} else if (lefttype == ClassType.INT ) {
				IntegerDomain iright = (IntegerDomain) ConvertDomain.DomainSwitch(rightdomain, ClassType.INT);
				IntegerDomain ileft = (IntegerDomain) ConvertDomain.DomainSwitch(leftdomain, ClassType.INT);

				IntegerInterval interval = iright.jointoOneInterval();
				String operator = operators[0];
				IntegerDomain may = null, must = null;
				if(iright.getUnknown()){
					may = ileft;
					condata.addMayDomain(v, may);
					must = IntegerDomain.getEmptyDomain();
					condata.addMustDomain(v, must);
				}else if (!interval.isEmpty()) {
					if (operator.equals(">")) {
						may = IntegerDomain.intersect(ileft, new IntegerDomain(IntegerMath.nextInt(interval.getMin()), Long.MAX_VALUE, false, false));
						condata.addMayDomain(v, may);
						must = IntegerDomain.intersect(ileft, new IntegerDomain(IntegerMath.nextInt(interval.getMax()), Long.MAX_VALUE, false, false));
						//must = IntegerDomain.intersect(ileft, new IntegerDomain(interval.getMax()+1, Long.MAX_VALUE, false, false));
						condata.addMustDomain(v, must);
					} else if (operator.equals(">=")) {
						may = IntegerDomain.intersect(ileft, new IntegerDomain(interval.getMin(), Long.MAX_VALUE, false, false));
						condata.addMayDomain(v, may);
						must = IntegerDomain.intersect(ileft, new IntegerDomain(interval.getMax(), Long.MAX_VALUE, false, false));
						condata.addMustDomain(v, must);
					} else if (operator.equals("<")) {
						may = IntegerDomain.intersect(ileft, new IntegerDomain(Long.MIN_VALUE, IntegerMath.prevInt(interval.getMax()), false, false));
						condata.addMayDomain(v, may);
						must = IntegerDomain.intersect(ileft, new IntegerDomain(Long.MIN_VALUE, IntegerMath.prevInt(interval.getMin()), false, false));
						//must = IntegerDomain.intersect(ileft, new IntegerDomain(Long.MIN_VALUE, interval.getMin()-1, false, false));
						condata.addMustDomain(v, must);
					} else if (operator.equals("<=")) {
						may = IntegerDomain.intersect(ileft, new IntegerDomain(Long.MIN_VALUE, interval.getMax(), false, false));
						condata.addMayDomain(v, may);
						must = IntegerDomain.intersect(ileft, new IntegerDomain(Long.MIN_VALUE, interval.getMin(), false, false));
						condata.addMustDomain(v, must);
					} else {
						throw new RuntimeException("This is not a legal RelationalExpression");
					}
				}
			}
		}/*else if (name != null && (name.getNameDeclaration() instanceof VariableNameDeclaration) && nameimage != null && nameimage.endsWith(".length")) {
			// 左边数组长度处理
			VariableNameDeclaration v = (VariableNameDeclaration) name.getNameDeclaration();

			DomainData expdata = new DomainData();
			expdata.sideeffect = false;
			// 计算右边表达式
			right.jjtAccept(new ExpressionDomainVisitor(), expdata);
			Object rightdomain = expdata.domain;

			Object leftdomain = node.findCurrentDomain(v);
			ClassType lefttype = DomainSet.getDomainType(leftdomain);

			if (lefttype == ClassType.ARRAY) {
				IntegerDomain iright = (IntegerDomain) ConvertDomain.DomainSwitch(rightdomain, ClassType.INT);
				ArrayDomain aleft = (ArrayDomain) ConvertDomain.DomainSwitch(leftdomain, ClassType.ARRAY);
			
				IntegerInterval interval = iright.jointoOneInterval();
				String operator = operators[0];
				ArrayDomain may = null, must = null;
				ArrayDomain atemp=new ArrayDomain(1);
				
				if (!interval.isEmpty()) {
					if (operator.equals(">")) {
						atemp.setDimension(0, new IntegerDomain(interval.getMin() + 1, Long.MAX_VALUE, false, false));
						may = ArrayDomain.intersect(aleft, atemp);
						condata.addMayDomain(v, may);
						atemp.setDimension(0, new IntegerDomain(interval.getMax() + 1, Long.MAX_VALUE, false, false));
						must = ArrayDomain.intersect(aleft, atemp);
						condata.addMustDomain(v, must);
					} else if (operator.equals(">=")) {
						atemp.setDimension(0, new IntegerDomain(interval.getMin(), Long.MAX_VALUE, false, false));
						may = ArrayDomain.intersect(aleft, atemp);
						condata.addMayDomain(v, may);
						atemp.setDimension(0, new IntegerDomain(interval.getMax(), Long.MAX_VALUE, false, false));
						must = ArrayDomain.intersect(aleft, atemp);
						condata.addMustDomain(v, must);
					} else if (operator.equals("<")) {
						atemp.setDimension(0, new IntegerDomain(Long.MIN_VALUE, interval.getMax() - 1, false, false));
						may = ArrayDomain.intersect(aleft, atemp);
						condata.addMayDomain(v, may);
						atemp.setDimension(0, new IntegerDomain(Long.MIN_VALUE, interval.getMin() - 1, false, false));
						must = ArrayDomain.intersect(aleft, atemp);
						condata.addMustDomain(v, must);
					} else if (operator.equals("<=")) {
						atemp.setDimension(0, new IntegerDomain(Long.MIN_VALUE, interval.getMax(), false, false));
						may = ArrayDomain.intersect(aleft, atemp);
						condata.addMayDomain(v, may);
						atemp.setDimension(0, new IntegerDomain(Long.MIN_VALUE, interval.getMin(), false, false));
						must = ArrayDomain.intersect(aleft, atemp);
						condata.addMustDomain(v, must);
					} else {
						throw new RuntimeException("This is not a legal RelationalExpression");
					}
				}		
			}
		}*/

		name = (ASTName) (right.getSingleChildofType(ASTName.class));
		if (name == null) {
			ASTExpression expression = (ASTExpression) (right.getSingleChildofType(ASTExpression.class));
			if (expression != null && expression.jjtGetNumChildren() == 3) {
				if (expression.jjtGetChild(1) instanceof ASTAssignmentOperator) {
					if (((SimpleJavaNode) (expression.jjtGetChild(1))).getImage().equals("=")) {
						name = (ASTName) ((SimpleJavaNode) (expression.jjtGetChild(0))).getSingleChildofType(ASTName.class);
					}
				}
			}
		}
		
		if (name != null) {
			nameimage = name.getImage();
		}
		
		if (name != null && (name.getNameDeclaration() instanceof VariableNameDeclaration) && nameimage != null && !nameimage.contains(".")) {
			// 右边为变量情况
			VariableNameDeclaration v = (VariableNameDeclaration) name.getNameDeclaration();

			DomainData expdata = new DomainData(condata.getCurrentVex());
			expdata.sideeffect = false;
			// 计算左边表达式
			left.jjtAccept(new ExpressionDomainVisitor(), expdata);
			Object leftdomain = expdata.domain;

			Object rightdomain = node.findCurrentDomain(v, vex);
			ClassType righttype = DomainSet.getDomainType(rightdomain);

			if (righttype == ClassType.DOUBLE ) {
				DoubleDomain dright = (DoubleDomain) ConvertDomain.DomainSwitch(rightdomain, ClassType.DOUBLE);
				DoubleDomain dleft = (DoubleDomain) ConvertDomain.DomainSwitch(leftdomain, ClassType.DOUBLE);

				DoubleInterval interval = dleft.jointoOneInterval();
				String operator = operators[0];
				DoubleDomain may = null, must = null;
				if(dleft.getUnknown()){
					may = dright;
					condata.addMayDomain(v, may);
					must = DoubleDomain.getEmptyDomain();
					condata.addMustDomain(v, must);
				}else if (!interval.isEmpty()) {
					if (operator.equals("<")) {
						may = DoubleDomain.intersect(dright, new DoubleDomain(DoubleMath.nextfp(interval.getMin()), Double.POSITIVE_INFINITY, false, false));
						condata.addMayDomain(v, may);
						must = DoubleDomain.intersect(dright, new DoubleDomain(DoubleMath.nextfp(interval.getMax()), Double.POSITIVE_INFINITY, false, false));
						condata.addMustDomain(v, must);
					} else if (operator.equals("<=")) {
						may = DoubleDomain.intersect(dright, new DoubleDomain(interval.getMin(), Double.POSITIVE_INFINITY, false, false));
						condata.addMayDomain(v, may);
						must = DoubleDomain.intersect(dright, new DoubleDomain(interval.getMax(), Double.POSITIVE_INFINITY, false, false));
						condata.addMustDomain(v, must);
					} else if (operator.equals(">")) {
						may = DoubleDomain.intersect(dright, new DoubleDomain(Double.NEGATIVE_INFINITY, DoubleMath.prevfp(interval.getMax()), false, false));
						condata.addMayDomain(v, may);
						must = DoubleDomain.intersect(dright, new DoubleDomain(Double.NEGATIVE_INFINITY, DoubleMath.prevfp(interval.getMin()), false, false));
						condata.addMustDomain(v, must);
					} else if (operator.equals(">=")) {
						may = DoubleDomain.intersect(dright, new DoubleDomain(Double.NEGATIVE_INFINITY, interval.getMax(), false, false));
						condata.addMayDomain(v, may);
						must = DoubleDomain.intersect(dright, new DoubleDomain(Double.NEGATIVE_INFINITY, interval.getMin(), false, false));
						condata.addMustDomain(v, must);
					} else {
						throw new RuntimeException("This is not a legal RelationalExpression");
					}
				}
			} else if (righttype == ClassType.INT ) {
				IntegerDomain iright = (IntegerDomain) ConvertDomain.DomainSwitch(rightdomain, ClassType.INT);
				IntegerDomain ileft = (IntegerDomain) ConvertDomain.DomainSwitch(leftdomain, ClassType.INT);
				IntegerInterval interval = ileft.jointoOneInterval();

				String operator = operators[0];
				IntegerDomain may = null, must = null;
				if(ileft.getUnknown()){
					may = iright;
					condata.addMayDomain(v, may);
					must = IntegerDomain.getEmptyDomain();
					condata.addMustDomain(v, must);
				}else if (!interval.isEmpty()) {
					if (operator.equals("<")) {
						may = IntegerDomain.intersect(iright, new IntegerDomain(IntegerMath.nextInt(interval.getMin()), Long.MAX_VALUE, false, false));
						condata.addMayDomain(v, may);
						//must = IntegerDomain.intersect(iright, new IntegerDomain(interval.getMax() + 1, Long.MAX_VALUE, false, false));
						must = IntegerDomain.intersect(iright, new IntegerDomain(IntegerMath.nextInt(interval.getMax()), Long.MAX_VALUE, false, false));
						condata.addMustDomain(v, must);
					} else if (operator.equals("<=")) {
						may = IntegerDomain.intersect(iright, new IntegerDomain(interval.getMin(), Long.MAX_VALUE, false, false));
						condata.addMayDomain(v, may);
						must = IntegerDomain.intersect(iright, new IntegerDomain(interval.getMax(), Long.MAX_VALUE, false, false));
						condata.addMustDomain(v, must);
					} else if (operator.equals(">")) {
						may = IntegerDomain.intersect(iright, new IntegerDomain(Long.MIN_VALUE, IntegerMath.prevInt(interval.getMax()), false, false));
						condata.addMayDomain(v, may);
						//must = IntegerDomain.intersect(iright, new IntegerDomain(Long.MIN_VALUE, interval.getMin() - 1, false, false));
						must = IntegerDomain.intersect(iright, new IntegerDomain(Long.MIN_VALUE, IntegerMath.prevInt(interval.getMin()), false, false));
						condata.addMustDomain(v, must);
					} else if (operator.equals(">=")) {
						may = IntegerDomain.intersect(iright, new IntegerDomain(Long.MIN_VALUE, interval.getMax(), false, false));
						condata.addMayDomain(v, may);
						must = IntegerDomain.intersect(iright, new IntegerDomain(Long.MIN_VALUE, interval.getMin(), false, false));
						condata.addMustDomain(v, must);
					} else {
						throw new RuntimeException("This is not a legal RelationalExpression");
					}
				}
			}
		}/*else if (name != null && (name.getNameDeclaration() instanceof VariableNameDeclaration) && nameimage != null && nameimage.endsWith(".length")) {
			//右边数组长度处理
			VariableNameDeclaration v = (VariableNameDeclaration) name.getNameDeclaration();

			DomainData expdata = new DomainData();
			expdata.sideeffect = false;
			// 计算左边表达式
			left.jjtAccept(new ExpressionDomainVisitor(), expdata);
			Object leftdomain = expdata.domain;

			Object rightdomain = node.findCurrentDomain(v, vex);
			ClassType righttype = DomainSet.getDomainType(rightdomain);
			
			if (righttype == ClassType.ARRAY) {
				ArrayDomain aright = (ArrayDomain) ConvertDomain.DomainSwitch(rightdomain, ClassType.ARRAY);
				IntegerDomain ileft = (IntegerDomain) ConvertDomain.DomainSwitch(leftdomain, ClassType.INT);
				
				
				IntegerInterval interval = ileft.jointoOneInterval();

				String operator = operators[0];
				ArrayDomain atemp=new ArrayDomain(1);
				ArrayDomain may = null, must = null;
				
				if (!interval.isEmpty()) {
					if (operator.equals("<")) {
						atemp.setDimension(0, new IntegerDomain(interval.getMin() + 1, Long.MAX_VALUE, false, false)); 
						may = ArrayDomain.intersect(aright, atemp);
						condata.addMayDomain(v, may);
						atemp.setDimension(0, new IntegerDomain(interval.getMax() + 1, Long.MAX_VALUE, false, false)); 
						must = ArrayDomain.intersect(aright, atemp);
						condata.addMustDomain(v, must);
					} else if (operator.equals("<=")) {
						atemp.setDimension(0, new IntegerDomain(interval.getMin(), Long.MAX_VALUE, false, false)); 
						may = ArrayDomain.intersect(aright, atemp);
						condata.addMayDomain(v, may);
						atemp.setDimension(0, new IntegerDomain(interval.getMax(), Long.MAX_VALUE, false, false)); 
						must = ArrayDomain.intersect(aright, atemp);
						condata.addMustDomain(v, must);;
					} else if (operator.equals(">")) {
						atemp.setDimension(0, new IntegerDomain(Long.MIN_VALUE, interval.getMax() - 1, false, false)); 
						may = ArrayDomain.intersect(aright, atemp);
						condata.addMayDomain(v, may);
						atemp.setDimension(0, new IntegerDomain(Long.MIN_VALUE, interval.getMin() - 1, false, false)); 
						must = ArrayDomain.intersect(aright, atemp);
						condata.addMustDomain(v, must);
					} else if (operator.equals(">=")) {
						atemp.setDimension(0, new IntegerDomain(Long.MIN_VALUE, interval.getMax(), false, false)); 
						may = ArrayDomain.intersect(aright, atemp);
						condata.addMayDomain(v, may);
						atemp.setDimension(0, new IntegerDomain(Long.MIN_VALUE, interval.getMin(), false, false)); 
						must = ArrayDomain.intersect(aright, atemp);
						condata.addMustDomain(v, must);
					} else {
						throw new RuntimeException("This is not a legal RelationalExpression");
					}
				}
			}
		}*/
		if(softtest.config.java.Config.LINEAR){
			ASTAdditiveExpression addexp=(ASTAdditiveExpression) left.getSingleChildofType(ASTAdditiveExpression.class);
			if(addexp!=null){
				String addimage = addexp.getImage();
				if (addimage == null) {
					throw new RuntimeException("This is not a legal AdditiveExpression");
				}
				String[] addoperators = addimage.split("#");
				if (addoperators.length != (addexp.jjtGetNumChildren() - 1)) {
					throw new RuntimeException("This is not a legal AdditiveExpression");
				}
				int opnum=addexp.jjtGetNumChildren();
				//存放每个操作数的区间值，最后一个存放不等式右边区间值
				DomainData expdata[] = new DomainData[opnum+1];
				SimpleJavaNode javanode = null;
				for(int i=0;i<opnum+1;i++){
					expdata[i]=new DomainData(condata.getCurrentVex());
					expdata[i].sideeffect=false;
				}
				//计算每个操作数的区间
				for (int i = 0; i < addexp.jjtGetNumChildren(); i++) {
					javanode = (SimpleJavaNode) addexp.jjtGetChild(i);
					javanode.jjtAccept(new ExpressionDomainVisitor(), expdata[i]);
				}
				right.jjtAccept(new ExpressionDomainVisitor(), expdata[opnum]);
				
				for(int i=0;i<addexp.jjtGetNumChildren();i++){
					javanode = (SimpleJavaNode) addexp.jjtGetChild(i);
					name = (ASTName) javanode.getSingleChildofType(ASTName.class);
					if (name != null) {
						nameimage = name.getImage();
					}	
					if (name != null && (name.getNameDeclaration() instanceof VariableNameDeclaration) && nameimage != null && !nameimage.contains(".")) {
						//当前操作数为变量，系数为-1或+1
						
						VariableNameDeclaration v = (VariableNameDeclaration) name.getNameDeclaration();
						if(!condata.isVariableContained(v)){
							//计算除去当前操作数之外的表达式区间值
							DomainData old=expdata[i];
							expdata[i]=new DomainData(condata.getCurrentVex());
							expdata[i].sideeffect=false;
							expdata[i].domain=new IntegerDomain(0,0);
							expdata[i].type=ClassType.INT;
							Object temp=calLinearWithout(i,expdata,addoperators,opnum);
							expdata[i]=old;
							
							boolean bunary=false;
							ASTUnaryExpression unary=(ASTUnaryExpression)name.getFirstParentOfType(ASTUnaryExpression.class, addexp);
							if(unary!=null&&unary.getImage().equals("-")){
								bunary=true;
							}
							
							if((i==0||addoperators[i-1].equals("+"))&&!bunary){//系数为+1
								Object leftdomain = node.findCurrentDomain(v, vex);
								ClassType lefttype = DomainSet.getDomainType(leftdomain);
								if (lefttype == ClassType.DOUBLE ) {
									DoubleDomain dright = (DoubleDomain) ConvertDomain.DomainSwitch(expdata[opnum].domain, ClassType.DOUBLE);
									DoubleDomain dleft = (DoubleDomain) ConvertDomain.DomainSwitch(leftdomain, ClassType.DOUBLE);
									dright=DoubleDomain.sub(dright, (DoubleDomain) ConvertDomain.DomainSwitch(temp, ClassType.DOUBLE));					
	
									DoubleInterval interval = dright.jointoOneInterval();
									String operator = operators[0];
									DoubleDomain may = null, must = null;
									if (!interval.isEmpty()) {
										if (operator.equals(">")) {
											may = DoubleDomain.intersect(dleft, new DoubleDomain(DoubleMath.nextfp(interval.getMin()), Double.POSITIVE_INFINITY, false, false));
											condata.addMayDomain(v, may);
											must = DoubleDomain.intersect(dleft, new DoubleDomain(DoubleMath.nextfp(interval.getMax()), Double.POSITIVE_INFINITY, false, false));
											condata.addMustDomain(v, must);
										} else if (operator.equals(">=")) {
											may = DoubleDomain.intersect(dleft, new DoubleDomain(interval.getMin(), Double.POSITIVE_INFINITY, false, false));
											condata.addMayDomain(v, may);
											must = DoubleDomain.intersect(dleft, new DoubleDomain(interval.getMax(), Double.POSITIVE_INFINITY, false, false));
											condata.addMustDomain(v, must);
										} else if (operator.equals("<")) {
											may = DoubleDomain.intersect(dleft, new DoubleDomain(Double.NEGATIVE_INFINITY, DoubleMath.prevfp(interval.getMax()), false, false));
											condata.addMayDomain(v, may);
											must = DoubleDomain.intersect(dleft, new DoubleDomain(Double.NEGATIVE_INFINITY, DoubleMath.prevfp(interval.getMin()), false, false));
											condata.addMustDomain(v, must);
										} else if (operator.equals("<=")) {
											may = DoubleDomain.intersect(dleft, new DoubleDomain(Double.NEGATIVE_INFINITY, interval.getMax(), false, false));
											condata.addMayDomain(v, may);
											must = DoubleDomain.intersect(dleft, new DoubleDomain(Double.NEGATIVE_INFINITY, interval.getMin(), false, false));
											condata.addMustDomain(v, must);
										} else {
											throw new RuntimeException("This is not a legal RelationalExpression");
										}
									}
								} else if (lefttype == ClassType.INT ) {
									IntegerDomain iright = (IntegerDomain) ConvertDomain.DomainSwitch(expdata[opnum].domain, ClassType.INT);
									IntegerDomain ileft = (IntegerDomain) ConvertDomain.DomainSwitch(leftdomain, ClassType.INT);
									iright=IntegerDomain.sub(iright, (IntegerDomain) ConvertDomain.DomainSwitch(temp, ClassType.INT));		
	
									IntegerInterval interval = iright.jointoOneInterval();
									String operator = operators[0];
									IntegerDomain may = null, must = null;
									if (!interval.isEmpty()) {
										if (operator.equals(">")) {
											may = IntegerDomain.intersect(ileft, new IntegerDomain(IntegerMath.nextInt(interval.getMin()), Long.MAX_VALUE, false, false));
											condata.addMayDomain(v, may);
											must = IntegerDomain.intersect(ileft, new IntegerDomain(IntegerMath.nextInt(interval.getMax()), Long.MAX_VALUE, false, false));
											condata.addMustDomain(v, must);
										} else if (operator.equals(">=")) {
											may = IntegerDomain.intersect(ileft, new IntegerDomain(interval.getMin(), Long.MAX_VALUE, false, false));
											condata.addMayDomain(v, may);
											must = IntegerDomain.intersect(ileft, new IntegerDomain(interval.getMax(), Long.MAX_VALUE, false, false));
											condata.addMustDomain(v, must);
										} else if (operator.equals("<")) {
											may = IntegerDomain.intersect(ileft, new IntegerDomain(Long.MIN_VALUE, IntegerMath.prevInt(interval.getMax()), false, false));
											condata.addMayDomain(v, may);
											must = IntegerDomain.intersect(ileft, new IntegerDomain(Long.MIN_VALUE, IntegerMath.prevInt(interval.getMin()), false, false));
											condata.addMustDomain(v, must);
										} else if (operator.equals("<=")) {
											may = IntegerDomain.intersect(ileft, new IntegerDomain(Long.MIN_VALUE, interval.getMax(), false, false));
											condata.addMayDomain(v, may);
											must = IntegerDomain.intersect(ileft, new IntegerDomain(Long.MIN_VALUE, interval.getMin(), false, false));
											condata.addMustDomain(v, must);
										} else {
											throw new RuntimeException("This is not a legal RelationalExpression");
										}
									}
								}
							}else{//系数为-1
								Object leftdomain = node.findCurrentDomain(v, vex);
								ClassType lefttype = DomainSet.getDomainType(leftdomain);
								if (lefttype == ClassType.DOUBLE ) {
									DoubleDomain dright = (DoubleDomain) ConvertDomain.DomainSwitch(expdata[opnum].domain, ClassType.DOUBLE);
									DoubleDomain dleft = (DoubleDomain) ConvertDomain.DomainSwitch(leftdomain, ClassType.DOUBLE);
									dright=DoubleDomain.sub((DoubleDomain) ConvertDomain.DomainSwitch(temp, ClassType.DOUBLE),dright);					
	
									DoubleInterval interval = dright.jointoOneInterval();
									String operator = operators[0];
									DoubleDomain may = null, must = null;
									if (!interval.isEmpty()) {
										if (operator.equals("<")) {
											may = DoubleDomain.intersect(dleft, new DoubleDomain(DoubleMath.nextfp(interval.getMin()), Double.POSITIVE_INFINITY, false, false));
											condata.addMayDomain(v, may);
											must = DoubleDomain.intersect(dleft, new DoubleDomain(DoubleMath.nextfp(interval.getMax()), Double.POSITIVE_INFINITY, false, false));
											condata.addMustDomain(v, must);
										} else if (operator.equals("<=")) {
											may = DoubleDomain.intersect(dleft, new DoubleDomain(interval.getMin(), Double.POSITIVE_INFINITY, false, false));
											condata.addMayDomain(v, may);
											must = DoubleDomain.intersect(dleft, new DoubleDomain(interval.getMax(), Double.POSITIVE_INFINITY, false, false));
											condata.addMustDomain(v, must);
										} else if (operator.equals(">")) {
											may = DoubleDomain.intersect(dleft, new DoubleDomain(Double.NEGATIVE_INFINITY, DoubleMath.prevfp(interval.getMax()), false, false));
											condata.addMayDomain(v, may);
											must = DoubleDomain.intersect(dleft, new DoubleDomain(Double.NEGATIVE_INFINITY, DoubleMath.prevfp(interval.getMin()), false, false));
											condata.addMustDomain(v, must);
										} else if (operator.equals(">=")) {
											may = DoubleDomain.intersect(dleft, new DoubleDomain(Double.NEGATIVE_INFINITY, interval.getMax(), false, false));
											condata.addMayDomain(v, may);
											must = DoubleDomain.intersect(dleft, new DoubleDomain(Double.NEGATIVE_INFINITY, interval.getMin(), false, false));
											condata.addMustDomain(v, must);
										} else {
											throw new RuntimeException("This is not a legal RelationalExpression");
										}
									}
								} else if (lefttype == ClassType.INT ) {
									IntegerDomain iright = (IntegerDomain) ConvertDomain.DomainSwitch(expdata[opnum].domain, ClassType.INT);
									IntegerDomain ileft = (IntegerDomain) ConvertDomain.DomainSwitch(leftdomain, ClassType.INT);
									iright=IntegerDomain.sub((IntegerDomain) ConvertDomain.DomainSwitch(temp, ClassType.INT),iright);		
	
									IntegerInterval interval = iright.jointoOneInterval();
									String operator = operators[0];
									IntegerDomain may = null, must = null;
									if (!interval.isEmpty()) {
										if (operator.equals("<")) {
											may = IntegerDomain.intersect(ileft, new IntegerDomain(IntegerMath.nextInt(interval.getMin()), Long.MAX_VALUE, false, false));
											condata.addMayDomain(v, may);
											must = IntegerDomain.intersect(ileft, new IntegerDomain(IntegerMath.nextInt(interval.getMax()), Long.MAX_VALUE, false, false));
											condata.addMustDomain(v, must);
										} else if (operator.equals("<=")) {
											may = IntegerDomain.intersect(ileft, new IntegerDomain(interval.getMin(), Long.MAX_VALUE, false, false));
											condata.addMayDomain(v, may);
											must = IntegerDomain.intersect(ileft, new IntegerDomain(interval.getMax(), Long.MAX_VALUE, false, false));
											condata.addMustDomain(v, must);
										} else if (operator.equals(">")) {
											may = IntegerDomain.intersect(ileft, new IntegerDomain(Long.MIN_VALUE, IntegerMath.prevInt(interval.getMax()), false, false));
											condata.addMayDomain(v, may);
											must = IntegerDomain.intersect(ileft, new IntegerDomain(Long.MIN_VALUE, IntegerMath.prevInt(interval.getMin()), false, false));
											condata.addMustDomain(v, must);
										} else if (operator.equals(">=")) {
											may = IntegerDomain.intersect(ileft, new IntegerDomain(Long.MIN_VALUE, interval.getMax(), false, false));
											condata.addMayDomain(v, may);
											must = IntegerDomain.intersect(ileft, new IntegerDomain(Long.MIN_VALUE, interval.getMin(), false, false));
											condata.addMustDomain(v, must);
										} else {
											throw new RuntimeException("This is not a legal RelationalExpression");
										}
									}
								}
							}
						}
					}
				}
			}
			
			addexp=(ASTAdditiveExpression) right.getSingleChildofType(ASTAdditiveExpression.class);
			if(addexp!=null){
				String addimage = addexp.getImage();
				if (addimage == null) {
					throw new RuntimeException("This is not a legal AdditiveExpression");
				}
				String[] addoperators = addimage.split("#");
				if (addoperators.length != (addexp.jjtGetNumChildren() - 1)) {
					throw new RuntimeException("This is not a legal AdditiveExpression");
				}
				int opnum=addexp.jjtGetNumChildren();
				//存放每个操作数的区间值，最后一个存放不等式左边区间值
				DomainData expdata[] = new DomainData[opnum+1];
				SimpleJavaNode javanode = null;
				for(int i=0;i<opnum+1;i++){
					expdata[i]=new DomainData(condata.getCurrentVex());
					expdata[i].sideeffect=false;
				}
				//计算每个操作数的区间
				for (int i = 0; i < addexp.jjtGetNumChildren(); i++) {
					javanode = (SimpleJavaNode) addexp.jjtGetChild(i);
					javanode.jjtAccept(new ExpressionDomainVisitor(), expdata[i]);
				}
				left.jjtAccept(new ExpressionDomainVisitor(), expdata[opnum]);
				
				for(int i=0;i<addexp.jjtGetNumChildren();i++){
					javanode = (SimpleJavaNode) addexp.jjtGetChild(i);
					name = (ASTName) javanode.getSingleChildofType(ASTName.class);
					if (name != null) {
						nameimage = name.getImage();
					}	
					if (name != null && (name.getNameDeclaration() instanceof VariableNameDeclaration) && nameimage != null && !nameimage.contains(".")) {
						//当前操作数为变量，系数为-1或+1
						
						VariableNameDeclaration v = (VariableNameDeclaration) name.getNameDeclaration();
						if(!condata.isVariableContained(v)){
							//计算除去当前操作数之外的表达式区间值
							DomainData old=expdata[i];
							expdata[i]=new DomainData(condata.getCurrentVex());
							expdata[i].sideeffect=false;
							expdata[i].domain=new IntegerDomain(0,0);
							expdata[i].type=ClassType.INT;
							Object temp=calLinearWithout(i,expdata,addoperators,opnum);
							expdata[i]=old;
							
							boolean bunary=false;
							ASTUnaryExpression unary=(ASTUnaryExpression)name.getFirstParentOfType(ASTUnaryExpression.class, addexp);
							if(unary!=null&&unary.getImage().equals("-")){
								bunary=true;
							}
							
							if((i==0||addoperators[i-1].equals("+"))&&!bunary){//系数为+1
								Object rightdomain = node.findCurrentDomain(v, vex);
								ClassType righttype = DomainSet.getDomainType(rightdomain);

								if (righttype == ClassType.DOUBLE ) {
									DoubleDomain dright = (DoubleDomain) ConvertDomain.DomainSwitch(rightdomain, ClassType.DOUBLE);
									DoubleDomain dleft = (DoubleDomain) ConvertDomain.DomainSwitch(expdata[opnum].domain, ClassType.DOUBLE);
									dleft=DoubleDomain.sub(dleft, (DoubleDomain) ConvertDomain.DomainSwitch(temp, ClassType.DOUBLE));	

									DoubleInterval interval = dleft.jointoOneInterval();
									String operator = operators[0];
									DoubleDomain may = null, must = null;
									if (!interval.isEmpty()) {
										if (operator.equals("<")) {
											may = DoubleDomain.intersect(dright, new DoubleDomain(DoubleMath.nextfp(interval.getMin()), Double.POSITIVE_INFINITY, false, false));
											condata.addMayDomain(v, may);
											must = DoubleDomain.intersect(dright, new DoubleDomain(DoubleMath.nextfp(interval.getMax()), Double.POSITIVE_INFINITY, false, false));
											condata.addMustDomain(v, must);
										} else if (operator.equals("<=")) {
											may = DoubleDomain.intersect(dright, new DoubleDomain(interval.getMin(), Double.POSITIVE_INFINITY, false, false));
											condata.addMayDomain(v, may);
											must = DoubleDomain.intersect(dright, new DoubleDomain(interval.getMax(), Double.POSITIVE_INFINITY, false, false));
											condata.addMustDomain(v, must);
										} else if (operator.equals(">")) {
											may = DoubleDomain.intersect(dright, new DoubleDomain(Double.NEGATIVE_INFINITY, DoubleMath.prevfp(interval.getMax()), false, false));
											condata.addMayDomain(v, may);
											must = DoubleDomain.intersect(dright, new DoubleDomain(Double.NEGATIVE_INFINITY, DoubleMath.prevfp(interval.getMin()), false, false));
											condata.addMustDomain(v, must);
										} else if (operator.equals(">=")) {
											may = DoubleDomain.intersect(dright, new DoubleDomain(Double.NEGATIVE_INFINITY, interval.getMax(), false, false));
											condata.addMayDomain(v, may);
											must = DoubleDomain.intersect(dright, new DoubleDomain(Double.NEGATIVE_INFINITY, interval.getMin(), false, false));
											condata.addMustDomain(v, must);
										} else {
											throw new RuntimeException("This is not a legal RelationalExpression");
										}
									}
								} else if (righttype == ClassType.INT ) {
									IntegerDomain iright = (IntegerDomain) ConvertDomain.DomainSwitch(rightdomain, ClassType.INT);
									IntegerDomain ileft = (IntegerDomain) ConvertDomain.DomainSwitch(expdata[opnum].domain, ClassType.INT);
									ileft=IntegerDomain.sub(ileft, (IntegerDomain) ConvertDomain.DomainSwitch(temp, ClassType.INT));	
									
									IntegerInterval interval = ileft.jointoOneInterval();

									String operator = operators[0];
									IntegerDomain may = null, must = null;
									if (!interval.isEmpty()) {
										if (operator.equals("<")) {
											may = IntegerDomain.intersect(iright, new IntegerDomain(IntegerMath.nextInt(interval.getMin()), Long.MAX_VALUE, false, false));
											condata.addMayDomain(v, may);
											must = IntegerDomain.intersect(iright, new IntegerDomain(IntegerMath.nextInt(interval.getMax()), Long.MAX_VALUE, false, false));
											condata.addMustDomain(v, must);
										} else if (operator.equals("<=")) {
											may = IntegerDomain.intersect(iright, new IntegerDomain(interval.getMin(), Long.MAX_VALUE, false, false));
											condata.addMayDomain(v, may);
											must = IntegerDomain.intersect(iright, new IntegerDomain(interval.getMax(), Long.MAX_VALUE, false, false));
											condata.addMustDomain(v, must);
										} else if (operator.equals(">")) {
											may = IntegerDomain.intersect(iright, new IntegerDomain(Long.MIN_VALUE, IntegerMath.prevInt(interval.getMax()), false, false));
											condata.addMayDomain(v, may);
											must = IntegerDomain.intersect(iright, new IntegerDomain(Long.MIN_VALUE, IntegerMath.prevInt(interval.getMin()), false, false));
											condata.addMustDomain(v, must);
										} else if (operator.equals(">=")) {
											may = IntegerDomain.intersect(iright, new IntegerDomain(Long.MIN_VALUE, interval.getMax(), false, false));
											condata.addMayDomain(v, may);
											must = IntegerDomain.intersect(iright, new IntegerDomain(Long.MIN_VALUE, interval.getMin(), false, false));
											condata.addMustDomain(v, must);
										} else {
											throw new RuntimeException("This is not a legal RelationalExpression");
										}
									}
								}
							}else{//系数为-1
								Object rightdomain = node.findCurrentDomain(v, vex);
								ClassType righttype = DomainSet.getDomainType(rightdomain);

								if (righttype == ClassType.DOUBLE ) {
									DoubleDomain dright = (DoubleDomain) ConvertDomain.DomainSwitch(rightdomain, ClassType.DOUBLE);
									DoubleDomain dleft = (DoubleDomain) ConvertDomain.DomainSwitch(expdata[opnum].domain, ClassType.DOUBLE);
									dleft=DoubleDomain.sub((DoubleDomain) ConvertDomain.DomainSwitch(temp, ClassType.DOUBLE),dleft);	

									DoubleInterval interval = dleft.jointoOneInterval();
									String operator = operators[0];
									DoubleDomain may = null, must = null;
									if (!interval.isEmpty()) {
										if (operator.equals(">")) {
											may = DoubleDomain.intersect(dright, new DoubleDomain(DoubleMath.nextfp(interval.getMin()), Double.POSITIVE_INFINITY, false, false));
											condata.addMayDomain(v, may);
											must = DoubleDomain.intersect(dright, new DoubleDomain(DoubleMath.nextfp(interval.getMax()), Double.POSITIVE_INFINITY, false, false));
											condata.addMustDomain(v, must);
										} else if (operator.equals(">=")) {
											may = DoubleDomain.intersect(dright, new DoubleDomain(interval.getMin(), Double.POSITIVE_INFINITY, false, false));
											condata.addMayDomain(v, may);
											must = DoubleDomain.intersect(dright, new DoubleDomain(interval.getMax(), Double.POSITIVE_INFINITY, false, false));
											condata.addMustDomain(v, must);
										} else if (operator.equals("<")) {
											may = DoubleDomain.intersect(dright, new DoubleDomain(Double.NEGATIVE_INFINITY, DoubleMath.prevfp(interval.getMax()), false, false));
											condata.addMayDomain(v, may);
											must = DoubleDomain.intersect(dright, new DoubleDomain(Double.NEGATIVE_INFINITY, DoubleMath.prevfp(interval.getMin()), false, false));
											condata.addMustDomain(v, must);
										} else if (operator.equals("<=")) {
											may = DoubleDomain.intersect(dright, new DoubleDomain(Double.NEGATIVE_INFINITY, interval.getMax(), false, false));
											condata.addMayDomain(v, may);
											must = DoubleDomain.intersect(dright, new DoubleDomain(Double.NEGATIVE_INFINITY, interval.getMin(), false, false));
											condata.addMustDomain(v, must);
										} else {
											throw new RuntimeException("This is not a legal RelationalExpression");
										}
									}
								} else if (righttype == ClassType.INT ) {
									IntegerDomain iright = (IntegerDomain) ConvertDomain.DomainSwitch(rightdomain, ClassType.INT);
									IntegerDomain ileft = (IntegerDomain) ConvertDomain.DomainSwitch(expdata[opnum].domain, ClassType.INT);
									ileft=IntegerDomain.sub((IntegerDomain) ConvertDomain.DomainSwitch(temp, ClassType.INT),ileft);	
									
									IntegerInterval interval = ileft.jointoOneInterval();

									String operator = operators[0];
									IntegerDomain may = null, must = null;
									if (!interval.isEmpty()) {
										if (operator.equals(">")) {
											may = IntegerDomain.intersect(iright, new IntegerDomain(IntegerMath.prevInt(interval.getMin()), Long.MAX_VALUE, false, false));
											condata.addMayDomain(v, may);
											must = IntegerDomain.intersect(iright, new IntegerDomain(IntegerMath.nextInt(interval.getMax()), Long.MAX_VALUE, false, false));
											condata.addMustDomain(v, must);
										} else if (operator.equals(">=")) {
											may = IntegerDomain.intersect(iright, new IntegerDomain(interval.getMin(), Long.MAX_VALUE, false, false));
											condata.addMayDomain(v, may);
											must = IntegerDomain.intersect(iright, new IntegerDomain(interval.getMax(), Long.MAX_VALUE, false, false));
											condata.addMustDomain(v, must);
										} else if (operator.equals("<")) {
											may = IntegerDomain.intersect(iright, new IntegerDomain(Long.MIN_VALUE, IntegerMath.prevInt(interval.getMax()), false, false));
											condata.addMayDomain(v, may);
											must = IntegerDomain.intersect(iright, new IntegerDomain(Long.MIN_VALUE, IntegerMath.prevInt(interval.getMin()), false, false));
											condata.addMustDomain(v, must);
										} else if (operator.equals("<=")) {
											may = IntegerDomain.intersect(iright, new IntegerDomain(Long.MIN_VALUE, interval.getMax(), false, false));
											condata.addMayDomain(v, may);
											must = IntegerDomain.intersect(iright, new IntegerDomain(Long.MIN_VALUE, interval.getMin(), false, false));
											condata.addMustDomain(v, must);
										} else {
											throw new RuntimeException("This is not a legal RelationalExpression");
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return null;
	}

	/** 处理相等关系表达式，目前只处理了（x op 表达式） 和（表达式 op x）,其中x为变量两种基本情况 */
	public Object visit(ASTEqualityExpression node, Object data) {
		// == ！=
		ConditionData condata = (ConditionData) data;
		SimpleJavaNode left = (SimpleJavaNode) node.jjtGetChild(0);
		SimpleJavaNode right = (SimpleJavaNode) node.jjtGetChild(1);
		
		VexNode vex = condata.getCurrentVex();
		if (vex == null) {
			vex = node.getCurrentVexNode();
		}
		
		String image = node.getImage();
		if (image == null) {
			throw new RuntimeException("This is not a legal EqualityExpression");
		}
		String[] operators = image.split("#");
		if (operators.length != (node.jjtGetNumChildren() - 1)) {
			throw new RuntimeException("This is not a legal EqualityExpression");
		}

		if (operators.length != 1) {
			return null;
		}

		ASTName name = (ASTName) (left.getSingleChildofType(ASTName.class));
		if (name == null) {
			ASTExpression expression = (ASTExpression) (left.getSingleChildofType(ASTExpression.class));
			if (expression != null && expression.jjtGetNumChildren() == 3) {
				if (expression.jjtGetChild(1) instanceof ASTAssignmentOperator) {
					if (((SimpleJavaNode) (expression.jjtGetChild(1))).getImage().equals("=")) {
						name = (ASTName) ((SimpleJavaNode) (expression.jjtGetChild(0))).getSingleChildofType(ASTName.class);
					}
				}
			}
		}
		String nameimage = null;
		if (name != null) {
			nameimage = name.getImage();
		}
		if (name != null && (name.getNameDeclaration() instanceof VariableNameDeclaration) && nameimage != null && !nameimage.contains(".")) {
			// 左边为变量情况
			VariableNameDeclaration v = (VariableNameDeclaration) name.getNameDeclaration(); 

			DomainData expdata = new DomainData(condata.getCurrentVex());
			expdata.sideeffect = false;
			// 计算等号右边表达式
			right.jjtAccept(new ExpressionDomainVisitor(), expdata);
			Object rightdomain = expdata.domain;
			
			Object leftdomain =  node.findCurrentDomain(v, vex);
			ClassType lefttype = DomainSet.getDomainType(leftdomain);

			if (lefttype == ClassType.DOUBLE ) {
				DoubleDomain dright = (DoubleDomain) ConvertDomain.DomainSwitch(rightdomain, ClassType.DOUBLE);
				DoubleDomain dleft = (DoubleDomain) ConvertDomain.DomainSwitch(leftdomain, ClassType.DOUBLE);

				String operator = operators[0];
				DoubleDomain may = null, must = null;
				if (operator.equals("==")) {
					may = DoubleDomain.intersect(dleft, dright);
					condata.addMayDomain(v, may);
					if (dright.isCanonical()) {
						must = DoubleDomain.intersect(dleft, dright);
					} else {
						must = new DoubleDomain();
					}
					condata.addMustDomain(v, must);
				} else if (operator.equals("!=")) {
					may = DoubleDomain.intersect(dleft, dright);
					if (dright.isCanonical()) {
						must = DoubleDomain.intersect(dleft, dright);
					} else {
						must = new DoubleDomain();
					}
					condata.addMayDomain(v, DoubleDomain.subtract(dleft, must));
					condata.addMustDomain(v, DoubleDomain.subtract(dleft, may));
				} else {
					throw new RuntimeException("This is not a legal EqualityExpression");
				}
			} else if (lefttype == ClassType.INT ) {
				IntegerDomain iright = (IntegerDomain) ConvertDomain.DomainSwitch(rightdomain, ClassType.INT);
				IntegerDomain ileft = (IntegerDomain) ConvertDomain.DomainSwitch(leftdomain, ClassType.INT);

				String operator = operators[0];
				IntegerDomain may = null, must = null;

				if (operator.equals("==")) {
					may = IntegerDomain.intersect(ileft, iright);
					condata.addMayDomain(v, may);
					if (iright.isCanonical()) {
						must = IntegerDomain.intersect(ileft, iright);
					} else {
						must = new IntegerDomain();
					}
					condata.addMustDomain(v, must);
				} else if (operator.equals("!=")) {
					may = IntegerDomain.intersect(ileft, iright);
					if (iright.isCanonical()) {
						must = IntegerDomain.intersect(ileft, iright);
					} else {
						must = new IntegerDomain();
					}
					condata.addMayDomain(v, IntegerDomain.subtract(ileft, must));
					condata.addMustDomain(v, IntegerDomain.subtract(ileft, may));
				} else {
					throw new RuntimeException("This is not a legal EqualityExpression");
				}
			} else if (lefttype == ClassType.BOOLEAN ) {
				BooleanDomain bright = (BooleanDomain) ConvertDomain.DomainSwitch(rightdomain, ClassType.BOOLEAN);
				BooleanDomain bleft = (BooleanDomain) ConvertDomain.DomainSwitch(leftdomain, ClassType.BOOLEAN);

				String operator = operators[0];
				BooleanDomain may = null, must = null;
				if (operator.equals("==")) {
					if(bright.getUnknown()){
						may = BooleanDomain.intersect(bleft, bright);
						condata.addMayDomain(v, may);
						must = new BooleanDomain(BooleanValue.EMPTY);//BooleanDomain.intersect(bleft, bright);
						condata.addMustDomain(v, must);
					}else{
						switch (bright.getValue()) {
						case TRUE:
							may = BooleanDomain.intersect(bleft,new BooleanDomain(BooleanValue.TRUE));
							condata.addMayDomain(v, may);
							must = BooleanDomain.intersect(bleft, new BooleanDomain(BooleanValue.TRUE));
							condata.addMustDomain(v, must);
							break;
						case FALSE:
							may = BooleanDomain.intersect(bleft, new BooleanDomain(BooleanValue.FALSE));
							condata.addMayDomain(v, may);
							must = BooleanDomain.intersect(bleft, new BooleanDomain(BooleanValue.FALSE));
							condata.addMustDomain(v, must);
							break;
						case TRUE_OR_FALSE:
							may = BooleanDomain.intersect(bleft, bright);
							condata.addMayDomain(v, new BooleanDomain(BooleanValue.TRUE_OR_FALSE));
							must = BooleanDomain.intersect(bleft, new BooleanDomain(BooleanValue.EMPTY));
							condata.addMustDomain(v, must);
							break;
						}
					}
				} else if (operator.equals("!=")) {
					if(bright.getUnknown()){
						may = BooleanDomain.intersect(bleft, bright);
						condata.addMayDomain(v, may);
						must = new BooleanDomain(BooleanValue.EMPTY);//BooleanDomain.intersect(bleft, bright);
						condata.addMustDomain(v, must);
					}else{
						switch (bright.getValue()) {
						case TRUE:
							may = BooleanDomain.intersect(bleft, new BooleanDomain(BooleanValue.FALSE));
							condata.addMayDomain(v, may);
							must = BooleanDomain.intersect(bleft, new BooleanDomain(BooleanValue.FALSE));
							condata.addMustDomain(v, must);
							break;
						case FALSE:
							may = BooleanDomain.intersect(bleft, new BooleanDomain(BooleanValue.TRUE));
							condata.addMayDomain(v, may);
							must = BooleanDomain.intersect(bleft, new BooleanDomain(BooleanValue.TRUE));
							condata.addMustDomain(v, must);
							break;
						case TRUE_OR_FALSE:
							may = BooleanDomain.intersect(bleft, new BooleanDomain(BooleanValue.TRUE_OR_FALSE));
							condata.addMayDomain(v, may);
							must = BooleanDomain.intersect(bleft, new BooleanDomain(BooleanValue.EMPTY));
							condata.addMustDomain(v, must);
							break;
						}
					}
				} else {
					throw new RuntimeException("This is not a legal EqualityExpression");
				}
			} else if (lefttype == ClassType.REF ) {
				ReferenceDomain rright = (ReferenceDomain) ConvertDomain.DomainSwitch(rightdomain, ClassType.REF);
				ReferenceDomain rleft = (ReferenceDomain) ConvertDomain.DomainSwitch(leftdomain, ClassType.REF);

				String operator = operators[0];
				ReferenceDomain may = null, must = null;
				if (operator.equals("==")) {
					if(rright.getUnknown()){
						//may = ReferenceDomain.intersect(rleft, rright);
						may=ReferenceDomain.getUnknownDomain();
						condata.addMayDomain(v, may);
						//must = ReferenceDomain.intersect(rleft, rright);
						//must = ReferenceDomain.intersect(rleft, new ReferenceDomain(ReferenceValue.EMPTY));
						//must = ReferenceDomain.intersect(rleft, new ReferenceDomain(ReferenceValue.EMPTY));
						must=ReferenceDomain.getUnknownDomain();
						condata.addMustDomain(v, must);
					}else{
						switch (rright.getValue()) {
						case NOTNULL:
							may = ReferenceDomain.intersect(rleft, new ReferenceDomain(ReferenceValue.NOTNULL));
							condata.addMayDomain(v, may);
							//must = ReferenceDomain.intersect(rleft, new ReferenceDomain(ReferenceValue.EMPTY));
							must=ReferenceDomain.getUnknownDomain();
							condata.addMustDomain(v, must);
							break;
						case NULL:
							may = ReferenceDomain.intersect(rleft, new ReferenceDomain(ReferenceValue.NULL));
							condata.addMayDomain(v, may);
							must = ReferenceDomain.intersect(rleft, new ReferenceDomain(ReferenceValue.NULL));
							condata.addMustDomain(v, must);
							break;
						case NULL_OR_NOTNULL:
							//may = ReferenceDomain.intersect(rleft, new ReferenceDomain(ReferenceValue.NULL_OR_NOTNULL));
							may=ReferenceDomain.getUnknownDomain();
							condata.addMayDomain(v, may);
							//must = ReferenceDomain.intersect(rleft, new ReferenceDomain(ReferenceValue.EMPTY));
							must=ReferenceDomain.getUnknownDomain();
							condata.addMustDomain(v, must);
							break;
						}
					}
				} else if (operator.equals("!=")) {
					if(rright.getUnknown()){
						//may = ReferenceDomain.intersect(rleft, rright);
						may=ReferenceDomain.getUnknownDomain();
						condata.addMayDomain(v, may);
						//must = ReferenceDomain.intersect(rleft, rright);
						//must = ReferenceDomain.intersect(rleft, new ReferenceDomain(ReferenceValue.EMPTY));
						must=ReferenceDomain.getUnknownDomain();
						condata.addMustDomain(v, must);
					}else{
						switch (rright.getValue()) {
						case NOTNULL:
							//may = ReferenceDomain.intersect(rleft, new ReferenceDomain(ReferenceValue.NULL_OR_NOTNULL));
							may=ReferenceDomain.getUnknownDomain();
							condata.addMayDomain(v, may);
							must = ReferenceDomain.intersect(rleft, new ReferenceDomain(ReferenceValue.NULL));
							condata.addMustDomain(v, must);
							break;
						case NULL:
							may = ReferenceDomain.intersect(rleft, new ReferenceDomain(ReferenceValue.NOTNULL));
							condata.addMayDomain(v, may);
							must = ReferenceDomain.intersect(rleft, new ReferenceDomain(ReferenceValue.NOTNULL));
							condata.addMustDomain(v, must);
							break;
						case NULL_OR_NOTNULL:
							//may = ReferenceDomain.intersect(rleft, new ReferenceDomain(ReferenceValue.NULL_OR_NOTNULL));
							may=ReferenceDomain.getUnknownDomain();
							condata.addMayDomain(v, may);
							//must = ReferenceDomain.intersect(rleft, new ReferenceDomain(ReferenceValue.EMPTY));
							must=ReferenceDomain.getUnknownDomain();
							condata.addMustDomain(v, must);
							break;
						}
					}
				} else {
					throw new RuntimeException("This is not a legal EqualityExpression");
				}
			}
			/***
			 * @author yang
			 * 2011-05-16 14:55
			 */
			
			else if (lefttype == ClassType.ARRAY ) {
				ReferenceDomain rright = (ReferenceDomain) ConvertDomain.DomainSwitch(rightdomain, ClassType.REF);
				ReferenceDomain rleft = (ReferenceDomain) ConvertDomain.DomainSwitch(leftdomain, ClassType.REF);

				String operator = operators[0];
				ReferenceDomain may = null, must = null;
				if (operator.equals("==")) {
					if(rright.getUnknown()){
						may=ReferenceDomain.getUnknownDomain();
						condata.addMayDomain(v, may);
						must=ReferenceDomain.getUnknownDomain();
						condata.addMustDomain(v, must);
					}else{
						switch (rright.getValue()) {
						case NOTNULL:
							may = ReferenceDomain.intersect(rleft, new ReferenceDomain(ReferenceValue.NOTNULL));
							condata.addMayDomain(v, may);
							must=ReferenceDomain.getUnknownDomain();
							condata.addMustDomain(v, must);
							break;
						case NULL:
							may = ReferenceDomain.intersect(rleft, new ReferenceDomain(ReferenceValue.NULL));
							condata.addMayDomain(v, may);
							must = ReferenceDomain.intersect(rleft, new ReferenceDomain(ReferenceValue.NULL));
							condata.addMustDomain(v, must);
							break;
						case NULL_OR_NOTNULL:
							may=ReferenceDomain.getUnknownDomain();
							condata.addMayDomain(v, may);
							must=ReferenceDomain.getUnknownDomain();
							condata.addMustDomain(v, must);
							break;
						}
					}
				} else if (operator.equals("!=")) {
					if(rright.getUnknown()){
						may=ReferenceDomain.getUnknownDomain();
						condata.addMayDomain(v, may);
						must=ReferenceDomain.getUnknownDomain();
						condata.addMustDomain(v, must);
					}else{
						switch (rright.getValue()) {
						case NOTNULL:
							may=ReferenceDomain.getUnknownDomain();
							condata.addMayDomain(v, may);
							must = ReferenceDomain.intersect(rleft, new ReferenceDomain(ReferenceValue.NULL));
							condata.addMustDomain(v, must);
							break;
						case NULL:
							may = ReferenceDomain.intersect(rleft, new ReferenceDomain(ReferenceValue.NOTNULL));
							condata.addMayDomain(v, may);
							must = ReferenceDomain.intersect(rleft, new ReferenceDomain(ReferenceValue.NOTNULL));
							condata.addMustDomain(v, must);
							break;
						case NULL_OR_NOTNULL:
							//may = ReferenceDomain.intersect(rleft, new ReferenceDomain(ReferenceValue.NULL_OR_NOTNULL));
							may=ReferenceDomain.getUnknownDomain();
							condata.addMayDomain(v, may);
							//must = ReferenceDomain.intersect(rleft, new ReferenceDomain(ReferenceValue.EMPTY));
							must=ReferenceDomain.getUnknownDomain();
							condata.addMustDomain(v, must);
							break;
						}
					}
				} else {
					throw new RuntimeException("This is not a legal EqualityExpression");
				}
			}
			//end-yang
			
		}

		name = (ASTName) (right.getSingleChildofType(ASTName.class));
		if (name == null) {
			ASTExpression expression = (ASTExpression) (right.getSingleChildofType(ASTExpression.class));
			if (expression != null && expression.jjtGetNumChildren() == 3) {
				if (expression.jjtGetChild(1) instanceof ASTAssignmentOperator) {
					if (((SimpleJavaNode) (expression.jjtGetChild(1))).getImage().equals("=")) {
						name = (ASTName) ((SimpleJavaNode) (expression.jjtGetChild(0))).getSingleChildofType(ASTName.class);
					}
				}
			}
		}
		if (name != null) {
			nameimage = name.getImage();
		}
		if (name != null && (name.getNameDeclaration() instanceof VariableNameDeclaration) && nameimage != null && !nameimage.contains(".")) {
			// 右边为变量情况
			VariableNameDeclaration v = (VariableNameDeclaration) name.getNameDeclaration();

			DomainData expdata = new DomainData(condata.getCurrentVex());
			expdata.sideeffect = false;
			// 计算等号左边表达式
			left.jjtAccept(new ExpressionDomainVisitor(), expdata);
			Object leftdomain = expdata.domain;

			Object rightdomain = node.findCurrentDomain(v, vex);
			ClassType righttype = DomainSet.getDomainType(rightdomain);

			// 左右互换
			if ( righttype == ClassType.DOUBLE) {
				DoubleDomain dleft = (DoubleDomain) ConvertDomain.DomainSwitch(rightdomain, ClassType.DOUBLE);
				DoubleDomain dright = (DoubleDomain) ConvertDomain.DomainSwitch(leftdomain, ClassType.DOUBLE);

				String operator = operators[0];
				DoubleDomain may = null, must = null;
				if (operator.equals("==")) {
					may = DoubleDomain.intersect(dleft, dright);
					condata.addMayDomain(v, may);
					if (dright.isCanonical()) {
						must = DoubleDomain.intersect(dleft, dright);
					} else {
						must = new DoubleDomain();
					}
					condata.addMustDomain(v, must);
				} else if (operator.equals("!=")) {
					may = DoubleDomain.intersect(dleft, dright);
					if (dright.isCanonical()) {
						must = DoubleDomain.intersect(dleft, dright);
					} else {
						must = new DoubleDomain();
					}
					condata.addMayDomain(v, DoubleDomain.subtract(dleft, must));
					condata.addMustDomain(v, DoubleDomain.subtract(dleft, may));
				} else {
					throw new RuntimeException("This is not a legal EqualityExpression");
				}
			} else if ( righttype == ClassType.INT) {
				IntegerDomain ileft = (IntegerDomain) ConvertDomain.DomainSwitch(rightdomain, ClassType.INT);
				IntegerDomain iright = (IntegerDomain) ConvertDomain.DomainSwitch(leftdomain, ClassType.INT);

				String operator = operators[0];
				IntegerDomain may = null, must = null;

				if (operator.equals("==")) {
					may = IntegerDomain.intersect(ileft, iright);
					condata.addMayDomain(v, may);
					if (iright.isCanonical()) {
						must = IntegerDomain.intersect(ileft, iright);
					} else {
						must = new IntegerDomain();
					}
					condata.addMustDomain(v, must);
				} else if (operator.equals("!=")) {
					may = IntegerDomain.intersect(ileft, iright);
					if (iright.isCanonical()) {
						must = IntegerDomain.intersect(ileft, iright);
					} else {
						must = new IntegerDomain();
					}
					condata.addMayDomain(v, IntegerDomain.subtract(ileft, must));
					condata.addMustDomain(v, IntegerDomain.subtract(ileft, may));
				} else {
					throw new RuntimeException("This is not a legal EqualityExpression");
				}
			} else if ( righttype == ClassType.BOOLEAN) {
				BooleanDomain bleft = (BooleanDomain) ConvertDomain.DomainSwitch(rightdomain, ClassType.BOOLEAN);
				BooleanDomain bright = (BooleanDomain) ConvertDomain.DomainSwitch(leftdomain, ClassType.BOOLEAN);

				String operator = operators[0];
				BooleanDomain may = null, must = null;
				if (operator.equals("==")) {
					if(bright.getUnknown()){
						may = BooleanDomain.intersect(bleft, bright);
						condata.addMayDomain(v, may);
						must = new BooleanDomain(BooleanValue.EMPTY);//BooleanDomain.intersect(bleft, bright);
						condata.addMustDomain(v, must);
					}else{
						switch (bright.getValue()) {
						case TRUE:
							may = BooleanDomain.intersect(bleft, new BooleanDomain(BooleanValue.TRUE));
							condata.addMayDomain(v, may);
							must = BooleanDomain.intersect(bleft, new BooleanDomain(BooleanValue.TRUE));
							condata.addMustDomain(v, must);
							break;
						case FALSE:
							may = BooleanDomain.intersect(bleft, new BooleanDomain(BooleanValue.FALSE));
							condata.addMayDomain(v, may);
							must = BooleanDomain.intersect(bleft, new BooleanDomain(BooleanValue.FALSE));
							condata.addMustDomain(v, must);
							break;
						case TRUE_OR_FALSE:
							may = BooleanDomain.intersect(bleft, new BooleanDomain(BooleanValue.TRUE_OR_FALSE));
							condata.addMayDomain(v, may);
							must = BooleanDomain.intersect(bleft, new BooleanDomain(BooleanValue.EMPTY));
							condata.addMustDomain(v, must);
							break;
						}
					}
				} else if (operator.equals("!=")) {
					if(bright.getUnknown()){
						may = BooleanDomain.intersect(bleft, bright);
						condata.addMayDomain(v, may);
						must = new BooleanDomain(BooleanValue.EMPTY);//BooleanDomain.intersect(bleft, bright);
						condata.addMustDomain(v, must);
					}else{
						switch (bright.getValue()) {
						case TRUE:
							may = BooleanDomain.intersect(bleft, new BooleanDomain(BooleanValue.FALSE));
							condata.addMayDomain(v, may);
							must = BooleanDomain.intersect(bleft, new BooleanDomain(BooleanValue.FALSE));
							condata.addMustDomain(v, must);
							break;
						case FALSE:
							may = BooleanDomain.intersect(bleft, new BooleanDomain(BooleanValue.TRUE));
							condata.addMayDomain(v, may);
							must = BooleanDomain.intersect(bleft, new BooleanDomain(BooleanValue.TRUE));
							condata.addMustDomain(v, must);
							break;
						case TRUE_OR_FALSE:
							may = BooleanDomain.intersect(bleft, new BooleanDomain(BooleanValue.TRUE_OR_FALSE));
							condata.addMayDomain(v, may);
							must = BooleanDomain.intersect(bleft, new BooleanDomain(BooleanValue.EMPTY));
							condata.addMustDomain(v, must);
							break;
						}
					}
				} else {
					throw new RuntimeException("This is not a legal EqualityExpression");
				}
			} else if (righttype == ClassType.REF) {
				ReferenceDomain rleft = (ReferenceDomain) ConvertDomain.DomainSwitch(rightdomain, ClassType.REF);
				ReferenceDomain rright = (ReferenceDomain) ConvertDomain.DomainSwitch(leftdomain, ClassType.REF);

				String operator = operators[0];
				ReferenceDomain may = null, must = null;
				if (operator.equals("==")) {
					if(rright.getUnknown()){
						//may = ReferenceDomain.intersect(rleft, rright);
						may=ReferenceDomain.getUnknownDomain();
						condata.addMayDomain(v, may);
						//must = ReferenceDomain.intersect(rleft, rright);
						//must = ReferenceDomain.intersect(rleft, new ReferenceDomain(ReferenceValue.EMPTY));
						must=ReferenceDomain.getUnknownDomain();
						condata.addMustDomain(v, must);
					}else{
						switch (rright.getValue()) {
						case NOTNULL:
							may = ReferenceDomain.intersect(rleft, new ReferenceDomain(ReferenceValue.NOTNULL));
							condata.addMayDomain(v, may);
							//must = ReferenceDomain.intersect(rleft, new ReferenceDomain(ReferenceValue.EMPTY));
							must=ReferenceDomain.getUnknownDomain();
							condata.addMustDomain(v, must);
							break;
						case NULL:
							may = ReferenceDomain.intersect(rleft, new ReferenceDomain(ReferenceValue.NULL));
							condata.addMayDomain(v, may);
							must = ReferenceDomain.intersect(rleft, new ReferenceDomain(ReferenceValue.NULL));
							condata.addMustDomain(v, must);
							break;
						case NULL_OR_NOTNULL:
							//may = ReferenceDomain.intersect(rleft, new ReferenceDomain(ReferenceValue.NULL_OR_NOTNULL));
							may=ReferenceDomain.getUnknownDomain();
							condata.addMayDomain(v, may);
							//must = ReferenceDomain.intersect(rleft, new ReferenceDomain(ReferenceValue.EMPTY));
							must=ReferenceDomain.getUnknownDomain();
							condata.addMustDomain(v, must);
							break;
						}
					}
				} else if (operator.equals("!=")) {
					if(rright.getUnknown()){
						//may = ReferenceDomain.intersect(rleft, rright);
						may=ReferenceDomain.getUnknownDomain();
						condata.addMayDomain(v, may);
						//must = ReferenceDomain.intersect(rleft, rright);
						//must = ReferenceDomain.intersect(rleft, new ReferenceDomain(ReferenceValue.EMPTY));
						must=ReferenceDomain.getUnknownDomain();
						condata.addMustDomain(v, must);
					}else{
						switch (rright.getValue()) {
						case NOTNULL:
							//may = ReferenceDomain.intersect(rleft, new ReferenceDomain(ReferenceValue.NULL_OR_NOTNULL));
							may=ReferenceDomain.getUnknownDomain();
							condata.addMayDomain(v, may);
							must = ReferenceDomain.intersect(rleft, new ReferenceDomain(ReferenceValue.NULL));
							condata.addMustDomain(v, must);
							break;
						case NULL:
							may = ReferenceDomain.intersect(rleft, new ReferenceDomain(ReferenceValue.NOTNULL));
							condata.addMayDomain(v, may);
							must = ReferenceDomain.intersect(rleft, new ReferenceDomain(ReferenceValue.NOTNULL));
							condata.addMustDomain(v, must);
							break;
						case NULL_OR_NOTNULL:
							//may = ReferenceDomain.intersect(rleft, new ReferenceDomain(ReferenceValue.NULL_OR_NOTNULL));
							may=ReferenceDomain.getUnknownDomain();
							condata.addMayDomain(v, may);
							//must = ReferenceDomain.intersect(rleft, new ReferenceDomain(ReferenceValue.EMPTY));
							must=ReferenceDomain.getUnknownDomain();
							condata.addMustDomain(v, must);
							break;
						}
					}
				} else {
					throw new RuntimeException("This is not a legal EqualityExpression");
				}
			}
			/***
			 * @author yang
			 * 2011-06-16 14:55
			 */
			
			else if (righttype == ClassType.ARRAY ) {
				ReferenceDomain rright = (ReferenceDomain) ConvertDomain.DomainSwitch(rightdomain, ClassType.REF);
				ReferenceDomain rleft = (ReferenceDomain) ConvertDomain.DomainSwitch(leftdomain, ClassType.REF);

				String operator = operators[0];
				ReferenceDomain may = null, must = null;
				if (operator.equals("==")) {
					if(rright.getUnknown()){
						may=ReferenceDomain.getUnknownDomain();
						condata.addMayDomain(v, may);
						must=ReferenceDomain.getUnknownDomain();
						condata.addMustDomain(v, must);
					}else{
						switch (rright.getValue()) {
						case NOTNULL:
							may = ReferenceDomain.intersect(rleft, new ReferenceDomain(ReferenceValue.NOTNULL));
							condata.addMayDomain(v, may);
							must=ReferenceDomain.getUnknownDomain();
							condata.addMustDomain(v, must);
							break;
						case NULL:
							may = ReferenceDomain.intersect(rleft, new ReferenceDomain(ReferenceValue.NULL));
							condata.addMayDomain(v, may);
							must = ReferenceDomain.intersect(rleft, new ReferenceDomain(ReferenceValue.NULL));
							condata.addMustDomain(v, must);
							break;
						case NULL_OR_NOTNULL:
							may=ReferenceDomain.getUnknownDomain();
							condata.addMayDomain(v, may);
							must=ReferenceDomain.getUnknownDomain();
							condata.addMustDomain(v, must);
							break;
						}
					}
				} else if (operator.equals("!=")) {
					if(rright.getUnknown()){
						may=ReferenceDomain.getUnknownDomain();
						condata.addMayDomain(v, may);
						must=ReferenceDomain.getUnknownDomain();
						condata.addMustDomain(v, must);
					}else{
						switch (rright.getValue()) {
						case NOTNULL:
							may=ReferenceDomain.getUnknownDomain();
							condata.addMayDomain(v, may);
							must = ReferenceDomain.intersect(rleft, new ReferenceDomain(ReferenceValue.NULL));
							condata.addMustDomain(v, must);
							break;
						case NULL:
							may = ReferenceDomain.intersect(rleft, new ReferenceDomain(ReferenceValue.NOTNULL));
							condata.addMayDomain(v, may);
							must = ReferenceDomain.intersect(rleft, new ReferenceDomain(ReferenceValue.NOTNULL));
							condata.addMustDomain(v, must);
							break;
						case NULL_OR_NOTNULL:
							//may = ReferenceDomain.intersect(rleft, new ReferenceDomain(ReferenceValue.NULL_OR_NOTNULL));
							may=ReferenceDomain.getUnknownDomain();
							condata.addMayDomain(v, may);
							//must = ReferenceDomain.intersect(rleft, new ReferenceDomain(ReferenceValue.EMPTY));
							must=ReferenceDomain.getUnknownDomain();
							condata.addMustDomain(v, must);
							break;
						}
					}
				} else {
					throw new RuntimeException("This is not a legal EqualityExpression");
				}
			}
			//end-yang
		}
		
		
		if(softtest.config.java.Config.LINEAR){
			ASTAdditiveExpression addexp=(ASTAdditiveExpression) left.getSingleChildofType(ASTAdditiveExpression.class);
			if(addexp!=null){
				String addimage = addexp.getImage();
				if (addimage == null) {
					throw new RuntimeException("This is not a legal AdditiveExpression");
				}
				String[] addoperators = addimage.split("#");
				if (addoperators.length != (addexp.jjtGetNumChildren() - 1)) {
					throw new RuntimeException("This is not a legal AdditiveExpression");
				}
				int opnum=addexp.jjtGetNumChildren();
				//存放每个操作数的区间值，最后一个存放不等式右边区间值
				DomainData expdata[] = new DomainData[opnum+1];
				SimpleJavaNode javanode = null;
				for(int i=0;i<opnum+1;i++){
					expdata[i]=new DomainData(condata.getCurrentVex());
					expdata[i].sideeffect=false;
				}
				//计算每个操作数的区间
				for (int i = 0; i < addexp.jjtGetNumChildren(); i++) {
					javanode = (SimpleJavaNode) addexp.jjtGetChild(i);
					javanode.jjtAccept(new ExpressionDomainVisitor(), expdata[i]);
				}
				right.jjtAccept(new ExpressionDomainVisitor(), expdata[opnum]);
				
				for(int i=0;i<addexp.jjtGetNumChildren();i++){
					javanode = (SimpleJavaNode) addexp.jjtGetChild(i);
					name = (ASTName) javanode.getSingleChildofType(ASTName.class);
					if (name != null) {
						nameimage = name.getImage();
					}	
					if (name != null && (name.getNameDeclaration() instanceof VariableNameDeclaration) && nameimage != null && !nameimage.contains(".")) {
						//当前操作数为变量，系数为-1或+1
						
						VariableNameDeclaration v = (VariableNameDeclaration) name.getNameDeclaration();
						if(!condata.isVariableContained(v)){
							//计算除去当前操作数之外的表达式区间值
							DomainData old=expdata[i];
							expdata[i]=new DomainData(condata.getCurrentVex());
							expdata[i].sideeffect=false;
							expdata[i].domain=new IntegerDomain(0,0);
							expdata[i].type=ClassType.INT;
							Object temp=calLinearWithout(i,expdata,addoperators,opnum);
							expdata[i]=old;
							
							boolean bunary=false;
							ASTUnaryExpression unary=(ASTUnaryExpression)name.getFirstParentOfType(ASTUnaryExpression.class, addexp);
							if(unary!=null&&unary.getImage().equals("-")){
								bunary=true;
							}
							
							if((i==0||addoperators[i-1].equals("+")&&!bunary)){//系数为+1
								Object leftdomain = node.findCurrentDomain(v, vex);
								ClassType lefttype = DomainSet.getDomainType(leftdomain);
								if (lefttype == ClassType.DOUBLE ) {
									DoubleDomain dright = (DoubleDomain) ConvertDomain.DomainSwitch(expdata[opnum].domain, ClassType.DOUBLE);
									DoubleDomain dleft = (DoubleDomain) ConvertDomain.DomainSwitch(leftdomain, ClassType.DOUBLE);
									dright=DoubleDomain.sub(dright, (DoubleDomain) ConvertDomain.DomainSwitch(temp, ClassType.DOUBLE));

									String operator = operators[0];
									DoubleDomain may = null, must = null;
									if (operator.equals("==")) {
										may = DoubleDomain.intersect(dleft, dright);
										condata.addMayDomain(v, may);
										if (dright.isCanonical()) {
											must = DoubleDomain.intersect(dleft, dright);
										} else {
											must = new DoubleDomain();
										}
										condata.addMustDomain(v, must);
									} else if (operator.equals("!=")) {
										may = DoubleDomain.intersect(dleft, dright);
										if (dright.isCanonical()) {
											must = DoubleDomain.intersect(dleft, dright);
										} else {
											must = new DoubleDomain();
										}
										condata.addMayDomain(v, DoubleDomain.subtract(dleft, must));
										condata.addMustDomain(v, DoubleDomain.subtract(dleft, may));
									} else {
										throw new RuntimeException("This is not a legal EqualityExpression");
									}
								} else if (lefttype == ClassType.INT ) {
									IntegerDomain iright = (IntegerDomain) ConvertDomain.DomainSwitch(expdata[opnum].domain, ClassType.INT);
									IntegerDomain ileft = (IntegerDomain) ConvertDomain.DomainSwitch(leftdomain, ClassType.INT);
									iright=IntegerDomain.sub(iright, (IntegerDomain) ConvertDomain.DomainSwitch(temp, ClassType.INT));		
	
									String operator = operators[0];
									IntegerDomain may = null, must = null;

									if (operator.equals("==")) {
										may = IntegerDomain.intersect(ileft, iright);
										condata.addMayDomain(v, may);
										if (iright.isCanonical()) {
											must = IntegerDomain.intersect(ileft, iright);
										} else {
											must = new IntegerDomain();
										}
										condata.addMustDomain(v, must);
									} else if (operator.equals("!=")) {
										may = IntegerDomain.intersect(ileft, iright);
										if (iright.isCanonical()) {
											must = IntegerDomain.intersect(ileft, iright);
										} else {
											must = new IntegerDomain();
										}
										condata.addMayDomain(v, IntegerDomain.subtract(ileft, must));
										condata.addMustDomain(v, IntegerDomain.subtract(ileft, may));
									} else {
										throw new RuntimeException("This is not a legal EqualityExpression");
									}
								}
							}else{//系数为-1
								Object leftdomain = node.findCurrentDomain(v, vex);
								ClassType lefttype = DomainSet.getDomainType(leftdomain);
								if (lefttype == ClassType.DOUBLE ) {
									DoubleDomain dright = (DoubleDomain) ConvertDomain.DomainSwitch(expdata[opnum].domain, ClassType.DOUBLE);
									DoubleDomain dleft = (DoubleDomain) ConvertDomain.DomainSwitch(leftdomain, ClassType.DOUBLE);
									dright=DoubleDomain.sub((DoubleDomain) ConvertDomain.DomainSwitch(temp, ClassType.DOUBLE),dright);					
	
									String operator = operators[0];
									DoubleDomain may = null, must = null;
									if (operator.equals("==")) {
										may = DoubleDomain.intersect(dleft, dright);
										condata.addMayDomain(v, may);
										if (dright.isCanonical()) {
											must = DoubleDomain.intersect(dleft, dright);
										} else {
											must = new DoubleDomain();
										}
										condata.addMustDomain(v, must);
									} else if (operator.equals("!=")) {
										may = DoubleDomain.intersect(dleft, dright);
										if (dright.isCanonical()) {
											must = DoubleDomain.intersect(dleft, dright);
										} else {
											must = new DoubleDomain();
										}
										condata.addMayDomain(v, DoubleDomain.subtract(dleft, must));
										condata.addMustDomain(v, DoubleDomain.subtract(dleft, may));
									} else {
										throw new RuntimeException("This is not a legal EqualityExpression");
									}
								} else if (lefttype == ClassType.INT ) {
									IntegerDomain iright = (IntegerDomain) ConvertDomain.DomainSwitch(expdata[opnum].domain, ClassType.INT);
									IntegerDomain ileft = (IntegerDomain) ConvertDomain.DomainSwitch(leftdomain, ClassType.INT);
									iright=IntegerDomain.sub((IntegerDomain) ConvertDomain.DomainSwitch(temp, ClassType.INT),iright);		
	
									String operator = operators[0];
									IntegerDomain may = null, must = null;

									if (operator.equals("==")) {
										may = IntegerDomain.intersect(ileft, iright);
										condata.addMayDomain(v, may);
										if (iright.isCanonical()) {
											must = IntegerDomain.intersect(ileft, iright);
										} else {
											must = new IntegerDomain();
										}
										condata.addMustDomain(v, must);
									} else if (operator.equals("!=")) {
										may = IntegerDomain.intersect(ileft, iright);
										if (iright.isCanonical()) {
											must = IntegerDomain.intersect(ileft, iright);
										} else {
											must = new IntegerDomain();
										}
										condata.addMayDomain(v, IntegerDomain.subtract(ileft, must));
										condata.addMustDomain(v, IntegerDomain.subtract(ileft, may));
									} else {
										throw new RuntimeException("This is not a legal EqualityExpression");
									}
								}
							}
						}
					}
				}
			}
			
			addexp=(ASTAdditiveExpression) right.getSingleChildofType(ASTAdditiveExpression.class);
			if(addexp!=null){
				String addimage = addexp.getImage();
				if (addimage == null) {
					throw new RuntimeException("This is not a legal AdditiveExpression");
				}
				String[] addoperators = addimage.split("#");
				if (addoperators.length != (addexp.jjtGetNumChildren() - 1)) {
					throw new RuntimeException("This is not a legal AdditiveExpression");
				}
				int opnum=addexp.jjtGetNumChildren();
				//存放每个操作数的区间值，最后一个存放不等式左边区间值
				DomainData expdata[] = new DomainData[opnum+1];
				SimpleJavaNode javanode = null;
				for(int i=0;i<opnum+1;i++){
					expdata[i]=new DomainData(condata.getCurrentVex());
					expdata[i].sideeffect=false;
				}
				//计算每个操作数的区间
				for (int i = 0; i < addexp.jjtGetNumChildren(); i++) {
					javanode = (SimpleJavaNode) addexp.jjtGetChild(i);
					javanode.jjtAccept(new ExpressionDomainVisitor(), expdata[i]);
				}
				left.jjtAccept(new ExpressionDomainVisitor(), expdata[opnum]);
				
				for(int i=0;i<addexp.jjtGetNumChildren();i++){
					javanode = (SimpleJavaNode) addexp.jjtGetChild(i);
					name = (ASTName) javanode.getSingleChildofType(ASTName.class);
					if (name != null) {
						nameimage = name.getImage();
					}	
					if (name != null && (name.getNameDeclaration() instanceof VariableNameDeclaration) && nameimage != null && !nameimage.contains(".")) {
						//当前操作数为变量，系数为-1或+1
						
						VariableNameDeclaration v = (VariableNameDeclaration) name.getNameDeclaration();
						if(!condata.isVariableContained(v)){
							//计算除去当前操作数之外的表达式区间值
							DomainData old=expdata[i];
							expdata[i]=new DomainData(condata.getCurrentVex());
							expdata[i].sideeffect=false;
							expdata[i].domain=new IntegerDomain(0,0);
							expdata[i].type=ClassType.INT;
							Object temp=calLinearWithout(i,expdata,addoperators,opnum);
							expdata[i]=old;
							
							boolean bunary=false;
							ASTUnaryExpression unary=(ASTUnaryExpression)name.getFirstParentOfType(ASTUnaryExpression.class, addexp);
							if(unary!=null&&unary.getImage().equals("-")){
								bunary=true;
							}
							
							if((i==0||addoperators[i-1].equals("+"))&&!bunary){//系数为+1
								Object rightdomain = node.findCurrentDomain(v, vex);
								ClassType righttype = DomainSet.getDomainType(rightdomain);

								if (righttype == ClassType.DOUBLE ) {
									DoubleDomain dright = (DoubleDomain) ConvertDomain.DomainSwitch(rightdomain, ClassType.DOUBLE);
									DoubleDomain dleft = (DoubleDomain) ConvertDomain.DomainSwitch(expdata[opnum].domain, ClassType.DOUBLE);
									dleft=DoubleDomain.sub(dleft, (DoubleDomain) ConvertDomain.DomainSwitch(temp, ClassType.DOUBLE));	

									String operator = operators[0];
									DoubleDomain may = null, must = null;
									if (operator.equals("==")) {
										may = DoubleDomain.intersect(dleft, dright);
										condata.addMayDomain(v, may);
										if (dright.isCanonical()) {
											must = DoubleDomain.intersect(dleft, dright);
										} else {
											must = new DoubleDomain();
										}
										condata.addMustDomain(v, must);
									} else if (operator.equals("!=")) {
										may = DoubleDomain.intersect(dleft, dright);
										if (dright.isCanonical()) {
											must = DoubleDomain.intersect(dleft, dright);
										} else {
											must = new DoubleDomain();
										}
										condata.addMayDomain(v, DoubleDomain.subtract(dleft, must));
										condata.addMustDomain(v, DoubleDomain.subtract(dleft, may));
									} else {
										throw new RuntimeException("This is not a legal EqualityExpression");
									}
								} else if (righttype == ClassType.INT ) {
									IntegerDomain iright = (IntegerDomain) ConvertDomain.DomainSwitch(rightdomain, ClassType.INT);
									IntegerDomain ileft = (IntegerDomain) ConvertDomain.DomainSwitch(expdata[opnum].domain, ClassType.INT);
									ileft=IntegerDomain.sub(ileft, (IntegerDomain) ConvertDomain.DomainSwitch(temp, ClassType.INT));	
									
									String operator = operators[0];
									IntegerDomain may = null, must = null;

									if (operator.equals("==")) {
										may = IntegerDomain.intersect(ileft, iright);
										condata.addMayDomain(v, may);
										if (iright.isCanonical()) {
											must = IntegerDomain.intersect(ileft, iright);
										} else {
											must = new IntegerDomain();
										}
										condata.addMustDomain(v, must);
									} else if (operator.equals("!=")) {
										may = IntegerDomain.intersect(ileft, iright);
										if (iright.isCanonical()) {
											must = IntegerDomain.intersect(ileft, iright);
										} else {
											must = new IntegerDomain();
										}
										condata.addMayDomain(v, IntegerDomain.subtract(ileft, must));
										condata.addMustDomain(v, IntegerDomain.subtract(ileft, may));
									} else {
										throw new RuntimeException("This is not a legal EqualityExpression");
									}
								}
							}else{//系数为-1
								Object rightdomain = node.findCurrentDomain(v, vex);
								ClassType righttype = DomainSet.getDomainType(rightdomain);

								if (righttype == ClassType.DOUBLE ) {
									DoubleDomain dright = (DoubleDomain) ConvertDomain.DomainSwitch(rightdomain, ClassType.DOUBLE);
									DoubleDomain dleft = (DoubleDomain) ConvertDomain.DomainSwitch(expdata[opnum].domain, ClassType.DOUBLE);
									dleft=DoubleDomain.sub((DoubleDomain) ConvertDomain.DomainSwitch(temp, ClassType.DOUBLE),dleft);	

									String operator = operators[0];
									DoubleDomain may = null, must = null;
									if (operator.equals("==")) {
										may = DoubleDomain.intersect(dleft, dright);
										condata.addMayDomain(v, may);
										if (dright.isCanonical()) {
											must = DoubleDomain.intersect(dleft, dright);
										} else {
											must = new DoubleDomain();
										}
										condata.addMustDomain(v, must);
									} else if (operator.equals("!=")) {
										may = DoubleDomain.intersect(dleft, dright);
										if (dright.isCanonical()) {
											must = DoubleDomain.intersect(dleft, dright);
										} else {
											must = new DoubleDomain();
										}
										condata.addMayDomain(v, DoubleDomain.subtract(dleft, must));
										condata.addMustDomain(v, DoubleDomain.subtract(dleft, may));
									} else {
										throw new RuntimeException("This is not a legal EqualityExpression");
									}
								} else if (righttype == ClassType.INT ) {
									IntegerDomain iright = (IntegerDomain) ConvertDomain.DomainSwitch(rightdomain, ClassType.INT);
									IntegerDomain ileft = (IntegerDomain) ConvertDomain.DomainSwitch(expdata[opnum].domain, ClassType.INT);
									ileft=IntegerDomain.sub((IntegerDomain) ConvertDomain.DomainSwitch(temp, ClassType.INT),ileft);	
									
									String operator = operators[0];
									IntegerDomain may = null, must = null;

									if (operator.equals("==")) {
										may = IntegerDomain.intersect(ileft, iright);
										condata.addMayDomain(v, may);
										if (iright.isCanonical()) {
											must = IntegerDomain.intersect(ileft, iright);
										} else {
											must = new IntegerDomain();
										}
										condata.addMustDomain(v, must);
									} else if (operator.equals("!=")) {
										may = IntegerDomain.intersect(ileft, iright);
										if (iright.isCanonical()) {
											must = IntegerDomain.intersect(ileft, iright);
										} else {
											must = new IntegerDomain();
										}
										condata.addMayDomain(v, IntegerDomain.subtract(ileft, must));
										condata.addMustDomain(v, IntegerDomain.subtract(ileft, may));
									} else {
										throw new RuntimeException("This is not a legal EqualityExpression");
									}
								}
							}
						}
					}
				}
			}
		}
		
		return null;
	}

	/** 处理逻辑或 */
	public Object visit(ASTConditionalOrExpression node, Object data) {
		// || 短路
		ConditionData condata = (ConditionData) data;
		ConditionData leftdata = new ConditionData(condata.getCurrentVex());
		JavaNode javanode = (JavaNode) node.jjtGetChild(0);
		javanode.jjtAccept(this, leftdata);
		VexNode vex = condata.getCurrentVex();
		if (vex == null) {
			vex = node.getCurrentVexNode();
		}

		for (int i = 1; i < node.jjtGetNumChildren(); i++) {
			ConditionData rightdata = new ConditionData();
			javanode = (JavaNode) node.jjtGetChild(i);
			javanode.jjtAccept(this, rightdata);
			
			leftdata=ConditionData.calLogicConditionalOrExpression(leftdata, rightdata, vex);

		}
		condata.setDomainsTable(leftdata.getDomainsTable());
		return null;
	}

	/** 处理逻辑与 */
	public Object visit(ASTConditionalAndExpression node, Object data) {
		// && 短路
		ConditionData condata = (ConditionData) data;
		ConditionData leftdata = new ConditionData(condata.getCurrentVex());
		JavaNode javanode = (JavaNode) node.jjtGetChild(0);
		javanode.jjtAccept(this, leftdata);
		VexNode vex = condata.getCurrentVex();
		if (vex == null) {
			vex = node.getCurrentVexNode();
		}

		for (int i = 1; i < node.jjtGetNumChildren(); i++) {
			ConditionData rightdata = new ConditionData();
			javanode = (JavaNode) node.jjtGetChild(i);
			javanode.jjtAccept(this, rightdata);

			leftdata=ConditionData.calConditionalAndExpression(leftdata, rightdata, vex);
		}
		condata.setDomainsTable(leftdata.getDomainsTable());
		return null;
	}

	/** 处理逻辑或 */
	public Object visit(ASTInclusiveOrExpression node, Object data) {
		// |
		ConditionData condata = (ConditionData) data;
		ConditionData leftdata = new ConditionData(condata.getCurrentVex());
		JavaNode javanode = (JavaNode) node.jjtGetChild(0);
		javanode.jjtAccept(this, leftdata);
		VexNode vex = condata.getCurrentVex();
		if (vex == null) {
			vex = node.getCurrentVexNode();
		}

		for (int i = 1; i < node.jjtGetNumChildren(); i++) {
			ConditionData rightdata = new ConditionData();
			javanode = (JavaNode) node.jjtGetChild(i);
			javanode.jjtAccept(this, rightdata);
			
			leftdata=ConditionData.calLogicConditionalOrExpression(leftdata, rightdata, vex);

		}
		condata.setDomainsTable(leftdata.getDomainsTable());
		return null;
	}

	/** 处理逻辑与 */
	public Object visit(ASTAndExpression node, Object data) {
		// &
		ConditionData condata = (ConditionData) data;
		ConditionData leftdata = new ConditionData(condata.getCurrentVex());
		JavaNode javanode = (JavaNode) node.jjtGetChild(0);
		javanode.jjtAccept(this, leftdata);
		VexNode vex = condata.getCurrentVex();
		if (vex == null) {
			vex = node.getCurrentVexNode();
		}

		for (int i = 1; i < node.jjtGetNumChildren(); i++) {
			ConditionData rightdata = new ConditionData();
			javanode = (JavaNode) node.jjtGetChild(i);
			javanode.jjtAccept(this, rightdata);

			leftdata=ConditionData.calConditionalAndExpression(leftdata, rightdata, vex);
		}
		condata.setDomainsTable(leftdata.getDomainsTable());
		return null;
	}

	/** 处理异或 */
	public Object visit(ASTExclusiveOrExpression node, Object data) {
		// ^
		ConditionData condata = (ConditionData) data;
		ConditionData leftdata = new ConditionData(condata.getCurrentVex());
		JavaNode javanode = (JavaNode) node.jjtGetChild(0);
		javanode.jjtAccept(this, leftdata);
		VexNode vex = condata.getCurrentVex();
		if (vex == null) {
			vex = node.getCurrentVexNode();
		}

		for (int i = 1; i < node.jjtGetNumChildren(); i++) {
			ConditionData rightdata = new ConditionData();
			javanode = (JavaNode) node.jjtGetChild(i);
			javanode.jjtAccept(this, rightdata);

			DomainSet may1 = leftdata.getTrueMayDomainSet();
			DomainSet may2 = rightdata.getTrueMayDomainSet();
			DomainSet may3 = leftdata.getFalseMayDomainSet(vex);
			DomainSet may4 = rightdata.getFalseMayDomainSet(vex);

			DomainSet must1 = leftdata.getTrueMustDomainSet();
			DomainSet must2 = rightdata.getTrueMustDomainSet();
			DomainSet must3 = leftdata.getFalseMustDomainSet(vex);
			DomainSet must4 = rightdata.getFalseMustDomainSet(vex);
			
			ConditionData temp1=null,temp2=null;
			
			leftdata.clearDomains();
			rightdata.clearDomains();
			
			leftdata.addMayDomain(may1);
			leftdata.addMustDomain(must1);
			
			rightdata.addMayDomain(may4);
			rightdata.addMustDomain(must4);
			
			temp1=ConditionData.calConditionalAndExpression(leftdata, rightdata, vex);
			
			
			leftdata.clearDomains();
			rightdata.clearDomains();
			
			leftdata.addMayDomain(may3);
			leftdata.addMustDomain(must3);
			
			rightdata.addMayDomain(may2);
			rightdata.addMustDomain(must2);
			
			temp2=ConditionData.calConditionalAndExpression(leftdata, rightdata, vex);			

			leftdata=ConditionData.calLogicConditionalOrExpression(temp1, temp2, vex);

		}
		condata.setDomainsTable(leftdata.getDomainsTable());
		return null;
	}

	/** 处理instanceof判断 */
	public Object visit(ASTInstanceOfExpression node, Object data) {
		// instanceof
		ConditionData condata = (ConditionData) data;
		condata.clearDomains();
		SimpleJavaNode left = (SimpleJavaNode) node.jjtGetChild(0);
		ASTName name = (ASTName) (left.getSingleChildofType(ASTName.class));
		String nameimage = null;
		if (name != null) {
			nameimage = name.getImage();
		}
		if (name != null && (name.getNameDeclaration() instanceof VariableNameDeclaration) && nameimage != null && !nameimage.contains(".")) {
			VariableNameDeclaration v = (VariableNameDeclaration) name.getNameDeclaration();

			/*ReferenceDomain leftdomain = (ReferenceDomain) ConvertDomain.DomainSwitch(node.findCurrentDomain(v), ClassType.REF);
			DomainSet ds = new DomainSet();
			Object domain = ReferenceDomain.intersect(leftdomain, new ReferenceDomain(ReferenceValue.NOTNULL));
			ds.addDomain(v, ConvertDomain.DomainSwitch(domain, DomainSet.getDomainType(v.getDomain())));
			condata.addMayDomain(ds);
			condata.addMustDomain(new DomainSet());*/
			ReferenceDomain may = null, must = null;
			may= new ReferenceDomain(ReferenceValue.NOTNULL);
			must = ReferenceDomain.getUnknownDomain();
			condata.addMayDomain(v, may);
			condata.addMustDomain(v,must);
		}
		return null;
	}

	/** 处理基本表达式，将访问向孩子传递 */
	public Object visit(ASTPrimaryExpression node, Object data) {
		ASTName name = (ASTName) (node.getSingleChildofType(ASTName.class));
		String nameimage = null;
		if (name != null) {
			nameimage = name.getImage();
		}
		if (name != null && (name.getNameDeclaration() instanceof VariableNameDeclaration) && nameimage != null && !nameimage.contains(".")) {
			node.childrenAccept(this, data);
		}
		ASTExpression expression = (ASTExpression) (node.getSingleChildofType(ASTExpression.class));
		if (expression != null) {
			node.childrenAccept(this, data);
		}
		return null;
	}

	/** 处理基本表达式前缀，将访问向孩子传递 */
	public Object visit(ASTPrimaryPrefix node, Object data) {
		node.childrenAccept(this, data);
		return null;
	}

	/** 处理基本表达式后缀，目前直接忽略 */
	public Object visit(ASTPrimarySuffix node, Object data) {
		return null;
	}

	/** 处理变量 */
	public Object visit(ASTName node, Object data) {
		ConditionData condata = (ConditionData) data;
		condata.clearDomains();
		VexNode vex = condata.getCurrentVex();
		if (vex == null) {
			vex = node.getCurrentVexNode();
		}
		
		if (!(node.getNameDeclaration() instanceof VariableNameDeclaration)) {
			return null;
		}
		VariableNameDeclaration v = (VariableNameDeclaration) node.getNameDeclaration();
		BooleanDomain domain = (BooleanDomain) ConvertDomain.DomainSwitch(node.findCurrentDomain(v, vex), ClassType.BOOLEAN);
		DomainSet ds = new DomainSet();
		Object temp = BooleanDomain.intersect(domain, new BooleanDomain(BooleanValue.TRUE));
		ds.addDomain(v, ConvertDomain.DomainSwitch(temp, DomainSet.getDomainType(v.getDomain())));
		condata.addMayDomain(ds);
		condata.addMustDomain(ds);
		return null;
	}

	/** 处理逻辑非 */
	public Object visit(ASTUnaryExpressionNotPlusMinus node, Object data) {
		// ~ !
		ConditionData condata = (ConditionData) data;
		condata.clearDomains();
		String image = node.getImage();
		JavaNode javanode = (JavaNode) node.jjtGetChild(0);

		if (image.equals("!")) {
			ConditionData tempdata = new ConditionData(condata.getCurrentVex());
			javanode.jjtAccept(this, tempdata);
			VexNode vex = condata.getCurrentVex();
			if (vex == null) {
				vex = node.getCurrentVexNode();
			}
			DomainSet may = tempdata.getFalseMayDomainSet(vex);
			DomainSet must = tempdata.getFalseMustDomainSet(vex);

			tempdata.clearDomains();
			tempdata.addMayDomain(may);
			tempdata.addMustDomain(must);
			condata.setDomainsTable(tempdata.getDomainsTable());
		}
		return null;
	}
	
	private Object calLinearWithout(int index,DomainData expdata[],String operators[],int num){
		ClassType lefttype = null, righttype = null;
		Object leftdomain = null, rightdomain = null;
		lefttype = expdata[0].type;
		leftdomain = expdata[0].domain;

		for (int i = 1; i < num; i++) {
			righttype = expdata[i].type;
			rightdomain = expdata[i].domain;
			String operator = operators[i - 1];

			if (lefttype == ClassType.REF && operator.equals("+")) {
				// 引用类型，什么都不坐，处理子符串+
			} else if (lefttype == ClassType.DOUBLE || righttype == ClassType.DOUBLE) {
				leftdomain = ConvertDomain.DomainSwitch(leftdomain, ClassType.DOUBLE);
				lefttype = ClassType.DOUBLE;
				rightdomain = ConvertDomain.DomainSwitch(rightdomain, ClassType.DOUBLE);
				righttype = ClassType.DOUBLE;
			} else {
				leftdomain = ConvertDomain.DomainSwitch(leftdomain, ClassType.INT);
				lefttype = ClassType.INT;
				rightdomain = ConvertDomain.DomainSwitch(rightdomain, ClassType.INT);
				righttype = ClassType.INT;
			}

			if (lefttype == ClassType.DOUBLE) {
				DoubleDomain a = (DoubleDomain) leftdomain;
				DoubleDomain b = (DoubleDomain) rightdomain;
				if (operator.equals("+")) {
					leftdomain = DoubleDomain.add(a, b);
				} else if (operator.equals("-")) {
					leftdomain = DoubleDomain.sub(a, b);
				} else {
					throw new RuntimeException("This is not a legal AdditiveExpression");
				}
			} else if (lefttype == ClassType.INT) {
				IntegerDomain a = (IntegerDomain) leftdomain;
				IntegerDomain b = (IntegerDomain) rightdomain;
				if (operator.equals("+")) {
					leftdomain = IntegerDomain.add(a, b);
				} else if (operator.equals("-")) {
					leftdomain = IntegerDomain.sub(a, b);
				} else {
					throw new RuntimeException("This is not a legal AdditiveExpression");
				}
			} else {
				lefttype = ClassType.REF;
				leftdomain = new ReferenceDomain(ReferenceValue.NOTNULL);
			}
		}
		return leftdomain;
	}
}
