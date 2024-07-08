package softtest.fsmanalysis.java;

import softtest.ast.java.*;
import softtest.cfg.java.*;
import softtest.fsm.java.*;
import softtest.rules.java.AbstractStateMachine;
import softtest.config.java.Config;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import softtest.database.java.*;

public class FSMAnalysisVisitor extends JavaParserVisitorAdapter {
	/** 待分析状态机集合 */
	private List<FSMMachine> fsms = new LinkedList<FSMMachine>();

	/** 迭代次数控制 */
	public static int LOOP_NUM = 1;
	
	private static String methodName = null;
	
	public static String getMethodName(){
		return methodName;
	}

	/** 增加fsm到状态机集合 */
	public void addFSMS(FSMMachine fsm) {
		fsms.add(fsm);
	}
	
	public void clearFSMS(){
		fsms.clear();
	}

	@Override
	public Object visit(ASTCompilationUnit treenode, Object data) {
		FSMControlFlowData loopdata = (FSMControlFlowData) data;
		Iterator<FSMMachine> i = fsms.iterator();
		List list = null;
		while (i.hasNext()) {
			FSMMachine fsm = i.next();
			if(fsm.isCreatedByFile()){
				//以文件为单位创建的状态机
				try {
					Object[] args = new Object[2];
					args[0] = treenode;
					args[1] = fsm;
					list = (List) fsm.getRelatedMethod().invoke(null, args);
					for (Object o : list) {
						runStateMachine((FSMMachineInstance) o, null, loopdata);
					}
				} catch (Exception e) {
					if(softtest.config.java.Config.DEBUG){
						e.printStackTrace();
					}
					throw new RuntimeException("Can't create FSM instances.",e);
				}	
			}
		}
		return super.visit(treenode, data);
	}

	@Override
	public Object visit(ASTConstructorDeclaration treenode, Object data) {
				
		Graph g = treenode.getGraph();
//		System.out.println("路径数目为："+g.getPathcount());
		FSMControlFlowData loopdata = (FSMControlFlowData) data;
		List list = null;
		
//		System.out.println("我在visit构造函数");
		
		if (g == null) {
			return null;
		}
		Iterator<FSMMachine> i = fsms.iterator();
		while (i.hasNext()) {
			FSMMachine fsm = i.next();
			if(fsm.isCreatedByFile()){
				//以文件为单位创建的状态机
				continue;
			}
			if (fsm.isPathSensitive()) {
				//路径相关
				try {
					Object[] args = new Object[2];
					args[0] = treenode;
					args[1] = fsm;
					list = (List) fsm.getRelatedMethod().invoke(null, args);
					treenode.getVexNode().get(0).getFSMMachineInstanceSet().addFSMMachineInstances(list);
				} catch (Exception e) {
					if(softtest.config.java.Config.DEBUG){
						e.printStackTrace();
					}
					throw new RuntimeException("Can't create FSM instances.",e);
				}
			} else {
				//路径无关
				try {
					Object[] args = new Object[2];
					args[0] = treenode;
					args[1] = fsm;
					list = (List) fsm.getRelatedMethod().invoke(null, args);
					for (Object o : list) {
						runStateMachine((FSMMachineInstance) o, treenode.getVexNode().get(0), loopdata);
					}
				} catch (Exception e) {
					if(softtest.config.java.Config.DEBUG){
						e.printStackTrace();
					}
					throw new RuntimeException("Can't create FSM instances.",e);
				}
			}
		}
		//System.out.println("2路径数目为：");
		//System.out.println("路径数目为："+g.getPathcount());
		//	控制流迭代
		for (int j = 0; j < LOOP_NUM; j++) {
			// g.dfs(new FSMControlFlowVisitor(), null);
			if (j == LOOP_NUM - 1) {
				loopdata.reporterror = true;
			} else {
				loopdata.reporterror = false;
			}
			
			if(softtest.config.java.Config.PATH_SENSITIVE==0){
				g.numberOrderVisit(new FSMPathInsensitiveVisitor(), loopdata);
				//System.out.println("0路径数目为："+g.getPathcount());
			}
			else if(softtest.config.java.Config.PATH_SENSITIVE==2 && g.getPathcount() >0 &&g.getPathcount() < softtest.config.java.Config.PATH_LIMIT){
				g.numberOrderVisit(new FSMControlFlowVisitor(), loopdata);
				//System.out.println("2路径数目为："+g.getPathcount());
			}
			else {
				// 把全路径敏感转换为状态合并
				int old = softtest.config.java.Config.PATH_SENSITIVE;
				softtest.config.java.Config.PATH_SENSITIVE = 1;
				g.numberOrderVisit(new FSMControlFlowVisitor(), loopdata);
				softtest.config.java.Config.PATH_SENSITIVE = old;
				//System.out.println("1路径数目为："+g.getPathcount());
			}
			g.clearVisited();			
		}

		//	输出到文件，测试用
		if (softtest.config.java.Config.CFGTRACE) {
			SimpleJavaNode simplejavanode = (SimpleJavaNode) treenode.jjtGetParent().jjtGetParent().jjtGetParent();
			String name = null;
			if (treenode.getType()!=null) {
				name = softtest.config.java.Config.DEBUGPATH + (treenode.getType());
			} else {
				name = softtest.config.java.Config.DEBUGPATH + simplejavanode.getImage();
			}
			name = name.replace(' ', '_');
			g.accept(new DumpEdgesVisitor(), name + "_.dot");
			System.out.println("控制流图输出到了文件 "+name + "_.dot");
			try {
				java.lang.Runtime.getRuntime().exec("dot -Tjpg -o " + name + "_.jpg " + name + "_.dot").waitFor();
			} catch (IOException e1) {
				System.out.println(e1);
			} catch (InterruptedException e2) {
				System.out.println(e2);
			}
			System.out.println("控制流图打印到了文件" + name + "_.jpg");
		}
		
		g.numberOrderVisit(new ClearFSMControlFlowVisitor(), null);
		g.clearVisited();
		return null;
	}

