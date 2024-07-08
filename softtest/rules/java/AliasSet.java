package softtest.rules.java;

import java.util.*;

import softtest.ast.java.*;
import softtest.jaxen.java.*;
import softtest.symboltable.java.*;
import softtest.ast.java.ASTArgumentList;
import softtest.ast.java.ASTAssignmentOperator;
import softtest.ast.java.ASTExpression;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTPrimaryExpression;
import softtest.ast.java.ASTReturnStatement;
import softtest.ast.java.ASTStatementExpression;
import softtest.ast.java.ASTVariableDeclarator;
import softtest.ast.java.ASTVariableDeclaratorId;
import softtest.ast.java.ASTVariableInitializer;
import softtest.ast.java.SimpleJavaNode;
import softtest.ast.java.SimpleNode;
import softtest.cfg.java.*;
import softtest.config.java.Config;
import softtest.fsm.java.*;
import softtest.jaxen.java.DocumentNavigator;
import softtest.symboltable.java.NameDeclaration;
import softtest.symboltable.java.Scope;
import softtest.symboltable.java.VariableNameDeclaration;

import org.jaxen.*;
import softtest.domain.java.*;
import softtest.IntervalAnalysis.java.*;

public class AliasSet extends FSMRelatedCalculation {
	private String resourcename = "";
	
	/**函数返回了别名集合元素标志*/
	private boolean hasreturned=false;
	
	public AliasSet(){
	}
	
	public AliasSet(FSMRelatedCalculation o) {
		super(o);
		if(!(o instanceof AliasSet)){
			return;
		}
		AliasSet t=(AliasSet)o;
		resourcename=t.resourcename;
		for(Enumeration<VariableNameDeclaration> e =t.table.elements();e.hasMoreElements();){
			VariableNameDeclaration v = e.nextElement();
			table.put(v, v);
		}
		hasreturned=t.hasreturned;
	}
	
	/** 设置函数返回了别名集合元素标志 */
	public void setHasReturned(boolean hasreturned){
		this.hasreturned=hasreturned;
	}
	
	/** 获取函数返回了别名集合元素标志 */
	public boolean getHasReturned(){
		return hasreturned;
	}
	
	/** 拷贝 */
	@Override
	public FSMRelatedCalculation copy(){
		FSMRelatedCalculation r = new AliasSet(this);
		return r;
	}

	private Hashtable<VariableNameDeclaration, VariableNameDeclaration> table = new Hashtable<VariableNameDeclaration, VariableNameDeclaration>();

	public void add(VariableNameDeclaration v) {
		table.put(v, v);
	}

	public void remove(VariableNameDeclaration v) {
		table.remove(v);
	}
	
	public boolean isAllMemberNullPointer(VexNode vex){
		for(VariableNameDeclaration v:table.values()){
			ReferenceDomain rdomain=(ReferenceDomain)ConvertDomain.DomainSwitch(vex.getDomainWithoutNull(v), ClassType.REF);
			if(rdomain.getValue()!=ReferenceValue.NULL){
				return false;
			}
		}
		return true;
	}

	public boolean isEmpty() {
		return table.isEmpty()&&!hasreturned;
	}

	public void setResource(SimpleJavaNode resource) {
		setTagTreeNode(resource);
	}

	public SimpleJavaNode getResource() {
		return getTagTreeNode();
	}

	public void setResouceName(String resourcename) {
		this.resourcename = resourcename;
	}

	public String getResourceName() {
		return this.resourcename;
	}

	public boolean contains(VariableNameDeclaration v) {
		if (v == null) {
			return false;
		}
		return table.containsKey(v);
	}

	public Hashtable<VariableNameDeclaration, VariableNameDeclaration> getTable() {
		return table;
	}
	
	public void  setTable(Hashtable<VariableNameDeclaration, VariableNameDeclaration> table) {
		this.table=table;
	}
	
