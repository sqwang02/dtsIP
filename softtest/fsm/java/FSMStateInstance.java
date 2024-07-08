package softtest.fsm.java;

//import softtest.domain.java.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import softtest.ast.java.*;
import softtest.IntervalAnalysis.java.*;
import softtest.ast.java.ASTAssertStatement;
import softtest.ast.java.ASTExpression;
import softtest.ast.java.ASTForEachVariableDeclaration;
import softtest.ast.java.ASTForInit;
import softtest.ast.java.ASTForStatement;
import softtest.ast.java.ASTForUpdate;
import softtest.ast.java.ASTIfStatement;
import softtest.ast.java.ASTLocalVariableDeclaration;
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
import softtest.fsmanalysis.java.ProjectAnalysis;
import softtest.symboltable.java.VariableNameDeclaration;

/** ״̬ʵ�� */
public class FSMStateInstance {
	/** ״̬ת���п������ڵ� */
	private List<VexNode> vexNodes = null;
	
	/** ״̬ת���д������� */
	private List<String> switchPoints = null;
	
	/** ��ǰ״̬ */
	private FSMState state = null;

	/** ��ǰ״̬���������������� */
	private DomainSet domainset = new DomainSet();

	/** ��ָ��״̬����״̬ʵ�� */
	public FSMStateInstance(FSMState state) {
		this.state = state;
	}

	/** ������״̬�Ŀ������ڵ� */
	private VexNode sitenode = null;
	
	public String getPreconditions() {
		if (switchPoints == null || switchPoints.size() == 0) {
			return "";
		}
		StringBuffer sb = new StringBuffer();
		for (int i = switchPoints.size()-1 ; i >= 0 ; --i) {
			sb.append(switchPoints.get(i).replace('\n', ' ').replace('\t', ' ').replace("  ", " "));
			sb.append('\n');
		}
		return sb.toString();
	}
	
	public String getPreconditionsByVex() {
		if (this.getState().getFSMMachine().isPathSensitive()){
			this.clearSwitchPoint();
			
			// �ÿ������ڵ���������Ϊ״̬ת������ 
			for (VexNode n : this.vexNodes) {
				if (n.isBackNode()) {
					this.addSwitchPoint("\""+n.getTreeNode().printNode(ProjectAnalysis.getCurrent_file(),n.getTreeNode().getEndLine())+"\" at Line:"+n.getTreeNode().getEndLine()+" File:"+ProjectAnalysis.getCurrent_file());
				}
				else {
					this.addSwitchPoint("\""+n.getTreeNode().printNode(ProjectAnalysis.getCurrent_file())+"\" at Line:"+n.getTreeNode().getBeginLine()+" File:"+ProjectAnalysis.getCurrent_file());
				}
			}
		}
		
		return getPreconditions();
	}
	//fsminstance��״̬ת����state��ʵ��: ����state���¹���һ��״̬ʵ������������Ӧֵ�������¹����״̬ʵ��
	public FSMStateInstance transferTo(FSMState state, FSMMachineInstance fsminstance, VexNode n) {
		FSMStateInstance newstateinstance = new FSMStateInstance(state);
		newstateinstance.setDomainSet(this.getDomainSet());
		newstateinstance.setSiteNode(n);
		newstateinstance.setSwitchPoints(this.getSwitchPoints());
		newstateinstance.setVexNodes(this.getVexNodes());
		
		// ��·�����У�n����Ϊnull�������func_in_xx�����Բ����
		if (fsminstance.getFSMMachine().isPathSensitive()) {
			newstateinstance.addVexNode(n);
		}
		
		// StateMachine.java�Զ����״̬ת����������
		if (fsminstance.getSwitchLine() != null && fsminstance.getSwitchLine().length() > 0) {
			newstateinstance.addSwitchPoint(fsminstance.getSwitchLine());
		}
		// �õ�ǰ�������ڵ���������Ϊ״̬ת������ 
		/*else if (fsminstance.getFSMMachine().isPathSensitive()){
			if (n.isBackNode()) {
				newstateinstance.addSwitchPoint("\""+n.getTreeNode().printNode(ProjectAnalysis.getCurrent_file(),n.getTreeNode().getEndLine())+"\" at Line:"+n.getTreeNode().getEndLine()+" File:"+ProjectAnalysis.getCurrent_file());
			}
			else {
				newstateinstance.addSwitchPoint("\""+n.getTreeNode().printNode(ProjectAnalysis.getCurrent_file())+"\" at Line:"+n.getTreeNode().getBeginLine()+" File:"+ProjectAnalysis.getCurrent_file());
			}
		}*/
		fsminstance.setSwitchLine(null);
		return newstateinstance;
	}