	@Override
	public Object visit(ASTMethodDeclaration treenode, Object data) {
		Graph g = treenode.getGraph();
		FSMControlFlowData loopdata = (FSMControlFlowData) data;
		if (g == null) {
			return null;
		}
		List list = null;
		Iterator<FSMMachine> i = fsms.iterator();
				
		while (i.hasNext()) {
			FSMMachine fsm = i.next();
			if(fsm.isCreatedByFile()){
				//以文件为单位创建的状态机
				continue;
			}
			if (fsm.isPathSensitive()) {
				try {
					Object[] args = new Object[2];
					args[0] = treenode;
					args[1] = fsm;
					list = (List) fsm.getRelatedMethod().invoke(null, args);
					treenode.getVexNode().get(0).getFSMMachineInstanceSet().addFSMMachineInstances(list);
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException("Can't create FSM instances.",e);
				}
			} else {
				//路径无关
				try {
					Object[] args = new Object[2];
					args[0] = treenode;
					args[1] = fsm;
					//logM( treenode.printNode());
					list = (List) fsm.getRelatedMethod().invoke(null, args);
					for (Object o : list) {
						runStateMachine((FSMMachineInstance) o, treenode.getVexNode().get(0), loopdata);
					}
				} catch (Exception e) {
					if(softtest.config.java.Config.DEBUG){
						
					}
					e.printStackTrace();
					throw new RuntimeException("Can't create FSM instances.",e);
				}
			}
		}
		//	控制流迭代
		for (int j = 0; j < LOOP_NUM; j++) {
			// g.dfs(new FSMControlFlowVisitor(), null);
			if (j == LOOP_NUM - 1) {
				loopdata.reporterror = true;
			} else {
				loopdata.reporterror = false;
			}
			if(softtest.config.java.Config.PATH_SENSITIVE==0){
				g.numberOrderVisit(new FSMPathInsensitiveVisitor(), loopdata);
			}
			else if(softtest.config.java.Config.PATH_SENSITIVE==2 && g.getPathcount() >0 && g.getPathcount() < softtest.config.java.Config.PATH_LIMIT){
				g.numberOrderVisit(new FSMControlFlowVisitor(), loopdata);
			}
			else {
				// 把全路径敏感转换为状态合并
				int old = softtest.config.java.Config.PATH_SENSITIVE;
				softtest.config.java.Config.PATH_SENSITIVE = 1;
				g.numberOrderVisit(new FSMControlFlowVisitor(), loopdata);
				softtest.config.java.Config.PATH_SENSITIVE = old;
			}
			g.clearVisited();

		}
				
		// 输出到文件，测试用
		if (softtest.config.java.Config.CFGTRACE) {
			SimpleJavaNode simplejavanode = (SimpleJavaNode) treenode.jjtGetChild(1);
			String name = null;
			if (treenode.getType()!=null) {
				name = softtest.config.java.Config.DEBUGPATH + (treenode.getType());
			} else {
				name = softtest.config.java.Config.DEBUGPATH + simplejavanode.getImage();
			}
			name = name.replace(' ', '_');
			g.accept(new DumpEdgesVisitor(), name + "_.dot");
			System.out.println("控制流图输出到了文件 " + name + "_.dot");
			try {
				java.lang.Runtime.getRuntime().exec("dot -Tjpg -o " + name + "_.jpg " + name + "_.dot").waitFor();
			} catch (IOException e1) {
				System.out.println(e1);
			} catch (InterruptedException e2) {
				System.out.println(e2);
			}
			System.out.println("控制流图打印到了文件" + name + "_.jpg");
		}
		
		g.numberOrderVisit(new ClearFSMControlFlowVisitor(), null);
		g.clearVisited();
		return null;
	}
	
