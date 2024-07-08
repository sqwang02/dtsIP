package softtest.fsmanalysis.java;

import softtest.IntervalAnalysis.java.*;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTSwitchLabel;
import softtest.ast.java.ASTSwitchStatement;
import softtest.ast.java.SimpleJavaNode;
import softtest.ast.java.SimpleNode;
import softtest.cfg.java.*;
import softtest.config.java.Config;
import softtest.database.java.DBAccess;

import java.util.*;

import softtest.ast.java.*;
import softtest.fsm.java.*;
import softtest.symboltable.java.*;

public class FSMControlFlowVisitor implements GraphVisitor {
	
	public void calculateIN(VexNode n, Object data) {
		List<Edge> list = new ArrayList<Edge>();
		for (Enumeration<Edge> e = n.getInedges().elements(); e.hasMoreElements();) {
			list.add(e.nextElement());
		}
		Collections.sort(list);
		if (n.getSnumber() != 0) {
			n.getFSMMachineInstanceSet().clear();
		}

		// 计算前驱节点的U(in)
		VexNode pre = null;
		Iterator<Edge> iter = list.iterator();
		while (iter.hasNext()) {
			Edge edge = iter.next();
			pre = edge.getTailNode();

			// 判断分支是否矛盾

			if (edge.getContradict()) {
				continue;
			}

			if (edge.getName().startsWith("T")) {
				FSMMachineInstanceSet temp = new FSMMachineInstanceSet(pre.getFSMMachineInstanceSet());
				temp.calCondition(pre, true);
				n.mergeFSMMachineInstances(temp);
			} else if (edge.getName().startsWith("F")) {
				// 处理一次循环
				if (n.getName().startsWith("while_out") || n.getName().startsWith("for_out")) {
					FSMControlFlowData loopdata = (FSMControlFlowData) data;
					loopdata.reporterror = false;
					visit(pre, loopdata);
					loopdata.reporterror = true;
				}

				FSMMachineInstanceSet temp = new FSMMachineInstanceSet(pre.getFSMMachineInstanceSet());
				temp.calCondition(pre, false);
				n.mergeFSMMachineInstances(temp);
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
				if (name != null && (name.getNameDeclaration() instanceof VariableNameDeclaration) && !label.isDefault() && nameimage != null && !nameimage.contains(".")) {
					ExpressionDomainVisitor exprvisitor = new ExpressionDomainVisitor();
					((SimpleJavaNode) label.jjtGetChild(0)).jjtAccept(exprvisitor, exprdata);

					if (exprdata.type == ClassType.INT) {
						FSMMachineInstanceSet temp = new FSMMachineInstanceSet(pre.getFSMMachineInstanceSet());
						temp.calSwitch(n, pre);
						n.mergeFSMMachineInstances(temp);
					} else {
						n.mergeFSMMachineInstances(pre.getFSMMachineInstanceSet());
					}
				} else {
					n.mergeFSMMachineInstances(pre.getFSMMachineInstanceSet());
				}
			} else {
				n.mergeFSMMachineInstances(pre.getFSMMachineInstanceSet());
			}
		}
	}

