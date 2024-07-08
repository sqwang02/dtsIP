package softtest.IntervalAnalysis.java;

import softtest.domain.java.*;
import softtest.jaxen.java.DocumentNavigator;
import softtest.ast.java.*;
import softtest.jaxen.java.*;
import softtest.symboltable.java.*;

import java.lang.reflect.Method;
import java.util.*;

import org.jaxen.*;

import softtest.ast.java.ASTAdditiveExpression;
import softtest.ast.java.ASTAllocationExpression;
import softtest.ast.java.ASTAndExpression;
import softtest.ast.java.ASTArgumentList;
import softtest.ast.java.ASTArguments;
import softtest.ast.java.ASTArrayDimsAndInits;
import softtest.ast.java.ASTArrayInitializer;
import softtest.ast.java.ASTAssignmentOperator;
import softtest.ast.java.ASTBooleanLiteral;
import softtest.ast.java.ASTCastExpression;
import softtest.ast.java.ASTConditionalAndExpression;
import softtest.ast.java.ASTConditionalExpression;
import softtest.ast.java.ASTConditionalOrExpression;
import softtest.ast.java.ASTEqualityExpression;
import softtest.ast.java.ASTExclusiveOrExpression;
import softtest.ast.java.ASTExpression;
import softtest.ast.java.ASTInclusiveOrExpression;
import softtest.ast.java.ASTInstanceOfExpression;
import softtest.ast.java.ASTLiteral;
import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.ASTMultiplicativeExpression;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTNullLiteral;
import softtest.ast.java.ASTPostfixExpression;
import softtest.ast.java.ASTPreDecrementExpression;
import softtest.ast.java.ASTPreIncrementExpression;
import softtest.ast.java.ASTPrimaryExpression;
import softtest.ast.java.ASTPrimaryPrefix;
import softtest.ast.java.ASTPrimarySuffix;
import softtest.ast.java.ASTPrimitiveType;
import softtest.ast.java.ASTRSIGNEDSHIFT;
import softtest.ast.java.ASTRUNSIGNEDSHIFT;
import softtest.ast.java.ASTReferenceType;
import softtest.ast.java.ASTRelationalExpression;
import softtest.ast.java.ASTShiftExpression;
import softtest.ast.java.ASTStatementExpression;
import softtest.ast.java.ASTType;
import softtest.ast.java.ASTUnaryExpression;
import softtest.ast.java.ASTUnaryExpressionNotPlusMinus;
import softtest.ast.java.ASTVariableDeclarator;
import softtest.ast.java.ASTVariableDeclaratorId;
import softtest.ast.java.ASTVariableInitializer;
import softtest.ast.java.ExpressionBase;
import softtest.ast.java.JavaNode;
import softtest.ast.java.JavaParserVisitorAdapter;
import softtest.ast.java.Node;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.*;
import softtest.cfg.java.*;

/** ���ڴ��������ʽ����ĳ����﷨�������� */
public class ExpressionDomainVisitor extends JavaParserVisitorAdapter {
	private static List getEvaluationResults(SimpleJavaNode node, String xPath) {
		List evaluationResults = null;
		try {
			XPath xpath = new BaseXPath(xPath, new DocumentNavigator());
			evaluationResults = xpath.selectNodes(node);
		} catch (JaxenException e) {
			if(softtest.config.java.Config.DEBUG){
				e.printStackTrace();
			}
			throw new RuntimeException("xpath error");
		}
		return evaluationResults;
	}

	/** Ĭ�ϵĴ������������ӽڵ㴫�� */
	public Object visit(SimpleJavaNode node, Object data) {
		node.childrenAccept(this, data);
		return null;
	}

