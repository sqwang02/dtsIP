package softtest.IntervalAnalysis.java;

import java.util.*;

import softtest.ast.java.*;
import softtest.symboltable.java.*;
import softtest.ast.java.ASTAssertStatement;
import softtest.ast.java.ASTExpression;
import softtest.ast.java.ASTForEachVariableDeclaration;
import softtest.ast.java.ASTForInit;
import softtest.ast.java.ASTForStatement;
import softtest.ast.java.ASTForUpdate;
import softtest.ast.java.ASTIfStatement;
import softtest.ast.java.ASTLocalVariableDeclaration;
import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTReturnStatement;
import softtest.ast.java.ASTStatementExpression;
import softtest.ast.java.ASTSwitchLabel;
import softtest.ast.java.ASTSwitchStatement;
import softtest.ast.java.ASTSynchronizedStatement;
import softtest.ast.java.ASTWhileStatement;
import softtest.ast.java.JavaNode;
import softtest.ast.java.SimpleJavaNode;
import softtest.ast.java.SimpleNode;
import softtest.cfg.java.*;
import softtest.config.java.*;
import softtest.domain.java.*;
import softtest.symboltable.java.VariableNameDeclaration;

/** 用于区间运算的控制流图访问者 */
public class DomainVexVisitor implements GraphVisitor {
	