	public void calculateOUT(VexNode n, Object data) {
		List<Edge> list = new ArrayList<Edge>();
		for (Enumeration<Edge> e = n.getInedges().elements(); e.hasMoreElements();) {
			list.add(e.nextElement());
		}
		Collections.sort(list);
		VexNode pre = null;
		Iterator<Edge> iter = list.iterator();

		if (!n.isBackNode()) {
			// 重新计算状态条件
			n.getFSMMachineInstanceSet().calDomainSet(n);
		}
		// 计算当前节点的Out

		List<FSMMachineInstance> todelete = new ArrayList<FSMMachineInstance>();
		Iterator<FSMMachineInstance> i = n.getFSMMachineInstanceSet().getTable().values().iterator();
		while (i.hasNext()) {
			// 遍历所有的状态机实例
			FSMMachineInstance fsminstance = i.next();
			// 创建新的状态实例集合
			boolean stateschanged = false;
			//计算多次状态变化
			do {
				stateschanged = false;

				FSMStateInstanceSet newstates = new FSMStateInstanceSet();
				// 遍历当前状态机实例的所有状态实例
				Iterator<FSMStateInstance> is = fsminstance.getStates().getTable().values().iterator();
	out:		while (is.hasNext()) {
					FSMStateInstance stateinstance = is.next();
					DomainSet old = n.getDomainSet();
					n.setDomainSet(stateinstance.getDomainSet());
					FSMState state = stateinstance.getState();

					Hashtable<String, FSMTransition> trans = state.getOutTransitions();
					// 计算当前状态实例的所有可能状态转换					
					boolean b = false;
					for (Enumeration<FSMTransition> e = trans.elements(); e.hasMoreElements();) {
						FSMTransition transition = e.nextElement();
						if (transition.evaluate(fsminstance, stateinstance, n)) {
							// 需要状态转换
							if (transition.getToState().isError()) {
								FSMControlFlowData loopdata = (FSMControlFlowData) data;
								if (loopdata != null && loopdata.reporterror) {
									FSMStateInstance newstateinstance = stateinstance.transferTo(transition.getToState(), fsminstance, n);
									/*
									FSMStateInstance newstateinstance = new FSMStateInstance(transition.getToState());
									newstateinstance.setDomainSet(stateinstance.getDomainSet());
									newstateinstance.setSiteNode(n);
									newstateinstance.setSwitchPoints(stateinstance.getSwitchPoints());
									
									// StateMachine.java自定义的状态转换的行描述
									if (fsminstance.getSwitchLine() != null && fsminstance.getSwitchLine().length() > 0) {
										newstateinstance.addSwitchPoint(fsminstance.getSwitchLine());
									}
									// 用当前控制流节点所在行作为状态转换描述 
									else if (fsminstance.getFSMMachine().isPathSensitive()){
										if (n.isBackNode()) {
											newstateinstance.addSwitchPoint("\""+n.getTreeNode().printNode(ProjectAnalysis.getCurrent_file(),n.getTreeNode().getEndLine())+"\" at Line:"+n.getTreeNode().getEndLine()+" File:"+ProjectAnalysis.getCurrent_file());
										}
										else {
											newstateinstance.addSwitchPoint("\""+n.getTreeNode().printNode(ProjectAnalysis.getCurrent_file())+"\" at Line:"+n.getTreeNode().getBeginLine()+" File:"+ProjectAnalysis.getCurrent_file());
										}
									}
									*/
									//获取出错变量所在的方法 added by Ruiqiang 2013-04-08
									String methodName = null;
									int beginline =0, endline =0;
									String varstr = "", errorname = fsminstance.getFSMMachine().getName();
									int id =n.getTreeNode().getId();
									
									if(n.getTreeNode()!=null){
										
										SimpleJavaNode x = n.getTreeNode();
										if(x.getFirstParentOfType(ASTMethodDeclaration.class)!=null){											
											Node parentNode = x.getFirstParentOfType(ASTMethodDeclaration.class);																					
											if(parentNode instanceof ASTMethodDeclaration){
												methodName = ((ASTMethodDeclaration) parentNode).getMethodName();
											}
										}else if(x.getFirstParentOfType(ASTConstructorDeclaration.class)!=null){											
											Node parentNode = x.getFirstParentOfType(ASTConstructorDeclaration.class);
											if(parentNode instanceof ASTConstructorDeclaration){
												String constructorName = ((ASTConstructorDeclaration) parentNode).getMethodName();
												methodName = "构造函数"+constructorName;
											}
										}else if(x.getFirstParentOfType(ASTClassOrInterfaceDeclaration.class)!=null){											
											Node parentNode = x.getFirstParentOfType(ASTClassOrInterfaceDeclaration.class);
											if(parentNode instanceof ASTClassOrInterfaceDeclaration){
												String className = ((ASTClassOrInterfaceDeclaration) parentNode).getImage();
												methodName = "类"+className + "成员变量";
											}
										}
										
										if (!n.isBackNode()) {
											endline = n.getTreeNode().getBeginLine();
										} else {
											endline = n.getTreeNode().getEndLine();
										}
									}//if(n.getTreeNode()!=null){

									if (fsminstance.getFSMMachine().isVariableRelated()) {
										beginline = fsminstance.getRelatedVariable().getNode().getBeginLine();
										varstr = fsminstance.getRelatedVariable().getImage();
										
									} else {
										varstr = fsminstance.getResultString();
										if (fsminstance.getRelatedObject() != null) {
											beginline = fsminstance.getRelatedObject().getTagTreeNode().getBeginLine();
										} else {
											beginline = n.getTreeNode().getBeginLine();
										}
									}									
									
									if(errorname.equals("UFM")&&null!=n.getCascadeNode())
									{
										if (!n.isBackNode()) {
											endline = n.getCascadeNode().getBeginLine();
										} else {
											endline = n.getCascadeNode().getEndLine();
										}
									}
									String str = "Find an error of " + errorname + " on line " + endline + ",about " + varstr + " which is started on line " + beginline+"\n"+fsminstance.getTraceinfo();
									System.out.println(str);
									String parsefilename = loopdata.parsefilename;
									/*
									if (!parsefilename.contains(":")) {
										parsefilename = System.getProperty("user.dir")+ "\\" + parsefilename;
									}
									*/
									//String preconditions=FSMAnalysisVisitor.getPrecontions(parsefilename,treenode);
									// Change To
									String preconditions = newstateinstance.getPreconditionsByVex();
									fsminstance.fillDescription(beginline, endline);
									
									if (Config.TESTING) {
										System.out.println(preconditions);
										System.out.println(fsminstance.getDescription());
										System.out.println("----------------------------------------------");
									}
									
									String code=DBAccess.getSouceCode(parsefilename, endline, endline);
									if(Config.ISTRIAL){
										loopdata.db.exportErrorDataBuff(fsminstance.getFSMMachine().getModelType(),errorname,id,parsefilename, varstr,beginline, endline,fsminstance.getDescription(),code,preconditions,fsminstance.getTraceinfo(),methodName);
									}else{
										loopdata.db.exportErrorData(fsminstance.getFSMMachine().getModelType(),errorname,id,parsefilename, varstr,beginline, endline,fsminstance.getDescription(),code,preconditions,fsminstance.getTraceinfo(), methodName);
									}
									//测试用，如果出现错误状态，则删除状态机实例
									break out;
								}
							}
							
							FSMStateInstance newstateinstance = stateinstance.transferTo(transition.getToState(), fsminstance, n);
							newstates.addStateInstance(newstateinstance);
							
							b = true;
							stateschanged = true;
							// 调用新状态相关动作
							newstateinstance.getState().invokeRelatedMethod(n,fsminstance);
							break;
						}
					}
					if (!b) {
						// 状态没有变化
						newstates.addStateInstance(stateinstance);
					}
					n.setDomainSet(old);
				}
				fsminstance.setStates(newstates);

				if (newstates.isEmpty()) {
					todelete.add(fsminstance);
					//计算多次时，如果状态机和为空则跳出
					break;
				}
			} while (stateschanged);
		}

		// 删除那些空的状态机
		Iterator<FSMMachineInstance> it = todelete.iterator();
		while (it.hasNext()) {
			n.getFSMMachineInstanceSet().getTable().remove(it.next());
		}

		if (!softtest.config.java.Config.TRACE) {
			iter = list.iterator();
			while (iter.hasNext()) {
				Edge edge = iter.next();
				pre = edge.getTailNode();

				boolean allvisited = true;
				for (Enumeration<Edge> e = pre.getOutedges().elements(); e.hasMoreElements();) {
					Edge tempedge = e.nextElement();
					if (!tempedge.getHeadNode().getVisited() || (tempedge.getHeadNode().getName().startsWith("while_head")) || (tempedge.getHeadNode().getName().startsWith("for_head"))) {
						allvisited = false;
						break;
					}
				}
				if (allvisited && pre.getVisited()) {
					pre.setFSMMachineInstanceSet(null);
				}
			}
		}
	}