	/** ���ò�����״̬�Ŀ������ڵ� */
	public void setSiteNode(VexNode sitenode) {
		this.sitenode = sitenode;
	}

	/** ��ò�����״̬�Ŀ������ڵ� */
	public VexNode getVexNode() {
		return sitenode;
	}
	
	/** ���һ��״̬ת���������ڵ� */
	public void addVexNode(VexNode v) {
		if (v == null) {
			return;
		}
		
		if (this.vexNodes == null) {
			this.vexNodes = new LinkedList<VexNode>();
		}
		this.vexNodes.add(v);
	}
	
	/** ���״̬ת���������ڵ� */
	public void clearVexNode() {
		if (this.vexNodes != null) {
			this.vexNodes.clear();
		}
	}

	/**
	 * @return the switchPoints
	 */
	public List<VexNode> getVexNodes() {
		return this.vexNodes;
	}

	/**
	 * @param vexNodes the switchPoints to set
	 */
	public void setVexNodes(List<VexNode> vexNodes) {
		if (vexNodes == null) {
			return;
		}
		if (this.vexNodes == null) {
			this.vexNodes = new LinkedList<VexNode>();
		}
		else {
			this.vexNodes.clear();
		}
		this.vexNodes.addAll(vexNodes);
	}
	
	/** ���һ��״̬ת������ */
	public void addSwitchPoint(String sp) {
		if (this.switchPoints == null) {
			this.switchPoints = new ArrayList<String>();
		}
		this.switchPoints.add(sp);
	}
	
	/** ���״̬ת������ */
	public void clearSwitchPoint() {
		if (this.switchPoints != null) {
			this.switchPoints.clear();
		}
	}

	/**
	 * @return the switchPoints
	 */
	public List<String> getSwitchPoints() {
		return switchPoints;
	}

	/**
	 * @param switchPoints the switchPoints to set
	 */
	public void setSwitchPoints(List<String> switchPoints) {
		if (switchPoints == null) {
			return;
		}
		if (this.switchPoints == null) {
			this.switchPoints = new ArrayList<String>();
		}
		else {
			this.switchPoints.clear();
		}
		this.switchPoints.addAll(switchPoints);
	}

	/** �������캯�� */
	public FSMStateInstance(FSMStateInstance instance) {
		this.state = instance.state;
		
		domainset = new DomainSet(instance.domainset);			
		// domainset = instance.domainset;
		this.sitenode = instance.sitenode;
		this.setSwitchPoints(instance.getSwitchPoints());
		this.setVexNodes(instance.getVexNodes());
	}

	/** ���õ�ǰ״̬ */
	public void setState(FSMState state) {
		this.state = state;
	}

	/** ��õ�ǰ״̬ */
	public FSMState getState() {
		return state;
	}

	/** ���õ�ǰ״̬���������������� */
	public void setDomainSet(DomainSet domainset) {
		this.domainset = domainset;
	}

	/** ��õ�ǰ״̬���������������� */
	public DomainSet getDomainSet() {
		return domainset;
	}

	/** ����Լ������ */
	public void addDomainSet(DomainSet set) {
		domainset = DomainSet.intersect(domainset, set);
	}

	/** ���ָ���״̬��ȣ�����Ϊ��� */
	@Override
	public boolean equals(Object o) {
		if(softtest.config.java.Config.PATH_SENSITIVE==2){
			return super.equals(o);
		}
		if ((o == null) || !(o instanceof FSMStateInstance)) {
			return false;
		}
		if (this == o) {
			return true;
		}
		FSMStateInstance x = (FSMStateInstance) o;
		if (state == x.state) {
			return true;
		}
		return false;
	}

