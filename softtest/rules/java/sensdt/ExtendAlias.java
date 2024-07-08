package softtest.rules.java.sensdt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import softtest.ast.java.*;
import softtest.symboltable.java.*;

import org.jaxen.BaseXPath;
import org.jaxen.JaxenException;
import org.jaxen.XPath;

import softtest.ast.java.ASTAllocationExpression;
import softtest.ast.java.ASTAssignmentOperator;
import softtest.ast.java.ASTCatchStatement;
import softtest.ast.java.ASTDoStatement;
import softtest.ast.java.ASTExpression;
import softtest.ast.java.ASTFinallyStatement;
import softtest.ast.java.ASTForStatement;
import softtest.ast.java.ASTIfStatement;
import softtest.ast.java.ASTLiteral;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTNullLiteral;
import softtest.ast.java.ASTPrimaryExpression;
import softtest.ast.java.ASTReturnStatement;
import softtest.ast.java.ASTSwitchStatement;
import softtest.ast.java.ASTSynchronizedStatement;
import softtest.ast.java.ASTTryStatement;
import softtest.ast.java.ASTVariableDeclarator;
import softtest.ast.java.ASTVariableDeclaratorId;
import softtest.ast.java.ASTVariableInitializer;
import softtest.ast.java.ASTWhileStatement;
import softtest.ast.java.SimpleJavaNode;
import softtest.cfg.java.Edge;
import softtest.cfg.java.VexNode;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;
import softtest.fsmanalysis.java.ProjectAnalysis;
import softtest.jaxen.java.DocumentNavigator;
import softtest.config.java.*;
import softtest.rules.java.AliasSet;
import softtest.rules.java.DsnInfoLeakStateMachine;
import softtest.symboltable.java.Scope;
import softtest.symboltable.java.VariableNameDeclaration;

/**
 * 
 */
public class ExtendAlias  extends FSMRelatedCalculation {
	
	private String resourcename = "";
	
	private AliasSet  sensSource;

	private AliasSet  sensEntity;
	
	//private Hashtable<VariableNameDeclaration, VariableNameDeclaration> table = new Hashtable<VariableNameDeclaration, VariableNameDeclaration>();

	public ExtendAlias(){
		sensSource = new AliasSet();
		sensEntity = new AliasSet();
	}
	
	public ExtendAlias(FSMRelatedCalculation o) {
		super(o);
		if(!(o instanceof ExtendAlias)){
			return;
		}
		ExtendAlias t = (ExtendAlias)o;
		resourcename = t.resourcename;
		sensSource = new AliasSet( t.getSensSource() );
		sensEntity = new AliasSet( t.getSensEntity() );
	}
	
	/** 拷贝 */
	@Override
	public FSMRelatedCalculation copy(){
		FSMRelatedCalculation r = new ExtendAlias(this);
		return r;
	}

	public void addSensSource(VariableNameDeclaration v) {
		sensSource.add(v);
	}
	public void addSensEntity(VariableNameDeclaration v) {
		sensEntity.add(v);
	}

	public void removeSensSource(VariableNameDeclaration v) {
		sensSource.remove(v);
	}
	public void removeSensEntity(VariableNameDeclaration v) {
		sensEntity.remove(v);
	}

	public boolean isSensSourceEmpty() {
		return  sensSource.isEmpty();
	}