	/** 计算数据流方程中的IN */
	@Override
	public void calculateIN(FSMMachineInstance fsmin,VexNode n,Object data){
		if(fsmin.getRelatedObject()!=this){
			throw new RuntimeException("AliasSet error");
		}
		List<Edge> list = new ArrayList<Edge>();
		for (Enumeration<Edge> e = n.getInedges().elements(); e.hasMoreElements();) {
			list.add(e.nextElement());
		}
		Collections.sort(list);

		boolean bfirst=true;
		Iterator<Edge> iter = list.iterator();
		while (iter.hasNext()) {
			Edge edge = iter.next();
			VexNode pre = edge.getTailNode();
			
			if(pre.getFSMMachineInstanceSet()!=null){
				FSMMachineInstance prefsmin=pre.getFSMMachineInstanceSet().getTable().get(fsmin);
				if(prefsmin!=null){
					if(bfirst){
						bfirst=false;
						Hashtable<VariableNameDeclaration, VariableNameDeclaration> table1,newtable;
						AliasSet s1=(AliasSet)prefsmin.getRelatedObject();
						table1=s1.getTable();
						newtable=new Hashtable<VariableNameDeclaration, VariableNameDeclaration>();
						
						for(Enumeration<VariableNameDeclaration> e =table1.elements();e.hasMoreElements();){
							VariableNameDeclaration v=e.nextElement();
							newtable.put(v, v);
						}
						setTable(newtable);
						hasreturned=s1.hasreturned;
					}
					else{
						Hashtable<VariableNameDeclaration, VariableNameDeclaration> table1,table2,newtable;
						AliasSet s1=(AliasSet)fsmin.getRelatedObject();
						AliasSet s2=(AliasSet)prefsmin.getRelatedObject();
						table1=s1.getTable();
						table2=s2.getTable();
						newtable=new Hashtable<VariableNameDeclaration, VariableNameDeclaration>();
						//求并集
						for(Enumeration<VariableNameDeclaration> e =table1.elements();e.hasMoreElements();){
							VariableNameDeclaration v=e.nextElement();
							newtable.put(v, v);
						}	
						for(Enumeration<VariableNameDeclaration> e =table2.elements();e.hasMoreElements();){
							VariableNameDeclaration v=e.nextElement();
							newtable.put(v, v);
						}	
						setTable(newtable);
						if(s1.hasreturned&&s2.hasreturned){
							hasreturned=true;
						}
					}
				}
			}
		}
	}
	
