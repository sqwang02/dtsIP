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

/** 状态实例 */
public class FSMStateInstance {
	/** 状态转换行控制流节点 */
	private List<VexNode> vexNodes = null;
	
	/** 状态转换行代码描述 */
	private List<String> switchPoints = null;
	
	/** 当前状态 */
	private FSMState state = null;

	/** 当前状态关联的条件变量域集 */
	private DomainSet domainset = new DomainSet();

	/** 以指定状态构造状态实例 */
	public FSMStateInstance(FSMState state) {
		this.state = state;
	}

	/** 产生该状态的控制流节点 */
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
			
			// 用控制流节点所在行作为状态转换描述 
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
	//fsminstance的状态转换到state。实现: 根据state重新构造一个状态实例，并设置相应值，返回新构造的状态实例
	public FSMStateInstance transferTo(FSMState state, FSMMachineInstance fsminstance, VexNode n) {
		FSMStateInstance newstateinstance = new FSMStateInstance(state);
		newstateinstance.setDomainSet(this.getDomainSet());
		newstateinstance.setSiteNode(n);
		newstateinstance.setSwitchPoints(this.getSwitchPoints());
		newstateinstance.setVexNodes(this.getVexNodes());
		
		// 非路径敏感：n可能为null或函数入口func_in_xx，所以不添加
		if (fsminstance.getFSMMachine().isPathSensitive()) {
			newstateinstance.addVexNode(n);
		}
		
		// StateMachine.java自定义的状态转换的行描述
		if (fsminstance.getSwitchLine() != null && fsminstance.getSwitchLine().length() > 0) {
			newstateinstance.addSwitchPoint(fsminstance.getSwitchLine());
		}
		// 用当前控制流节点所在行作为状态转换描述 
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

	/** 设置产生该状态的控制流节点 */
	public void setSiteNode(VexNode sitenode) {
		this.sitenode = sitenode;
	}

	/** 获得产生该状态的控制流节点 */
	public VexNode getVexNode() {
		return sitenode;
	}
	
	/** 添加一个状态转换控制流节点 */
	public void addVexNode(VexNode v) {
		if (v == null) {
			return;
		}
		
		if (this.vexNodes == null) {
			this.vexNodes = new LinkedList<VexNode>();
		}
		this.vexNodes.add(v);
	}
	
	/** 清空状态转换控制流节点 */
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
	
	/** 添加一个状态转换描述 */
	public void addSwitchPoint(String sp) {
		if (this.switchPoints == null) {
			this.switchPoints = new ArrayList<String>();
		}
		this.switchPoints.add(sp);
	}
	
	/** 清空状态转换描述 */
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

	/** 拷贝构造函数 */
	public FSMStateInstance(FSMStateInstance instance) {
		this.state = instance.state;
		
		domainset = new DomainSet(instance.domainset);			
		// domainset = instance.domainset;
		this.sitenode = instance.sitenode;
		this.setSwitchPoints(instance.getSwitchPoints());
		this.setVexNodes(instance.getVexNodes());
	}

	/** 设置当前状态 */
	public void setState(FSMState state) {
		this.state = state;
	}

	/** 获得当前状态 */
	public FSMState getState() {
		return state;
	}

	/** 设置当前状态关联的条件变量域集 */
	public void setDomainSet(DomainSet domainset) {
		this.domainset = domainset;
	}

	/** 获得当前状态关联的条件变量域集 */
	public DomainSet getDomainSet() {
		return domainset;
	}

	/** 增加约束条件 */
	public void addDomainSet(DomainSet set) {
		domainset = DomainSet.intersect(domainset, set);
	}

	/** 如果指向的状态相等，则被认为相等 */
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

	/** 用于hash表的key，需要保证值相等则hashcode相等 */
	@Override
	public int hashCode() {
		if(softtest.config.java.Config.PATH_SENSITIVE==2){
			return super.hashCode();
		}else{
			return state.hashCode();
		}
	}

	/** 打印 */
	@Override
	public String toString() {
		return state.getName() + "(" + domainset + ")";
	}

	/** 根据caselabel节点和switch节点，计算状态的状态条件 */
	public void calSwitch(VexNode n, VexNode pre) {
		if (!(n.getTreeNode() instanceof ASTSwitchLabel && pre.getTreeNode() instanceof ASTSwitchStatement)) {
			return;
		}
		// 保存域集
		DomainSet oldset = pre.getDomainSet();

		// 采用状态条件进行计算，更新状态条件
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

		// 恢复原来的域集
		pre.setDomainSet(oldset);
	}

	/** 根据前趋控制流判断节点和是否为真分支标志，计算状态的状态条件 */
	public void calCondition(VexNode pre, boolean istruebranch) {
		// 保存域集
		DomainSet oldset = pre.getDomainSet();

		// 采用状态条件进行计算，更新状态条件
		pre.setDomainSet(domainset);
		SimpleJavaNode treenode = pre.getTreeNode();
		if (/*treenode.getFirstVexNode() == pre*/!pre.isBackNode()) {
			// 确定是该语法树节点对应的第一个控制流节点
			ConditionData condata = new ConditionData(pre);
			ConditionDomainVisitor convisitor = new ConditionDomainVisitor();
			if (treenode instanceof ASTIfStatement) {
				((JavaNode) treenode.jjtGetChild(0)).jjtAccept(convisitor, condata);
			} else if (treenode instanceof ASTWhileStatement) {
				//处理正确的循环条件
				condata=ConditionData.calLoopCondtion(condata,pre,convisitor,((SimpleJavaNode) treenode.jjtGetChild(0)));
			} else if (treenode instanceof ASTExpression) {
				// ASTDoStatement,
				//处理正确的循环条件
				condata=ConditionData.calLoopCondtion(condata,pre,convisitor,treenode);
			} else if (treenode instanceof ASTForStatement) {
				List results = treenode.findDirectChildOfType(ASTExpression.class);
				if (!results.isEmpty()) {
					if (!(treenode.jjtGetChild(0) instanceof ASTForEachVariableDeclaration)) {
						// 过滤for-each
						//处理正确的循环条件
						condata=ConditionData.calLoopCondtion(condata,pre,convisitor,((SimpleJavaNode) results.get(0)));
					}
				}
			}

			DomainSet ds = null;
			if (istruebranch) {
				ds = condata.getTrueMayDomainSet();
				//对循环条件进行特殊处理
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
				// 对循环条件进行特殊处理
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
				// 矛盾
				domainset = ds;
			}*/
		}

		//domainset.removeRedundantDomain();
		// 恢复原来的域集
		pre.setDomainSet(oldset);
	}

	/** 根据当前控制流节点，计算状态的状态条件 */
	public void calDomainSet(VexNode vex) {
		// 保存域集
		DomainSet oldset = vex.getDomainSet();

		// 采用状态条件进行计算，更新状态条件
		vex.setDomainSet(domainset);
		SimpleJavaNode treenode = vex.getTreeNode();
		if (/*treenode.getFirstVexNode() == vex*/!vex.isBackNode()) {
			// 确定是该语法树节点对应的第一个控制流节点
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
				// 在控制流图处理中，已经处理了，该语法树节点已经设置为表达式节点了
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
		// 去除那些超出作用域的变量
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
		// 恢复原来的域集
		vex.setDomainSet(oldset);
	}

}