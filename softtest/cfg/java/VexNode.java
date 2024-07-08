package softtest.cfg.java;

import softtest.ast.java.*;
import softtest.symboltable.java.*;

import java.util.*;
import softtest.DefUseAnalysis.java.*;
import softtest.IntervalAnalysis.java.*;
import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.SimpleJavaNode;
import softtest.fsm.java.*;
import softtest.symboltable.java.NameOccurrence;
import softtest.symboltable.java.VariableNameDeclaration;

/** 图的顶点类 */
public class VexNode extends Element implements Comparable<VexNode> {
	/** 名称 */
	String name;

	/** 入边集合 */
	Hashtable<String, Edge> inedges = new Hashtable<String, Edge>();

	/** 出边集合 */
	Hashtable<String, Edge> outedges = new Hashtable<String, Edge>();
	
	/** 语句可能抛出的异常集合 */
	Set<Class> exceptions = new HashSet<Class>();

	/** 节点对应的抽象语法树节点 */
	SimpleJavaNode treenode = null;

	/** 真分支标志 */
	boolean truetag = false;

	/** 假分支标志 */
	boolean falsetag = false;

	/** 访问标志 */
	boolean visited = false;

	/** 条件限定域集 */
	ConditionData condata = null;

	/** 节点关联的变量域集 */
	private DomainSet domainset = null;
	
	/** 计算当前节点之前的变量域集 */
	private DomainSet lastdomainset=null;

	/** 用于比较的数字 */
	int snumber = 0;

	/** 该节点是否矛盾标志，用于控制流图中的不可达路径 */
	boolean contradict = false;
	
	private Graph g=null;

	/** 状态机实例集合 */
	FSMMachineInstanceSet fsminstanceset = new FSMMachineInstanceSet();
	
	/** 节点上的变量出现集合 */
	ArrayList<NameOccurrence> occs=new ArrayList<NameOccurrence>();
	
	/** 能够到达当前节点的变量定义出现 */
	LiveDefsSet liveDefs = new LiveDefsSet();
	
	public void addExceptions(Set<Class> ee) {
		exceptions.addAll(ee);
	}
	
	public Set<Class> getExceptions() {
		return exceptions;
	}
	
	/** 获得节点上的变量出现集合 */
	public ArrayList<NameOccurrence> getOccurrences(){
		return occs;
	}
	
	/**
	 * @param location 要查找的出现对应的语法树节点
	 * @return 变量出现，如果没有找到则返回null
	 */
	public NameOccurrence findOccurrence(SimpleJavaNode location){
		NameOccurrence ret=null;
		for(NameOccurrence occ:occs){
			if(occ.getLocation()==location){
				ret=occ;
				break;
			}
		}
		return ret;
	}
	
	/**
	 * @param v 要查找的出现对应的变量声明
	 * @return 所有的符合条件的出现
	 */
	public List<NameOccurrence> findOccurrenceOfVarDecl(VariableNameDeclaration v){
		ArrayList<NameOccurrence> ret=new ArrayList<NameOccurrence>();
		for(NameOccurrence occ:occs){
			if(occ.getLocation() instanceof ASTName){
				ASTName name=(ASTName)occ.getLocation();
				for(NameDeclaration v1:name.getNameDeclarationList()){
					if(v1==v){
						ret.add(occ);
					}
				}
			}
		}
		return ret;
	}
	
	/** 设置节点上的变量出现集合 */
	public void setOccurrences(ArrayList<NameOccurrence> occs){
		this.occs=occs;
	}
	
	/** 获得节点到达定义集合 */
	public LiveDefsSet getLiveDefs(){
		return liveDefs;
	}
	
	/** 设置节点到达定义集合 */
	public void setLiveDefs(LiveDefsSet liveDefs){
		this.liveDefs=liveDefs;
	}
	
	/** 获得状态机实例集合 */
	public FSMMachineInstanceSet getFSMMachineInstanceSet() {
		return fsminstanceset;
	}

	/** 设置状态机实例集合 */
	public void setFSMMachineInstanceSet(FSMMachineInstanceSet fsminstanceset) {
		this.fsminstanceset = fsminstanceset;
	}
	
	/**设置序号数字*/
	public void setSnumber(int snumber){
		this.snumber=snumber;
	}
	
	/**获得序号数字*/
	public int getSnumber(){
		return this.snumber;
	}

	/** 以指定的名字和语法树节点创建控制流图节点 */
	public VexNode(String name, SimpleJavaNode treenode) {
		this.name = name;
		this.treenode = treenode;
		treenode.setVexNode(this);
	}

	/** 控制流图访问者的accept */
	@Override
	public void accept(GraphVisitor visitor, Object data) {
		visitor.visit(this, data);
	}

	/** 设置节点关联的变量域集 */
	public void setDomainSet(DomainSet domainset) {
		this.domainset = domainset;
	}

	/** 获得节点关联的变量域集 */
	public DomainSet getDomainSet() {
		return domainset;
	}
	
	/** 设置节点之前的变量域集 */
	public void setLastDomainSet(DomainSet lastdomainset) {
		this.lastdomainset = lastdomainset;
	}

	/** 获得节点之前的变量域集 */
	public DomainSet getLastDomainSet() {
		return lastdomainset;
	}

	/** 设置节点关联的条件限定域集 */
	public void setConditionData(ConditionData condata) {
		this.condata = condata;
	}

	/** 获得节点关联的条件限定域集 */
	public ConditionData getConditionData() {
		return condata;
	}

