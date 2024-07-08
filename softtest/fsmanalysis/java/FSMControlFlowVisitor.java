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

		// ����ǰ���ڵ��U(in)
		VexNode pre = null;
		Iterator<Edge> iter = list.iterator();
		while (iter.hasNext()) {
			Edge edge = iter.next();
			pre = edge.getTailNode();

			// �жϷ�֧�Ƿ�ì��

			if (edge.getContradict()) {
				continue;
			}

			if (edge.getName().startsWith("T")) {
				FSMMachineInstanceSet temp = new FSMMachineInstanceSet(pre.getFSMMachineInstanceSet());
				temp.calCondition(pre, true);
				n.mergeFSMMachineInstances(temp);
			} else if (edge.getName().startsWith("F")) {
				// ����һ��ѭ��
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
			// ���¼���״̬����
			n.getFSMMachineInstanceSet().calDomainSet(n);
		}
		// ���㵱ǰ�ڵ��Out

		List<FSMMachineInstance> todelete = new ArrayList<FSMMachineInstance>();
		Iterator<FSMMachineInstance> i = n.getFSMMachineInstanceSet().getTable().values().iterator();
		while (i.hasNext()) {
			// �������е�״̬��ʵ��
			FSMMachineInstance fsminstance = i.next();
			// �����µ�״̬ʵ������
			boolean stateschanged = false;
			//������״̬�仯
			do {
				stateschanged = false;

				FSMStateInstanceSet newstates = new FSMStateInstanceSet();
				// ������ǰ״̬��ʵ��������״̬ʵ��
				Iterator<FSMStateInstance> is = fsminstance.getStates().getTable().values().iterator();
	out:		while (is.hasNext()) {
					FSMStateInstance stateinstance = is.next();
					DomainSet old = n.getDomainSet();
					n.setDomainSet(stateinstance.getDomainSet());
					FSMState state = stateinstance.getState();

					Hashtable<String, FSMTransition> trans = state.getOutTransitions();
					// ���㵱ǰ״̬ʵ�������п���״̬ת��					
					boolean b = false;
					for (Enumeration<FSMTransition> e = trans.elements(); e.hasMoreElements();) {
						FSMTransition transition = e.nextElement();
						if (transition.evaluate(fsminstance, stateinstance, n)) {
							// ��Ҫ״̬ת��
							if (transition.getToState().isError()) {
								FSMControlFlowData loopdata = (FSMControlFlowData) data;
								if (loopdata != null && loopdata.reporterror) {
									FSMStateInstance newstateinstance = stateinstance.transferTo(transition.getToState(), fsminstance, n);
									/*
									FSMStateInstance newstateinstance = new FSMStateInstance(transition.getToState());
									newstateinstance.setDomainSet(stateinstance.getDomainSet());
									newstateinstance.setSiteNode(n);
									newstateinstance.setSwitchPoints(stateinstance.getSwitchPoints());
									
									// StateMachine.java�Զ����״̬ת����������
									if (fsminstance.getSwitchLine() != null && fsminstance.getSwitchLine().length() > 0) {
										newstateinstance.addSwitchPoint(fsminstance.getSwitchLine());
									}
									// �õ�ǰ�������ڵ���������Ϊ״̬ת������ 
									else if (fsminstance.getFSMMachine().isPathSensitive()){
										if (n.isBackNode()) {
											newstateinstance.addSwitchPoint("\""+n.getTreeNode().printNode(ProjectAnalysis.getCurrent_file(),n.getTreeNode().getEndLine())+"\" at Line:"+n.getTreeNode().getEndLine()+" File:"+ProjectAnalysis.getCurrent_file());
										}
										else {
											newstateinstance.addSwitchPoint("\""+n.getTreeNode().printNode(ProjectAnalysis.getCurrent_file())+"\" at Line:"+n.getTreeNode().getBeginLine()+" File:"+ProjectAnalysis.getCurrent_file());
										}
									}
									*/
									//��ȡ����������ڵķ��� added by Ruiqiang 2013-04-08
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
												methodName = "���캯��"+constructorName;
											}
										}else if(x.getFirstParentOfType(ASTClassOrInterfaceDeclaration.class)!=null){											
											Node parentNode = x.getFirstParentOfType(ASTClassOrInterfaceDeclaration.class);
											if(parentNode instanceof ASTClassOrInterfaceDeclaration){
												String className = ((ASTClassOrInterfaceDeclaration) parentNode).getImage();
												methodName = "��"+className + "��Ա����";
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
									//�����ã�������ִ���״̬����ɾ��״̬��ʵ��
									break out;
								}
							}
							
							FSMStateInstance newstateinstance = stateinstance.transferTo(transition.getToState(), fsminstance, n);
							newstates.addStateInstance(newstateinstance);
							
							b = true;
							stateschanged = true;
							// ������״̬��ض���
							newstateinstance.getState().invokeRelatedMethod(n,fsminstance);
							break;
						}
					}
					if (!b) {
						// ״̬û�б仯
						newstates.addStateInstance(stateinstance);
					}
					n.setDomainSet(old);
				}
				fsminstance.setStates(newstates);

				if (newstates.isEmpty()) {
					todelete.add(fsminstance);
					//������ʱ�����״̬����Ϊ��������
					break;
				}
			} while (stateschanged);
		}

		// ɾ����Щ�յ�״̬��
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

	/** �Խڵ���з��� */
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

	/** �Ա߽��з��� */
	public void visit(Edge e, Object data) {

	}

	/** ��ͼ���з��� */
	public void visit(Graph g, Object data) {

	}
}