	/** 对节点进行访问 */
	public void visit(VexNode n, Object data) {
		List<Edge> list = new ArrayList<Edge>();
		for (Enumeration<Edge> e = n.getInedges().elements(); e.hasMoreElements();) {
			list.add(e.nextElement());
		}

		n.setVisited(true);

		if (list.isEmpty() /*&& n.getSnumber() != 0*/) {
			return;
		}

		calculateIN(n, data);
		Iterator<FSMMachineInstance> i = n.getFSMMachineInstanceSet().getTable().values().iterator();
		while (i.hasNext()) {
			FSMMachineInstance fsmin = i.next();
			if (fsmin.getRelatedObject() != null) {
				fsmin.getRelatedObject().calculateIN(fsmin, n, data);
			}
		}

		i = n.getFSMMachineInstanceSet().getTable().values().iterator();
		while (i.hasNext()) {
			FSMMachineInstance fsmin = i.next();
			if (fsmin.getRelatedObject() != null) {
				fsmin.getRelatedObject().calculateOUT(fsmin, n, data);
			}
		}

		if (!n.getContradict()) {
			calculateOUT(n, data);
		}
	}

	/** 对边进行访问 */
	public void visit(Edge e, Object data) {

	}

	/** 对图进行访问 */
	public void visit(Graph g, Object data) {

	}
}
