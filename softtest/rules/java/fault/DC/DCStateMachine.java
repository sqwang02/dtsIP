package softtest.rules.java.fault.DC;

import softtest.fsm.java.*;
import softtest.jaxen.java.DocumentNavigator;

import java.util.*;

import org.jaxen.*;

import softtest.ast.java.*;
import softtest.jaxen.java.*;
import softtest.rules.java.AbstractStateMachine;
import softtest.symboltable.java.*;

import softtest.ast.java.ASTAdditiveExpression;
import softtest.ast.java.ASTAssignmentOperator;
import softtest.ast.java.ASTBooleanLiteral;
import softtest.ast.java.ASTCatchStatement;
import softtest.ast.java.ASTClassOrInterfaceDeclaration;
import softtest.ast.java.ASTDoStatement;
import softtest.ast.java.ASTEqualityExpression;
import softtest.ast.java.ASTExpression;
import softtest.ast.java.ASTExtendsList;
import softtest.ast.java.ASTForInit;
import softtest.ast.java.ASTForStatement;
import softtest.ast.java.ASTForUpdate;
import softtest.ast.java.ASTLiteral;
import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.ASTMethodDeclarator;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTRelationalExpression;
import softtest.ast.java.ASTStatementExpression;
import softtest.ast.java.ASTTryStatement;
import softtest.ast.java.ASTType;
import softtest.ast.java.ASTWhileStatement;
import softtest.ast.java.SimpleJavaNode;
import softtest.ast.java.SimpleNode;
import softtest.callgraph.java.*;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.IntervalAnalysis.java.*;
import softtest.cfg.java.*;
import softtest.domain.java.*;