	/** ����ֵ���ʽ��������������õĺ��� */
	private void dealExprOrSExpr(SimpleJavaNode node, DomainData expdata) {
		ASTName name = (ASTName) ((SimpleJavaNode) node.jjtGetChild(0)).getSingleChildofType(ASTName.class);
		VariableNameDeclaration v = null;
		VexNode vex = expdata.getCurrentVex();
		if (vex == null) {
			vex = node.getCurrentVexNode();
		}
		expdata.domain = new ArbitraryDomain();
		expdata.type = ClassType.ARBITRARY;
		if (name == null || !(name.getNameDeclaration() instanceof VariableNameDeclaration)) {
			if (name == null) {
				// ��������ռ����
				String xPath = "./PrimaryExpression[./PrimaryPrefix/Name and ./PrimarySuffix[@ArrayDereference=\'true\'] and ../AssignmentOperator[@Image='='] and ../Expression/PrimaryExpression/PrimaryPrefix/AllocationExpression/ArrayDimsAndInits]";
				List list = getEvaluationResults(node, xPath);
				if (!list.isEmpty()) {
					JavaNode javanode = (JavaNode) node.jjtGetChild(2);
					javanode.jjtAccept(this, expdata);
					if (expdata.type != ClassType.ARRAY) {
						return;
					}
					ArrayDomain rightdomain = (ArrayDomain) expdata.domain;
					ASTPrimaryExpression pe = (ASTPrimaryExpression) list.get(0);
					name = (ASTName) ((SimpleJavaNode) pe.jjtGetChild(0)).getSingleChildofType(ASTName.class);
					if (name == null || !(name.getNameDeclaration() instanceof VariableNameDeclaration)) {
						return;
					}
					String nameimage = null;
					if (name != null) {
						nameimage = name.getImage();
					}
					if (nameimage == null || nameimage.contains(".")) {
						return;
					}
					v = (VariableNameDeclaration) name.getNameDeclaration();
					Object domain = node.findCurrentDomain(v, vex);
					ClassType lefttype = DomainSet.getDomainType(domain);
					if (lefttype != ClassType.ARRAY) {
						return;
					}
					ArrayDomain leftdomain = new ArrayDomain((ArrayDomain) domain);
					xPath = "./PrimarySuffix[@ArrayDereference=\'true\']";
					list = getEvaluationResults(pe, xPath);
					if (list.size() != getEvaluationResults(pe, "./PrimarySuffix").size()) {
						// ����a[].x=new int [];���
						return;
					}
		
					for (int i = 0; i < rightdomain.getAllDimensions().size(); i++) {
						leftdomain.setDimension(i + list.size(), rightdomain.getAllDimensions().get(i));
					}
					
					if (expdata.sideeffect) {
						vex.addDomain(v, leftdomain);
					}
					expdata.domain = rightdomain;
					expdata.type = ClassType.ARRAY;
					return;
				} else {
					// ����this.i
					xPath = "./PrimaryExpression[./PrimaryPrefix[@ThisModifier=\'true\']]";
					list = getEvaluationResults(node, xPath);
					if (!list.isEmpty()) {
						ASTPrimaryExpression pe = (ASTPrimaryExpression) list.get(0);
						if (pe.jjtGetNumChildren() == 2 && pe.jjtGetChild(1) instanceof ASTPrimarySuffix) {
							ASTPrimarySuffix ps = (ASTPrimarySuffix) pe.jjtGetChild(1);
							if (ps.getNameDeclaration() instanceof VariableNameDeclaration) {
								v = (VariableNameDeclaration) ps.getNameDeclaration();
							} else {
								node.childrenAccept(this, expdata);
								return;
							}
						} else {
							node.childrenAccept(this, expdata);
							return;
						}
					} else {
						node.childrenAccept(this, expdata);
						return;
					}
				}
			} else {
				node.childrenAccept(this, expdata);
				return;
			}
		} else {
			String nameimage = name.getImage();
			if (nameimage == null ) {
				return;
			}
			v = (VariableNameDeclaration) name.getNameDeclaration();
			if(nameimage.contains(".")){
				//ֻ���� ����.���� ����� Test.a
				if(name.getNameDeclarationList().get(0) instanceof ClassNameDeclaration){
					ClassNameDeclaration c=(ClassNameDeclaration)name.getNameDeclarationList().get(0);
					if(!nameimage.equals(c.getImage()+"."+v.getImage())){
						node.childrenAccept(this, expdata);
						return;
					}
				}else{
					node.childrenAccept(this, expdata);
					return;
				}
			}
		}

		// ����Ⱥ��ұ߱��ʽ
		JavaNode javanode = (JavaNode) node.jjtGetChild(2);
		javanode.jjtAccept(this, expdata);
		// ����������������
		Object leftdomain = node.findCurrentDomain(v, vex);
		ClassType lefttype = DomainSet.getDomainType(leftdomain);
		Object rightdomain = expdata.domain;

		// ��ֵ����
		ASTAssignmentOperator operator = (ASTAssignmentOperator) node.jjtGetChild(1);
		String image = operator.getImage();
		if (image.equals("=")) {
			// ����Ҫ�޸�exprdata��ֱ�ӷ��صȺ��ұߵ�data������
			if (expdata.sideeffect) {
				vex.addDomain(v, ConvertDomain.DomainSwitch(rightdomain, lefttype));
			}
		} else if (image.equals("+=")) {
			// �Ϸ���ֻ���������
			if (lefttype == ClassType.DOUBLE) {
				expdata.domain = DoubleDomain.add((DoubleDomain) leftdomain, (DoubleDomain) ConvertDomain.DomainSwitch(rightdomain, lefttype));
				expdata.type = ClassType.DOUBLE;
				if (expdata.sideeffect) {
					vex.addDomain(v, expdata.domain);
				}
			} else if (lefttype == ClassType.INT) {
				expdata.domain = IntegerDomain.add((IntegerDomain) leftdomain, (IntegerDomain) ConvertDomain.DomainSwitch(rightdomain, lefttype));
				expdata.type = ClassType.INT;
				if (expdata.sideeffect) {
					vex.addDomain(v, expdata.domain);
				}
			} else if (lefttype == ClassType.REF) {
				// String s="ab";s+=5;
				expdata.domain = new ReferenceDomain(ReferenceValue.NOTNULL);
				expdata.type = ClassType.REF;
				if (expdata.sideeffect) {
					vex.addDomain(v, expdata.domain);
				}
			} else {// BUGFOUND 20090204 yangxiu,  a.x is int
				return;
				//throw new RuntimeException("this is an illegal assignment");
			}
		} else if (image.equals("-=")) {
			// �Ϸ���ֻ���������
			if (lefttype == ClassType.DOUBLE) {
				expdata.domain = DoubleDomain.sub((DoubleDomain) leftdomain, (DoubleDomain) ConvertDomain.DomainSwitch(rightdomain, lefttype));
				expdata.type = ClassType.DOUBLE;
				if (expdata.sideeffect) {
					vex.addDomain(v, expdata.domain);
				}
			} else if (lefttype == ClassType.INT) {
				expdata.domain = IntegerDomain.sub((IntegerDomain) leftdomain, (IntegerDomain) ConvertDomain.DomainSwitch(rightdomain, lefttype));
				expdata.type = ClassType.INT;
				if (expdata.sideeffect) {
					vex.addDomain(v, expdata.domain);
				}
			} else {// BUGFOUND 20090204 yangxiu,  a.x is int
				return;
				//throw new RuntimeException("this is an illegal assignment");
			}
		} else if (image.equals("*=")) {
			// �Ϸ���ֻ���������
			if (lefttype == ClassType.DOUBLE) {
				expdata.domain = DoubleDomain.mul((DoubleDomain) leftdomain, (DoubleDomain) ConvertDomain.DomainSwitch(rightdomain, lefttype));
				expdata.type = ClassType.DOUBLE;
				if (expdata.sideeffect) {
					vex.addDomain(v, expdata.domain);
				}
			} else if (lefttype == ClassType.INT) {
				expdata.domain = IntegerDomain.mul((IntegerDomain) leftdomain, (IntegerDomain) ConvertDomain.DomainSwitch(rightdomain, lefttype));
				expdata.type = ClassType.INT;
				if (expdata.sideeffect) {
					vex.addDomain(v, expdata.domain);
				}
			} else {// BUGFOUND 20090204 yangxiu,  a.x is int
				return;
				//throw new RuntimeException("this is an illegal assignment");
			}
		} else if (image.equals("/=")) {
			// �Ϸ���ֻ���������
			if (lefttype == ClassType.DOUBLE) {
				expdata.domain = DoubleDomain.div((DoubleDomain) leftdomain, (DoubleDomain) ConvertDomain.DomainSwitch(rightdomain, lefttype));
				expdata.type = ClassType.DOUBLE;
				if (expdata.sideeffect) {
					vex.addDomain(v, expdata.domain);
				}
			} else if (lefttype == ClassType.INT) {
				expdata.domain = IntegerDomain.div((IntegerDomain) leftdomain, (IntegerDomain) ConvertDomain.DomainSwitch(rightdomain, lefttype));
				expdata.type = ClassType.INT;
				if (expdata.sideeffect) {
					vex.addDomain(v, expdata.domain);
				}
			} else {// BUGFOUND 20090204 yangxiu,  a.x is int
				return;
				//throw new RuntimeException("this is an illegal assignment");
			}
		} else if (image.equals("%=")) {
			// �Ϸ���ֻ�������������
			if (lefttype == ClassType.DOUBLE) {
				expdata.domain = DoubleDomain.mod((DoubleDomain) leftdomain, (DoubleDomain) ConvertDomain.DomainSwitch(rightdomain, lefttype));
				expdata.type = ClassType.DOUBLE;
				if (expdata.sideeffect) {
					vex.addDomain(v, expdata.domain);
				}
			}else if (lefttype == ClassType.INT) {
				expdata.domain = IntegerDomain.mod((IntegerDomain) leftdomain, (IntegerDomain) ConvertDomain.DomainSwitch(rightdomain, lefttype));
				expdata.type = ClassType.INT;
				if (expdata.sideeffect) {
					vex.addDomain(v, expdata.domain);
				}
			} else {// BUGFOUND 20090204 yangxiu,  a.x is int
				return;
				//throw new RuntimeException("this is an illegal assignment");
			}
		} else if (image.equals("<<=")) {
			// �Ϸ���ֻ������һ�����
			if (lefttype == ClassType.INT) {
				expdata.domain = IntegerDomain.getUnknownDomain();
				expdata.type = ClassType.INT;
				if (expdata.sideeffect) {
					vex.addDomain(v, expdata.domain);
				}
			} else {// BUGFOUND 20090204 yangxiu,  a.x is int
				return;
				//throw new RuntimeException("this is an illegal assignment");
			}
		} else if (image.equals(">>=")) {
			// �Ϸ���ֻ������һ�����
			if (lefttype == ClassType.INT) {
				expdata.domain = IntegerDomain.getUnknownDomain();
				expdata.type = ClassType.INT;
				if (expdata.sideeffect) {
					vex.addDomain(v, expdata.domain);
				}
			} else {// BUGFOUND 20090204 yangxiu,  a.x is int
				return;
				//throw new RuntimeException("this is an illegal assignment");
			}
		} else if (image.equals(">>>=")) {
			// �Ϸ���ֻ������һ�����
			if (lefttype == ClassType.INT) {
				expdata.domain = IntegerDomain.getUnknownDomain();
				expdata.type = ClassType.INT;
				if (expdata.sideeffect) {
					vex.addDomain(v, expdata.domain);
				}
			} else {// BUGFOUND 20090204 yangxiu,  a.x is int
				return;
				//throw new RuntimeException("this is an illegal assignment");
			}
		} else if (image.equals("&=")) {
			// �Ϸ���ֻ�������������
			if (lefttype == ClassType.INT) {
				expdata.domain = IntegerDomain.getUnknownDomain();
				expdata.type = ClassType.INT;
				if (expdata.sideeffect) {
					vex.addDomain(v, expdata.domain);
				}
			} else if (lefttype == ClassType.BOOLEAN) {
				BooleanDomain lb = (BooleanDomain) leftdomain;
				BooleanDomain rb = (BooleanDomain) ConvertDomain.DomainSwitch(rightdomain, ClassType.BOOLEAN);
				if (lb.getValue() == BooleanValue.TRUE && rb.getValue() == BooleanValue.TRUE) {
					expdata.domain = new BooleanDomain(BooleanValue.TRUE);
				} else if ((lb.getValue() == BooleanValue.TRUE || lb.getValue() == BooleanValue.TRUE_OR_FALSE)
						&& (rb.getValue() == BooleanValue.TRUE || rb.getValue() == BooleanValue.TRUE_OR_FALSE)) {
					expdata.domain = new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
				} else {
					expdata.domain = new BooleanDomain(BooleanValue.FALSE);
				}
				expdata.type = ClassType.BOOLEAN;
			} else {// BUGFOUND 20090204 yangxiu,  a.x is int
				return;
				//throw new RuntimeException("this is an illegal assignment");
			}
		} else if (image.equals("^=")) {
			// �Ϸ���ֻ�������������
			if (lefttype == ClassType.INT) {
				expdata.domain = IntegerDomain.getUnknownDomain();
				expdata.type = ClassType.INT;
				if (expdata.sideeffect) {
					vex.addDomain(v, expdata.domain);
				}
			} else if (lefttype == ClassType.BOOLEAN) {
				BooleanDomain lb = (BooleanDomain) leftdomain;
				BooleanDomain rb = (BooleanDomain) ConvertDomain.DomainSwitch(rightdomain, ClassType.BOOLEAN);
				if ((lb.getValue() == BooleanValue.TRUE && rb.getValue() == BooleanValue.FALSE)
						|| (lb.getValue() == BooleanValue.FALSE && rb.getValue() == BooleanValue.TRUE)) {
					expdata.domain = new BooleanDomain(BooleanValue.TRUE);
				} else if ((lb.getValue() == BooleanValue.TRUE && rb.getValue() == BooleanValue.TRUE)
						|| (lb.getValue() == BooleanValue.FALSE && rb.getValue() == BooleanValue.FALSE)) {
					expdata.domain = new BooleanDomain(BooleanValue.FALSE);
				} else {
					expdata.domain = new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
				}
				expdata.type = ClassType.BOOLEAN;
			} else {// BUGFOUND 20090204 yangxiu,  a.x is int
				return;
				//throw new RuntimeException("this is an illegal assignment");
			}
		} else if (image.equals("|=")) {
			// �Ϸ���ֻ�������������
			if (lefttype == ClassType.INT) {
				expdata.domain = IntegerDomain.getUnknownDomain();
				expdata.type = ClassType.INT;
				if (expdata.sideeffect) {
					vex.addDomain(v, expdata.domain);
				}
			} else if (lefttype == ClassType.BOOLEAN) {
				BooleanDomain lb = (BooleanDomain) leftdomain;
				BooleanDomain rb = (BooleanDomain) ConvertDomain.DomainSwitch(rightdomain, ClassType.BOOLEAN);
				if (lb.getValue() == BooleanValue.FALSE && rb.getValue() == BooleanValue.FALSE) {
					expdata.domain = new BooleanDomain(BooleanValue.FALSE);
				} else if ((lb.getValue() == BooleanValue.FALSE || lb.getValue() == BooleanValue.TRUE_OR_FALSE)
						&& (rb.getValue() == BooleanValue.FALSE || rb.getValue() == BooleanValue.TRUE_OR_FALSE)) {
					expdata.domain = new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
				} else {
					expdata.domain = new BooleanDomain(BooleanValue.TRUE);
				}
				expdata.type = ClassType.BOOLEAN;
			} else {// BUGFOUND 20090204 yangxiu,  a.x is int
				return;
				//throw new RuntimeException("this is an illegal assignment");
			}
		} else {
			throw new RuntimeException("this is an illegal assignment");
		}
	}

	/** ���ʽ��� */
	public Object visit(ASTStatementExpression node, Object data) {
		DomainData expdata = (DomainData) data;
		if (node.jjtGetNumChildren() == 3) {
			dealExprOrSExpr(node, expdata);
		} else {
			JavaNode javanode = (JavaNode) node.jjtGetChild(0);
			javanode.jjtAccept(this, data);
		}
		return null;
	}

	/** ���ʽ */
	public Object visit(ASTExpression node, Object data) {
		DomainData expdata = (DomainData) data;
		if (node.jjtGetNumChildren() == 1) {
			JavaNode javanode = (JavaNode) node.jjtGetChild(0);
			javanode.jjtAccept(this, data);
		} else {
			dealExprOrSExpr(node, expdata);
		}
		// System.out.println(expdata.domain);
		return null;
	}

	/** �������� */
	public Object visit(ASTVariableDeclarator node, Object data) {
		// int i=0;
		DomainData expdata = (DomainData) data;
		if (node.jjtGetNumChildren() == 2) {
			ASTVariableDeclaratorId id = (ASTVariableDeclaratorId) node.jjtGetChild(0);
			JavaNode javanode = (JavaNode) node.jjtGetChild(1);
			javanode.jjtAccept(this, data);
			List<VexNode> vexlist = node.getCurrentVexList();
			VexNode vex = vexlist.get(0);
			VariableNameDeclaration v = id.getNameDeclaration();
			if (javanode.jjtGetChild(0) instanceof ASTExpression) {
				// �����int i=0������
				Object leftdomain = v.getDomain();
				ClassType lefttype = DomainSet.getDomainType(leftdomain);
				Object rightdomain = expdata.domain;

				expdata.domain = ConvertDomain.DomainSwitch(rightdomain, lefttype);
				expdata.type = lefttype;

				if (expdata.sideeffect) {
					vex.addDomain(v, expdata.domain);
				}
			} else if (javanode.jjtGetChild(0) instanceof ASTArrayInitializer) {
				// ���������ʼ��
				ASTArrayInitializer arraynode = (ASTArrayInitializer) javanode.jjtGetChild(0);
				arraynode.calDims();
				ArrayList<Integer> dims = arraynode.getdims();
				ArrayDomain domain = new ArrayDomain(dims.size());
				for (int i = 0; i < dims.size(); i++) {
					domain.setDimension(i, new IntegerDomain(dims.get(i),dims.get(i),false,false));
				}
				expdata.domain = domain;
				expdata.type = ClassType.ARRAY;
				if (expdata.sideeffect) {
					vex.addDomain(v, domain);
				}
			} else {
				throw new RuntimeException("This is not a legal VariableDeclarator");
			}
		}
		return null;
	}

	/** ������ʼ�� */
	public Object visit(ASTVariableInitializer node, Object data) {
		return super.visit(node, data);
	}