	public void  setResource(SimpleJavaNode resource) {
		setTagTreeNode(resource);
		sensSource.setResource( resource );
		sensEntity.setResource( resource );
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

	public boolean sensSourceContains(VariableNameDeclaration v) {
		return  sensSource.contains(v);
	}
	public boolean sensEntityContains(VariableNameDeclaration v) {
		return  sensEntity.contains(v);
	}

	public Hashtable<VariableNameDeclaration, VariableNameDeclaration> getSensSourceTable() {
		return  sensSource.getTable();
	}
	
	public Hashtable<VariableNameDeclaration, VariableNameDeclaration> getSensEntityTable() {
		return  sensEntity.getTable();
	}
	
		
	/** 计算数据流方程中的IN */
	@Override
	public void calculateIN(FSMMachineInstance fsmin,VexNode vex, Object data){
		if(fsmin.getRelatedObject()!=this){
			throw new RuntimeException("AliasSet error");
		}
		List<Edge> list = new ArrayList<Edge>();
		for (Enumeration<Edge> e = vex.getInedges().elements(); e.hasMoreElements();) {
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
						fsmin.setRelatedObject(new ExtendAlias(prefsmin.getRelatedObject()));
					}
					else{
						Hashtable<VariableNameDeclaration, VariableNameDeclaration> table1,table2,newtable;
						ExtendAlias s1=(ExtendAlias)fsmin.getRelatedObject();
						ExtendAlias s2=(ExtendAlias)prefsmin.getRelatedObject();
						table1 = s1.getSensSourceTable();
						table2 = s2.getSensSourceTable();
						newtable=new Hashtable<VariableNameDeclaration, VariableNameDeclaration>();
						for(Enumeration<VariableNameDeclaration> e =table1.elements();e.hasMoreElements();){
							VariableNameDeclaration v=e.nextElement();
							if(table2.containsKey(v)){
								newtable.put(v, v);
							}
						}
						this.sensSource.setTable( newtable );
						
						table1 = s1.getSensEntityTable();//.getTable();
						table2 = s2.getSensEntityTable();//.getTable();
						newtable=new Hashtable<VariableNameDeclaration, VariableNameDeclaration>();
						for(Enumeration<VariableNameDeclaration> e =table1.elements();e.hasMoreElements();){
							VariableNameDeclaration v = e.nextElement();
							if(table2.containsKey(v)){
								newtable.put(v, v);
							}
						}	
						this.sensEntity.setTable( newtable );
					}
				}
			}
		}
	}
	
	/** 计算数据流方程中的OUT */
	@Override
	public void calculateOUT(FSMMachineInstance fsmin,VexNode n,Object data){
		if(fsmin.getRelatedObject()!=this){
			throw new RuntimeException("ExtendAlias error");
		}
		List evaluationResults = new LinkedList();
		SimpleJavaNode treenode = n.getTreeNode();
		// 处理赋值
		// xpath不处理那些尾节点
		if (treenode.getVexNode().get(0) == n) {
			try {
				XPath xpath = new BaseXPath(".//AssignmentOperator[@Image=\'=\']", new DocumentNavigator());

				if (treenode instanceof ASTIfStatement) {
					treenode = (SimpleJavaNode) treenode.jjtGetChild(0);
				} else if (treenode instanceof ASTWhileStatement) {
					treenode = (SimpleJavaNode) treenode.jjtGetChild(0);
				} else if (treenode instanceof ASTSwitchStatement) {
					treenode = (SimpleJavaNode) treenode.jjtGetChild(0);
				} else if (treenode instanceof ASTForStatement) {
					List results = treenode.findDirectChildOfType(ASTExpression.class);
					if (!results.isEmpty()) {
						treenode = (SimpleJavaNode) results.get(0);
					}
				} else if (treenode instanceof ASTSynchronizedStatement) {
					treenode = (SimpleJavaNode) treenode.jjtGetChild(0);
				} else if (treenode instanceof ASTTryStatement) {
					treenode = null;
				} else if (treenode instanceof ASTCatchStatement) {
					treenode = null;
				} else if (treenode instanceof ASTFinallyStatement) {
					treenode = null;
				} else if (treenode instanceof ASTDoStatement) {
					treenode = null;
				}
				if (treenode != null) {
					evaluationResults = xpath.selectNodes(treenode);
					logc2("xpath : AssignmentOperator");
				}
			} catch (JaxenException e) {
				// e.printStackTrace();
				throw new RuntimeException("xpath error",e);
			}

			//ExtendAlias extAlias = (ExtendAlias) fsmin.getRelatedObject();
			Iterator i = evaluationResults.iterator();
			while (i.hasNext()) {
				ASTAssignmentOperator assign = (ASTAssignmentOperator) i.next();
				SimpleJavaNode parent = (SimpleJavaNode) assign.jjtGetParent();
				
				logc2("-----------------------------------------------");
				logc2("AssignStatement: " + parent.printNode(ProjectAnalysis.getCurrent_file()));
				
				if (parent.jjtGetNumChildren() < 3) {
					logc2(parent.printNode(ProjectAnalysis.getCurrent_file()) + " " + parent.jjtGetNumChildren() + " NumChild < 3");
					continue;
				}
				SimpleJavaNode left = (SimpleJavaNode) parent.jjtGetChild(0);
				SimpleJavaNode right = (SimpleJavaNode) parent.jjtGetChild(2);
				ASTName leftname = (ASTName) left.getSingleChildofType(ASTName.class);
				ASTName rightname = (ASTName) right.getSingleChildofType(ASTName.class);
				
				/** 处理 xx = Xxx.xx(..); xx = new Xx(..); 
				 *       xx = yy;         xx = "..";           四种形式 **/
				if (leftname != null && (leftname.getNameDeclaration() instanceof VariableNameDeclaration) ) {
					
					VariableNameDeclaration leftv = (VariableNameDeclaration) leftname.getNameDeclaration();
					
					//  xx = yy;
					if( rightname != null && (rightname.getNameDeclaration() instanceof VariableNameDeclaration) ) {
						VariableNameDeclaration rightv = (VariableNameDeclaration) rightname.getNameDeclaration();

						if( sensSource.contains( leftv )) {
							sensSource.remove(leftv);
						}
						if( sensSource.contains( rightv ) ) {
							sensSource.add( leftv );
						}
						
						if( sensEntity.contains( leftv )) {
							sensEntity.remove(leftv);
						}
						if( sensEntity.contains( rightv ) ) {
							sensEntity.add( leftv );
						}
						logc2("Case : xx = yy;");
					}
					//  xx = "..";
					else if(right.getSingleChildofType(ASTLiteral.class) != null) {
						ASTLiteral astlit = (ASTLiteral)right.getSingleChildofType(ASTLiteral.class);
						if( astlit.jjtGetNumChildren() > 0 && astlit.jjtGetChild(0) instanceof ASTNullLiteral ) {
							sensSource.remove(leftv);
							sensEntity.remove(leftv);
							continue;
						}
						else if( ! astlit.isStringLiteral() ) {
							continue;
						}
						String withquote  = astlit.getImage();
						String noquote = withquote.substring(1, withquote.length() - 1);
						if( this.resourcename.equals(noquote) ) {
							sensSource.add(leftv);
						} else {
							sensSource.remove(leftv);
							sensEntity.remove(leftv);
						}
					}
					//  xx = Xxx.xx(..);  xx = new Xxx(..);  x = [xx.]xx()[.xx()];
					else {
						SensInfo sinfo = DsnInfoLeakStateMachine.sInfo;
						SensClasses sclses = DsnInfoLeakStateMachine.sensClasses;
						ASTPrimaryExpression rightPE = (ASTPrimaryExpression) right.getSingleChildofType( ASTPrimaryExpression.class );
						if( rightPE == null ) {
							logc2("???????? not PrimaryExpression :" + ((SimpleJavaNode)right.jjtGetChild(0)).printNode(ProjectAnalysis.getCurrent_file()));
							continue;
						}
						if( SensClasses.isSensitive(rightPE, sclses, this) ) {
							sensEntity.add(leftv);
							logc2("Case : xx = Xxx.xx(..);  is sens");
						} else {
							sensEntity.remove(leftv);
							sensSource.remove(leftv);
							logc2("Case : xx = new Xx(..);  not sens");
						}
					}
				} // if( leftv != null ....
			} // while 
			
			/*********  下面是处理 Xx xx = new Xxx(..);   Xx xx = xx; 
			 *                     Xx xx = "..";          Xx xx = [Xxx.]xx(...);
			 * 与上面的关系是并列的，只是没有表现出来
			 * 形式String st[] = {"ab", "cd"};没有考虑  */
			List  evalRlts = new LinkedList();
			try {
				XPath xpath = new BaseXPath(".//VariableInitializer", new DocumentNavigator());
				/**  treenode 已处理过，不需要再次处理  **/
				if (treenode != null) {
					evalRlts = xpath.selectNodes(treenode);
					logc2("xpath : variableInitializer");
				}

			} catch (JaxenException e) {
				// e.printStackTrace();
				throw new RuntimeException("xpath error",e);
			}

			i = evalRlts.iterator();
			while (i.hasNext()) {
				ASTVariableInitializer astIni = (ASTVariableInitializer) i.next();
				ASTVariableDeclarator astVarDecltor = (ASTVariableDeclarator) astIni.jjtGetParent();
				ASTVariableDeclaratorId astVarId = (ASTVariableDeclaratorId) astVarDecltor.jjtGetChild(0);
				VariableNameDeclaration varDecl = astVarId.getNameDeclaration();
				
				logc2("-----------------------------------------------");
				logc2("VarInitializer: " + astVarDecltor.printNode(ProjectAnalysis.getCurrent_file()));
				
				if( ! (astIni.jjtGetChild(0) instanceof ASTExpression ) ) {
					/** 形式String st[] = {"ab", "cd"};没有考虑  **/
					continue;
				}
				ASTExpression astExpr = (ASTExpression) astIni.jjtGetChild(0);
				SensInfo sinfo = DsnInfoLeakStateMachine.sInfo;
				SensClasses sclses = DsnInfoLeakStateMachine.sensClasses;
				
				boolean probe = false; // 如下四种情况的标记，若有重叠执行则报异常
				/** Case 1:  Xxx xx = "...";  **/
				ASTLiteral astLit = (ASTLiteral) astExpr.getSingleChildofType( ASTLiteral.class );
				if( astLit != null ) {
					probe = true;
					if( ! astLit.isStringLiteral() ) {
						continue;
					}
					String withquote = astLit.getImage();
					String noquote   = withquote.substring(1, withquote.length()-1);
					logc2("Case 1: Xxx xx = \"..\";");
					if( sinfo.isSensInfo( noquote ) ) {
						/**  Must be the same String -- resource  **/
						if( resourcename.equals( noquote ) ) {
							sensSource.add( varDecl );
							logc2(resourcename + " add Source :" + varDecl.getImage());
						}
					}
				}
				/** Case 2:  Xxx xx = new Xxx(..);  **/
				ASTAllocationExpression astAlloc = (ASTAllocationExpression) astExpr.getSingleChildofType(ASTAllocationExpression.class);
				if( astAlloc != null ) {
					if( ! probe ) {
						probe = true;
					} else {
						throw new RuntimeException("No possible execute path");
					}
					logc2("Case 2: Xxx xx = new Xxx(..);");
					ASTPrimaryExpression astPE = (ASTPrimaryExpression) astAlloc.jjtGetParent().jjtGetParent();
					if( SensClasses.isSensitive(astPE, sclses, this)) {
						sensEntity.add( varDecl );
						logc2(resourcename + " add Entity :" + varDecl.getImage());
					}
				}
				/** Case 3:  Xxx xx = [Xxx.]xx(..);  **/
				ASTPrimaryExpression astPExpr = (ASTPrimaryExpression) astExpr.getSingleChildofType(ASTPrimaryExpression.class);
				if( astPExpr != null && astPExpr.jjtGetNumChildren() == 2 ) {
					if( ! probe ) {
						probe = true;
					} else {
						throw new RuntimeException("No possible execute path");
					}
					logc2("Case 3: Xxx xx = [Xx.]xx(..);");
					if( SensClasses.isSensitive(astPExpr, sclses, this)) {
						sensEntity.add( varDecl );
						logc2(resourcename + " add Entity :" + varDecl.getImage());
					}
				}
				/** Case 4:  Xxx xx = xx;  **/
				ASTName astNameRight = (ASTName) astExpr.getSingleChildofType(ASTName.class);
				if( astNameRight != null ) {
					if( ! probe ) {
						probe = true;
					} else {
						throw new RuntimeException("No possible execute path");
					}
					logc2("Case 4: Xxx xx = xx;");
					VariableNameDeclaration rightDecl = (VariableNameDeclaration)astNameRight.getNameDeclaration();
					if( this.sensEntity.contains( rightDecl )) {
						this.sensEntity.add(varDecl);
						logc2(resourcename + " add Entity :" + varDecl.getImage());
					}
					if( this.sensSource.contains( rightDecl )) {
						this.sensSource.add(varDecl);
						logc2(resourcename + " add Source :" + varDecl.getImage());
					}
				}
			} // while 
		}// treenode.getVexNode().get(0) == n
		else {
			logc2("Xpath not do on  Vex:" + treenode.getVexNode().get(0));
		}
		
		//处理作用域变化
		ExtendAlias extAlias = (ExtendAlias) fsmin.getRelatedObject();
		// 处理作用域变化
		ArrayList<VariableNameDeclaration> todelete = new ArrayList<VariableNameDeclaration>();
		Hashtable<VariableNameDeclaration, VariableNameDeclaration> table = extAlias.getSensSourceTable();
		for (Enumeration<VariableNameDeclaration> e = table.keys(); e.hasMoreElements();) {
			VariableNameDeclaration v = e.nextElement();
			Scope delscope = v.getDeclareScope();
			SimpleJavaNode astnode = n.getTreeNode();
			
			boolean b = true;
			if (!astnode.getScope().isSelfOrAncestor(delscope)) {
				// 声明作用域已经不是当前作用域自己或父亲了
				b = true;
			} else if (delscope.isSelfOrAncestor(astnode.getScope()) && astnode.getFirstVexNode() != n) {
				// 当前作用域是声明作用域自己或者父亲，但是当前节点需要终止当前作用域
				b = true;
			} else if (astnode instanceof ASTReturnStatement &&delscope.isSelfOrAncestor(astnode.getScope().getEnclosingMethodScope())){
				b=true;
			}else {
				b = false;
			}
			if (b) {
				todelete.add(v);
			}
		}
		for (VariableNameDeclaration v : todelete) {
			extAlias.removeSensSource(v);
		}
		todelete = null;
		todelete = new ArrayList<VariableNameDeclaration>();
		table = extAlias.getSensEntityTable();
		for (Enumeration<VariableNameDeclaration> e = table.keys(); e.hasMoreElements(); ) {
			VariableNameDeclaration v = e.nextElement();
			Scope delscope = v.getDeclareScope();
			SimpleJavaNode astnode = n.getTreeNode();
			
			boolean b = true;
			if (!astnode.getScope().isSelfOrAncestor(delscope)) {
				// 声明作用域已经不是当前作用域自己或父亲了
				b = true;
			} else if (delscope.isSelfOrAncestor(astnode.getScope()) && astnode.getFirstVexNode() != n) {
				// 当前作用域是声明作用域自己或者父亲，但是当前节点需要终止当前作用域
				b = true;
			} else if (astnode instanceof ASTReturnStatement &&delscope.isSelfOrAncestor(astnode.getScope().getEnclosingMethodScope())){
				b=true;
			}else {
				b = false;
			}
			if (b) {
				todelete.add(v);
			}
		}
		for (VariableNameDeclaration v : todelete) {
			extAlias.removeSensEntity(v);
		}
		
		logc2("[end] fsmStates: " + fsmin.getStates().toString());
	}
	
	public AliasSet getSensEntity() {
		return sensEntity;
	}
	public AliasSet getSensSource() {
		return sensSource;
	}
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[Elias]");
		sb.append(resourcename);
		
		return sb.toString();
	}
	public void dump() {
		logc("=================================== [Begin]  " + resourcename);
		if( Config.DEBUG ) {
			sensSource.simpleDump();
			sensEntity.simpleDump();
		}
		logc("=================================== [ End ]");
	}
	public void logc1(String str) {
		logc("IN() - " + str);
	}
	public void logc2(String str) {
		logc("OUT() - " + str);
	}
	public void logc(String str) {
		if( Config.DEBUG ) {
			System.out.println("ExtendAlias::" + str);
		}
	}
}