	/** 计算数据流方程中的OUT */
	@Override
	public void calculateOUT(FSMMachineInstance fsmin,VexNode n,Object data){
		if(fsmin.getRelatedObject()!=this){
			throw new RuntimeException("AliasSet error");
		}
		List evaluationResults = new LinkedList();
		SimpleJavaNode treenode = n.getTreeNode();
		// 处理赋值
		// xpath不处理那些尾节点
		if (/*treenode.getVexNode().get(0) == n*/!n.isBackNode()) {
			try {
				XPath xpath = new BaseXPath(".//AssignmentOperator[@Image=\'=\']", new DocumentNavigator());

				treenode=(SimpleJavaNode)treenode.getConcreteNode();
				if (treenode != null) {
					evaluationResults = xpath.selectNodes(treenode);
				}

			} catch (JaxenException e) {
				// e.printStackTrace();
				throw new RuntimeException("xpath error");
			}
			//  xx = xx;  xx = ..; if it is the case of xx = ...; remove xx from alias
			//  xx=yy=..=[zz|other];    
			AliasSet alias = (AliasSet) fsmin.getRelatedObject();
			Iterator i = evaluationResults.iterator();
			while (i.hasNext()) {
				ASTAssignmentOperator assign = (ASTAssignmentOperator) i.next();
				SimpleJavaNode parent = (SimpleJavaNode) assign.jjtGetParent();
				if (parent.jjtGetNumChildren() != 3) {
					continue;
				}
				
				//  xx=yy=..=[zz|other];
				if( procMultiAssignOperator(parent) ) {
					continue;
				}
				
				SimpleJavaNode left = (SimpleJavaNode) parent.jjtGetChild(0);
				SimpleJavaNode right = (SimpleJavaNode) parent.jjtGetChild(2);
				VariableNameDeclaration leftv = null;
				VariableNameDeclaration rightv = null;
				
				// fetch variable name declaration at left side of the assignment expression
				if (left.getSingleChildofType(ASTName.class) != null) {
					ASTName leftname = (ASTName) left.getSingleChildofType(ASTName.class);
					if (leftname.getNameDeclaration() instanceof VariableNameDeclaration) {
						leftv = (VariableNameDeclaration) leftname.getNameDeclaration();
					}
				} else {
					if (left.jjtGetNumChildren() == 2 && left.jjtGetChild(0) instanceof ASTPrimaryPrefix && left.jjtGetChild(1) instanceof ASTPrimarySuffix) {
						ASTPrimaryPrefix pp = (ASTPrimaryPrefix) left.jjtGetChild(0);
						ASTPrimarySuffix ps = (ASTPrimarySuffix) left.jjtGetChild(1);
						if (pp.usesThisModifier() && ps.getNameDeclaration() instanceof VariableNameDeclaration) {
							leftv = (VariableNameDeclaration) ps.getNameDeclaration();
						}
					}
				}
				
				// fetch variable name declaration at right side of the assignment expression
				if (right.getSingleChildofType(ASTName.class) != null) {
					ASTName rightname = (ASTName) right.getSingleChildofType(ASTName.class);
					if (rightname.getNameDeclaration() instanceof VariableNameDeclaration) {
						rightv = (VariableNameDeclaration) rightname.getNameDeclaration();
					}
				} else {
					if (right.jjtGetNumChildren() == 2 && right.jjtGetChild(0) instanceof ASTPrimaryPrefix && right.jjtGetChild(1) instanceof ASTPrimarySuffix) {
						ASTPrimaryPrefix pp = (ASTPrimaryPrefix) right.jjtGetChild(0);
						ASTPrimarySuffix ps = (ASTPrimarySuffix) right.jjtGetChild(1);
						if (pp.usesThisModifier() && ps.getNameDeclaration() instanceof VariableNameDeclaration) {
							rightv = (VariableNameDeclaration) ps.getNameDeclaration();
						}
					}
				}
				
				// BUG this.p
				if (leftv == null) {
					continue;
				}
				
				if(rightv == null) {
					if (right.getFirstChildOfType(ASTAllocationExpression.class)==null) {
						if (alias.contains(leftv)) {
							// 将等号左边变量从别名集合去除
							alias.remove(leftv);
						}
					}
					continue;
				}
				
				//logc("" + leftv.getImage() + "=" + rightv.getImage());				
				if (alias.contains(leftv)) {
					// 将等号左边变量从别名集合去除
					alias.remove(leftv);
				}
				if (alias.contains(rightv)) {
					// 将等号左边变量加入别名集合
					alias.add(leftv);
				}
			}
			
			//  Xxx xx = yy; if yy is in the alias, add xx to the alias.
			evaluationResults = new LinkedList();
			try {
				XPath xpath = new BaseXPath(".//VariableDeclarator/VariableInitializer", new DocumentNavigator());

				if (treenode != null) {
					evaluationResults = xpath.selectNodes(treenode);
				}
			} catch (JaxenException e) {
				// e.printStackTrace();
				throw new RuntimeException("xpath error");
			}
			i = evaluationResults.iterator();
			while (i.hasNext()) {
				ASTVariableInitializer astIni = (ASTVariableInitializer) i.next();
				ASTVariableDeclarator  astVDecltor = (ASTVariableDeclarator) astIni.jjtGetParent();
				// Xxx xx=yy=..=[zz|other];
				if( procMultiAssignOperator(astVDecltor) ) {
					continue;
				}
				ASTName  astNameRight = (ASTName)astIni.getSingleChildofType(ASTName.class);
				if ( astNameRight == null|| !(astNameRight.getNameDeclaration()instanceof VariableNameDeclaration)) {
					continue;
				}
				ASTVariableDeclaratorId astVId = (ASTVariableDeclaratorId) astVDecltor.jjtGetChild(0);
				VariableNameDeclaration leftv = astVId.getNameDeclaration();
				VariableNameDeclaration rightv = (VariableNameDeclaration) astNameRight.getNameDeclaration();

				if (!astNameRight.getImage().contains(".")&&alias.contains(rightv)) {
					// 将等号左边变量加入别名集合
					alias.add(leftv);
				}
			}
		}
		//处理return
		if ( n.getTreeNode() instanceof ASTReturnStatement){
			ASTReturnStatement returnstmt=(ASTReturnStatement)n.getTreeNode();
			if(returnstmt.jjtGetNumChildren()>0){
				ASTName name=(ASTName)returnstmt.getSingleChildofType(ASTName.class);
				if(name!=null&&name.getNameDeclaration() instanceof VariableNameDeclaration){
					VariableNameDeclaration v=(VariableNameDeclaration)name.getNameDeclaration();
					AliasSet alias = (AliasSet) fsmin.getRelatedObject();
					if(alias.contains(v)){
						alias.setHasReturned(true);
					}
				}
				if(!Config.RL_USEASPARAM){
					List nodes=returnstmt.findChildrenOfType(ASTArgumentList.class);
					Iterator i = nodes.iterator();
					while (i.hasNext()&&!this.getHasReturned()) {
						Object o = i.next();
						ASTArgumentList arglist=(ASTArgumentList)o;
						for(int k=0;k<arglist.jjtGetNumChildren();k++){
							name=(ASTName)((SimpleNode)arglist.jjtGetChild(k)).getSingleChildofType(ASTName.class);
							if(name!=null&&name.getNameDeclaration() instanceof VariableNameDeclaration){
								VariableNameDeclaration v=(VariableNameDeclaration)name.getNameDeclaration();
								AliasSet alias = (AliasSet) fsmin.getRelatedObject();
								if(alias.contains(v)){
									alias.setHasReturned(true);
								}
							}
						}
					}
				}
			}
		}
		//处理作用域变化
		AliasSet alias = (AliasSet) fsmin.getRelatedObject();
		// 处理作用域变化
		ArrayList<VariableNameDeclaration> todelete = new ArrayList<VariableNameDeclaration>();
		Hashtable<VariableNameDeclaration, VariableNameDeclaration> table = alias.getTable();
		for (Enumeration<VariableNameDeclaration> e = table.keys(); e.hasMoreElements();) {
			VariableNameDeclaration v = e.nextElement();
			Scope delscope = v.getDeclareScope();
			SimpleJavaNode astnode = n.getTreeNode();
			
			boolean b = false;
			if (!astnode.getScope().isSelfOrAncestor(delscope)) {
				// 声明作用域已经不是当前作用域自己或父亲了
				b = true;
			} else if (delscope.isSelfOrAncestor(astnode.getScope()) && /*astnode.getFirstVexNode() != n*/n.isBackNode()) {
				// 当前作用域是声明作用域自己或者父亲，但是当前节点需要终止当前作用域
				b = true;
			} else if (astnode instanceof ASTReturnStatement &&delscope.isSelfOrAncestor(astnode.getScope().getEnclosingMethodScope())){
				Hashtable<String, Edge> outedges=n.getOutedges();
				for(Edge edge:outedges.values()){
					if(edge.getHeadNode().getName().startsWith("func_out")){
						ReferenceDomain rdomain=(ReferenceDomain)ConvertDomain.DomainSwitch(n.getDomainWithoutNull(v), ClassType.REF);
						if(rdomain.getValue()!=ReferenceValue.NULL){
							b=true;
						}
						break;
					}
				}
			} /*else if(n.getName().startsWith("func_eout")&&delscope.isSelfOrAncestor(astnode.getScope().getEnclosingMethodScope())){
				b=true;
			}*/
			else {
				b = false;
			}
			if (b) {
				todelete.add(v);
			}
		}
		
		for (VariableNameDeclaration v : todelete) {
			alias.remove(v);
		}
	}
	