	/**
	 * @param parsefilename 当前分析文件名
	 * @param treenode 错误点对应的语法树节点
	 * @return 返回函数口到错误点经过的所有条件判断代码
	 */
	public static String getPrecontions(String parsefilename,SimpleJavaNode treenode){
		Node entrynode=treenode.getFirstParentOfTypes(new Class[]{ASTMethodDeclaration.class,ASTConstructorDeclaration.class});
		Graph g=null;
		VexNode fromvex=null,tovex=treenode.getCurrentVexNode();
		if(entrynode instanceof ASTMethodDeclaration){
			ASTMethodDeclaration m=(ASTMethodDeclaration)entrynode;
			g=m.getGraph();
			fromvex=m.getCurrentVexNode();
		}else if(entrynode instanceof ASTConstructorDeclaration){
			ASTConstructorDeclaration m=(ASTConstructorDeclaration)entrynode;
			g=m.getGraph();
			fromvex=m.getCurrentVexNode();
		}
		if(g!=null&&fromvex!=null&&tovex!=null){
			List<VexNode> list=Graph.findAPath(fromvex, tovex);
			StringBuffer buffer=new StringBuffer();
			if(list.size()<2){
				return "";
			}
			ArrayList<Edge> elist=new ArrayList<Edge> ();
			VexNode vex=list.get(0),next;
			for(int i=1;i<list.size();i++){
				next=list.get(i);
				Edge e=vex.getEdgeByHead(next);
				if(e==null){
					return "";
				}
				elist.add(e);
				vex=next;	
			}
			
			for(Edge e:elist){
				if(e.getName().startsWith("T_")){
					SimpleJavaNode node=e.getTailNode().getTreeNode();
					if(node instanceof ASTIfStatement || node instanceof ASTWhileStatement ){
						node=(SimpleJavaNode)node.jjtGetChild(0);
						buffer.append(""+node.getBeginLine()+": ("+DBAccess.getSouceCode(parsefilename, node.getBeginLine(), node.getBeginColumn(), node.getEndLine(), node.getEndColumn())+")==true\n");
					}else if(node instanceof ASTDoStatement){
						node=(SimpleJavaNode)node.jjtGetChild(1);
						buffer.append(""+node.getBeginLine()+": ("+DBAccess.getSouceCode(parsefilename, node.getBeginLine(), node.getBeginColumn(), node.getEndLine(), node.getEndColumn())+")==true\n");
					}else if(node instanceof ASTCatchStatement){
						node=(SimpleJavaNode)node.jjtGetChild(1);
						buffer.append(""+node.getBeginLine()+": ("+DBAccess.getSouceCode(parsefilename, node.getBeginLine(), node.getBeginColumn(), node.getEndLine(), node.getEndColumn())+") catched\n");
					}
				}else if (e.getName().startsWith("F_")){
					SimpleJavaNode node=e.getTailNode().getTreeNode();
					if(node instanceof ASTIfStatement || node instanceof ASTWhileStatement ){
						node=(SimpleJavaNode)node.jjtGetChild(0);
						buffer.append(""+node.getBeginLine()+": ("+DBAccess.getSouceCode(parsefilename, node.getBeginLine(), node.getBeginColumn(), node.getEndLine(), node.getEndColumn())+")==false\n");
					}else if(node instanceof ASTDoStatement){
						node=(SimpleJavaNode)node.jjtGetChild(1);
						buffer.append(""+node.getBeginLine()+": ("+DBAccess.getSouceCode(parsefilename, node.getBeginLine(), node.getBeginColumn(), node.getEndLine(), node.getEndColumn())+")==false\n");
					}else if(node instanceof ASTCatchStatement){
						node=(SimpleJavaNode)node.jjtGetChild(1);
						buffer.append(""+node.getBeginLine()+": ("+DBAccess.getSouceCode(parsefilename, node.getBeginLine(), node.getBeginColumn(), node.getEndLine(), node.getEndColumn())+") not catched\n");
					}
				}else if (e.getName().startsWith("E_")){
					SimpleJavaNode node=(SimpleJavaNode)e.getTailNode().getTreeNode().getConcreteNode();
					if(node!=null){
						buffer.append(""+node.getBeginLine()+": ("+DBAccess.getSouceCode(parsefilename, node.getBeginLine(), node.getBeginColumn(), node.getEndLine(), node.getEndColumn())+") throw an exception\n");
					}
				}
			}
			return buffer.toString();
		}
		return "";
	}