public class DCStateMachine  extends AbstractStateMachine{
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(fsmmi.getRelatedObject().getTagTreeNode() instanceof ASTForStatement){
			if(softtest.config.java.Config.LANGUAGE==0){
				f.format("死循环: %d 行的 \'%s\' 循环可能为一个死循环", errorline, fsmmi.getResultString());
			}else{
				f.format("Dead Cycle: the \'%s\' loop statement at line %d may be a dead cycle.", fsmmi.getResultString(),errorline);
			}
		}else if(fsmmi.getRelatedObject().getTagTreeNode() instanceof ASTWhileStatement){
			if(softtest.config.java.Config.LANGUAGE==0){
				f.format("死循环: %d 行的 \'%s\' 循环可能为一个死循环", errorline, fsmmi.getResultString());
			}else{
				f.format("Dead Cycle: the \'%s\' loop statement at line %d may be a dead cycle.", fsmmi.getResultString(),errorline);
			}
		}else if(fsmmi.getRelatedObject().getTagTreeNode() instanceof ASTDoStatement){
			if(softtest.config.java.Config.LANGUAGE==0){
				f.format("死循环: %d 行的 \'%s\' 循环可能为一个死循环", errorline, fsmmi.getResultString());
			}else{
				f.format("Dead Cycle: the \'%s\' loop statement at line %d may be a dead cycle.", fsmmi.getResultString(),errorline);
			}
		}else {
			if(softtest.config.java.Config.LANGUAGE==0){
				f.format("死循环: %d 行的 \'%s\' 方法可能为一个死循环递归调用", errorline, fsmmi.getResultString());
			}else{
				f.format("Dead Cycle: the recursive method \'%s\' at line %d may be a dead cycle.", fsmmi.getResultString(),errorline);
			}
		}
		fsmmi.setDescription(f.toString());
		f.close();
	}
	@Override
	public  void registerPrecondition(PreconditionListenerSet listeners) {
	}
	
	@Override
	public  void registerFeature(FeatureListenerSet listenerSet) {
	}
	
	/** 在node上查询xPath，返回选中的节点列表 */
	private static List findTreeNodes(SimpleJavaNode node, String xPath) {
		List evaluationResults = null;
		try {
			XPath xpath = new BaseXPath(xPath, new DocumentNavigator());
			evaluationResults = xpath.selectNodes(node);
		} catch (JaxenException e) {
			// e.printStackTrace();
			throw new RuntimeException("xpath error",e);
		}
		return evaluationResults;
	}
	
	private static boolean isOnlyContainOneVar(SimpleNode node,VariableNameDeclaration v){
		VexNode vex=null;
		if(node!=null){
			vex=node.getCurrentVexNode();
		}
		if(vex!=null){
			for(NameOccurrence occ:vex.getOccurrences()){
				if(!occ.getLocation().isSelOrAncestor(node)){
					continue;
				}
				if(occ.getDeclaration()!=v){
					return false;
				}
			}
			return true;
		}
		return  false;
	}

	/** 检查for while是否为没有其他出口的循环语句 */
	private static boolean checkForOrWhileNoJump(VexNode head, VexNode out) {
		Hashtable<VexNode, VexNode> table = new Hashtable<VexNode, VexNode>();
		Stack<VexNode> stack = new Stack<VexNode>();
		
		
		//检察是否存在异常catch子句
		List list=findTreeNodes(head.getTreeNode(),".//CatchStatement");
		Iterator i=list.iterator();
		while(i.hasNext()){
			ASTCatchStatement ca=(ASTCatchStatement)i.next();
			if(ca.hasLocalMethod(head.getTreeNode())){
				continue;
			}
			stack.push(ca.getCurrentVexNode());
		}
		
		stack.push(head);
		
		while (!stack.empty()) {
			VexNode vex = stack.pop();
			table.put(vex, vex);
			for (Enumeration<Edge> e = vex.getOutedges().elements(); e.hasMoreElements();) {
				Edge edge = e.nextElement();
				VexNode hvex = edge.getHeadNode(), tvex = edge.getTailNode();
				if ((hvex.getSnumber() >= out.getSnumber() && tvex != head) || (hvex.getSnumber() < head.getSnumber())) {
					return false;
				}
				if (hvex.getSnumber() < out.getSnumber() && hvex.getSnumber() > head.getSnumber() && !table.contains(hvex)) {
					stack.push(hvex);
				}
			}
		}
		//检察是否存在exit函数调用
		if(findTreeNodes(head.getTreeNode(),".//PrimaryExpression/PrimaryPrefix/Name[matches(@Image,\'^((.+\\.exit)|(exit))$\')]").size()>0){
			return false;
		}

		return true;
	}

	/** 检查do-while是否为没有其他出口的循环语句 */
	private static boolean checkDoWhileNoJump(VexNode head, VexNode out1, VexNode out2) {
		Hashtable<VexNode, VexNode> table = new Hashtable<VexNode, VexNode>();
		Stack<VexNode> stack = new Stack<VexNode>();
		
		//检察是否存在异常catch子句
		List list=findTreeNodes(head.getTreeNode(),".//CatchStatement");
		Iterator i=list.iterator();
		while(i.hasNext()){
			ASTCatchStatement ca=(ASTCatchStatement)i.next();
			if(ca.hasLocalMethod(head.getTreeNode())){
				continue;
			}
			stack.push(ca.getCurrentVexNode());
		}
		
		stack.push(head);

		while (!stack.empty()) {
			VexNode vex = stack.pop();
			table.put(vex, vex);
			for (Enumeration<Edge> e = vex.getOutedges().elements(); e.hasMoreElements();) {
				Edge edge = e.nextElement();
				VexNode hvex = edge.getHeadNode(), tvex = edge.getTailNode();
				if ((hvex.getSnumber() >= out2.getSnumber() && tvex != out1) || (hvex.getSnumber() < head.getSnumber())) {
					return false;
				}
				if (hvex.getSnumber() < out2.getSnumber() && hvex.getSnumber() > head.getSnumber() && !table.contains(hvex)) {
					stack.push(hvex);
				}
			}
		}
		//检察是否存在exit函数调用
		if(findTreeNodes(head.getTreeNode(),".//PrimaryExpression/PrimaryPrefix/Name[matches(@Image,\'^((.+\\.exit)|(exit))$\')]").size()>0){
			return false;
		}
		return true;
	}

	/** 得到表达式expr的must集合 */
	private static DomainSet getTrueMustDomainSet(ASTExpression expr) {
		DomainSet ds = new DomainSet();
		VexNode n = expr.getCurrentVexNode();
		if (n != null) {
			ConditionData condata = n.getConditionData();
			if (condata != null) {
				ds = condata.getTrueMustDomainSet();
			}
		}
		return ds;
	}

	/** 检察caller是不是每条路径上都调用了callee */
	private static boolean checkDominateCall(ASTMethodDeclaration caller, Hashtable<CVexNode, CVexNode> table) {
		Graph g = caller.getGraph();
		if(g==null){
			return false;
		}
		g.clearVisited();
		
		// 在每个调用点上做上标记
		
		for (Enumeration<CVexNode> e = table.elements(); e.hasMoreElements();) {
			ASTMethodDeclaration callee = e.nextElement().getMethodDeclaration();
			ASTMethodDeclarator declaratorcallee = (ASTMethodDeclarator) callee.getFirstDirectChildOfType(ASTMethodDeclarator.class);
			MethodNameDeclaration declcallee = declaratorcallee.getMethodNameDeclaration();
			ArrayList listocc = (ArrayList) declcallee.getScope().getEnclosingClassScope().getMethodDeclarations().get(declcallee);
			Iterator i = listocc.iterator();
			while (i.hasNext()) {
				NameOccurrence occ = (NameOccurrence) i.next();
				VexNode vex = occ.getLocation().getCurrentVexNode();
				SimpleNode name=occ.getLocation();
				if(name.getImage()==null||name.getImage().contains(".")){
					continue;
				}
				if (vex != null && g.nodes.get(vex.getName()) == vex) {
					vex.setVisited(true);
				}
			}
		}

		// 检察是否还存在入口到出口的路径
		boolean bpath = false;
		VexNode entry = g.getEntryNode(), exit = g.getExitNode();
		Stack<VexNode> stack = new Stack<VexNode>();

		entry.setVisited(true);
		stack.push(entry);
		while (!stack.isEmpty()) {
			VexNode next = g.getAdjUnvisitedVertex(stack.peek());
			if (next == null) {
				stack.pop();
			} else {
				if (next == exit) {
					bpath = true;
				}
				next.setVisited(true);
				stack.push(next);
			}
		}
		g.clearVisited();
		return !bpath;
	}
	
	/**检查是否包含相同参数个数的同名函数*/
	private static boolean hasSameNameAndParam(ASTMethodDeclaration method){
		ASTMethodDeclarator declarator = (ASTMethodDeclarator) method.getFirstDirectChildOfType(ASTMethodDeclarator.class);
		MethodNameDeclaration decl = declarator.getMethodNameDeclaration();
		ClassScope classscope=method.getScope().getEnclosingClassScope();
		if(classscope==null||classscope.getMethodDeclarations()==null){
			return false;
		}
		for(Object o :classscope.getMethodDeclarations().keySet()){
			MethodNameDeclaration tempdecl=(MethodNameDeclaration)o;
			if(tempdecl!=decl){
				if(decl.getImage().equals(tempdecl.getImage())
						&&decl.getParameterCount()==tempdecl.getParameterCount()){
					return true;
				}
			}
		}
		return false;
	}

	/** 检察是否存在递归 */
	private static boolean isRecursion(ASTMethodDeclaration method) {
		ASTMethodDeclarator declarator = (ASTMethodDeclarator) method.getFirstDirectChildOfType(ASTMethodDeclarator.class);
		MethodNameDeclaration caller = declarator.getMethodNameDeclaration();
		CVexNode ncaller = caller.getCallGraphVex();
		Hashtable<CVexNode, CVexNode> table = new Hashtable<CVexNode, CVexNode>();
		Stack<CVexNode> stack = new Stack<CVexNode>();
		// 出发函数入栈
		stack.push(ncaller);
		while (!stack.empty()) {
			CVexNode tempcaller = stack.pop();
			// 能到达的函数
			for (Enumeration<CEdge> e = tempcaller.getOutedges().elements(); e.hasMoreElements();) {
				CVexNode tempcallee = e.nextElement().getHeadNode();
				if (tempcallee == ncaller) {
					// 能到达的函数==出发函数，递归
					return true;
				}
				if (!table.contains(tempcallee)) {
					// 新的能到达函数
					table.put(tempcallee, tempcallee);
					stack.push(tempcallee);
				}
			}
		}
		return false;
	}

	public static List<FSMMachineInstance> createDCStateMachines(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		// For语句
		String xPath = ".//ForStatement";
		List evaluationResults = null;
		try {
			XPath xpath = new BaseXPath(xPath, new DocumentNavigator());
			evaluationResults = xpath.selectNodes(node);
		} catch (JaxenException e) {
			// e.printStackTrace();
			throw new RuntimeException("xpath error");
		}
		Iterator i = evaluationResults.iterator();
		while (i.hasNext()) {
			ASTForStatement forstmt = (ASTForStatement) i.next();
			if (forstmt.getFirstDirectChildOfType(ASTType.class) != null) {
				// for each新语法
				continue;
			}
			if(forstmt.hasLocalMethod(node)){
				continue;
			}
			MethodScope ms=forstmt.getScope().getEnclosingMethodScope();
			if(ms!=null&&ms.getName()!=null&&ms.getName().equals("run")){
				continue;
			}
			
			if(ms.getAstTreeNode() instanceof ASTMethodDeclaration){
				ASTMethodDeclaration t=(ASTMethodDeclaration)ms.getAstTreeNode();
				if(t.isSynchronized()){
					continue;
				}
			}
			
			if(forstmt.getFirstParentOfType(ASTTryStatement.class)!=null){
				continue;
			}
			
			FSMMachineInstance fsminstance = fsm.creatInstance();
			fsminstance.setRelatedObject(new FSMRelatedCalculation(forstmt));
			fsminstance.setResultString("for");
			VexNode head = null, out = null;
			for (VexNode n : forstmt.getVexNode()) {
				if (n.getName().startsWith("for_head")) {
					head = n;
				} else if (n.getName().startsWith("for_out")) {
					out = n;
				}
			}
			if (head != null && out != null && checkForOrWhileNoJump(head, out)) {
				list.add(fsminstance);
			}
		}

		// while语句
		xPath = ".//WhileStatement";
		try {
			XPath xpath = new BaseXPath(xPath, new DocumentNavigator());
			evaluationResults = xpath.selectNodes(node);
		} catch (JaxenException e) {
			// e.printStackTrace();
			throw new RuntimeException("xpath error");
		}
		i = evaluationResults.iterator();
		while (i.hasNext()) {
			ASTWhileStatement whilestmt = (ASTWhileStatement) i.next();
			if(whilestmt.hasLocalMethod(node)){
				continue;
			}
			
			MethodScope ms=whilestmt.getScope().getEnclosingMethodScope();
			if(ms!=null&&ms.getName()!=null&&ms.getName().equals("run")){
				continue;
			}
	
			if(ms.getAstTreeNode() instanceof ASTMethodDeclaration){
				ASTMethodDeclaration t=(ASTMethodDeclaration)ms.getAstTreeNode();
				if(t.isSynchronized()){
					continue;
				}
			}		
			
			if(whilestmt.getFirstParentOfType(ASTTryStatement.class)!=null){
				continue;
			}
			
			FSMMachineInstance fsminstance = fsm.creatInstance();
			fsminstance.setRelatedObject(new FSMRelatedCalculation(whilestmt));
			fsminstance.setResultString("while");
			VexNode head = null, out = null;
			for (VexNode n : whilestmt.getVexNode()) {
				if (n.getName().startsWith("while_head")) {
					head = n;
				} else if (n.getName().startsWith("while_out")) {
					out = n;
				}
			}
			if (head != null && out != null && checkForOrWhileNoJump(head, out)) {
				list.add(fsminstance);
			}
		}

		// do-while语句
		xPath = ".//DoStatement";
		try {
			XPath xpath = new BaseXPath(xPath, new DocumentNavigator());
			evaluationResults = xpath.selectNodes(node);
		} catch (JaxenException e) {
			// e.printStackTrace();
			throw new RuntimeException("xpath error");
		}
		i = evaluationResults.iterator();
		while (i.hasNext()) {
			ASTDoStatement dostmt = (ASTDoStatement) i.next();
			if(dostmt.hasLocalMethod(node)){
				continue;
			}
			
			MethodScope ms=dostmt.getScope().getEnclosingMethodScope();
			if(ms!=null&&ms.getName()!=null&&ms.getName().equals("run")){
				continue;
			}
			
			if(ms.getAstTreeNode() instanceof ASTMethodDeclaration){
				ASTMethodDeclaration t=(ASTMethodDeclaration)ms.getAstTreeNode();
				if(t.isSynchronized()){
					continue;
				}
			}		
			
			if(dostmt.getFirstParentOfType(ASTTryStatement.class)!=null){
				continue;
			}
			
			ASTExpression expr = (ASTExpression) dostmt.getFirstDirectChildOfType(ASTExpression.class);
			FSMMachineInstance fsminstance = fsm.creatInstance();
			fsminstance.setRelatedObject(new FSMRelatedCalculation(dostmt));
			fsminstance.setResultString("do-while");
			VexNode head = null, out1 = null, out2 = null;
			for (VexNode n : dostmt.getVexNode()) {
				if (n.getName().startsWith("do_while_head")) {
					head = n;
				} else if (n.getName().startsWith("do_while_out2")) {
					out2 = n;
				}
			}
			out1 = expr.getFirstVexNode();
			if (head != null && out1 != null && out2 != null && checkDoWhileNoJump(head, out1, out2)) {
				list.add(fsminstance);
			}
		}

		// 函数
		if (node instanceof ASTMethodDeclaration) {
			// 检察是否存在递归
			ASTMethodDeclaration method = (ASTMethodDeclaration) node;
			ASTMethodDeclarator declarator = (ASTMethodDeclarator) method.getFirstDirectChildOfType(ASTMethodDeclarator.class);
			MethodNameDeclaration decl = declarator.getMethodNameDeclaration();
			
			if(hasSameNameAndParam(method)){
				return list;
			}
			ASTClassOrInterfaceDeclaration classdecl=(ASTClassOrInterfaceDeclaration)method.getFirstParentOfType(ASTClassOrInterfaceDeclaration.class);
			if(classdecl!=null){
				if(classdecl.getFirstDirectChildOfType(ASTExtendsList.class)!=null){
					return list;
				}
			}
						
			CVexNode n = decl.getCallGraphVex();
			Hashtable<CVexNode, CVexNode> table = new Hashtable<CVexNode, CVexNode>();
			table.put(n, n);
			// 逐步扩散寻找是否当前函数是否dominatecall自己
			boolean change = true;
			while (change) {
				change = false;
				xq:
				for (Enumeration<CVexNode> e1 = table.elements(); e1.hasMoreElements();) {
					CVexNode callee = e1.nextElement();
					for (Enumeration<CEdge> e2 = callee.getInedges().elements(); e2.hasMoreElements();) {
						CVexNode caller = e2.nextElement().getTailNode();
						if (checkDominateCall(caller.getMethodDeclaration(), table)) {
							if (caller == n) {
								FSMMachineInstance fsminstance = fsm.creatInstance();
								fsminstance.setRelatedObject(new FSMRelatedCalculation(method));
								fsminstance.setResultString(declarator.getImage());
								list.add(fsminstance);
								return list;
							}
							if (!table.contains(caller)
									&&table.size()<=5
									&&!hasSameNameAndParam(caller.getMethodDeclaration())) {
								//超过5个就不加入了，减少复杂度
								//那些具有相同参数个数同名函数的函数也不加入
								table.put(caller, caller);
								change = true;
								break xq;
							}
						}
					}
				}

			}
		}

		return list;
	}

	public static boolean checkDeadCycle(VexNode n, FSMMachineInstance fsm) {
		if (fsm.getRelatedObject().getTagTreeNode() instanceof ASTForStatement) {
			return checkForStatement(n, fsm);
		} else if (fsm.getRelatedObject().getTagTreeNode() instanceof ASTWhileStatement) {
			return checkWhileStatement(n, fsm);
		} else if (fsm.getRelatedObject().getTagTreeNode() instanceof ASTDoStatement) {
			return checkDoStatement(n, fsm);
		} else if (fsm.getRelatedObject().getTagTreeNode() instanceof ASTMethodDeclaration) {
			return checkRecursion(n, fsm);
		}

		return false;
	}

	private static boolean checkWhileStatement(VexNode n, FSMMachineInstance fsm) {
		ASTWhileStatement whilestmt = (ASTWhileStatement) fsm.getRelatedObject().getTagTreeNode();
		ASTExpression expr = (ASTExpression) whilestmt.getFirstDirectChildOfType(ASTExpression.class);
		ASTBooleanLiteral boolliteral = (ASTBooleanLiteral) expr.getSingleChildofType(ASTBooleanLiteral.class);
		if (boolliteral != null && boolliteral.isTrue()) {
			return true;
		}
		DomainSet ds = getTrueMustDomainSet(expr);
		Set entryset = ds.getTable().entrySet();
		Iterator i = entryset.iterator();
		VariableNameDeclaration v = null;
		Object d = null;
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			v = (VariableNameDeclaration) e.getKey();
			d = e.getValue();
			ArrayList occs = (ArrayList) v.getDeclareScope().getVariableDeclarations().get(v);
	
			SimpleNode treeinexpr = null;
			for (Object o : occs) {
				NameOccurrence occ = (NameOccurrence) o;
				SimpleNode treenode = occ.getLocation();
				if (treenode.isSelOrAncestor(expr) && !treenode.hasLocalMethod(expr)) {
					treeinexpr = treenode;
				}
			}
			ASTRelationalExpression relation=null;
			ASTEqualityExpression equal=null;
			if(treeinexpr!=null){
				relation= (ASTRelationalExpression) treeinexpr.getFirstParentOfType(ASTRelationalExpression.class);
				if(relation!=null&&!isOnlyContainOneVar(relation,v)){
					continue;
				}
				equal= (ASTEqualityExpression) treeinexpr.getFirstParentOfType(ASTEqualityExpression.class);
				if(equal!=null&&!isOnlyContainOneVar(equal,v)){
					continue;
				}
			}
			
			boolean validassign = false;
			switch (DomainSet.getDomainType(d)) {
			case INT:
				IntegerDomain intdomain = (IntegerDomain) d;
				if (!intdomain.isEmpty()) {
					for (Object o : occs) {
						NameOccurrence occ = (NameOccurrence) o;
						SimpleNode treenode = occ.getLocation();
						if (treenode.isSelOrAncestor(whilestmt) && !treenode.hasLocalMethod(whilestmt)) {
							// 检察方向
							if ((occ.isOnLeftHandSide()||occ.isSelfAssignment()) && treeinexpr != null) {
								int kind = 0;
								if (relation != null) {
									if (relation.getImage().equals(">") || relation.getImage().equals(">=")) {
										if (treeinexpr.isSelOrAncestor((SimpleNode) relation.jjtGetChild(0))) {
											kind = 1;
										} else {
											kind = -1;
										}
									} else if (relation.getImage().equals("<") || relation.getImage().equals("<=")) {
										if (treeinexpr.isSelOrAncestor((SimpleNode) relation.jjtGetChild(0))) {
											kind = -1;
										} else {
											kind = 1;
										}
									}
								}
								VexNode vex = treenode.getCurrentVexNode();
								if(vex==null){
									return false;
								}
								IntegerDomain last = (IntegerDomain) ConvertDomain.DomainSwitch(vex.getLastDomainWithoutNull(v), ClassType.INT);
								IntegerDomain current = (IntegerDomain) ConvertDomain.DomainSwitch(vex.getDomainWithoutNull(v), ClassType.INT);
								if(last.getUnknown()||current.getUnknown()){
									return false;
								}
								switch (kind) {
								case 0:
									validassign = true;
									break;
								case 1:
									if (current.jointoOneInterval().getMax() < last.jointoOneInterval().getMax()
											||current.jointoOneInterval().getMin() < last.jointoOneInterval().getMin()) {
										validassign = true;
									}
									break;
								case -1:
									if (current.jointoOneInterval().getMin() > last.jointoOneInterval().getMin()
											||current.jointoOneInterval().getMax() > last.jointoOneInterval().getMax()) {
										validassign = true;
									}
									break;
								}
								if (validassign) {
									break;
								}
							}
						}
					}
					if (!validassign) {
						return true;
					}
				}
				break;
			case DOUBLE:
				DoubleDomain doubledomain = (DoubleDomain) d;
				if (!doubledomain.isEmpty()) {
					for (Object o : occs) {
						NameOccurrence occ = (NameOccurrence) o;
						SimpleNode treenode = occ.getLocation();
						if (treenode.isSelOrAncestor(whilestmt) && !treenode.hasLocalMethod(whilestmt)) {
							// 检察方向
							if ((occ.isOnLeftHandSide()||occ.isSelfAssignment()) && treeinexpr != null) {
								int kind = 0;
								if (relation != null) {
									if (relation.getImage().equals(">") || relation.getImage().equals(">=")) {
										if (treeinexpr.isSelOrAncestor((SimpleNode) relation.jjtGetChild(0))) {
											kind = 1;
										} else {
											kind = -1;
										}
									} else if (relation.getImage().equals("<") || relation.getImage().equals("<=")) {
										if (treeinexpr.isSelOrAncestor((SimpleNode) relation.jjtGetChild(0))) {
											kind = -1;
										} else {
											kind = 1;
										}
									}
								}
								VexNode vex = treenode.getCurrentVexNode();
								if(vex==null){
									return false;
								}
								DoubleDomain last = (DoubleDomain) ConvertDomain.DomainSwitch(vex.getLastDomainWithoutNull(v), ClassType.DOUBLE);
								DoubleDomain current = (DoubleDomain) ConvertDomain.DomainSwitch(vex.getDomainWithoutNull(v), ClassType.DOUBLE);
								if(last.getUnknown()||current.getUnknown()){
									return false;
								}
								switch (kind) {
								case 0:
									validassign = true;
									break;
								case 1:
									if (current.jointoOneInterval().getMax() < last.jointoOneInterval().getMax()
											||current.jointoOneInterval().getMin() < last.jointoOneInterval().getMin()) {
										validassign = true;
									}
									break;
								case -1:
									if (current.jointoOneInterval().getMin() > last.jointoOneInterval().getMin()
											||current.jointoOneInterval().getMax() > last.jointoOneInterval().getMax()) {
										validassign = true;
									}
									break;
								}
								if (validassign) {
									break;
								}
							}
						}
					}
					if (!validassign) {
						return true;
					}
				}
				break;
			case REF:
				ReferenceDomain refdomain = (ReferenceDomain) d;
				if (refdomain.getValue() != ReferenceValue.EMPTY) {	
					for (Object o : occs) {
						NameOccurrence occ = (NameOccurrence) o;
						SimpleNode treenode = occ.getLocation();
						if (treenode.isSelOrAncestor(whilestmt) && !treenode.hasLocalMethod(whilestmt)) {
							if (occ.isOnLeftHandSide()||occ.isSelfAssignment()) {
								validassign = true;
								break;
							}
						}
					}
					if (!validassign) {
						return true;
					}
				}
				break;
			case BOOLEAN:
				BooleanDomain booldomain = (BooleanDomain) d;
				if (booldomain.getValue() != BooleanValue.EMPTY) {
					for (Object o : occs) {
						NameOccurrence occ = (NameOccurrence) o;
						SimpleNode treenode = occ.getLocation();
						if (treenode.isSelOrAncestor(whilestmt) && !treenode.hasLocalMethod(whilestmt)) {
							if (occ.isOnLeftHandSide()||occ.isSelfAssignment()) {
								validassign = true;
								break;
							}
						}
					}
					if (!validassign) {
						return true;
					}
				}
				break;
			}
		}
		return false;
	}

	private static boolean checkDoStatement(VexNode n, FSMMachineInstance fsm) {
		ASTDoStatement dostmt = (ASTDoStatement) fsm.getRelatedObject().getTagTreeNode();
		ASTExpression expr = (ASTExpression) dostmt.getFirstDirectChildOfType(ASTExpression.class);
		ASTBooleanLiteral boolliteral = (ASTBooleanLiteral) expr.getSingleChildofType(ASTBooleanLiteral.class);
		if (boolliteral != null && boolliteral.isTrue()) {
			return true;
		}
		DomainSet ds = getTrueMustDomainSet(expr);
		Set entryset = ds.getTable().entrySet();
		Iterator i = entryset.iterator();
		VariableNameDeclaration v = null;
		Object d = null;
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			v = (VariableNameDeclaration) e.getKey();
			d = e.getValue();
			ArrayList occs = (ArrayList) v.getDeclareScope().getVariableDeclarations().get(v);
			
			SimpleNode treeinexpr = null;
			for (Object o : occs) {
				NameOccurrence occ = (NameOccurrence) o;
				SimpleNode treenode = occ.getLocation();
				if (treenode.isSelOrAncestor(expr) && !treenode.hasLocalMethod(expr)) {
					treeinexpr = treenode;
				}
			}
			ASTRelationalExpression relation=null;
			ASTEqualityExpression equal=null;
			if(treeinexpr!=null){
				relation= (ASTRelationalExpression) treeinexpr.getFirstParentOfType(ASTRelationalExpression.class);
				if(relation!=null&&!isOnlyContainOneVar(relation,v)){
					continue;
				}
				equal= (ASTEqualityExpression) treeinexpr.getFirstParentOfType(ASTEqualityExpression.class);
				if(equal!=null&&!isOnlyContainOneVar(equal,v)){
					continue;
				}
			}			
			
			boolean validassign = false;
			switch (DomainSet.getDomainType(d)) {
			case INT:
				IntegerDomain intdomain = (IntegerDomain) d;
				if (!intdomain.isEmpty()) {
					for (Object o : occs) {
						NameOccurrence occ = (NameOccurrence) o;
						SimpleNode treenode = occ.getLocation();
						if (treenode.isSelOrAncestor(expr) && !treenode.hasLocalMethod(expr)) {
							treeinexpr = treenode;
						}
					}

					for (Object o : occs) {
						NameOccurrence occ = (NameOccurrence) o;
						SimpleNode treenode = occ.getLocation();
						if (treenode.isSelOrAncestor(dostmt) && !treenode.hasLocalMethod(dostmt)) {
							// 检察方向
							if ((occ.isOnLeftHandSide()||occ.isSelfAssignment()) && treeinexpr != null) {
								int kind = 0;
								if (relation != null) {
									if (relation.getImage().equals(">") || relation.getImage().equals(">=")) {
										if (treeinexpr.isSelOrAncestor((SimpleNode) relation.jjtGetChild(0))) {
											kind = 1;
										} else {
											kind = -1;
										}
									} else if (relation.getImage().equals("<") || relation.getImage().equals("<=")) {
										if (treeinexpr.isSelOrAncestor((SimpleNode) relation.jjtGetChild(0))) {
											kind = -1;
										} else {
											kind = 1;
										}
									}
								}
								VexNode vex = treenode.getCurrentVexNode();
								if(vex==null){
									return false;
								}
								IntegerDomain last = (IntegerDomain) ConvertDomain.DomainSwitch(vex.getLastDomainWithoutNull(v), ClassType.INT);
								IntegerDomain current = (IntegerDomain) ConvertDomain.DomainSwitch(vex.getDomainWithoutNull(v), ClassType.INT);
								if(last.getUnknown()||current.getUnknown()){
									return false;
								}
								switch (kind) {
								case 0:
									validassign = true;
									break;
								case 1:
									if (current.jointoOneInterval().getMax() < last.jointoOneInterval().getMax()
											||current.jointoOneInterval().getMin() < last.jointoOneInterval().getMin()) {
										validassign = true;
									}
									break;
								case -1:
									if (current.jointoOneInterval().getMin() > last.jointoOneInterval().getMin()
											||current.jointoOneInterval().getMax() > last.jointoOneInterval().getMax()) {
										validassign = true;
									}
									break;
								}
								if (validassign) {
									break;
								}
							}
						}
					}
					if (!validassign) {
						return true;
					}
				}
				break;
			case DOUBLE:
				DoubleDomain doubledomain = (DoubleDomain) d;
				if (!doubledomain.isEmpty()) {
					for (Object o : occs) {
						NameOccurrence occ = (NameOccurrence) o;
						SimpleNode treenode = occ.getLocation();
						if (treenode.isSelOrAncestor(expr) && !treenode.hasLocalMethod(expr)) {
							treeinexpr = treenode;
						}
					}

					for (Object o : occs) {
						NameOccurrence occ = (NameOccurrence) o;
						SimpleNode treenode = occ.getLocation();
						if (treenode.isSelOrAncestor(dostmt) && !treenode.hasLocalMethod(dostmt)) {
							// 检察方向
							if ((occ.isOnLeftHandSide()||occ.isSelfAssignment()) && treeinexpr != null) {
								int kind = 0;
								if (relation != null) {
									if (relation.getImage().equals(">") || relation.getImage().equals(">=")) {
										if (treeinexpr.isSelOrAncestor((SimpleNode) relation.jjtGetChild(0))) {
											kind = 1;
										} else {
											kind = -1;
										}
									} else if (relation.getImage().equals("<") || relation.getImage().equals("<=")) {
										if (treeinexpr.isSelOrAncestor((SimpleNode) relation.jjtGetChild(0))) {
											kind = -1;
										} else {
											kind = 1;
										}
									}
								}
								VexNode vex = treenode.getCurrentVexNode();
								if(vex==null){
									return false;
								}
								DoubleDomain last = (DoubleDomain) ConvertDomain.DomainSwitch(vex.getLastDomainWithoutNull(v), ClassType.DOUBLE);
								DoubleDomain current = (DoubleDomain) ConvertDomain.DomainSwitch(vex.getDomainWithoutNull(v), ClassType.DOUBLE);
								if(last.getUnknown()||current.getUnknown()){
									return false;
								}
								switch (kind) {
								case 0:
									validassign = true;
									break;
								case 1:
									if (current.jointoOneInterval().getMax() < last.jointoOneInterval().getMax()
											||current.jointoOneInterval().getMin() < last.jointoOneInterval().getMin()) {
										validassign = true;
									}
									break;
								case -1:
									if (current.jointoOneInterval().getMin() > last.jointoOneInterval().getMin()
											||current.jointoOneInterval().getMax() > last.jointoOneInterval().getMax()) {
										validassign = true;
									}
									break;
								}
								if (validassign) {
									break;
								}
							}
						}
					}
					if (!validassign) {
						return true;
					}
				}
				break;
			case REF:
				ReferenceDomain refdomain = (ReferenceDomain) d;
				if (refdomain.getValue() != ReferenceValue.EMPTY) {
					for (Object o : occs) {
						NameOccurrence occ = (NameOccurrence) o;
						SimpleNode treenode = occ.getLocation();
						if (treenode.isSelOrAncestor(dostmt) && !treenode.hasLocalMethod(dostmt)) {
							if (occ.isOnLeftHandSide()||occ.isSelfAssignment()) {
								validassign = true;
								break;
							}
						}
					}
					if (!validassign) {
						return true;
					}
				}
				break;
			case BOOLEAN:
				BooleanDomain booldomain = (BooleanDomain) d;
				if (booldomain.getValue() != BooleanValue.EMPTY) {
					for (Object o : occs) {
						NameOccurrence occ = (NameOccurrence) o;
						SimpleNode treenode = occ.getLocation();
						if (treenode.isSelOrAncestor(dostmt) && !treenode.hasLocalMethod(dostmt)) {
							if (occ.isOnLeftHandSide()||occ.isSelfAssignment()) {
								validassign = true;
								break;
							}
						}
					}
					if (!validassign) {
						return true;
					}
				}
				break;
			}
		}
		return false;
	}

	private static boolean checkRecursion(VexNode n, FSMMachineInstance fsm) {
		return true;
	}

	private static boolean checkForStatement(VexNode n, FSMMachineInstance fsm) {
		ASTForStatement forstmt = (ASTForStatement) fsm.getRelatedObject().getTagTreeNode();
		ASTForUpdate updata = (ASTForUpdate) forstmt.getFirstDirectChildOfType(ASTForUpdate.class);
		ASTExpression condition = (ASTExpression) forstmt.getFirstDirectChildOfType(ASTExpression.class);
		ASTForInit init = (ASTForInit) forstmt.getFirstDirectChildOfType(ASTForInit.class);
		// 无结束条件
		if (condition == null) {
			return true;
		}
		DomainSet ds = getTrueMustDomainSet(condition);
		Set entryset = ds.getTable().entrySet();
		Iterator i = entryset.iterator();
		VariableNameDeclaration v = null;
		Object d = null;
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			v = (VariableNameDeclaration) e.getKey();
			d = e.getValue();
			ArrayList occs = (ArrayList) v.getDeclareScope().getVariableDeclarations().get(v);
			
			SimpleNode treeinexpr = null;
			for (Object o : occs) {
				NameOccurrence occ = (NameOccurrence) o;
				SimpleNode treenode = occ.getLocation();
				if (treenode.isSelOrAncestor(condition) && !treenode.hasLocalMethod(condition)) {
					treeinexpr = treenode;
				}
			}
			ASTRelationalExpression relation=null;
			ASTEqualityExpression equal=null;
			if(treeinexpr!=null){
				relation= (ASTRelationalExpression) treeinexpr.getFirstParentOfType(ASTRelationalExpression.class);
				if(relation!=null&&!isOnlyContainOneVar(relation,v)){
					continue;
				}
				equal= (ASTEqualityExpression) treeinexpr.getFirstParentOfType(ASTEqualityExpression.class);
				if(equal!=null&&!isOnlyContainOneVar(equal,v)){
					continue;
				}
			}		
			
			boolean validassign = false;
			switch (DomainSet.getDomainType(d)) {
			case INT:
				IntegerDomain intdomain = (IntegerDomain) d;
				if (!intdomain.isEmpty()) {
					for (Object o : occs) {
						NameOccurrence occ = (NameOccurrence) o;
						SimpleNode treenode = occ.getLocation();
						if (treenode.isSelOrAncestor(condition) && !treenode.hasLocalMethod(condition)) {
							treeinexpr = treenode;
						}
					}

					for (Object o : occs) {
						NameOccurrence occ = (NameOccurrence) o;
						SimpleNode treenode = occ.getLocation();
						if (treenode.isSelOrAncestor(forstmt) && !treenode.hasLocalMethod(forstmt) && (init == null || !treenode.isSelOrAncestor(init))) {
							// 检察方向 (occ.isOnLeftHandSide()||occ.isSelfAssignment())
							if ((occ.isOnLeftHandSide()||occ.isSelfAssignment()) && treeinexpr != null) {
								int kind = 0;
								if (relation != null) {
									if (relation.getImage().equals(">") || relation.getImage().equals(">=")) {
										if (treeinexpr.isSelOrAncestor((SimpleNode) relation.jjtGetChild(0))) {
											kind = 1;
										} else {
											kind = -1;
										}
									} else if (relation.getImage().equals("<") || relation.getImage().equals("<=")) {
										if (treeinexpr.isSelOrAncestor((SimpleNode) relation.jjtGetChild(0))) {
											kind = -1;
										} else {
											kind = 1;
										}
									}
								}
								VexNode vex = treenode.getCurrentVexNode();
								if(vex==null){
									return false;
								}
								IntegerDomain last = (IntegerDomain) ConvertDomain.DomainSwitch(vex.getLastDomainWithoutNull(v), ClassType.INT);
								IntegerDomain current = (IntegerDomain) ConvertDomain.DomainSwitch(vex.getDomainWithoutNull(v), ClassType.INT);
								if(last.getUnknown()||current.getUnknown()){
									return false;
								}
								switch (kind) {
								case 0:
									validassign = true;
									break;
								case 1:
									if (current.jointoOneInterval().getMax() < last.jointoOneInterval().getMax()
											||current.jointoOneInterval().getMin() < last.jointoOneInterval().getMin()) {
										validassign = true;
									}
									break;
								case -1:
									if (current.jointoOneInterval().getMin() > last.jointoOneInterval().getMin()
											||current.jointoOneInterval().getMax() > last.jointoOneInterval().getMax()) {
										validassign = true;
									}
									break;
								}
								if (validassign) {
									break;
								}
							}
						}
					}
					if (!validassign) {
						return true;
					}
				}
				break;
			case DOUBLE:
				DoubleDomain doubledomain = (DoubleDomain) d;
				if (!doubledomain.isEmpty()) {
					for (Object o : occs) {
						NameOccurrence occ = (NameOccurrence) o;
						SimpleNode treenode = occ.getLocation();
						if (treenode.isSelOrAncestor(condition) && !treenode.hasLocalMethod(condition)) {
							treeinexpr = treenode;
						}
					}

					for (Object o : occs) {
						NameOccurrence occ = (NameOccurrence) o;
						SimpleNode treenode = occ.getLocation();
						if (treenode.isSelOrAncestor(forstmt) && !treenode.hasLocalMethod(forstmt) && (init == null || !treenode.isSelOrAncestor(init))) {
							// 检察方向
							if ((occ.isOnLeftHandSide()||occ.isSelfAssignment()) && treeinexpr != null) {
								int kind = 0;
								if (relation != null) {
									if (relation.getImage().equals(">") || relation.getImage().equals(">=")) {
										if (treeinexpr.isSelOrAncestor((SimpleNode) relation.jjtGetChild(0))) {
											kind = 1;
										} else {
											kind = -1;
										}
									} else if (relation.getImage().equals("<") || relation.getImage().equals("<=")) {
										if (treeinexpr.isSelOrAncestor((SimpleNode) relation.jjtGetChild(0))) {
											kind = -1;
										} else {
											kind = 1;
										}
									}
								}
								VexNode vex = treenode.getCurrentVexNode();
								if(vex==null){
									return false;
								}
								DoubleDomain last = (DoubleDomain) ConvertDomain.DomainSwitch(vex.getLastDomainWithoutNull(v), ClassType.DOUBLE);
								DoubleDomain current = (DoubleDomain) ConvertDomain.DomainSwitch(vex.getDomainWithoutNull(v), ClassType.DOUBLE);
								if(last.getUnknown()||current.getUnknown()){
									return false;
								}
								switch (kind) {
								case 0:
									validassign = true;
									break;
								case 1:
									if (current.jointoOneInterval().getMax() < last.jointoOneInterval().getMax()
											||current.jointoOneInterval().getMin() < last.jointoOneInterval().getMin()) {
										validassign = true;
									}
									break;
								case -1:
									if (current.jointoOneInterval().getMin() > last.jointoOneInterval().getMin()
											||current.jointoOneInterval().getMax() > last.jointoOneInterval().getMax()) {
										validassign = true;
									}
									break;
								}
								if (validassign) {
									break;
								}
							}
						}
					}
					if (!validassign) {
						return true;
					}
				}
				break;
			case REF:
				ReferenceDomain refdomain = (ReferenceDomain) d;
				if (refdomain.getValue() != ReferenceValue.EMPTY) {
					for (Object o : occs) {
						NameOccurrence occ = (NameOccurrence) o;
						SimpleNode treenode = occ.getLocation();
						if (treenode.isSelOrAncestor(forstmt) && !treenode.hasLocalMethod(forstmt)) {
							if ((occ.isOnLeftHandSide()||occ.isSelfAssignment())) {
								validassign = true;
								break;
							}
						}
					}
					if (!validassign) {
						return true;
					}
				}
				break;
			case BOOLEAN:
				BooleanDomain booldomain = (BooleanDomain) d;
				if (booldomain.getValue() != BooleanValue.EMPTY) {
					for (Object o : occs) {
						NameOccurrence occ = (NameOccurrence) o;
						SimpleNode treenode = occ.getLocation();
						if (treenode.isSelOrAncestor(forstmt) && !treenode.hasLocalMethod(forstmt)) {
							if ((occ.isOnLeftHandSide()||occ.isSelfAssignment())) {
								validassign = true;
								break;
							}
						}
					}
					if (!validassign) {
						return true;
					}
				}
				break;
			}
		}

		// i=i-2 i=i+2趋势
		if (condition != null) {
			if (condition.jjtGetNumChildren() == 1 && condition.jjtGetChild(0) instanceof ASTEqualityExpression) {
				ASTEqualityExpression equal = (ASTEqualityExpression) condition.jjtGetChild(0);
				ASTName name = null, nameupdate = null;
				ASTLiteral literalcon = null, literalup = null;
				if (equal.getImage().equals("!=")) {
					name = (ASTName) ((SimpleJavaNode) equal.jjtGetChild(0)).getSingleChildofType(ASTName.class);
					if (name == null) {
						name = (ASTName) ((SimpleJavaNode) equal.jjtGetChild(1)).getSingleChildofType(ASTName.class);
					}
					literalcon = (ASTLiteral) ((SimpleJavaNode) equal.jjtGetChild(0)).getSingleChildofType(ASTLiteral.class);
					if (literalcon == null) {
						literalcon = (ASTLiteral) ((SimpleJavaNode) equal.jjtGetChild(1)).getSingleChildofType(ASTLiteral.class);
					}
				}
				if (name != null && name.getNameDeclaration() instanceof VariableNameDeclaration && literalcon != null) {
					v = (VariableNameDeclaration) name.getNameDeclaration();
					String type = v.getTypeImage();
					if (type.equals("int") || type.equals("short") || type.equals("long") || type.equals("double") || type.equals("float")) {
						VexNode forhead = condition.getCurrentVexNode();
						ASTForInit forinit = (ASTForInit) forstmt.getFirstDirectChildOfType(ASTForInit.class);
						IntegerDomain start = null, con = null, up = null;
						DomainData exprdata = new DomainData();
						ExpressionDomainVisitor exprvisitor = new ExpressionDomainVisitor();
						if (forinit != null) {
							VexNode initvex = forinit.getFirstVexNode();
							DomainSet old = initvex.getDomainSet();
							forinit.jjtAccept(exprvisitor, exprdata);
							start = (IntegerDomain) ConvertDomain.DomainSwitch(initvex.getDomainWithoutNull(v), ClassType.INT);

							initvex.setDomainSet(old);
						} else {
							start = (IntegerDomain) ConvertDomain.DomainSwitch(forhead.getDomainWithoutNull(v), ClassType.INT);
						}
						literalcon.jjtAccept(exprvisitor, exprdata);
						con = (IntegerDomain) ConvertDomain.DomainSwitch(exprdata.domain, ClassType.INT);

						ArrayList occs = (ArrayList) v.getDeclareScope().getVariableDeclarations().get(v);
						for (Object o : occs) {
							NameOccurrence occ = (NameOccurrence) o;
							SimpleNode treenode = occ.getLocation();
							if ((occ.isOnLeftHandSide()||occ.isSelfAssignment()) && treenode.isSelOrAncestor(forstmt) && !treenode.hasLocalMethod(forstmt)
									&& (init == null || !treenode.isSelOrAncestor(init))) {
								ASTStatementExpression exp = (ASTStatementExpression) treenode.getFirstParentOfType(ASTStatementExpression.class);
								if (exp != null) {
									if (exp.jjtGetNumChildren() == 3 && exp.jjtGetChild(1) instanceof ASTAssignmentOperator) {
										ASTAssignmentOperator operator = (ASTAssignmentOperator) exp.jjtGetChild(1);
										if (operator.getImage().equals("=")) {
											ASTName left = (ASTName) ((SimpleJavaNode) exp.jjtGetChild(0)).getSingleChildofType(ASTName.class);
											ASTAdditiveExpression right = (ASTAdditiveExpression) ((SimpleJavaNode) exp.jjtGetChild(2))
													.getSingleChildofType(ASTAdditiveExpression.class);
											if (left != null && right != null && left.getNameDeclaration() == v) {
												if (right.getImage().equals("+")) {
													nameupdate = (ASTName) ((SimpleJavaNode) right.jjtGetChild(0)).getSingleChildofType(ASTName.class);
													if (nameupdate == null) {
														nameupdate = (ASTName) ((SimpleJavaNode) right.jjtGetChild(1)).getSingleChildofType(ASTName.class);
													}
													literalup = (ASTLiteral) ((SimpleJavaNode) right.jjtGetChild(0)).getSingleChildofType(ASTLiteral.class);
													if (literalup == null) {
														literalup = (ASTLiteral) ((SimpleJavaNode) right.jjtGetChild(1)).getSingleChildofType(ASTLiteral.class);
													}
													if (nameupdate != null && literalup != null && nameupdate.getNameDeclaration() == v) {
														literalup.jjtAccept(exprvisitor, exprdata);
														up = (IntegerDomain) ConvertDomain.DomainSwitch(exprdata.domain, ClassType.INT);
														if ((((con.jointoOneInterval().getMax() - start.jointoOneInterval().getMax()) % up.jointoOneInterval()
																.getMax()) != 0)
																|| con.jointoOneInterval().getMax() < start.jointoOneInterval().getMax()) {
															return true;
														}
													}
												} else if (right.getImage().equals("-")) {
													nameupdate = (ASTName) ((SimpleJavaNode) right.jjtGetChild(0)).getSingleChildofType(ASTName.class);
													literalup = (ASTLiteral) ((SimpleJavaNode) right.jjtGetChild(1)).getSingleChildofType(ASTLiteral.class);
													if (nameupdate != null && literalup != null && nameupdate.getNameDeclaration() == v) {
														literalup.jjtAccept(exprvisitor, exprdata);
														up = (IntegerDomain) ConvertDomain.DomainSwitch(exprdata.domain, ClassType.INT);
														if ((((start.jointoOneInterval().getMax() - con.jointoOneInterval().getMax()) % up.jointoOneInterval()
																.getMax()) != 0)
																|| con.jointoOneInterval().getMax() > start.jointoOneInterval().getMax()) {
															return true;
														}
													}
												}
											}
										} else if (operator.getImage().equals("+=")) {
											nameupdate = (ASTName) ((SimpleJavaNode) exp.jjtGetChild(0)).getSingleChildofType(ASTName.class);
											literalup = (ASTLiteral) ((SimpleJavaNode) exp.jjtGetChild(2)).getSingleChildofType(ASTLiteral.class);
											if (nameupdate != null && literalup != null && nameupdate.getNameDeclaration() == v) {
												literalup.jjtAccept(exprvisitor, exprdata);
												up = (IntegerDomain) ConvertDomain.DomainSwitch(exprdata.domain, ClassType.INT);
												if ((((con.jointoOneInterval().getMax() - start.jointoOneInterval().getMax()) % up.jointoOneInterval().getMax()) != 0)
														|| con.jointoOneInterval().getMax() < start.jointoOneInterval().getMax()) {
													return true;
												}
											}
										} else if (operator.getImage().equals("-=")) {
											nameupdate = (ASTName) ((SimpleJavaNode) exp.jjtGetChild(0)).getSingleChildofType(ASTName.class);
											literalup = (ASTLiteral) ((SimpleJavaNode) exp.jjtGetChild(2)).getSingleChildofType(ASTLiteral.class);
											if (nameupdate != null && literalup != null && nameupdate.getNameDeclaration() == v) {
												literalup.jjtAccept(exprvisitor, exprdata);
												up = (IntegerDomain) ConvertDomain.DomainSwitch(exprdata.domain, ClassType.INT);
												if ((((start.jointoOneInterval().getMax()) - con.jointoOneInterval().getMax() % up.jointoOneInterval().getMax()) != 0)
														|| con.jointoOneInterval().getMax() > start.jointoOneInterval().getMax()) {
													return true;
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return false;
	}
}