	/** �������ʽ */
	public Object visit(ASTConditionalExpression node, Object data) {
		// �������ʽ������
		DomainData expdata = (DomainData) data;
		JavaNode condition = (JavaNode) node.jjtGetChild(0);
		
		ConditionData condata = new ConditionData(expdata.getCurrentVex());
		condition.jjtAccept(this, expdata);
		BooleanDomain condomain = (BooleanDomain) ConvertDomain.DomainSwitch(expdata.domain, ClassType.BOOLEAN);
		
		ConditionDomainVisitor convisitor = new ConditionDomainVisitor();
		condition.jjtAccept(convisitor, condata);
		
		JavaNode truebranch = (JavaNode) node.jjtGetChild(1);
		JavaNode falsebranch = (JavaNode) node.jjtGetChild(2);
		DomainSet domainset=null;
		List<VexNode> vexlist = node.getCurrentVexList();
		if(vexlist==null||vexlist.isEmpty()){
			expdata.domain = new ArbitraryDomain();
			expdata.type = ClassType.ARBITRARY;
			return null;
		}
		VexNode vex = vexlist.get(0);
		DomainSet old=vex.getDomainSet();
		
		domainset=condata.getTrueMayDomainSet();
		vex.setDomainSet(DomainSet.intersect(domainset, old));
		truebranch.jjtAccept(this, expdata);
		Object truedomain = expdata.domain;
		ClassType truetype = expdata.type;
		vex.setDomainSet(old);
		
		domainset=condata.getFalseMayDomainSet(vex);
		vex.setDomainSet(DomainSet.intersect(domainset, old));
		falsebranch.jjtAccept(this, expdata);
		Object falsedomain = expdata.domain;
		ClassType falsetype = expdata.type;
		vex.setDomainSet(old);
		
		
		switch (condomain.getValue()) {
		case TRUE:
			expdata.domain = truedomain;
			expdata.type = truetype;
			break;
		case FALSE:
			expdata.domain = falsedomain;
			expdata.type = falsetype;
			break;
		case TRUE_OR_FALSE:			
			if (truetype == ClassType.DOUBLE || falsetype == ClassType.DOUBLE) {
				DoubleDomain d1 = (DoubleDomain) ConvertDomain.DomainSwitch(truedomain, ClassType.DOUBLE);
				DoubleDomain d2 = (DoubleDomain) ConvertDomain.DomainSwitch(falsedomain, ClassType.DOUBLE);
				expdata.domain = DoubleDomain.union(d1, d2);
				expdata.type = ClassType.DOUBLE;
			} else if (truetype == ClassType.INT || falsetype == ClassType.INT) {
				IntegerDomain i1 = (IntegerDomain) ConvertDomain.DomainSwitch(truedomain, ClassType.INT);
				IntegerDomain i2 = (IntegerDomain) ConvertDomain.DomainSwitch(falsedomain, ClassType.INT);
				expdata.domain = IntegerDomain.union(i1, i2);
				expdata.type = ClassType.INT;
			} else if (truetype == ClassType.BOOLEAN || falsetype == ClassType.BOOLEAN) {
				BooleanDomain b1 = (BooleanDomain) ConvertDomain.DomainSwitch(truedomain, ClassType.BOOLEAN);
				BooleanDomain b2 = (BooleanDomain) ConvertDomain.DomainSwitch(falsedomain, ClassType.BOOLEAN);
				expdata.domain = BooleanDomain.union(b1, b2);
				expdata.type = ClassType.BOOLEAN;
			} else if (truetype == ClassType.REF || falsetype == ClassType.REF) {
				ReferenceDomain r1 = (ReferenceDomain) ConvertDomain.DomainSwitch(truedomain, ClassType.REF);
				ReferenceDomain r2 = (ReferenceDomain) ConvertDomain.DomainSwitch(falsedomain, ClassType.REF);
				expdata.domain = ReferenceDomain.union(r1, r2);
				expdata.type = ClassType.REF;
			} else if (truetype == ClassType.ARRAY || falsetype == ClassType.ARRAY) {
				ArrayDomain a1 = (ArrayDomain) ConvertDomain.DomainSwitch(truedomain, ClassType.ARRAY);
				ArrayDomain a2 = (ArrayDomain) ConvertDomain.DomainSwitch(falsedomain, ClassType.ARRAY);
				expdata.domain = ArrayDomain.union(a1, a2);
				expdata.type = ClassType.ARRAY;
			} else {
				expdata.domain = new ArbitraryDomain();
				expdata.type = ClassType.ARBITRARY;
			}
			break;
		case EMPTY:
			expdata.domain = new ArbitraryDomain();
			expdata.type = ClassType.ARBITRARY;
			break;
		default:
			throw new RuntimeException("this is not a legal conditionalexpression");
		}
		return null;
	}

	/** ��·���߼��� */
	public Object visit(ASTConditionalOrExpression node, Object data) {
		// || ��·
		DomainData expdata = (DomainData) data;
		BooleanDomain leftdomain = null, rightdomain = null;

		JavaNode javanode = (JavaNode) node.jjtGetChild(0);
		javanode.jjtAccept(this, expdata);
		leftdomain = (BooleanDomain) ConvertDomain.DomainSwitch(expdata.domain, ClassType.BOOLEAN);

		for (int i = 1; i < node.jjtGetNumChildren(); i++) {
			if (leftdomain.getValue() == BooleanValue.TRUE) {
				break;
			}
			javanode = (JavaNode) node.jjtGetChild(i);
			javanode.jjtAccept(this, expdata);
			rightdomain = (BooleanDomain) ConvertDomain.DomainSwitch(expdata.domain, ClassType.BOOLEAN);

			if (leftdomain.getValue() == BooleanValue.FALSE && rightdomain.getValue() == BooleanValue.FALSE) {
				leftdomain = new BooleanDomain(BooleanValue.FALSE);
			} else if ((leftdomain.getValue() == BooleanValue.FALSE || leftdomain.getValue() == BooleanValue.TRUE_OR_FALSE)
					&& (rightdomain.getValue() == BooleanValue.FALSE || rightdomain.getValue() == BooleanValue.TRUE_OR_FALSE)) {
				leftdomain = new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
			} else {
				leftdomain = new BooleanDomain(BooleanValue.TRUE);
			}
		}
		expdata.type = ClassType.BOOLEAN;
		expdata.domain = leftdomain;
		return null;
	}

	/** ��·���߼��� */
	public Object visit(ASTConditionalAndExpression node, Object data) {
		// && ��·
		DomainData expdata = (DomainData) data;
		BooleanDomain leftdomain = null, rightdomain = null;

		JavaNode javanode = (JavaNode) node.jjtGetChild(0);
		javanode.jjtAccept(this, expdata);
		leftdomain = (BooleanDomain) ConvertDomain.DomainSwitch(expdata.domain, ClassType.BOOLEAN);

		for (int i = 1; i < node.jjtGetNumChildren(); i++) {
			if (leftdomain.getValue() == BooleanValue.FALSE) {
				break;
			}
			javanode = (JavaNode) node.jjtGetChild(i);
			javanode.jjtAccept(this, expdata);
			rightdomain = (BooleanDomain) ConvertDomain.DomainSwitch(expdata.domain, ClassType.BOOLEAN);

			if (leftdomain.getValue() == BooleanValue.TRUE && rightdomain.getValue() == BooleanValue.TRUE) {
				leftdomain = new BooleanDomain(BooleanValue.TRUE);
			} else if ((leftdomain.getValue() == BooleanValue.TRUE || leftdomain.getValue() == BooleanValue.TRUE_OR_FALSE)
					&& (rightdomain.getValue() == BooleanValue.TRUE || rightdomain.getValue() == BooleanValue.TRUE_OR_FALSE)) {
				leftdomain = new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
			} else {
				leftdomain = new BooleanDomain(BooleanValue.FALSE);
			}
		}
		expdata.type = ClassType.BOOLEAN;
		expdata.domain = leftdomain;
		return null;
	}

	/** �Ƕ�·���߼���ͻ�λ���� */
	public Object visit(ASTInclusiveOrExpression node, Object data) {
		// |
		DomainData expdata = (DomainData) data;
		ClassType lefttype = null;

		JavaNode javanode = (JavaNode) node.jjtGetChild(0);
		javanode.jjtAccept(this, expdata);
		lefttype = expdata.type;
		if (lefttype == ClassType.BOOLEAN) {
			BooleanDomain leftdomain = null, rightdomain = null;
			leftdomain = (BooleanDomain) ConvertDomain.DomainSwitch(expdata.domain, ClassType.BOOLEAN);
			for (int i = 1; i < node.jjtGetNumChildren(); i++) {
				javanode = (JavaNode) node.jjtGetChild(i);
				javanode.jjtAccept(this, expdata);
				rightdomain = (BooleanDomain) ConvertDomain.DomainSwitch(expdata.domain, ClassType.BOOLEAN);

				if (leftdomain.getValue() == BooleanValue.FALSE && rightdomain.getValue() == BooleanValue.FALSE) {
					leftdomain = new BooleanDomain(BooleanValue.FALSE);
				} else if ((leftdomain.getValue() == BooleanValue.FALSE || leftdomain.getValue() == BooleanValue.TRUE_OR_FALSE)
						&& (rightdomain.getValue() == BooleanValue.FALSE || rightdomain.getValue() == BooleanValue.TRUE_OR_FALSE)) {
					leftdomain = new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
				} else {
					leftdomain = new BooleanDomain(BooleanValue.TRUE);
				}
			}
			expdata.type = ClassType.BOOLEAN;
			expdata.domain = leftdomain;
		} else {
			expdata.domain = new ArbitraryDomain();
			expdata.type = ClassType.ARBITRARY;
		}
		return null;
	}

	/** ���λ���� */
	public Object visit(ASTExclusiveOrExpression node, Object data) {
		// ^
		DomainData expdata = (DomainData) data;
		ClassType lefttype = null;

		JavaNode javanode = (JavaNode) node.jjtGetChild(0);
		javanode.jjtAccept(this, expdata);
		lefttype = expdata.type;
		if (lefttype == ClassType.BOOLEAN) {
			BooleanDomain leftdomain = null, rightdomain = null;
			leftdomain = (BooleanDomain) ConvertDomain.DomainSwitch(expdata.domain, ClassType.BOOLEAN);
			for (int i = 1; i < node.jjtGetNumChildren(); i++) {
				javanode = (JavaNode) node.jjtGetChild(i);
				javanode.jjtAccept(this, expdata);
				rightdomain = (BooleanDomain) ConvertDomain.DomainSwitch(expdata.domain, ClassType.BOOLEAN);

				if ((leftdomain.getValue() == BooleanValue.TRUE && rightdomain.getValue() == BooleanValue.FALSE)
						|| (leftdomain.getValue() == BooleanValue.FALSE && rightdomain.getValue() == BooleanValue.TRUE)) {
					leftdomain = new BooleanDomain(BooleanValue.TRUE);
				} else if ((leftdomain.getValue() == BooleanValue.TRUE && rightdomain.getValue() == BooleanValue.TRUE)
						|| (leftdomain.getValue() == BooleanValue.FALSE && rightdomain.getValue() == BooleanValue.FALSE)) {
					leftdomain = new BooleanDomain(BooleanValue.FALSE);
				} else {
					leftdomain = new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
				}
			}
			expdata.type = ClassType.BOOLEAN;
			expdata.domain = leftdomain;
		} else {
			expdata.domain = new ArbitraryDomain();
			expdata.type = ClassType.ARBITRARY;
		}
		return null;
	}