	//  xx=yy=..=[zz|other];   node could be   StatementExpression, Expression
	//  Xx xx = yy=..=[zz|other];              VariableDeclarator
	/**
	 * return true if  node  has at least 2 = operators
	 */
	private boolean  procMultiAssignOperator(SimpleJavaNode node) {
		boolean  ismulti = false;
		boolean  add     = false;
		
		VariableNameDeclaration xxv  = null;
		SimpleJavaNode        yyppzz  = null;
		VariableNameDeclaration mostRightv  = null;
		SimpleJavaNode                left  = null;
		SimpleJavaNode               right  = null;
		
		// Xx xx =yy=..=[zz|other];   变量定义式的节点
		if( node instanceof ASTVariableDeclarator ) {
			ASTVariableDeclaratorId astVid = (ASTVariableDeclaratorId) node.jjtGetChild(0);
			xxv = astVid.getNameDeclaration();
			right = (SimpleJavaNode) node.jjtGetChild(1).jjtGetChild(0);
			yyppzz = right;
		}
		// xx=yy=..=[zz|other];       变量赋值式的节点
		else if( node instanceof ASTStatementExpression ||
				node instanceof ASTExpression ) {
			left = (SimpleJavaNode) node.jjtGetChild(0);
			right = (SimpleJavaNode) node.jjtGetChild(2);
			ASTName leftname = (ASTName) left.getSingleChildofType(ASTName.class);
			if(leftname!=null&&leftname.getNameDeclaration() instanceof VariableNameDeclaration) {
				xxv = (VariableNameDeclaration) leftname.getNameDeclaration();
			}
			yyppzz = right;
		}
		// yyppzz代表xx右边的表达式节点，若yyppzz包含有=号，则yyppzz被赋值为=右边的表达式节点
		while( yyppzz != null && yyppzz.jjtGetNumChildren() == 3 && yyppzz.jjtGetChild(1) instanceof ASTAssignmentOperator ) {
			yyppzz = (ASTExpression) yyppzz.jjtGetChild(2);
		}
		// 若yyppzz 不等于right,即表示xx右边的表达式节点，则表明至少有两个=。
		if( yyppzz != null && yyppzz != right ) {
			ismulti = true;
			ASTName mostRightName = (ASTName)yyppzz.getSingleChildofType(ASTName.class);
			if( mostRightName != null ) {
				NameDeclaration ndecl = mostRightName.getNameDeclaration();
				if( ndecl instanceof VariableNameDeclaration ) {
					mostRightv = (VariableNameDeclaration)ndecl;
					if( this.table.contains(mostRightv)) {
						add = true;
					}
				}
			}
			//	add or remvoe  yy, ..
			while( yyppzz != right ) {
				yyppzz = (ASTExpression) yyppzz.jjtGetParent();
				ASTPrimaryExpression astPE = (ASTPrimaryExpression)yyppzz.jjtGetChild(0);
				ASTName  ppname = (ASTName) astPE.getSingleChildofType(ASTName.class);
				if(ppname==null||!(ppname.getNameDeclaration() instanceof VariableNameDeclaration) ){
					continue;
				}
				VariableNameDeclaration ppv = (VariableNameDeclaration)ppname.getNameDeclaration();
				if( add ) {
					this.table.put(ppv, ppv);
				} else {
					if( this.table.contains(ppv) ) {
						this.table.remove(ppv);
					}
				}
			}
			//  add or remove xx
			if( add && xxv != null ) {
				this.table.put(xxv, xxv);
			} else {
				if( this.table.contains(xxv) ) {
					this.table.remove(xxv);
				}
			}
		}
		return ismulti;
	}
	
	public void  dump() {
		logc("=============================[Begin]  " + resourcename);
		for(Enumeration<VariableNameDeclaration> e = table.elements();e.hasMoreElements();) {
			VariableNameDeclaration v=e.nextElement();
			logc("  " + v.getImage());
		}
		logc("=============================[ End ]");
	}
	public void  simpleDump() {
		System.out.print("[Alias]" + resourcename + " : ");
		for(Enumeration<VariableNameDeclaration> e = table.elements();e.hasMoreElements();) {
			VariableNameDeclaration v=e.nextElement();
			System.out.print("  " + v.getImage());
		}
		System.out.println();
	}
	public void logc(String str) {
		System.out.println("AliasSet::" + str);
	}
}