	/** ����hash���key����Ҫ��ֵ֤�����hashcode��� */
	@Override
	public int hashCode() {
		if(softtest.config.java.Config.PATH_SENSITIVE==2){
			return super.hashCode();
		}else{
			return state.hashCode();
		}
	}

	/** ��ӡ */
	@Override
	public String toString() {
		return state.getName() + "(" + domainset + ")";
	}

	/** ����caselabel�ڵ��switch�ڵ㣬����״̬��״̬���� */
	public void calSwitch(VexNode n, VexNode pre) {
		if (!(n.getTreeNode() instanceof ASTSwitchLabel && pre.getTreeNode() instanceof ASTSwitchStatement)) {
			return;
		}
		// ������
		DomainSet oldset = pre.getDomainSet();

		// ����״̬�������м��㣬����״̬����
		pre.setDomainSet(domainset);

		DomainData exprdata = new DomainData(n);
		exprdata.sideeffect = false;
		SimpleNode expnode = (SimpleNode) pre.getTreeNode().jjtGetChild(0);
		ASTName name = (ASTName) (expnode.getSingleChildofType(ASTName.class));
		ASTSwitchLabel label = (ASTSwitchLabel) n.getTreeNode();
		String nameimage=null;
		if(name!=null){
			nameimage=name.getImage();
		}
		if (name != null && (name.getNameDeclaration() instanceof VariableNameDeclaration) && !label.isDefault()
			&&nameimage!=null&&!nameimage.contains(".")) {
			ExpressionDomainVisitor exprvisitor = new ExpressionDomainVisitor();
			((SimpleJavaNode) label.jjtGetChild(0)).jjtAccept(exprvisitor, exprdata);
			VariableNameDeclaration v = (VariableNameDeclaration) name.getNameDeclaration();
			DomainSet ds = new DomainSet();
			ds.addDomain(v, exprdata.domain);
			if (exprdata.type == ClassType.INT&& DomainSet.getDomainType(v.getDomain())==ClassType.INT) {
				addDomainSet(ds);
			}
		}
		
		//domainset.removeRedundantDomain();

		// �ָ�ԭ������
		pre.setDomainSet(oldset);
	}

	/** ����ǰ���������жϽڵ���Ƿ�Ϊ���֧��־������״̬��״̬���� */
	public void calCondition(VexNode pre, boolean istruebranch) {
		// ������
		DomainSet oldset = pre.getDomainSet();

		// ����״̬�������м��㣬����״̬����
		pre.setDomainSet(domainset);
		SimpleJavaNode treenode = pre.getTreeNode();
		if (/*treenode.getFirstVexNode() == pre*/!pre.isBackNode()) {
			// ȷ���Ǹ��﷨���ڵ��Ӧ�ĵ�һ���������ڵ�
			ConditionData condata = new ConditionData(pre);
			ConditionDomainVisitor convisitor = new ConditionDomainVisitor();
			if (treenode instanceof ASTIfStatement) {
				((JavaNode) treenode.jjtGetChild(0)).jjtAccept(convisitor, condata);
			} else if (treenode instanceof ASTWhileStatement) {
				//������ȷ��ѭ������
				condata=ConditionData.calLoopCondtion(condata,pre,convisitor,((SimpleJavaNode) treenode.jjtGetChild(0)));
			} else if (treenode instanceof ASTExpression) {
				// ASTDoStatement,
				//������ȷ��ѭ������
				condata=ConditionData.calLoopCondtion(condata,pre,convisitor,treenode);
			} else if (treenode instanceof ASTForStatement) {
				List results = treenode.findDirectChildOfType(ASTExpression.class);
				if (!results.isEmpty()) {
					if (!(treenode.jjtGetChild(0) instanceof ASTForEachVariableDeclaration)) {
						// ����for-each
						//������ȷ��ѭ������
						condata=ConditionData.calLoopCondtion(condata,pre,convisitor,((SimpleJavaNode) results.get(0)));
					}
				}
			}

			DomainSet ds = null;
			if (istruebranch) {
				ds = condata.getTrueMayDomainSet();
				//��ѭ�������������⴦��
				if(pre.getName().startsWith("while_head")||pre.getName().startsWith("for_head")||pre.getName().startsWith("do_while_out1")){
					domainset=DomainSet.intersect(domainset, ds);
					if(!domainset.isContradict()){
						domainset=DomainSet.join(domainset, ds);
					}
				}else{
					domainset=DomainSet.intersect(domainset, ds);
				}

			} else {
				ds=condata.getFalseMayDomainSet(pre);
				// ��ѭ�������������⴦��
				if (pre.getName().startsWith("while_head") || pre.getName().startsWith("for_head") || pre.getName().startsWith("do_while_out1")) {
					DomainSet old=pre.getDomainSet();
					pre.setDomainSet(null);
					ds=condata.getFalseMayDomainSet(pre);
					pre.setDomainSet(old);
					domainset = DomainSet.join(DomainSet.intersect(domainset, ds), ds);
				} else {
					domainset = DomainSet.intersect(domainset, ds);
				}
			}
			
			/*if (!ds.isContradict()) {
				addDomainSet(ds);
			} else {
				// ì��
				domainset = ds;
			}*/
		}

		//domainset.removeRedundantDomain();
		// �ָ�ԭ������
		pre.setDomainSet(oldset);
	}