	private void calculateIN(VexNode n ,Object data){
		if (n.getDomainSet() != null) {
			n.getDomainSet().clearDomainSet();
		}

		List<Edge> list = new ArrayList<Edge>();
		for (Enumeration<Edge> e = n.getInedges().elements(); e.hasMoreElements();) {
			list.add(e.nextElement());
		}
		Collections.sort(list);

		Iterator<Edge> iter = list.iterator();
		while (iter.hasNext()) {
			Edge edge = iter.next();
			VexNode pre = edge.getTailNode();
			DomainSet domainset = pre.getDomainSet();

			//判断分支是否矛盾
			if (edge.getContradict()) {
				continue;
			}

			//如果前驱节没有访问过则跳过
			if (!pre.getVisited()) {
				continue;
			}

			if (edge.getName().startsWith("T")) {
				ConditionData condata = pre.getConditionData();
				if (condata != null) {
					DomainSet ds = condata.getTrueMayDomainSet();
					//对循环条件进行特殊处理,考虑循环多次的累积效果
					if (pre.getName().startsWith("while_head") || pre.getName().startsWith("for_head") || pre.getName().startsWith("do_while_out1")) {
						domainset=DomainSet.intersect(domainset, ds);
						if(!domainset.isContradict()){
							domainset=DomainSet.join(domainset, ds);
						}
					} else {
						domainset=DomainSet.intersect(domainset, ds);
					}
					if(domainset.isContradict()){
						edge.setContradict(true);
					}else{
						n.mergeDomainSet(domainset);
					}	
				} else {
					n.mergeDomainSet(domainset);
				}
			} else if (edge.getName().startsWith("F")) {
				//处理一次循环
				if(n.getName().startsWith("while_out")||n.getName().startsWith("for_out")){
					visit(pre,data);
					domainset=pre.getDomainSet();
				}
				
				ConditionData condata = pre.getConditionData();
				if (condata != null) {
					DomainSet ds =null;
					
					ds = condata.getFalseMayDomainSet(pre);
					// 对循环条件进行特殊处理,考虑循环多次的累积效果
					if (pre.getName().startsWith("while_head") || pre.getName().startsWith("for_head") || pre.getName().startsWith("do_while_out1")) {
						DomainSet old=pre.getDomainSet();
						pre.setDomainSet(null);
						ds=condata.getFalseMayDomainSet(pre);
						pre.setDomainSet(old);
						
						domainset=DomainSet.join(DomainSet.intersect(domainset, ds), ds);
					} else {
						domainset=DomainSet.intersect(domainset, ds);
					}
					if(domainset.isContradict()){
						edge.setContradict(true);
					}else{
						n.mergeDomainSet(domainset);
					}	
				} else {
					n.mergeDomainSet(domainset);
				}
			} else if (n.getTreeNode() instanceof ASTSwitchLabel && pre.getTreeNode() instanceof ASTSwitchStatement) {
				DomainData exprdata = new DomainData(n);
				exprdata.sideeffect = false;
				SimpleNode expnode = (SimpleNode) pre.getTreeNode().jjtGetChild(0);
				ASTName name = (ASTName) (expnode.getSingleChildofType(ASTName.class));
				ASTSwitchLabel label = (ASTSwitchLabel) n.getTreeNode();
				String nameimage = null;
				if (name != null) {
					nameimage = name.getImage();
				}
				if (name != null && (name.getNameDeclaration() instanceof VariableNameDeclaration) && !label.isDefault() && nameimage != null
						&& !nameimage.contains(".")) {
					ExpressionDomainVisitor exprvisitor = new ExpressionDomainVisitor();
					((SimpleJavaNode) label.jjtGetChild(0)).jjtAccept(exprvisitor, exprdata);
					VariableNameDeclaration v = (VariableNameDeclaration) name.getNameDeclaration();
					DomainSet ds = new DomainSet();
					ds.addDomain(v, exprdata.domain);
					if (exprdata.type == ClassType.INT&& DomainSet.getDomainType(v.getDomain())==ClassType.INT) {
						ds = DomainSet.intersect(domainset, ds);
						if (!ds.isContradict()) {
							n.mergeDomainSet(ds);
						} else {
							edge.setContradict(true);
						}
					} else {
						n.mergeDomainSet(domainset);
					}
				} else {
					n.mergeDomainSet(domainset);
				}
			} else {
				n.mergeDomainSet(domainset);
			}
		}
				
		boolean b = !n.getInedges().isEmpty();
		
		iter = list.iterator();
		while (iter.hasNext()) {
			Edge edge = iter.next();
			if (n.getName().startsWith("while_head") || n.getName().startsWith("for_head")||n.getName().startsWith("do_while_head")) {
				//对于for和While语句 do-while语句只检查第一条入边
				if(edge.getContradict()){
					break;
				}
			}
			if (!edge.getContradict()) {
				b = false;
				break;
			}
		}

		//该节点矛盾，不可达,设置所有出边的矛盾标志
		if (b || (n.getDomainSet() != null && n.getDomainSet().isContradict())) {
			n.setContradict(true);
			for (Enumeration<Edge> e = n.getOutedges().elements(); e.hasMoreElements();) {
				Edge edge = e.nextElement();
				edge.setContradict(true);
			}
			return;
		}
	}
	private void calculateOUT(VexNode n ,Object data){
		// 计算新的值
		SimpleJavaNode treenode = n.getTreeNode();
		if (/*treenode.getFirstVexNode() == n*/!n.isBackNode()) {
			// 确定是该语法树节点对应的第一个控制流节点
			DomainData exprdata = new DomainData(n);
			exprdata.firstcal=true;
			ExpressionDomainVisitor exprvisitor = new ExpressionDomainVisitor();
			ConditionData condata = new ConditionData(n);
			ConditionDomainVisitor convisitor = new ConditionDomainVisitor();
			// 条件分支可能：if do while for switch

			if (treenode instanceof ASTIfStatement) {
				((JavaNode) treenode.jjtGetChild(0)).jjtAccept(exprvisitor, exprdata);
				((JavaNode) treenode.jjtGetChild(0)).jjtAccept(convisitor, condata);
				n.setConditionData(condata);
			} else if (treenode instanceof ASTWhileStatement) {
				((JavaNode) treenode.jjtGetChild(0)).jjtAccept(exprvisitor, exprdata);
				//对循环条件进行特殊处理,考虑循环多次的累积效果
				condata=ConditionData.calLoopCondtion(condata,n,convisitor,((SimpleJavaNode) treenode.jjtGetChild(0)));
				n.setConditionData(condata);
			} else if (treenode instanceof ASTLocalVariableDeclaration) {
				treenode.jjtAccept(exprvisitor, exprdata);
			} else if (treenode instanceof ASTSwitchStatement) {
				((JavaNode) treenode.jjtGetChild(0)).jjtAccept(exprvisitor, exprdata);
			} else if (treenode instanceof ASTExpression) {
				// ASTDoStatement,
				// 在控制流图处理中，已经处理了，该语法树节点已经设置为表达式节点了
				treenode.jjtAccept(exprvisitor, exprdata);
				//对循环条件进行特殊处理,考虑循环多次的累积效果
				condata=ConditionData.calLoopCondtion(condata,n,convisitor,treenode);
				n.setConditionData(condata);
			} else if (treenode instanceof ASTForInit) {
				treenode.jjtAccept(exprvisitor, exprdata);
			} else if (treenode instanceof ASTForStatement) {
				List results = treenode.findDirectChildOfType(ASTExpression.class);
				if (!results.isEmpty()) {
					((JavaNode) results.get(0)).jjtAccept(exprvisitor, exprdata);
					if (!(treenode.jjtGetChild(0) instanceof ASTForEachVariableDeclaration)) {
						// 过滤for-each
						//对循环条件进行特殊处理,考虑循环多次的累积效果
						condata=ConditionData.calLoopCondtion(condata,n,convisitor,((SimpleJavaNode) results.get(0)));
						n.setConditionData(condata);
					}
				}
			} else if (treenode instanceof ASTForUpdate) {
				treenode.jjtAccept(exprvisitor, exprdata);
			} else if (treenode instanceof ASTReturnStatement) {
				treenode.jjtAccept(exprvisitor, exprdata);
			} else if (treenode instanceof ASTAssertStatement) {
				treenode.jjtAccept(exprvisitor, exprdata);
			} else if (treenode instanceof ASTSynchronizedStatement) {
				((JavaNode) treenode.jjtGetChild(0)).jjtAccept(exprvisitor, exprdata);
			} else if (treenode instanceof ASTStatementExpression) {
				treenode.jjtAccept(exprvisitor, exprdata);
			}//还有lable，它空处理就行了
		} else if (treenode instanceof ASTMethodDeclaration) {
			ASTMethodDeclaration method = (ASTMethodDeclaration) treenode;
			if(method.getDomain()==null && method.getDomainType()==ClassType.ARBITRARY){
				method.setDomain(new ArbitraryDomain());
			}
			//函数出口节点
			for (Enumeration<Edge> e = n.getInedges().elements(); e.hasMoreElements();) {
				Edge tempedge = e.nextElement();
				VexNode temppre = tempedge.getTailNode();
				if (tempedge.getContradict()) {
					continue;
				}
				if (temppre.getTreeNode() instanceof ASTReturnStatement) {
					ASTReturnStatement resultnode = (ASTReturnStatement) temppre.getTreeNode();
					if (resultnode.jjtGetNumChildren() > 0) {
						DomainData exprdata = new DomainData(temppre);
						exprdata.sideeffect=false;
						ExpressionDomainVisitor exprvisitor = new ExpressionDomainVisitor();
						((JavaNode) resultnode.jjtGetChild(0)).jjtAccept(exprvisitor, exprdata);
						if (method.getDomain() == null) {
							method.setDomain(ConvertDomain.DomainSwitch(exprdata.domain, method.getDomainType()));
						} else {
							Object d1 = method.getDomain();
							Object d2 = ConvertDomain.DomainSwitch(exprdata.domain,method.getDomainType());
							switch (method.getDomainType()) {
							case ARBITRARY:
								break;
							case BOOLEAN:
								method.setDomain(BooleanDomain.union((BooleanDomain) d1, (BooleanDomain) d2));
								break;
							case REF:
								method.setDomain(ReferenceDomain.union((ReferenceDomain) d1, (ReferenceDomain) d2));
								break;
							case INT:
								method.setDomain(IntegerDomain.union((IntegerDomain) d1, (IntegerDomain) d2));
								break;
							case DOUBLE:
								method.setDomain(DoubleDomain.union((DoubleDomain) d1, (DoubleDomain) d2));
								break;
							case ARRAY:
								method.setDomain(ArrayDomain.union((ArrayDomain) d1, (ArrayDomain) d2));
								break;
							default:
								throw new RuntimeException("do not know the type of Variable");
							}
						}
					}
				}
			}
		}

		if (n.getDomainSet() != null && n.getDomainSet().isEmpty()) {
			n.setDomainSet(null);
		}
	}
	/** 对节点进行访问 */
	public void visit(VexNode n, Object data) {
		// 由前驱节点的domain求当前节点domain
		n.setVisited(true);
		
		calculateIN(n,data);
		//n.removeRedundantDomain();
		
		if(n.getDomainSet()==null){
			n.setLastDomainSet(null);
		}else{
			n.setLastDomainSet(new DomainSet(n.getDomainSet()));
		}	
		if(!n.getContradict()){
			calculateOUT(n,data);	
			//n.removeRedundantDomain();
		}
	}

	/** 对边进行访问 */
	public void visit(Edge e, Object data) {

	}

	/** 对图进行访问 */
	public void visit(Graph g, Object data) {

	}
	
	public void logCin(String str) {
		logc("calculateIN(..) - " + str);
	}
	public void logCout(String str) {
		logc("calculateOUT(..) - " + str);
	}

	public void logc(String str) {
		if( Config.DEBUG ) {
			System.out.println("DomainVexVisitor::" + str);
		}
	}
}
