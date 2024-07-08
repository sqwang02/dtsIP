package softtest.fsmanalysis.java;

import java.util.*;

import softtest.ast.java.*;
import softtest.cfg.java.*;
import softtest.config.java.Config;
import softtest.database.java.*;
import softtest.fsm.java.*;



public class FSMPathInsensitiveVisitor implements GraphVisitor {
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
			
			if (edge.getContradict()) {
				continue;
			}
			
			if (edge.getName().startsWith("F")) {
				// 处理一次循环
				if (n.getName().startsWith("while_out") || n.getName().startsWith("for_out")) {
					FSMControlFlowData loopdata = (FSMControlFlowData) data;
					loopdata.reporterror = false;
					visit(pre, loopdata);
					loopdata.reporterror = true;
				}

				n.mergFSMMachineInstancesWithoutConditon(pre.getFSMMachineInstanceSet());
			}else{
				n.mergFSMMachineInstancesWithoutConditon(pre.getFSMMachineInstanceSet());
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

		SimpleJavaNode treenode = n.getTreeNode();
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
				while (is.hasNext()) {
					FSMStateInstance stateinstance = is.next();
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
									
									//获取出错点所在的函数，  added by Ruiqiang 2013-04-09
									String methodName = null;	
									int beginline = 0, endline = 0;
									int id=0;
									String varstr = "", errorname = fsminstance.getFSMMachine().getName();
									
									if(n.getTreeNode() != null){
										//System.out.println("PahInsensitiveVisitor-----进入所在的方法");
										SimpleNode tempTreeNode = n.getTreeNode();
										if(tempTreeNode.getFirstParentOfType(ASTMethodDeclaration.class) != null){
											//System.out.println("PahInsensitiveVisitor-----find Method");
											Node parentNode = tempTreeNode.getFirstParentOfType(ASTMethodDeclaration.class);
											//Node methodNode = parentNode.jjtGetChild(1);
											if(parentNode instanceof ASTMethodDeclaration){
												methodName = ((ASTMethodDeclaration) parentNode).getMethodName();
											}
										}else if(tempTreeNode.getFirstParentOfType(ASTConstructorDeclaration.class)!=null){
											//System.out.println("PahInsensitiveVisitor-----find Constructor");
											Node parentNode = tempTreeNode.getFirstParentOfType(ASTConstructorDeclaration.class);
											if(parentNode instanceof ASTConstructorDeclaration){
												String constructorName = ((ASTConstructorDeclaration) parentNode).getMethodName();
												methodName = "构造函数"+constructorName;
											}
										}else if(tempTreeNode.getFirstParentOfType(ASTClassOrInterfaceDeclaration.class)!=null){
											//System.out.println("PahInsensitiveVisitor-----find Class");
											Node parentNode = tempTreeNode.getFirstParentOfType(ASTClassOrInterfaceDeclaration.class);
											if(parentNode instanceof ASTClassOrInterfaceDeclaration){
												String className = ((ASTClassOrInterfaceDeclaration) parentNode).getImage();
												methodName = "类"+className + "成员变量";
											}
										}
										//System.out.println("PahInsensitiveVisitor-----出来了");
										if (!n.isBackNode()) {
											endline = n.getTreeNode().getBeginLine();
										} else {
											endline = n.getTreeNode().getEndLine();
										}
									}
									
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

									String str = "Find an error of " + errorname + " on line " + endline + ",about " + varstr + " which is started on line " + beginline+"\n"+fsminstance.getTraceinfo();
									System.out.println(str);
									String parsefilename = loopdata.parsefilename;
									/*
									if (!parsefilename.contains(":")) {
										parsefilename = System.getProperty("user.dir")+ "\\" + parsefilename;
									}
									*/
									// String preconditions=FSMAnalysisVisitor.getPrecontions(parsefilename,treenode);
									// Change To
									String preconditions = newstateinstance.getPreconditionsByVex();
									fsminstance.fillDescription(beginline, endline);
									
									if (Config.TESTING) {
										System.out.println(preconditions);
										System.out.println(fsminstance.getDescription());
									}
									
									String code=DBAccess.getSouceCode(parsefilename, endline, endline);
									if(Config.ISTRIAL){
										loopdata.db.exportErrorDataBuff(fsminstance.getFSMMachine().getModelType(),errorname,id, parsefilename, varstr,beginline, endline,fsminstance.getDescription(),code,preconditions,fsminstance.getTraceinfo(),methodName);
									}else{
										loopdata.db.exportErrorData(fsminstance.getFSMMachine().getModelType(),errorname, id,parsefilename, varstr,beginline, endline,fsminstance.getDescription(),code,preconditions,fsminstance.getTraceinfo(),methodName);
									}
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