	/** ���ݵ�ǰ�������ڵ㣬����״̬��״̬���� */
	public void calDomainSet(VexNode vex) {
		// ������
		DomainSet oldset = vex.getDomainSet();

		// ����״̬�������м��㣬����״̬����
		vex.setDomainSet(domainset);
		SimpleJavaNode treenode = vex.getTreeNode();
		if (/*treenode.getFirstVexNode() == vex*/!vex.isBackNode()) {
			// ȷ���Ǹ��﷨���ڵ��Ӧ�ĵ�һ���������ڵ�
			DomainData exprdata = new DomainData(vex);
			ExpressionDomainVisitor exprvisitor = new ExpressionDomainVisitor();

			if (treenode instanceof ASTIfStatement) {
				((JavaNode) treenode.jjtGetChild(0)).jjtAccept(exprvisitor, exprdata);
			} else if (treenode instanceof ASTWhileStatement) {
				((JavaNode) treenode.jjtGetChild(0)).jjtAccept(exprvisitor, exprdata);
			} else if (treenode instanceof ASTLocalVariableDeclaration) {
				treenode.jjtAccept(exprvisitor, exprdata);
			} else if (treenode instanceof ASTSwitchStatement) {
				((JavaNode) treenode.jjtGetChild(0)).jjtAccept(exprvisitor, exprdata);
			} else if (treenode instanceof ASTExpression) {
				// ASTDoStatement,
				// �ڿ�����ͼ�����У��Ѿ������ˣ����﷨���ڵ��Ѿ�����Ϊ���ʽ�ڵ���
				treenode.jjtAccept(exprvisitor, exprdata);
			} else if (treenode instanceof ASTForInit) {
				treenode.jjtAccept(exprvisitor, exprdata);
			} else if (treenode instanceof ASTForStatement) {
				List results = treenode.findDirectChildOfType(ASTExpression.class);
				if (!results.isEmpty()) {
					((JavaNode) ((JavaNode) results.get(0)).jjtGetChild(0)).jjtAccept(exprvisitor, exprdata);
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
			}
		}
		
		domainset = vex.getDomainSet();
		// ȥ����Щ����������ı���
		DomainSet temp = new DomainSet();
		Set entryset = vex.getDomainSet().getTable().entrySet();
		Iterator i = entryset.iterator();
		Object d1 = null;
		VariableNameDeclaration v1 = null;
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			v1 = (VariableNameDeclaration) e.getKey();
			d1 = e.getValue();
			if (vex.getTreeNode().getScope().isSelfOrAncestor(v1.getDeclareScope())) {
				temp.getTable().put(v1, d1);
			}
		}
		domainset=temp;

		domainset.removeRedundantDomain();
		// �ָ�ԭ������
		vex.setDomainSet(oldset);
	}

}