	/** �Ƕ�·���߼������λ���� */
	public Object visit(ASTAndExpression node, Object data) {
		// &
		DomainData expdata = (DomainData) data;
		ClassType lefttype = null;

		JavaNode javanode = (JavaNode) node.jjtGetChild(0);
		javanode.jjtAccept(this, expdata);
		lefttype = expdata.type;
		if (lefttype == ClassType.BOOLEAN) {
			BooleanDomain leftdomain = null, rightdomain = null;
			leftdomain = (BooleanDomain) ConvertDomain.DomainSwitch(expdata.domain, ClassType.BOOLEAN);
			for (int i = 1; i < node.jjtGetNumChildren(); i++) {
				javanode = (JavaNode) node.jjtGetChild(i);
				javanode.jjtAccept(this, expdata);
				rightdomain = (BooleanDomain) ConvertDomain.DomainSwitch(expdata.domain, ClassType.BOOLEAN);

				if (leftdomain.getValue() == BooleanValue.TRUE && rightdomain.getValue() == BooleanValue.TRUE) {
					leftdomain = new BooleanDomain(BooleanValue.TRUE);
				} else if ((leftdomain.getValue() == BooleanValue.TRUE || leftdomain.getValue() == BooleanValue.TRUE_OR_FALSE)
						&& (rightdomain.getValue() == BooleanValue.TRUE || rightdomain.getValue() == BooleanValue.TRUE_OR_FALSE)) {
					leftdomain = new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
				} else {
					leftdomain = new BooleanDomain(BooleanValue.FALSE);
				}
			}
			expdata.type = ClassType.BOOLEAN;
			expdata.domain = leftdomain;
		} else {
			expdata.domain = new ArbitraryDomain();
			expdata.type = ClassType.ARBITRARY;
		}
		return null;
	}

	/** ��ȱ��ʽ */
	public Object visit(ASTEqualityExpression node, Object data) {
		// == ��=
		DomainData expdata = (DomainData) data;
		ClassType lefttype = null, righttype = null;
		Object leftdomain = null, rightdomain = null;
		String image = node.getImage();
		if (image == null) {
			throw new RuntimeException("This is not a legal EqualityExpression");
		}
		String[] operators = image.split("#");
		if (operators.length != (node.jjtGetNumChildren() - 1)) {
			throw new RuntimeException("This is not a legal EqualityExpression");
		}

		JavaNode javanode = (JavaNode) node.jjtGetChild(0);
		javanode.jjtAccept(this, expdata);
		lefttype = expdata.type;
		leftdomain = expdata.domain;

		for (int i = 1; i < node.jjtGetNumChildren(); i++) {
			javanode = (JavaNode) node.jjtGetChild(i);
			javanode.jjtAccept(this, data);
			righttype = ((DomainData) data).type;
			rightdomain = ((DomainData) data).domain;
			String operator = operators[i - 1];
			if (lefttype != righttype) {
				if (lefttype == ClassType.DOUBLE || righttype == ClassType.DOUBLE) {
					leftdomain = ConvertDomain.DomainSwitch(leftdomain, ClassType.DOUBLE);
					lefttype = ClassType.DOUBLE;
					rightdomain = ConvertDomain.DomainSwitch(rightdomain, ClassType.DOUBLE);
					righttype = ClassType.DOUBLE;
				} else if (lefttype == ClassType.INT || righttype == ClassType.INT) {
					leftdomain = ConvertDomain.DomainSwitch(leftdomain, ClassType.INT);
					lefttype = ClassType.INT;
					rightdomain = ConvertDomain.DomainSwitch(rightdomain, ClassType.INT);
					righttype = ClassType.INT;
				} else if (lefttype == ClassType.BOOLEAN || righttype == ClassType.BOOLEAN) {
					leftdomain = ConvertDomain.DomainSwitch(leftdomain, ClassType.BOOLEAN);
					lefttype = ClassType.BOOLEAN;
					rightdomain = ConvertDomain.DomainSwitch(rightdomain, ClassType.BOOLEAN);
					righttype = ClassType.BOOLEAN;
				} else if (lefttype == ClassType.REF || righttype == ClassType.REF) {
					/**ADDED BY YANG 2011-05-16 10:40
					if(lefttype==ClassType.ARRAY){
						leftdomain = ConvertDomain.DomainSwitch(leftdomain, ClassType.ARRAY);
						rightdomain = ConvertDomain.DomainSwitch(rightdomain, ClassType.ARRAY);
						righttype = ClassType.ARRAY;
					}
					else if(righttype==ClassType.ARRAY){
						leftdomain = ConvertDomain.DomainSwitch(leftdomain, ClassType.ARRAY);
						lefttype = ClassType.ARRAY;
						rightdomain = ConvertDomain.DomainSwitch(rightdomain, ClassType.ARRAY);
						righttype = ClassType.ARRAY;
					}
					
					else{END-YANG**/
					leftdomain = ConvertDomain.DomainSwitch(leftdomain, ClassType.REF);
					lefttype = ClassType.REF;
					rightdomain = ConvertDomain.DomainSwitch(rightdomain, ClassType.REF);
					righttype = ClassType.REF;
				  //  } by:yang
				   
				} else if (lefttype == ClassType.ARRAY || righttype == ClassType.ARRAY) {
					leftdomain = ConvertDomain.DomainSwitch(leftdomain, ClassType.ARRAY);
					lefttype = ClassType.ARRAY;
					rightdomain = ConvertDomain.DomainSwitch(rightdomain, ClassType.ARRAY);
					righttype = ClassType.ARRAY;
				} else {
					leftdomain = ConvertDomain.DomainSwitch(leftdomain, ClassType.ARBITRARY);
					lefttype = ClassType.ARBITRARY;
					rightdomain = ConvertDomain.DomainSwitch(rightdomain, ClassType.ARBITRARY);
					righttype = ClassType.ARBITRARY;
				}
			}
			if(ConvertDomain.DomainIsUnknown(leftdomain)||ConvertDomain.DomainIsUnknown(rightdomain)){
				leftdomain = new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
			}else if (operator.equals("==")) {
				switch (lefttype) {
				case INT:
					if (IntegerDomain.intersect((IntegerDomain) ConvertDomain.DomainSwitch(leftdomain, ClassType.INT), (IntegerDomain) ConvertDomain.DomainSwitch(rightdomain, ClassType.INT)).isEmpty()) {
						leftdomain = new BooleanDomain(BooleanValue.FALSE);
					} else if (((IntegerDomain) leftdomain).isCanonical() && leftdomain.equals(rightdomain)) {
						leftdomain = new BooleanDomain(BooleanValue.TRUE);
					} else {
						leftdomain = new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
					}
					break;
				case DOUBLE:
					if (DoubleDomain.intersect((DoubleDomain) leftdomain, (DoubleDomain) rightdomain).isEmpty()) {
						leftdomain = new BooleanDomain(BooleanValue.FALSE);
					} else if (((DoubleDomain) leftdomain).isCanonical() && leftdomain.equals(rightdomain)) {
						leftdomain = new BooleanDomain(BooleanValue.TRUE);
					} else {
						leftdomain = new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
					}
					break;
				case ARRAY:
					leftdomain = new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
					break;
				case BOOLEAN:
					if (leftdomain.equals(rightdomain)) {
						if (((BooleanDomain) leftdomain).getUnknown()||((BooleanDomain) leftdomain).getValue() == BooleanValue.TRUE_OR_FALSE) {
							leftdomain = new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
						} else {
							leftdomain = new BooleanDomain(BooleanValue.TRUE);
						}
					} else {
						leftdomain = new BooleanDomain(BooleanValue.FALSE);
					}
					break;
				case REF:
					if (leftdomain.equals(rightdomain)) {
						if (((ReferenceDomain) leftdomain).getUnknown()||((ReferenceDomain) leftdomain).getValue() == ReferenceValue.NULL_OR_NOTNULL) {
							leftdomain = new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
						} else {
							leftdomain = new BooleanDomain(BooleanValue.TRUE);
						}
					} else {
						/**
						 * added by yang
						 * 2011-06-08 10:51
						
						if( (((ReferenceDomain) leftdomain).getUnknown()||((ReferenceDomain) leftdomain).getValue() == ReferenceValue.NULL_OR_NOTNULL)||(((ReferenceDomain) rightdomain).getUnknown()||((ReferenceDomain) rightdomain).getValue() == ReferenceValue.NULL_OR_NOTNULL)){
							leftdomain = new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
						} else{//end-yang
						 */    leftdomain = new BooleanDomain(BooleanValue.FALSE);
						}   
					//}
					break;
				case ARBITRARY:
					leftdomain = new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
					break;
				default:
					throw new RuntimeException("Do not know the domain type other than INT,DOUBLE,ARRAY,BOOLEAN and REF");
				}
			} else {
				switch (lefttype) {
				case INT:
					if (IntegerDomain.intersect((IntegerDomain) leftdomain, (IntegerDomain) rightdomain).isEmpty()) {
						leftdomain = new BooleanDomain(BooleanValue.TRUE);
					} else if (((IntegerDomain) leftdomain).isCanonical() && leftdomain.equals(rightdomain)) {
						leftdomain = new BooleanDomain(BooleanValue.FALSE);
					} else {
						leftdomain = new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
					}
					break;
				case DOUBLE:
					if (DoubleDomain.intersect((DoubleDomain) leftdomain, (DoubleDomain) rightdomain).isEmpty()) {
						leftdomain = new BooleanDomain(BooleanValue.TRUE);
					} else if (((DoubleDomain) leftdomain).isCanonical() && leftdomain.equals(rightdomain)) {
						leftdomain = new BooleanDomain(BooleanValue.FALSE);
					} else {
						leftdomain = new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
					}
					break;
				case ARRAY:
					leftdomain = new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
					break;
				case BOOLEAN:
					if (leftdomain.equals(rightdomain)) {
						if (((BooleanDomain) leftdomain).getUnknown()||((BooleanDomain) leftdomain).getValue() == BooleanValue.TRUE_OR_FALSE) {
							leftdomain = new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
						} else {
							leftdomain = new BooleanDomain(BooleanValue.FALSE);
						}
					} else {
						leftdomain = new BooleanDomain(BooleanValue.TRUE);
					}
					break;
				case REF:
					if (leftdomain.equals(rightdomain)) {
						if (((ReferenceDomain) leftdomain).getUnknown()||((ReferenceDomain) leftdomain).getValue() == ReferenceValue.NULL_OR_NOTNULL) {
							leftdomain = new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
						} else {
							leftdomain = new BooleanDomain(BooleanValue.FALSE);
						}
					} else {
						/**
						 * added by yang
						 * 2011-06-08 10:51
						
						if( (((ReferenceDomain) leftdomain).getUnknown()||((ReferenceDomain) leftdomain).getValue() == ReferenceValue.NULL_OR_NOTNULL)||(((ReferenceDomain) rightdomain).getUnknown()||((ReferenceDomain) rightdomain).getValue() == ReferenceValue.NULL_OR_NOTNULL)){
							leftdomain = new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
						} else{//end-yang
						 */leftdomain = new BooleanDomain(BooleanValue.TRUE);
					    //  }
					}
					break;
				case ARBITRARY:
					leftdomain = new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
					break;
				default:
					throw new RuntimeException("Do not know the domain type other than INT,DOUBLE,ARRAY,BOOLEAN and REF");
				}
			}
			lefttype = ClassType.BOOLEAN;
		}
		expdata.type = lefttype;
		expdata.domain = leftdomain;
		return null;
	}