	/** 将指定的变量域集同当前的变量域集融合，融合逻辑上通常用于多个控制流会合时的域集合并 */
	public void mergeDomainSet(DomainSet d) {
		if (domainset == null) {
			domainset = new DomainSet();
		}
		if (d != null) {
			// 去除那些超出作用域的变量
			DomainSet temp = new DomainSet();
			Set entryset = d.getTable().entrySet();
			Iterator i = entryset.iterator();
			Object d1 = null;
			VariableNameDeclaration v1 = null;
			while (i.hasNext()) {
				Map.Entry e = (Map.Entry) i.next();
				v1 = (VariableNameDeclaration) e.getKey();
				d1 = e.getValue();
				if (getTreeNode().getScope().isSelfOrAncestor(v1.getDeclareScope())) {
					temp.getTable().put(v1, d1);
				}
			}
			domainset.mergeDomainSet(temp,this);
		} else {
			domainset.mergeDomainSet(d,this);
		}
	}
	
	/** 删除那没有必要存放在节点上的域 */
	public void removeRedundantDomain(){
		if(domainset!=null){
			domainset.removeRedundantDomain();
		}
	}

	/** 修改变量v的域 */
	public Object addDomain(VariableNameDeclaration v, Object domain) {
		if (domainset == null) {
			domainset = new DomainSet();
		}
		return domainset.addDomain(v, domain);
	}

	/** 返回变量v的域，如果变量v不属于节点关联的域集则返回null */
	public Object getDomain(VariableNameDeclaration v) {
		if (domainset == null) {
			return null;
		} else {
			return domainset.getDomain(v);
		}
	}

	/** 返回变量v的域，如果变量v不属于节点关联的域集则返回缺省域 */
	public Object getDomainWithoutNull(VariableNameDeclaration v) {
		Object domain = getDomain(v);
		if (domain == null) {
			domain = v.getDomain();
		}
		return domain;
	}
	
	/** 返回变量v进入节点时的域，如果变量v不属于节点关联的域集则返回缺省域 */
	public Object getLastDomainWithoutNull(VariableNameDeclaration v) {
		Object domain=null;
		if (lastdomainset != null) {
			domain=lastdomainset.getDomain(v);
		} 
		if (domain == null) {
			domain = v.getDomain();
		}
		return domain;
	}

	/** 设置节点访问标志 */
	public void setVisited(boolean visited) {
		this.visited = visited;
	}

	/** 获得节点访问标志 */
	public boolean getVisited() {
		return visited;
	}

	/** 获得节点是否矛盾标志 */
	public boolean getContradict() {
		return contradict;
	}

	/** 设置节点是否矛盾标志 */
	public void setContradict(boolean contradict) {
		this.contradict = contradict;
	}

	/** 设置节点关联的语法树节点 */
	public void setTreeNode(SimpleJavaNode treenode) {
		this.treenode = treenode;
	}

	/** 获得节点关联的语法树节点 */
	public SimpleJavaNode getTreeNode() {
		return treenode;
	}

	/** 获得节点名称 */
	public String getName() {
		return name;
	}

	/** 获得入边集合 */
	public Hashtable<String, Edge> getInedges() {
		return inedges;
	}

	/** 获得出边集合 */
	public Hashtable<String, Edge> getOutedges() {
		return outedges;
	}

	/** 比较区间的顺序，用于排序 */
	public int compareTo(VexNode e) {
		if (snumber == e.snumber) {
			return 0;
		} else if (snumber > e.snumber) {
			return 1;
		} else {
			return -1;
		}
	}

	/** 将状态机实例集合set合并到fsminstanceset中 */
	public void mergeFSMMachineInstances(FSMMachineInstanceSet set) {
		fsminstanceset.mergeFSMMachineInstances(set);
	}
	
	public void mergFSMMachineInstancesWithoutConditon(FSMMachineInstanceSet set){
		fsminstanceset.mergFSMMachineInstancesWithoutConditon(set);
	}

	/** 得到函数返回值域 */
	public Object getReturnDomain() {
		if (treenode instanceof ASTMethodDeclaration) {
			ASTMethodDeclaration method = (ASTMethodDeclaration) treenode;
			return method.getDomain();
		}
		return null;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(name);
		return sb.toString();
	}
	
	/** 判断节点是不是语句的末尾节点，如if的out */
	public boolean isBackNode(){
		if(name.startsWith("if_out")||name.startsWith("while_out")
		||name.startsWith("for_out")||name.startsWith("switch_out")
		||name.startsWith("do_while_out2")||name.startsWith("func_out")
		||name.startsWith("try_out")||name.startsWith("lable_out")
		||name.startsWith("func_eout")){
			return true;
		}
		return false;
	}	
	
	/**
	 * 查找当前节点到下一个节点之间的边
	 * @param head 指定的下一个节点
	 * @return 当前节点到下一个节点如果存在一条边，则返回改变，否则返回null
	 */
	public Edge getEdgeByHead(VexNode head){
		for(Edge e:outedges.values()){
			if(e.headnode==head){
				return e;
			}
		}
		return null;
	}

	public Graph getGraph() {
		return g;
	}

	public void setGraph(Graph g) {
		this.g = g;
	}
	
	private SimpleJavaNode cascadeNode;

	public SimpleJavaNode getCascadeNode() {
		return cascadeNode;
	}

	public void setCascadeNode(SimpleJavaNode cascadeNode) {
		this.cascadeNode = cascadeNode;
	}
}
