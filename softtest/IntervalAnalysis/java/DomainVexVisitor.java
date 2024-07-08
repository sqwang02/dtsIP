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

/** ������������Ŀ�����ͼ������ */
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

			//�жϷ�֧�Ƿ�ì��
			if (edge.getContradict()) {
				continue;
			}

			//���ǰ����û�з��ʹ�������
			if (!pre.getVisited()) {
				continue;
			}

			if (edge.getName().startsWith("T")) {
				ConditionData condata = pre.getConditionData();
				if (condata != null) {
					DomainSet ds = condata.getTrueMayDomainSet();
					//��ѭ�������������⴦��,����ѭ����ε��ۻ�Ч��
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
				//����һ��ѭ��
				if(n.getName().startsWith("while_out")||n.getName().startsWith("for_out")){
					visit(pre,data);
					domainset=pre.getDomainSet();
				}
				
				ConditionData condata = pre.getConditionData();
				if (condata != null) {
					DomainSet ds =null;
					
					ds = condata.getFalseMayDomainSet(pre);
					// ��ѭ�������������⴦��,����ѭ����ε��ۻ�Ч��
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
				//����for��While��� do-while���ֻ����һ�����
				if(edge.getContradict()){
					break;
				}
			}
			if (!edge.getContradict()) {
				b = false;
				break;
			}
		}

		//�ýڵ�ì�ܣ����ɴ�,�������г��ߵ�ì�ܱ�־
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
		// �����µ�ֵ
		SimpleJavaNode treenode = n.getTreeNode();
		if (/*treenode.getFirstVexNode() == n*/!n.isBackNode()) {
			// ȷ���Ǹ��﷨���ڵ��Ӧ�ĵ�һ���������ڵ�
			DomainData exprdata = new DomainData(n);
			exprdata.firstcal=true;
			ExpressionDomainVisitor exprvisitor = new ExpressionDomainVisitor();
			ConditionData condata = new ConditionData(n);
			ConditionDomainVisitor convisitor = new ConditionDomainVisitor();
			// ������֧���ܣ�if do while for switch

			if (treenode instanceof ASTIfStatement) {
				((JavaNode) treenode.jjtGetChild(0)).jjtAccept(exprvisitor, exprdata);
				((JavaNode) treenode.jjtGetChild(0)).jjtAccept(convisitor, condata);
				n.setConditionData(condata);
			} else if (treenode instanceof ASTWhileStatement) {
				((JavaNode) treenode.jjtGetChild(0)).jjtAccept(exprvisitor, exprdata);
				//��ѭ�������������⴦��,����ѭ����ε��ۻ�Ч��
				condata=ConditionData.calLoopCondtion(condata,n,convisitor,((SimpleJavaNode) treenode.jjtGetChild(0)));
				n.setConditionData(condata);
			} else if (treenode instanceof ASTLocalVariableDeclaration) {
				treenode.jjtAccept(exprvisitor, exprdata);
			} else if (treenode instanceof ASTSwitchStatement) {
				((JavaNode) treenode.jjtGetChild(0)).jjtAccept(exprvisitor, exprdata);
			} else if (treenode instanceof ASTExpression) {
				// ASTDoStatement,
				// �ڿ�����ͼ�����У��Ѿ������ˣ����﷨���ڵ��Ѿ�����Ϊ���ʽ�ڵ���
				treenode.jjtAccept(exprvisitor, exprdata);
				//��ѭ�������������⴦��,����ѭ����ε��ۻ�Ч��
				condata=ConditionData.calLoopCondtion(condata,n,convisitor,treenode);
				n.setConditionData(condata);
			} else if (treenode instanceof ASTForInit) {
				treenode.jjtAccept(exprvisitor, exprdata);
			} else if (treenode instanceof ASTForStatement) {
				List results = treenode.findDirectChildOfType(ASTExpression.class);
				if (!results.isEmpty()) {
					((JavaNode) results.get(0)).jjtAccept(exprvisitor, exprdata);
					if (!(treenode.jjtGetChild(0) instanceof ASTForEachVariableDeclaration)) {
						// ����for-each
						//��ѭ�������������⴦��,����ѭ����ε��ۻ�Ч��
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
			}//����lable�����մ��������
		} else if (treenode instanceof ASTMethodDeclaration) {
			ASTMethodDeclaration method = (ASTMethodDeclaration) treenode;
			if(method.getDomain()==null && method.getDomainType()==ClassType.ARBITRARY){
				method.setDomain(new ArbitraryDomain());
			}
			//�������ڽڵ�
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
	/** �Խڵ���з��� */
	public void visit(VexNode n, Object data) {
		// ��ǰ���ڵ��domain��ǰ�ڵ�domain
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

	/** �Ա߽��з��� */
	public void visit(Edge e, Object data) {

	}

	/** ��ͼ���з��� */
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