	/** instanceof���ʽ */
	public Object visit(ASTInstanceOfExpression node, Object data) {
		// instanceof
		// ֱ�ӷ�����ٶ�����
		DomainData expdata = (DomainData) data;
		JavaNode javanode = (JavaNode) node.jjtGetChild(0);
		javanode.jjtAccept(this, expdata);
		ReferenceDomain domain = (ReferenceDomain) ConvertDomain.DomainSwitch(expdata.domain, ClassType.REF);

		if (domain.getValue() == ReferenceValue.NULL) {
			expdata.domain = new BooleanDomain(BooleanValue.FALSE);
			expdata.type = ClassType.BOOLEAN;
		} else {
			expdata.domain = new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
			expdata.type = ClassType.BOOLEAN;
		}
		return null;
	}

	/** ��С�жϱ��ʽ */
	public Object visit(ASTRelationalExpression node, Object data) {
		// < > <= >=
		DomainData expdata = (DomainData) data;
		ClassType lefttype = null, righttype = null;
		Object leftdomain = null, rightdomain = null;
		String image = node.getImage();
		if (image == null) {
			throw new RuntimeException("This is not a legal RelationalExpression");
		}
		String[] operators = image.split("#");
		if (operators.length != (node.jjtGetNumChildren() - 1)) {
			throw new RuntimeException("This is not a legal RelationalExpression");
		}

		if (operators.length != 1) {
			// ����Ϸ������ʵ����ֻ������һ��������Ϊ�˳���ͳһ��񣬻��Ǻ�ǰ���һ��ģʽ������
			throw new RuntimeException("This is not a legal RelationalExpression");
		}

		JavaNode javanode = (JavaNode) node.jjtGetChild(0);
		javanode.jjtAccept(this, expdata);
		lefttype = expdata.type;
		leftdomain = expdata.domain;

		for (int i = 1; i < node.jjtGetNumChildren(); i++) {
			javanode = (JavaNode) node.jjtGetChild(i);
			javanode.jjtAccept(this, expdata);
			righttype = expdata.type;
			rightdomain = expdata.domain;
			String operator = operators[i - 1];

			if (lefttype == ClassType.DOUBLE || righttype == ClassType.DOUBLE) {
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

			if (lefttype == ClassType.INT) {
				IntegerDomain inter = IntegerDomain.intersect((IntegerDomain) leftdomain, (IntegerDomain) rightdomain);
				IntegerDomain li = (IntegerDomain) leftdomain, ri = (IntegerDomain) rightdomain;
				if(li.getUnknown()||ri.getUnknown()){
					leftdomain = new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
				}else if (operator.equals(">")) {
					if (inter.isEmpty() || inter.isCanonical()) {
						if (li.isEmpty() || ri.isEmpty() || li.jointoOneInterval().getMax() <= ri.jointoOneInterval().getMin()) {
							leftdomain = new BooleanDomain(BooleanValue.FALSE);
						} else if (li.jointoOneInterval().getMin() > ri.jointoOneInterval().getMax()) {
							leftdomain = new BooleanDomain(BooleanValue.TRUE);
						} else {
							leftdomain = new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
						}
					} else {
						leftdomain = new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
					}
				} else if (operator.equals(">=")) {
					if (inter.isEmpty() || inter.isCanonical()) {
						if (li.isEmpty() || ri.isEmpty() || li.jointoOneInterval().getMax() < ri.jointoOneInterval().getMin()) {
							leftdomain = new BooleanDomain(BooleanValue.FALSE);
						} else if (li.jointoOneInterval().getMin() >= ri.jointoOneInterval().getMax()) {
							leftdomain = new BooleanDomain(BooleanValue.TRUE);
						} else {
							leftdomain = new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
						}
					} else {
						leftdomain = new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
					}
				} else if (operator.equals("<")) {
					if (inter.isEmpty() || inter.isCanonical()) {
						if (li.isEmpty() || ri.isEmpty() || li.jointoOneInterval().getMin() >= ri.jointoOneInterval().getMax()) {
							leftdomain = new BooleanDomain(BooleanValue.FALSE);
						} else if (li.jointoOneInterval().getMax() < ri.jointoOneInterval().getMin()) {
							leftdomain = new BooleanDomain(BooleanValue.TRUE);
						} else {
							leftdomain = new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
						}
					} else {
						leftdomain = new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
					}
				} else if (operator.equals("<=")) {
					if (inter.isEmpty() || inter.isCanonical()) {
						if (li.isEmpty() || ri.isEmpty() || li.jointoOneInterval().getMin() > ri.jointoOneInterval().getMax()) {
							leftdomain = new BooleanDomain(BooleanValue.FALSE);
						} else if (li.jointoOneInterval().getMax() <= ri.jointoOneInterval().getMin()) {
							leftdomain = new BooleanDomain(BooleanValue.TRUE);
						} else {
							leftdomain = new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
						}
					} else {
						leftdomain = new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
					}
				} else {
					throw new RuntimeException("This is not a legal RelationalExpression");
				}
			} else if (lefttype == ClassType.DOUBLE) {
				DoubleDomain inter = DoubleDomain.intersect((DoubleDomain) leftdomain, (DoubleDomain) rightdomain);
				DoubleDomain li = (DoubleDomain) leftdomain, ri = (DoubleDomain) rightdomain;
				if(li.getUnknown()||ri.getUnknown()){
					leftdomain = new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
				}else if (operator.equals(">")) {
					if (inter.isEmpty() || inter.isCanonical()) {
						if (li.isEmpty() || ri.isEmpty() || li.jointoOneInterval().getMax() <= ri.jointoOneInterval().getMin()) {
							leftdomain = new BooleanDomain(BooleanValue.FALSE);
						} else if (li.jointoOneInterval().getMin() > ri.jointoOneInterval().getMax()) {
							leftdomain = new BooleanDomain(BooleanValue.TRUE);
						} else {
							leftdomain = new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
						}
					} else {
						leftdomain = new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
					}
				} else if (operator.equals(">=")) {
					if (inter.isEmpty() || inter.isCanonical()) {
						if (li.isEmpty() || ri.isEmpty() || li.jointoOneInterval().getMax() < ri.jointoOneInterval().getMin()) {
							leftdomain = new BooleanDomain(BooleanValue.FALSE);
						} else if (li.jointoOneInterval().getMin() >= ri.jointoOneInterval().getMax()) {
							leftdomain = new BooleanDomain(BooleanValue.TRUE);
						} else {
							leftdomain = new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
						}
					} else {
						leftdomain = new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
					}
				} else if (operator.equals("<")) {
					if (inter.isEmpty() || inter.isCanonical()) {
						if (li.isEmpty() || ri.isEmpty() || li.jointoOneInterval().getMin() >= ri.jointoOneInterval().getMax()) {
							leftdomain = new BooleanDomain(BooleanValue.FALSE);
						} else if (li.jointoOneInterval().getMax() < ri.jointoOneInterval().getMin()) {
							leftdomain = new BooleanDomain(BooleanValue.TRUE);
						} else {
							leftdomain = new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
						}
					} else {
						leftdomain = new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
					}
				} else if (operator.equals("<=")) {
					if (inter.isEmpty() || inter.isCanonical()) {
						if (li.isEmpty() || ri.isEmpty() || li.jointoOneInterval().getMin() > ri.jointoOneInterval().getMax()) {
							leftdomain = new BooleanDomain(BooleanValue.FALSE);
						} else if (li.jointoOneInterval().getMax() <= ri.jointoOneInterval().getMin()) {
							leftdomain = new BooleanDomain(BooleanValue.TRUE);
						} else {
							leftdomain = new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
						}
					} else {
						leftdomain = new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
					}
				} else {
					throw new RuntimeException("This is not a legal RelationalExpression");
				}
			} else {
				throw new RuntimeException("This is not a legal RelationalExpression");
			}
			lefttype = ClassType.BOOLEAN;
		}
		expdata.type = lefttype;
		expdata.domain = leftdomain;
		return null;
	}

	/** ��λ���� */
	public Object visit(ASTShiftExpression node, Object data) {
		// << >> >>>
		// �Ϸ���ֻ��һ�����
		DomainData expdata = (DomainData) data;

		JavaNode javanode = (JavaNode) node.jjtGetChild(0);
		javanode.jjtAccept(this, expdata);

		for (int i = 1; i < node.jjtGetNumChildren(); i++) {
			javanode = (JavaNode) node.jjtGetChild(i);
			if ((javanode instanceof ASTRSIGNEDSHIFT) || (javanode instanceof ASTRUNSIGNEDSHIFT)) {
				// ������ʾ���ŵķ��ս��
				continue;
			}
			javanode.jjtAccept(this, data);
		}
		expdata.domain = IntegerDomain.getUnknownDomain();
		expdata.type = ClassType.INT;
		return null;
	}

	/** �Ӽ����ʽ */
	public Object visit(ASTAdditiveExpression node, Object data) {
		// + -
		DomainData expdata = (DomainData) data;
		String image = node.getImage();
		if (image == null) {
			throw new RuntimeException("This is not a legal AdditiveExpression");
		}
		String[] operators = image.split("#");
		if (operators.length != (node.jjtGetNumChildren() - 1)) {
			throw new RuntimeException("This is not a legal AdditiveExpression");
		}
		ClassType lefttype = null, righttype = null;
		Object leftdomain = null, rightdomain = null;

		JavaNode javanode = (JavaNode) node.jjtGetChild(0);
		javanode.jjtAccept(this, expdata);
		lefttype = expdata.type;
		leftdomain = expdata.domain;

		for (int i = 1; i < node.jjtGetNumChildren(); i++) {
			javanode = (JavaNode) node.jjtGetChild(i);
			javanode.jjtAccept(this, expdata);
			righttype = expdata.type;
			rightdomain = expdata.domain;
			String operator = operators[i - 1];

			if (lefttype == ClassType.REF && operator.equals("+")) {
				// �������ͣ�ʲô�������������ӷ���+
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
		expdata.type = lefttype;
		expdata.domain = leftdomain;

		// System.out.println(leftdomain);

		return null;
	}

	/** �˷���������ȡģ���ʽ */
	public Object visit(ASTMultiplicativeExpression node, Object data) {
		// * / %
		DomainData expdata = (DomainData) data;
		String image = node.getImage();
		if (image == null) {
			throw new RuntimeException("This is not a legal MultiplicativeExpression");
		}
		String[] operators = image.split("#");
		if (operators.length != (node.jjtGetNumChildren() - 1)) {
			throw new RuntimeException("This is not a legal MultiplicativeExpression");
		}
		ClassType lefttype = null, righttype = null;
		Object leftdomain = null, rightdomain = null;

		JavaNode javanode = (JavaNode) node.jjtGetChild(0);
		javanode.jjtAccept(this, expdata);
		lefttype = expdata.type;
		leftdomain = expdata.domain;

		for (int i = 1; i < node.jjtGetNumChildren(); i++) {
			javanode = (JavaNode) node.jjtGetChild(i);
			javanode.jjtAccept(this, expdata);
			righttype = expdata.type;
			rightdomain = expdata.domain;
			String operator = operators[i - 1];

			if (lefttype == ClassType.DOUBLE || righttype == ClassType.DOUBLE) {
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
				if (operator.equals("*")) {
					leftdomain = DoubleDomain.mul(a, b);
				} else if (operator.equals("/")) {
					leftdomain = DoubleDomain.div(a, b);
				}else if (operator.equals("%")) {
					leftdomain = DoubleDomain.mod(a, b);
				} else {
					throw new RuntimeException("This is not a legal AdditiveExpression");
				}
			} else if (lefttype == ClassType.INT) {
				IntegerDomain a = (IntegerDomain) leftdomain;
				IntegerDomain b = (IntegerDomain) rightdomain;
				if (operator.equals("*")) {
					leftdomain = IntegerDomain.mul(a, b);
				} else if (operator.equals("/")) {
					leftdomain = IntegerDomain.div(a, b);
				} else if (operator.equals("%")) {
					leftdomain = IntegerDomain.mod(a, b);
				} else {
					throw new RuntimeException("This is not a legal AdditiveExpression");
				}
			} else {
				throw new RuntimeException("can not deal with domain other than int or double ");
			}
		}
		expdata.type = lefttype;
		expdata.domain = leftdomain;

		return null;
	}

	/** �������ʽ */
	public Object visit(ASTUnaryExpression node, Object data) {
		// + -����
		DomainData expdata = (DomainData) data;
		if (node.hasImageEqualTo("-")) {
			JavaNode javanode = (JavaNode) node.jjtGetChild(0);
			javanode.jjtAccept(this, expdata);
			if (expdata.type == ClassType.INT) {
				expdata.domain = IntegerDomain.uminus((IntegerDomain) expdata.domain);
			} else if (expdata.type == ClassType.DOUBLE) {
				expdata.domain = DoubleDomain.uminus((DoubleDomain) expdata.domain);
			} else {
				expdata.type = ClassType.INT;
				expdata.domain = ConvertDomain.DomainSwitch(expdata.domain, ClassType.INT);
			}
		} else {
			node.childrenAccept(this, expdata);
		}
		return null;
	}

	/** ++i���ʽ */
	public Object visit(ASTPreIncrementExpression node, Object data) {
		// ++i
		DomainData expdata = (DomainData) data;
		expdata.domain = new ArbitraryDomain();
		expdata.type = ClassType.ARBITRARY;
		ASTName name = (ASTName) ((SimpleJavaNode) node.jjtGetChild(0)).getSingleChildofType(ASTName.class);
		if (name == null || !(name.getNameDeclaration() instanceof VariableNameDeclaration)) {
			return null;
		}
		String nameimage = name.getImage();
		if (nameimage == null && nameimage.contains(".")) {
			return null;
		}
		VariableNameDeclaration v = (VariableNameDeclaration) name.getNameDeclaration();
		// ����++�ұ߱��ʽ
		JavaNode javanode = (JavaNode) node.jjtGetChild(0);
		javanode.jjtAccept(this, expdata);
		// ����++�ұ߱��ʽ���������
		Object beforedomain = expdata.domain, afterdomain = null;
		ClassType beforetype = expdata.type, aftertype = ClassType.INT;

		if (beforetype == ClassType.INT) {
			afterdomain = IntegerDomain.add((IntegerDomain) beforedomain, 1);
			aftertype = ClassType.INT;
		} else if (beforetype == ClassType.DOUBLE) {
			afterdomain = DoubleDomain.add((DoubleDomain) beforedomain, 1);
			aftertype = ClassType.DOUBLE;
		} else {
			aftertype = ClassType.INT;
			afterdomain = ConvertDomain.DomainSwitch(expdata.domain, ClassType.INT);
		}

		// ��ֵ����
		List<VexNode> vexlist = node.getCurrentVexList();
		VexNode vex = vexlist.get(0);
		if (expdata.sideeffect) {
			afterdomain = ConvertDomain.DomainSwitch(afterdomain, DomainSet.getDomainType(v.getDomain()));
			vex.addDomain(v, afterdomain);
		}

		expdata.type = aftertype;
		expdata.domain = afterdomain;
		return null;
	}

	/** --i���ʽ */
	public Object visit(ASTPreDecrementExpression node, Object data) {
		// --i
		DomainData expdata = (DomainData) data;
		expdata.domain = new ArbitraryDomain();
		expdata.type = ClassType.ARBITRARY;
		ASTName name = (ASTName) ((SimpleJavaNode) node.jjtGetChild(0)).getSingleChildofType(ASTName.class);
		if (name == null || !(name.getNameDeclaration() instanceof VariableNameDeclaration)) {
			return null;
		}

		String nameimage = name.getImage();
		if (nameimage == null && nameimage.contains(".")) {
			return null;
		}

		VariableNameDeclaration v = (VariableNameDeclaration) name.getNameDeclaration();
		// ����--�ұ߱��ʽ
		JavaNode javanode = (JavaNode) node.jjtGetChild(0);
		javanode.jjtAccept(this, expdata);
		// ����--�ұ߱��ʽ���������
		Object beforedomain = expdata.domain, afterdomain = null;
		ClassType beforetype = expdata.type, aftertype = ClassType.INT;

		if (beforetype == ClassType.INT) {
			afterdomain = IntegerDomain.sub((IntegerDomain) beforedomain, 1);
			aftertype = ClassType.INT;
		} else if (beforetype == ClassType.DOUBLE) {
			afterdomain = DoubleDomain.sub((DoubleDomain) beforedomain, 1);
			aftertype = ClassType.DOUBLE;
		} else {
			aftertype = ClassType.INT;
			afterdomain = ConvertDomain.DomainSwitch(expdata.domain, ClassType.INT);
		}

		// ��ֵ����
		List<VexNode> vexlist = node.getCurrentVexList();
		VexNode vex = vexlist.get(0);
		if (expdata.sideeffect) {
			afterdomain = ConvertDomain.DomainSwitch(afterdomain, DomainSet.getDomainType(v.getDomain()));
			vex.addDomain(v, afterdomain);
		}

		expdata.type = aftertype;
		expdata.domain = afterdomain;
		return null;
	}

	/** �߼��Ǻͷ�λ���� */
	public Object visit(ASTUnaryExpressionNotPlusMinus node, Object data) {
		// ~ !
		DomainData expdata = (DomainData) data;
		String image = node.getImage();
		JavaNode javanode = (JavaNode) node.jjtGetChild(0);
		javanode.jjtAccept(this, expdata);

		Object domain = expdata.domain;
		ClassType type = expdata.type;

		if (image.equals("!")) {
			BooleanDomain bd = (BooleanDomain) ConvertDomain.DomainSwitch(domain, ClassType.BOOLEAN);
			type = ClassType.BOOLEAN;
			if (bd.getValue() == BooleanValue.TRUE) {
				domain = new BooleanDomain(BooleanValue.FALSE);
			} else if (bd.getValue() == BooleanValue.FALSE) {
				domain = new BooleanDomain(BooleanValue.TRUE);
			} else {
				domain = new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
			}
		} else if (image.equals("~")) {
			type = ClassType.INT;
			domain = IntegerDomain.getUnknownDomain();
		} else {
			throw new RuntimeException("This is not a legal UnaryExpressionNotPlusMinus");
		}

		expdata.domain = domain;
		expdata.type = type;
		return null;
	}

	/** i++ i--���ʽ */
	public Object visit(ASTPostfixExpression node, Object data) {
		// i++ i--
		DomainData expdata = (DomainData) data;
		expdata.domain = new ArbitraryDomain();
		expdata.type = ClassType.ARBITRARY;
		ASTName name = (ASTName) ((SimpleJavaNode) node.jjtGetChild(0)).getSingleChildofType(ASTName.class);
		if (name == null || !(name.getNameDeclaration() instanceof VariableNameDeclaration)) {
			return null;
		}
		String nameimage = name.getImage();
		if (nameimage == null && nameimage.contains(".")) {
			return null;
		}

		String image = node.getImage();
		VariableNameDeclaration v = (VariableNameDeclaration) name.getNameDeclaration();
		// ����--��߱��ʽ
		JavaNode javanode = (JavaNode) node.jjtGetChild(0);
		javanode.jjtAccept(this, expdata);
		// ����--��߱��ʽ���������
		Object beforedomain = expdata.domain, afterdomain = null;
		ClassType beforetype = expdata.type; // aftertype = ClassType.INT;

		if (beforetype == ClassType.DOUBLE) {
			if (image.equals("++")) {
				afterdomain = DoubleDomain.add((DoubleDomain) beforedomain, 1);
			} else {
				afterdomain = DoubleDomain.sub((DoubleDomain) beforedomain, 1);
			}
		} else {
			beforetype = ClassType.INT;
			beforedomain = ConvertDomain.DomainSwitch(beforedomain, ClassType.INT);
			if (image.equals("++")) {
				afterdomain = IntegerDomain.add((IntegerDomain) beforedomain, 1);
			} else {
				afterdomain = IntegerDomain.sub((IntegerDomain) beforedomain, 1);
			}
		}

		// ��ֵ����
		List<VexNode> vexlist = node.getCurrentVexList();
		VexNode vex = vexlist.get(0);
		afterdomain = ConvertDomain.DomainSwitch(afterdomain, DomainSet.getDomainType(v.getDomain()));
		if (expdata.sideeffect) {
			vex.addDomain(v, afterdomain);
		}

		expdata.type = beforetype;
		expdata.domain = beforedomain;
		return null;
	}

	/** ����ת�����ʽ */
	public Object visit(ASTCastExpression node, Object data) {
		// ǿ������ת��
		DomainData expdata = (DomainData) data;
		ASTType typenode = (ASTType) node.jjtGetChild(0);
		JavaNode javanode = (JavaNode) node.jjtGetChild(1);
		javanode.jjtAccept(this, expdata);
		Object domain = expdata.domain;
		ClassType type = expdata.type;

		if (typenode.jjtGetChild(0) instanceof ASTReferenceType) {
			// �����κδ���
		} else if (typenode.jjtGetChild(0) instanceof ASTPrimitiveType) {
			ASTPrimitiveType primitivetype = (ASTPrimitiveType) typenode.jjtGetChild(0);
			String image = primitivetype.getImage();
			if (image.equals("char") || image.equals("byte") || image.equals("short") || image.equals("int") || image.equals("long")) {
				domain = ConvertDomain.DomainSwitch(domain, ClassType.INT);
				type = ClassType.INT;
			} else if (image.equals("float") || image.equals("double")) {
				domain = ConvertDomain.DomainSwitch(domain, ClassType.DOUBLE);
				type = ClassType.DOUBLE;
			}
		} else {
			throw new RuntimeException("This is not a legal CastExpression");
		}
		expdata.type = type;
		expdata.domain = domain;
		return null;
	}

	/** �������ʽ */
	public Object visit(ASTPrimaryExpression node, Object data) {
		DomainData expdata = (DomainData) data;
		if (node.jjtGetNumChildren() == 1) {
			// ������Щû�к�׺�ı��ʽ
			JavaNode javanode = (JavaNode) node.jjtGetChild(0);
			javanode.jjtAccept(this, expdata);
		} else {
			// expdata.domain=new ArbitraryDomain();
			// expdata.type=ClassType.ARBITRARY;
			Object firstdomain = new ArbitraryDomain();
			ClassType firsttype = ClassType.ARBITRARY;
			for (int i = 0; i < node.jjtGetNumChildren(); i++) {
				((JavaNode) node.jjtGetChild(i)).jjtAccept(this, expdata);
				/*ASTName namenode = (ASTName) ((SimpleJavaNode) node.jjtGetChild(i)).getSingleChildofType(ASTName.class);
				ASTPrimarySuffix suffixnode = (ASTPrimarySuffix) ((SimpleJavaNode) node.jjtGetChild(i)).getSingleChildofType(ASTPrimarySuffix.class);
				if ((namenode != null && namenode.getNameDeclaration() instanceof MethodNameDeclaration)
						|| (suffixnode != null && suffixnode.getNameDeclaration() instanceof MethodNameDeclaration)) {
					firstdomain = expdata.domain;
					firsttype = expdata.type;
				}*/
				ExpressionBase e=null;
				if(!(node.jjtGetChild(i) instanceof ExpressionBase)){
					continue;
				}
				e=(ExpressionBase)node.jjtGetChild(i);
				if(!(e.getType() instanceof Method)){
					continue;
				}
				firstdomain = expdata.domain;
				firsttype = expdata.type;
			}
			expdata.domain = firstdomain;
			expdata.type = firsttype;
		}
		return null;
	}

	/** ǰ׺ */
	public Object visit(ASTPrimaryPrefix node, Object data) {
		// ��ʱ���ø���
		DomainData expdata = (DomainData) data;
		expdata.domain = new ArbitraryDomain();
		expdata.type = ClassType.ARBITRARY;
		if(node.usesThisModifier()){
			expdata.domain = new ReferenceDomain(ReferenceValue.NOTNULL);
			expdata.type = ClassType.REF;
		}
		return super.visit(node, expdata);
	}

	/** ��׺ */
	public Object visit(ASTPrimarySuffix node, Object data) {
		DomainData expdata = (DomainData) data;
		VexNode currentVex = expdata.getCurrentVex();
		if (currentVex == null) {
			currentVex = node.getCurrentVexNode();
		}
		
		node.childrenAccept(this, expdata);

		expdata.domain = new ArbitraryDomain();
		expdata.type = ClassType.ARBITRARY;

		String image = node.getImage();
		// ���������[].length���
		if (!node.isArguments() && !node.isArrayDereference() && image != null && image.equals("length")
				&& (node.jjtGetParent().jjtGetChild(node.jjtGetParent().jjtGetNumChildren() - 1) == node)) {
			if (node.jjtGetParent() instanceof ASTPrimaryExpression) {
				ASTPrimaryExpression pe = (ASTPrimaryExpression) node.jjtGetParent();
				ASTName name = (ASTName) ((SimpleJavaNode) pe.jjtGetChild(0)).getSingleChildofType(ASTName.class);
				String nameimage = null;
				if (name != null) {
					nameimage = name.getImage();
				}
				if (nameimage == null || nameimage.contains(".")) {
					return null;
				}
				if (name != null && (name.getNameDeclaration() instanceof VariableNameDeclaration)) {
					VariableNameDeclaration v = (VariableNameDeclaration) name.getNameDeclaration();
					Object leftdomain = node.findCurrentDomain(v, currentVex);
					ClassType lefttype = DomainSet.getDomainType(leftdomain);
					if (lefttype == ClassType.ARRAY) {
						ArrayDomain arraydomain = (ArrayDomain) leftdomain;
						for (int i = 1; i < node.jjtGetParent().jjtGetNumChildren() - 1; i++) {
							Node n = node.jjtGetParent().jjtGetChild(i);
							if (n instanceof ASTPrimarySuffix) {
								ASTPrimarySuffix temp = (ASTPrimarySuffix) n;
								if (!temp.isArrayDereference()) {
									return null;
								}
							}
						}

						expdata.domain=arraydomain.getDimension(node.jjtGetParent().jjtGetNumChildren() - 2);
						expdata.type=ClassType.INT;
					}
				}
			}
		}

		// ����this.i������ͺ�������
		if (!node.isArguments() && !node.isArrayDereference()) {
			if (node.jjtGetParent() instanceof ASTPrimaryExpression) {
				ASTPrimaryExpression pe = (ASTPrimaryExpression) node.jjtGetParent();
				if (pe.jjtGetNumChildren() >= 2 && pe.jjtGetChild(0) instanceof ASTPrimaryPrefix) {
					ASTPrimaryPrefix pr = (ASTPrimaryPrefix) pe.jjtGetChild(0);
					if (pr.usesThisModifier() && node.getNameDeclaration() instanceof VariableNameDeclaration) {
						VariableNameDeclaration v = (VariableNameDeclaration) node.getNameDeclaration();
						expdata.domain = node.findCurrentDomain(v, currentVex);
						expdata.type = DomainSet.getDomainType(expdata.domain);
					} else if (pr.usesThisModifier() && node.getNameDeclaration() instanceof MethodNameDeclaration) {
						MethodNameDeclaration m = (MethodNameDeclaration) node.getNameDeclaration();
						ASTMethodDeclaration method = (ASTMethodDeclaration) m.getMethodNameDeclaratorNode().jjtGetParent();
						if (method.getDomain() == null) {
							expdata.domain = method.getInitDomain();
							expdata.type = DomainSet.getDomainType(expdata.domain);
						} else {
							//2013-04-15 added to solve unknown add by dongyuk
							Domain  domain=(Domain)method.getDomain();
							if(domain instanceof ReferenceDomain){
								ReferenceDomain rd=(ReferenceDomain)domain;
								if(rd.getUnknown()){
									rd.setUnknown(false);
									rd.setValue(ReferenceValue.NULL_OR_NOTNULL);
								}
							}							
							expdata.domain = domain;
							expdata.type = DomainSet.getDomainType(expdata.domain);
						}
					}
				}
			}
		}
		if(node.getType() instanceof Method){
			Method method=(Method)node.getType();
			MethodNode mn=null;
			if(method!=null){
				mn=MethodNode.findMethodNode(method);
			}
			if(mn!=null){
				expdata.domain=mn.getDomain();
				expdata.type = DomainSet.getDomainType(expdata.domain);
			}
			return null;
		}
		
		return null;
	}

	/** �ַ������ַ������ֳ��� */
	public Object visit(ASTLiteral node, Object data) {
		// ����
		DomainData expdata = (DomainData) data;
		String image = node.getImage();

		if (node.jjtGetNumChildren() > 0) {
			// null �Ͳ������������
			node.childrenAccept(this, data);
			return null;
		}
		if (image.startsWith("\"")) {
			ReferenceDomain r = new ReferenceDomain(ReferenceValue.NOTNULL);
			expdata.type = ClassType.REF;
			expdata.domain = r;
		} else if (image.startsWith("\'")) {
			if (image.length() <= 2) {
				throw new RuntimeException("This is not a legal character");
			}
			int count = 1;
			int secondChar = image.charAt(count++);
			int nextChar = image.charAt(count++);
			char value = (char) secondChar;
			if (secondChar == '\\') {
				switch (nextChar) {
				case 'b':
					value = '\b';
					break;
				case 't':
					value = '\t';
					break;
				case 'n':
					value = '\n';
					break;
				case 'f':
					value = '\f';
					break;
				case 'r':
					value = '\r';
					break;
				case '\"':
					value = '\"';
					break;
				case '\'':
					value = '\'';
					break;
				case '\\':
					value = '\\';
					break;
				default: // octal (well-formed: ended by a ' )
					if ('0' <= nextChar && nextChar <= '7') {
						int number = nextChar - '0';
						if (count >= image.length()) {
							throw new RuntimeException("This is not a legal character");
						}
						nextChar = image.charAt(count);
						if (nextChar != '\'') {
							count++;
							if (!('0' <= nextChar && nextChar <= '7')) {
								throw new RuntimeException("This is not a legal character");
							}
							number = (number * 8) + nextChar - '0';
							if (count >= image.length()) {
								throw new RuntimeException("This is not a legal character");
							}
							nextChar = image.charAt(count);
							if (nextChar != '\'') {
								count++;
								if (!('0' <= nextChar && nextChar <= '7')) {
									throw new RuntimeException("This is not a legal character");
								}
								number = (number * 8) + nextChar - '0';
							}
						}
						value = (char) number;
					} else {
						throw new RuntimeException("This is not a legal character");
					}
				}
				if (count >= image.length()) {
					System.out.println("cnt:" + count + "  " + image + image.length());
					throw new RuntimeException("This is not a legal character");
				}
				nextChar = image.charAt(count++);
			}
			if (nextChar != '\'') {
				throw new RuntimeException("This is not a legal character");
			}
			IntegerDomain r = new IntegerDomain(value, value, false, false);
			expdata.type = ClassType.INT;
			expdata.domain = r;
		} else {
			boolean isInteger = false;
			if (image.endsWith("l") || image.endsWith("L")) {
				image = image.substring(0, image.length() - 1);
			}
			char[] source = image.toCharArray();
			int length = source.length;
			int intValue = 0;
			long computeValue = 0L;
			double doubleValue = 0;
			try {
				if (source[0] == '0') {
					if (length == 1) {
						computeValue = 0;
					} else {
						final int shift, radix;
						int j;
						if ((source[1] == 'x') || (source[1] == 'X')) {
							shift = 4;
							j = 2;
							radix = 16;
						} else {
							shift = 3;
							j = 1;
							radix = 8;
						}
						while (source[j] == '0') {
							j++; // jump over redondant zero
							if (j == length) { // watch for 000000000000000000
								computeValue = 0;
								break;
							}
						}
						while (j < length) {
							int digitValue = 0;
							if (radix == 8) {
								if ('0' <= source[j] && source[j] <= '7') {
									digitValue = source[j++] - '0';
								} else {
									throw new RuntimeException("This is not a legal integer");
								}
							} else {
								if ('0' <= source[j] && source[j] <= '9') {
									digitValue = source[j++] - '0';
								} else if ('a' <= source[j] && source[j] <= 'f') {
									digitValue = source[j++] - 'a' + 10;
								} else if ('A' <= source[j] && source[j] <= 'F') {
									digitValue = source[j++] - 'A' + 10;
								} else {
									throw new RuntimeException("This is not a legal integer");
								}
							}
							computeValue = (computeValue << shift) | digitValue;

						}
					}
				} else { // -----------regular case : radix = 10-----------
					for (int i = 0; i < length; i++) {
						int digitValue;
						if ('0' <= source[i] && source[i] <= '9') {
							digitValue = source[i] - '0';
						} else {
							throw new RuntimeException("This is not a legal integer");
						}
						computeValue = 10 * computeValue + digitValue;
					}
				}
				intValue = (int) computeValue;
				isInteger = true;
			} catch (RuntimeException e) {
			}
			if (isInteger) {
				IntegerDomain r = new IntegerDomain(intValue, intValue, false, false);
				expdata.type = ClassType.INT;
				expdata.domain = r;
			} else {
				doubleValue = Double.valueOf(image);
				DoubleDomain r = new DoubleDomain(doubleValue, doubleValue, false, false);
				expdata.type = ClassType.DOUBLE;
				expdata.domain = r;
			}
		}
		return null;
	}

	/** �������� */
	public Object visit(ASTBooleanLiteral node, Object data) {
		// true false
		DomainData expdata = (DomainData) data;
		BooleanDomain r = new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
		if (node.isTrue()) {
			r.setValue(BooleanValue.TRUE);
		} else {
			r.setValue(BooleanValue.FALSE);
		}
		expdata.type = ClassType.BOOLEAN;
		expdata.domain = r;
		return null;
	}

	/** null���� */
	public Object visit(ASTNullLiteral node, Object data) {
		// null
		DomainData expdata = (DomainData) data;
		ReferenceDomain r = new ReferenceDomain(ReferenceValue.NULL);
		expdata.type = ClassType.REF;
		expdata.domain = r;
		return null;
	}

	/** ���� */
	public Object visit(ASTArguments node, Object data) {
		DomainData expdata = (DomainData) data;

		node.childrenAccept(this, expdata);

		expdata.domain = new ArbitraryDomain();
		expdata.type = ClassType.ARBITRARY;
		return null;
	}

	/** �����б� */
	public Object visit(ASTArgumentList node, Object data) {
		DomainData expdata = (DomainData) data;

		/*for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			((JavaNode) node.jjtGetChild(i)).jjtAccept(this, expdata);
			if (expdata.type == ClassType.REF) {
				ReferenceDomain r = (ReferenceDomain) expdata.domain;
				if (expdata.firstcal&&(r.getValue() == ReferenceValue.NULL || (!r.getUnknown()&&r.getValue() == ReferenceValue.NULL_OR_NOTNULL&&softtest.config.java.Config.NESTPARAMMAYNULL))) {
					if (node.jjtGetParent() instanceof ASTArguments) {
						ASTArguments arguments = (ASTArguments) node.jjtGetParent();
						if (arguments.jjtGetParent() instanceof ASTPrimarySuffix) {
							ASTPrimarySuffix primarysuffix = (ASTPrimarySuffix) arguments.jjtGetParent();
							if (primarysuffix.jjtGetParent() instanceof ASTPrimaryExpression) {
								ASTPrimaryExpression primaryexpression = (ASTPrimaryExpression) primarysuffix.jjtGetParent();
								SimpleJavaNode javanode = (SimpleJavaNode) primaryexpression.jjtGetChild(0);
								ASTName name = (ASTName) javanode.getSingleChildofType(ASTName.class);
								if (name != null) {
									if (name.getNameDeclaration() instanceof MethodNameDeclaration) {
										String image = name.getImage();
										if (image != null && !image.contains(".")) {
											MethodNameDeclaration mdecl = (MethodNameDeclaration) name.getNameDeclaration();
											ASTMethodDeclarator tor = mdecl.getMethodNameDeclaratorNode();
											if (tor.jjtGetNumChildren() > 0 && tor.jjtGetChild(0) instanceof ASTFormalParameters) {
												ASTFormalParameters formalparameters = (ASTFormalParameters) tor.jjtGetChild(0);
												if (i < formalparameters.jjtGetNumChildren() && formalparameters.jjtGetChild(i) instanceof ASTFormalParameter) {
													ASTFormalParameter formalparameter = (ASTFormalParameter) formalparameters.jjtGetChild(i);
													ASTVariableDeclaratorId id = (ASTVariableDeclaratorId) formalparameter.jjtGetChild(formalparameter
															.jjtGetNumChildren() - 1);
													id.getNameDeclaration().setDomain(new ReferenceDomain(ReferenceValue.NULL_OR_NOTNULL));
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}*/
		node.childrenAccept(this, expdata);

		expdata.domain = new ArbitraryDomain();
		expdata.type = ClassType.ARBITRARY;
		return null;
	}

	/** ����new���ʽ */
	public Object visit(ASTAllocationExpression node, Object data) {
		DomainData expdata = (DomainData) data;
		List list = node.findDirectChildOfType(ASTArrayDimsAndInits.class);
		if (!list.isEmpty()) {
			// �������
			ASTArrayDimsAndInits arraydim = (ASTArrayDimsAndInits) list.get(0);
			ArrayDomain arraydomain = new ArrayDomain(arraydim.getArrayDepth());
			list = arraydim.findDirectChildOfType(ASTExpression.class);
			if (!list.isEmpty()) {
				// arraydomain = new ArrayDomain(list.size());
				ASTExpression exp = null;
				for (int i = 0; i < list.size(); i++) {
					exp = (ASTExpression) list.get(i);
					exp.jjtAccept(this, expdata);
					IntegerDomain idomain= (IntegerDomain)ConvertDomain.DomainSwitch(expdata.domain, ClassType.INT);
					arraydomain.setDimension(i, idomain );
				}
			} else {
				// ����̬��ʼ��
				ASTArrayInitializer arrinit = (ASTArrayInitializer) arraydim.jjtGetChild(arraydim.jjtGetNumChildren() - 1);
				arrinit.calDims();
				ArrayList<Integer> dims = arrinit.getdims();
				// arraydomain = new ArrayDomain(dims.size());
				for (int i = 0; i < dims.size(); i++) {
					arraydomain.setDimension(i, new IntegerDomain(dims.get(i),dims.get(i),false,false));
				}
			}
			expdata.domain = arraydomain;
			expdata.type = ClassType.ARRAY;
		} else {
			// ��ͨ�������
			expdata.domain = new ReferenceDomain(ReferenceValue.NOTNULL);
			expdata.type = ClassType.REF;
		}
		return null;
	}

	/** ��������� */
	public Object visit(ASTName node, Object data) {
		DomainData expdata = (DomainData) data;
		VexNode currentVex = expdata.getCurrentVex();
		if (currentVex == null) {
			currentVex = node.getCurrentVexNode();
		}
		//�����м���.�ŵ�����	
		/*if (!(node.getNameDeclaration() instanceof VariableNameDeclaration)) {
			expdata.domain = new ArbitraryDomain();
			expdata.type = ClassType.ARBITRARY;
			if ((node.getNameDeclaration() instanceof MethodNameDeclaration)&&!node.getImage().contains(".")) {
				MethodNameDeclaration mdecl = (MethodNameDeclaration) node.getNameDeclaration();
				ASTMethodDeclaration method = (ASTMethodDeclaration) mdecl.getMethodNameDeclaratorNode().jjtGetParent();
				if (method.getDomain() == null) {
					expdata.domain = method.getInitDomain();
					expdata.type = DomainSet.getDomainType(expdata.domain);
				} else {
					expdata.domain = method.getDomain();
					expdata.type = DomainSet.getDomainType(expdata.domain);
				}
			}
			return null;
		}*/
		if(node.getType() instanceof Method){
			Method method=(Method)node.getType();
			MethodNode mn=MethodNode.findMethodNode(method);
			if(mn!=null){
				expdata.domain=mn.getDomain();
				expdata.type = DomainSet.getDomainType(expdata.domain);
			}else if(node.getImage()!=null&&node.getImage().endsWith(".readLine")){
				expdata.domain = new ReferenceDomain(ReferenceValue.NULL_OR_NOTNULL);
				expdata.type = ClassType.REF;
			}else{
				expdata.domain = new ArbitraryDomain();
				expdata.type = ClassType.ARBITRARY;
			}
			return null;
		}
		if (!(node.getNameDeclaration() instanceof VariableNameDeclaration)) {
			expdata.domain = new ArbitraryDomain();
			expdata.type = ClassType.ARBITRARY;
			if (node.getNameDeclaration() instanceof MethodNameDeclaration) {
				if(!node.getImage().contains(".")){
					MethodNameDeclaration mdecl = (MethodNameDeclaration) node.getNameDeclaration();
					ASTMethodDeclaration method = (ASTMethodDeclaration) mdecl.getMethodNameDeclaratorNode().jjtGetParent();
					if (method.getDomain() == null) {
						expdata.domain = method.getInitDomain();
						expdata.type = DomainSet.getDomainType(expdata.domain);
					} else {
						expdata.domain = method.getDomain();
						expdata.type = DomainSet.getDomainType(expdata.domain);
					}
				}
			}else if(node.getImage().endsWith(".readLine")){
				expdata.domain = new ReferenceDomain(ReferenceValue.NULL_OR_NOTNULL);
				expdata.type = ClassType.REF;
			}else{
				expdata.domain = new ArbitraryDomain();
				expdata.type = ClassType.ARBITRARY;
			}
			return null;
		}
		
		String image = node.getImage();
		if (image.contains(".")) {
			String[] ss = image.split("\\.");
			// name.length�����
			if (ss.length == 2 && ss[1].equals("length")) {
				VariableNameDeclaration v = (VariableNameDeclaration) node.getNameDeclaration();
				Object domain = node.findCurrentDomain(v, currentVex);
				if (DomainSet.getDomainType(domain) == ClassType.ARRAY) {
					ArrayDomain arraydomain = (ArrayDomain) domain;

					expdata.domain = arraydomain.getDimension(0);
					expdata.type = ClassType.INT;
					return null;
				}
			}
			
			//��unkown�����ñ�������. ���ú�ת��Ϊnotnull
			/*VariableNameDeclaration v = (VariableNameDeclaration) node.getNameDeclaration();
			Object d=node.findCurrentDomain(v);
			if(d!=null&&DomainSet.getDomainType(d)==ClassType.REF){
				ReferenceDomain r=(ReferenceDomain)d;
				if(r.getUnknown()){
					VexNode vex = node.getCurrentVexNode();
					if (expdata.sideeffect) {
						vex.addDomain(v, r);
					}
				}
			}*/
			
			expdata.domain = new ArbitraryDomain();
			expdata.type = ClassType.ARBITRARY;
			return null;
		}		
	
		VariableNameDeclaration v = (VariableNameDeclaration) node.getNameDeclaration();
		expdata.domain = node.findCurrentDomain(v, currentVex);
		if (expdata.domain != null) {
			expdata.type = DomainSet.getDomainType(expdata.domain);
		}
		return null;
	}

	@Override
	public Object visit(ExpressionBase node, Object data) {
		if (node.getType() == null || node.getType() instanceof Class) {
			return super.visit(node, data);
		}
		//������ժҪ�е���������������������õĸ����ã��������Ļ������޸�
		if (node.getType() instanceof Method && node instanceof ASTPrimaryPrefix) {
			ASTPrimaryPrefix pr=(ASTPrimaryPrefix)node;
			ASTName name=(ASTName)pr.getSingleChildofType(ASTName.class);
			if(name==null){
				return super.visit(node, data);
			}
			if(name.getImage()!=null&&name.getImage().contains(".")){
				return super.visit(node, data);
			}
			
			DomainData expdata = (DomainData) data;
			Object o=node.getType();
			MethodNode methodnode=MethodNode.findMethodNode(o);
			if(methodnode==null){
				return super.visit(node, data);
			}
			MethodSummary summary = methodnode.getMethodsummary();
			if (summary == null) {
				return super.visit(node, data);
			}
			
			for (AbstractPostcondition post: summary.getPostconditons().getTable().values()) {
				if (!(post instanceof DomainPostcondition)) {
					continue;
				}
				DomainPostcondition domainpost=(DomainPostcondition)post;
				if (expdata.sideeffect) {
					DomainData exprdata = (DomainData) data;
					VexNode vex = exprdata.getCurrentVex();
					if (vex == null) {
						vex = node.getCurrentVexNode();
					}
					VariableNameDeclaration v = null;
					Object domain = null;
					Set entryset = domainpost.getTable().entrySet();
					Iterator i = entryset.iterator();
					while (i.hasNext()) {
						Map.Entry e = (Map.Entry) i.next();
						v = ((MapOfVariable) e.getKey()).findVariable(node);
						domain = e.getValue();
						if(v!=null){
							vex.addDomain(v, domain);
						}
					}
				}
			}
		}
		return super.visit(node, data);
	}
}