	private void runStateMachine(FSMMachineInstance fsminstance, VexNode n, FSMControlFlowData loopdata) {
		boolean stateschanged = false;
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
						//System.out.println("AnalysisVisitor------runStateMachine");
						//System.out.println(loopdata.parsefilename);
						if (transition.getToState().isError() && loopdata != null) {
							FSMStateInstance newstateinstance = stateinstance.transferTo(transition.getToState(), fsminstance, n);
							
							String parsefilename = loopdata.parsefilename;

							SimpleJavaNode treenode = null;
							String methodName = null;
							String varstr = "";
							int beginline = 0, errorline = 0;
							int id=0;
							//System.out.println("AnalysisVistor-----获取出错行数");
							if (fsminstance.getRelatedVariable() != null) {
								treenode = (SimpleJavaNode) fsminstance	.getRelatedVariable().getNode();
								beginline = treenode.getBeginLine();
								errorline = treenode.getBeginLine();
								varstr = fsminstance.getRelatedVariable().getImage();
								id =treenode.getId();
							}
														
							if (fsminstance.getRelatedObject() != null) {
								treenode = fsminstance.getRelatedObject().getTagTreeNode();
								if (beginline == 0) {
									beginline = treenode.getBeginLine();
								}
								errorline = treenode.getBeginLine();
								varstr = fsminstance.getResultString();	
								id =treenode.getId();
								//获取出错变量所在的方法 added by Ruiqiang 2013-04-08
								if(treenode.getFirstParentOfType(ASTMethodDeclaration.class)!=null){
									Node parentNode = treenode.getFirstParentOfType(ASTMethodDeclaration.class);								
									if(parentNode instanceof ASTMethodDeclaration){
										methodName = ((ASTMethodDeclaration) parentNode).getMethodName();
									}
								}else if(treenode.getFirstParentOfType(ASTConstructorDeclaration.class)!=null){
									Node parentNode = treenode.getFirstParentOfType(ASTConstructorDeclaration.class);
									if(parentNode instanceof ASTConstructorDeclaration){
										String constructorName = ((ASTConstructorDeclaration) parentNode).getMethodName();
										methodName = "构造函数"+constructorName;
									}
								}else if(treenode.getFirstParentOfType(ASTClassOrInterfaceDeclaration.class)!=null){
									//System.out.println("AnalysisVistor-----find Class");
									Node parentNode = treenode.getFirstParentOfType(ASTClassOrInterfaceDeclaration.class);
									if(parentNode instanceof ASTClassOrInterfaceDeclaration){
										String className = ((ASTClassOrInterfaceDeclaration) parentNode).getImage();
										methodName = "类"+className + "成员变量";
									}
								}
								//System.out.println("AnalysisVistor-----出来了");
							}
							// String preconditions=FSMAnalysisVisitor.getPrecontions(parsefilename,treenode);
							// Change To
							String preconditions = newstateinstance.getPreconditionsByVex();

							fsminstance.fillDescription(beginline, errorline);
							
							String str = "Find an error of " + fsminstance.getFSMMachine().getName() + " on line " + errorline + " about " + varstr + "\n" + fsminstance.getTraceinfo();
							System.out.println(str);
							if (Config.TESTING) {
								System.out.println(preconditions);
								System.out.println(fsminstance.getDescription());
							}
							String code = DBAccess.getSouceCode(parsefilename,errorline, errorline);
							if(Config.ISTRIAL){
								loopdata.db.exportErrorDataBuff(fsminstance.getFSMMachine().getModelType(),fsminstance.getFSMMachine().getName(),id,parsefilename, varstr, beginline, errorline, fsminstance.getDescription(),code, preconditions, fsminstance.getTraceinfo(), methodName);
							}else{
								loopdata.db.exportErrorData(fsminstance.getFSMMachine().getModelType(),fsminstance.getFSMMachine().getName(),id,parsefilename, varstr, beginline, errorline, fsminstance.getDescription(),code, preconditions, fsminstance.getTraceinfo(), methodName);
								//System.out.println("AnalysisVisitor------++ 错误写入数据库");
							}
						}
						
						FSMStateInstance newstateinstance = stateinstance.transferTo(transition.getToState(), fsminstance, n);
						newstates.addStateInstance(newstateinstance);
						
						b = true;
						stateschanged = true;
						//调用新状态相关动作
						newstateinstance.getState().invokeRelatedMethod(n,fsminstance);
					}
				}
				if (!b) {
					// 状态没有变化
					newstates.addStateInstance(stateinstance);
				}
			}
			fsminstance.setStates(newstates);
		} while (stateschanged);
	}
	
	public void logM(String str) {
		logc("visit(MthDecl) - " + str);
	}
	public void logc(String str) {
		if( Config.DEBUG ) {
			System.out.println("FSMAnalysisVisitor::" + str);
		}
	}
}
