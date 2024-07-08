package softtest.cfg.java;

import java.util.*;

import softtest.ast.java.*;
import softtest.symboltable.java.TypeSet;

/** 用于生成控制流图的抽象语法树访问者 */
public class ControlFlowVisitor extends JavaParserVisitorAdapter {
	 
	/** 判断语法树节点parent是否为另一个语法树child节点的祖先 */
	public static boolean isParent(SimpleJavaNode child, SimpleJavaNode parent) {
		boolean bret = false;
		SimpleJavaNode parentNode = (SimpleJavaNode) child.jjtGetParent();
		while (parentNode != null) {
			if (parentNode == parent) {
				bret = true;
				break;
			}
			parentNode = (SimpleJavaNode) parentNode.jjtGetParent();
		}
		return bret;
	}

	/** 处理构造函数 */
	@Override
	public Object visit(ASTConstructorDeclaration treenode, Object data) {
		ControlFlowData flowdata = new ControlFlowData();

		flowdata.graph = new Graph();
		treenode.setGraph(flowdata.graph);

		// 添加函数名字
		SimpleJavaNode simplejavanode = (SimpleJavaNode) treenode.jjtGetParent().jjtGetParent().jjtGetParent();
		String name = simplejavanode.getImage();

		// 增加一个函数try处理
		TryData trydata = new TryData();
		trydata.name="fun";
		flowdata.trystack.push(trydata);

		flowdata.vexnode = flowdata.graph.addVex("func_head_" + name + "_", treenode);
		for (int i = 1; i < treenode.jjtGetNumChildren(); i++) {
			JavaNode javanode = (JavaNode) treenode.jjtGetChild(i);
			javanode.jjtAccept(this, flowdata);
		}

		VexNode in = flowdata.vexnode;
		VexNode eout=null;
		
		//处理所有的throw
		trydata = flowdata.trystack.pop();
		if (trydata.thrownodes.size() > 0) {
			// 增加函数异常出口
			eout = flowdata.graph.addVex("func_eout_" + name + "_", treenode);
			ListIterator<VexNode> i = trydata.thrownodes.listIterator();
			while (i.hasNext()) {
				VexNode node1 = i.next();
				if (node1 != null) {
					flowdata.graph.addEdgeWithFlag(node1, eout,true);
				}
			}
		}
		
		VexNode out = flowdata.graph.addVex("func_out_" + name + "_", treenode);
		if(eout!=null){
			flowdata.graph.addEdgeWithFlag(eout, out,false);
		}
		if (in != null) {
			flowdata.graph.addEdgeWithFlag(in, out,false);
		}

		// 处理跳转语句,return 也被看作是转跳
		LabelData labeldata = flowdata.labeltable.get("return");
		if (labeldata == null) {
			labeldata = new LabelData();
			flowdata.labeltable.put("return", labeldata);
		}
		labeldata.labelnode = out;

		for (Enumeration<LabelData> e = flowdata.labeltable.elements(); e.hasMoreElements();) {
			labeldata = e.nextElement();
			ListIterator<VexNode> i = labeldata.jumpnodes.listIterator();
			while (i.hasNext()) {
				VexNode node1 = i.next();
				VexNode node2 = labeldata.labelnode;
				if (node1 != null && node2 != null) {
					flowdata.graph.addEdgeWithFlag(node1, node2,false);
				}
			}
		}

		return null;
	}

	/** 处理普通的成员函数 */
	@Override
	public Object visit(ASTMethodDeclaration treenode, Object data) {
		if(!(treenode.jjtGetChild(treenode.jjtGetNumChildren() - 1) instanceof ASTBlock)){
			return null;
		}
		ASTBlock javanode = (ASTBlock) (JavaNode) treenode.jjtGetChild(treenode.jjtGetNumChildren() - 1);
		ControlFlowData flowdata = new ControlFlowData();

		flowdata.graph = new Graph();
		treenode.setGraph(flowdata.graph);
		if (javanode != null) {
			// 添加函数名字
			SimpleJavaNode simplejavanode = (SimpleJavaNode) treenode.jjtGetChild(1);
			String name = simplejavanode.getImage();

			// 增加一个函数try处理
			TryData trydata = new TryData();
			trydata.name="fun";
			flowdata.trystack.push(trydata);

			flowdata.vexnode = flowdata.graph.addVex("func_head_" + name + "_", treenode);
			javanode.jjtAccept(this, flowdata);
			VexNode in = flowdata.vexnode;
			VexNode eout=null;
			
			//处理所有的throw
			trydata = flowdata.trystack.pop();
			if (trydata.thrownodes.size() > 0) {
				// 增加函数异常出口
				eout = flowdata.graph.addVex("func_eout_" + name + "_", treenode);
				ListIterator<VexNode> i = trydata.thrownodes.listIterator();
				while (i.hasNext()) {
					VexNode node1 = i.next();
					if (node1 != null) {
						flowdata.graph.addEdgeWithFlag(node1, eout,true);
					}
				}
				
			}
			
			VexNode out = flowdata.graph.addVex("func_out_" + name + "_", treenode);
			if(eout!=null){
				flowdata.graph.addEdgeWithFlag(eout, out,false);
			}
			if (in != null) {
				flowdata.graph.addEdgeWithFlag(in, out,false);
			}

			// 处理跳转语句,return 也被看作是转跳
			LabelData labeldata = flowdata.labeltable.get("return");
			if (labeldata == null) {
				labeldata = new LabelData();
				flowdata.labeltable.put("return", labeldata);
			}
			labeldata.labelnode = out;

			for (Enumeration<LabelData> e = flowdata.labeltable.elements(); e.hasMoreElements();) {
				labeldata = e.nextElement();
				ListIterator<VexNode> i = labeldata.jumpnodes.listIterator();
				while (i.hasNext()) {
					VexNode node1 = i.next();
					VexNode node2 = labeldata.labelnode;
					if (node1 != null && node2 != null) {
						flowdata.graph.addEdgeWithFlag(node1, node2,false);
					}
				}
			}
		}
		return null;
	}

	/** 处理语句块 */
	@Override
	public Object visit(ASTBlock treenode, Object data) {
		for (int i = 0; i < treenode.jjtGetNumChildren(); i++) {
			JavaNode javanode = (JavaNode) treenode.jjtGetChild(i);
			javanode.jjtAccept(this, data);
		}
		return null;
	}

	/** 处理语句块中的语句 */
	@Override
	public Object visit(ASTBlockStatement treenode, Object data) {
		JavaNode javanode = (JavaNode) treenode.jjtGetChild(0);
		javanode.jjtAccept(this, data);
		return null;
	}

	/** 处理语句，请参考语法规则java.jjt文件 */
	@Override
	public Object visit(ASTStatement treenode, Object data) {
		JavaNode javanode = (JavaNode) treenode.jjtGetChild(0);
		javanode.jjtAccept(this, data);
		return null;
	}

	/** 处理空语句 */
	@Override
	public Object visit(ASTEmptyStatement treenode, Object data) {
		ControlFlowData flowdata = (ControlFlowData) data;
		Graph graph = flowdata.graph;
		VexNode in = flowdata.vexnode;
		VexNode out = graph.addVex("empty_", treenode);
		if (in != null) {
			graph.addEdgeWithFlag(in, out,false);
		}
		flowdata.vexnode = out;
		return null;
	}

	/** 处理for语句中初始化和更新语句中可能会出现的语句列表，请参考语法规则java.jjt文件 */
	@Override
	public Object visit(ASTStatementExpressionList treenode, Object data) {
		for (int i = 0; i < treenode.jjtGetNumChildren(); i++) {
			JavaNode javanode = (JavaNode) treenode.jjtGetChild(i);
			javanode.jjtAccept(this, data);
		}
		return null;
	}

	/** 处理表达式语句 */
	@Override
	public Object visit(ASTStatementExpression treenode, Object data) {
		ControlFlowData flowdata = (ControlFlowData) data;
		Graph graph = flowdata.graph;
		VexNode in = flowdata.vexnode;
		VexNode out = graph.addVex("stmt_", treenode);
			
		if (in != null) {
			graph.addEdgeWithFlag(in, out,false);
		}
		
		//处理隐式异常
		addImplicitException(treenode,flowdata,out);
		
		flowdata.vexnode = out;
		return null;
	}

	/** 处理if语句 */
	@Override
	public Object visit(ASTIfStatement treenode, Object data) {
		VexNode head, out, f_branch, t_branch;
		ControlFlowData flowdata = (ControlFlowData) data;
		VexNode in = flowdata.vexnode;
		Graph g = flowdata.graph;

		// 产生if语句的头结点head
		head = g.addVex("if_head_", treenode);
		
		if (in != null) {
			g.addEdgeWithFlag(in, head,false);
		}
		
		//处理隐式异常
		addImplicitException(treenode,flowdata,head);

		// 以头节点为入口，调用真分支语句处理
		head.truetag = true;
		flowdata.vexnode = head;
		JavaNode javanode = (JavaNode) treenode.jjtGetChild(1);// 真分支
		javanode.jjtAccept(this, flowdata);
		t_branch = flowdata.vexnode;
		
		//真分支为空语句
		if(head==t_branch){
			head.truetag = false;
		}

		// 以头节点为入口，调用假分支语句处理
		head.falsetag = true;
		if (treenode.jjtGetNumChildren() > 2) {
			// 大于2说明有else分支
			flowdata.vexnode = head;
			javanode = (JavaNode) treenode.jjtGetChild(2);
			javanode.jjtAccept(this, flowdata);
			f_branch = flowdata.vexnode;
		} else {
			f_branch = head;
		}

		// 考虑出口结点的不同情况
		if (t_branch == null && f_branch == null) {
			// 真假分支都已经转跳走了，不需要产生if出口了
			out = null;
		} else {
			// 产生出口，并连接
			out = g.addVex("if_out_", treenode);
			//真分支为空语句
			if(head==t_branch){
				head.truetag = true;
			}
			if (t_branch != null) {
				g.addEdgeWithFlag(t_branch, out,false);
			}
			if (f_branch != null) {
				g.addEdgeWithFlag(f_branch, out,false);
			}
		}
		flowdata.vexnode = out;
		return null;
	}

	/** 处理while语句 */
	@Override
	public Object visit(ASTWhileStatement treenode, Object data) {
		VexNode head, out, subout;
		ControlFlowData flowdata = (ControlFlowData) data;
		VexNode in = flowdata.vexnode;
		Graph g = flowdata.graph;

		//产生当前循环辅助结构
		LoopData loopdata = new LoopData();
		loopdata.name = "while";

		// 产生while语句的头结点head
		head = g.addVex("while_head_", treenode);
		loopdata.head = head;
		if (in != null) {
			g.addEdgeWithFlag(in, head,false);
		}

		// 当前循环入栈
		flowdata.loopstack.push(loopdata);
		
		//处理隐式异常
		addImplicitException(treenode,flowdata,head);

		// 以头节点为入口，调用子语句处理
		head.truetag = true;
		flowdata.vexnode = head;
		JavaNode javanode = (JavaNode) (treenode).jjtGetChild(1);
		javanode.jjtAccept(this, flowdata);
		subout = flowdata.vexnode;
		if (subout != null) {
			g.addEdgeWithFlag(subout, head,false);
		}

		// 产生while语句出口节点
		out = g.addVex("while_out_", treenode);
		head.falsetag = true;
		g.addEdgeWithFlag(head, out,false);

		// 当前循环出栈
		loopdata = flowdata.loopstack.pop();

		// 处理当前循环的break
		ListIterator<VexNode> i = loopdata.breaknodes.listIterator();
		while (i.hasNext()) {
			VexNode node1 = i.next();
			if (node1 != null) {
				flowdata.graph.addEdgeWithFlag(node1, out,false);
			}
		}

		// 处理当前循环的continue
		i = loopdata.continuenodes.listIterator();
		while (i.hasNext()) {
			VexNode node1 = i.next();
			if (node1 != null) {
				flowdata.graph.addEdgeWithFlag(node1, head,false);
			}
		}
		flowdata.vexnode = out;
		return null;
	}

	/** 处理声明语句 */
	@Override
	public Object visit(ASTLocalVariableDeclaration treenode, Object data) {
		ControlFlowData flowdata = (ControlFlowData) data;
		Graph graph = flowdata.graph;
		VexNode in = flowdata.vexnode;
		VexNode out = graph.addVex("decl_stmt_", treenode);
		if (in != null) {
			graph.addEdgeWithFlag(in, out,false);
		}
		
		//处理隐式异常
		addImplicitException(treenode,flowdata,out);
		
		flowdata.vexnode = out;
		return null;
	}

	/** 处理break语句 */
	@Override
	public Object visit(ASTBreakStatement treenode, Object data) {
		ControlFlowData flowdata = (ControlFlowData) data;
		Graph graph = flowdata.graph;
		VexNode in = flowdata.vexnode;
		VexNode out = graph.addVex("break_", treenode);
		if (in != null) {
			graph.addEdgeWithFlag(in, out,false);
		}
		// 控制流已经转跳走了，出口置为null
		flowdata.vexnode = null;

		if (treenode.getImage() == null) {
			// break 后无标号
			LoopData loopdata = flowdata.loopstack.peek();
			loopdata.breaknodes.add(out);
		} else {
			// break 后有标号
			String name = treenode.getImage();
			name = "#" + name;
			LabelData labeldata = flowdata.labeltable.get(name);
			if (labeldata == null) {
				labeldata = new LabelData();
				flowdata.labeltable.put(name, labeldata);
			}
			labeldata.jumpnodes.add(out);
		}
		return null;
	}

	/** 处理continue语句 */
	@Override
	public Object visit(ASTContinueStatement treenode, Object data) {
		ControlFlowData flowdata = (ControlFlowData) data;
		Graph graph = flowdata.graph;
		VexNode in = flowdata.vexnode;
		VexNode out = graph.addVex("continue_", treenode);
		if (in != null) {
			graph.addEdgeWithFlag(in, out,false);
		}
		// 控制流已经转跳走了，出口置为null
		flowdata.vexnode = null;

		if (treenode.getImage() == null) {
			// continue 后无标号
			LoopData loopdata = null;
			// 查找非switch的循环
			ListIterator<LoopData> i = flowdata.loopstack.listIterator(flowdata.loopstack.size());
			while (i.hasPrevious()) {
				loopdata = i.previous();
				if (loopdata.name != "switch") {
					break;
				}
			}
			loopdata.continuenodes.add(out);
		} else {
			// continue 后有标号
			String name = treenode.getImage();
			LabelData labeldata = flowdata.labeltable.get(name);
			if (labeldata == null) {
				labeldata = new LabelData();
				flowdata.labeltable.put(name, labeldata);
			}
			labeldata.jumpnodes.add(out);
		}
		return null;
	}

	/** 处理switch语句 */
	@Override
	public Object visit(ASTSwitchStatement treenode, Object data) {
		ControlFlowData flowdata = (ControlFlowData) data;
		VexNode head, out, subout;
		VexNode in = flowdata.vexnode;
		Graph g = flowdata.graph;

		LoopData loopdata = new LoopData();
		loopdata.name = "switch";

		// 产生switch语句的头结点head
		head = g.addVex("switch_head_", treenode);
		loopdata.head = head;

		if (in != null) {
			g.addEdgeWithFlag(in, head,false);
		}
		// 当前循环入栈，switch和循环统一处理
		flowdata.loopstack.push(loopdata);
		
		//处理隐式异常
		addImplicitException(treenode,flowdata,head);

		// 以null为入口，调用子语句处理
		flowdata.vexnode = null;
		for (int i = 1; i < treenode.jjtGetNumChildren(); i++) {
			JavaNode javanode = (JavaNode) treenode.jjtGetChild(i);
			javanode.jjtAccept(this, flowdata);
		}
		subout = flowdata.vexnode;

		// 产生出口节点
		out = g.addVex("switch_out_", treenode);
		if (subout != null) {
			g.addEdgeWithFlag(subout, out,false);
		}

		// 当前循环出栈
		loopdata = flowdata.loopstack.pop();

		// 如果没有default子句，增加一条从head到out的边
		if (!loopdata.hasdefault) {
			g.addEdgeWithFlag(head, out,false);
		}

		// 处理break
		ListIterator<VexNode> i = loopdata.breaknodes.listIterator();
		while (i.hasNext()) {
			VexNode node1 = i.next();
			if (node1 != null) {
				flowdata.graph.addEdgeWithFlag(node1, out,false);
			}
		}
		flowdata.vexnode = out;
		return null;
	}

	/** 处理case,default语句 */
	@Override
	public Object visit(ASTSwitchLabel treenode, Object data) {
		ControlFlowData flowdata = (ControlFlowData) data;
		Graph graph = flowdata.graph;
		VexNode in = flowdata.vexnode;
		String name;
		if (treenode.isDefault()) {
			name = "default_";
		} else {
			name = "case_";
		}
		VexNode out = graph.addVex(name, treenode);

		// 连接当前switch头和case
		LoopData loopdata = flowdata.loopstack.peek();
		VexNode switchnode = loopdata.head;
		graph.addEdgeWithFlag(switchnode, out,false);

		// 设置default标志
		if (treenode.isDefault()) {
			loopdata.hasdefault = true;
		}

		if (in != null) {
			graph.addEdgeWithFlag(in, out,false);
		}
		flowdata.vexnode = out;
		return null;
	}

	/** 处理do-while语句 */
	@Override
	public Object visit(ASTDoStatement treenode, Object data) {
		ControlFlowData flowdata = (ControlFlowData) data;
		VexNode head, out1, out2, subout;
		VexNode in = flowdata.vexnode;
		Graph g = flowdata.graph;

		LoopData loopdata = new LoopData();
		loopdata.name = "do-while";
		// 产生do-while语句的头结点head
		head = g.addVex("do_while_head_", treenode);
		loopdata.head = head;

		if (in != null) {
			g.addEdgeWithFlag(in, head,false);
		}

		// 当前循环入栈
		flowdata.loopstack.push(loopdata);

		// 以头节点为入口，调用子语句处理
		flowdata.vexnode = head;
		JavaNode javanode = (JavaNode) (treenode).jjtGetChild(0);
		javanode.jjtAccept(this, flowdata);
		subout = flowdata.vexnode;
		out1 = g.addVex("do_while_out1_", (SimpleJavaNode) treenode.jjtGetChild(1));// 出口节点
		out2 = g.addVex("do_while_out2_", treenode);// 最终出口节点
		if (subout != null) {
			g.addEdgeWithFlag(subout, out1,false);
		}
		out1.truetag = true;
		g.addEdgeWithFlag(out1, head,false);
		out1.falsetag = true;
		g.addEdgeWithFlag(out1, out2,false);
		
		//处理隐式异常
		addImplicitException((SimpleJavaNode) treenode.jjtGetChild(1),flowdata,out1);

		// 当前循环出栈
		loopdata = flowdata.loopstack.pop();

		// 处理break
		ListIterator<VexNode> i = loopdata.breaknodes.listIterator();
		while (i.hasNext()) {
			VexNode node1 = i.next();
			if (node1 != null) {
				flowdata.graph.addEdgeWithFlag(node1, out2,false);
			}
		}

		// 处理continue
		i = loopdata.continuenodes.listIterator();
		while (i.hasNext()) {
			VexNode node1 = i.next();
			if (node1 != null) {
				flowdata.graph.addEdgeWithFlag(node1, head,false);
			}
		}
		flowdata.vexnode = out2;
		return null;
	}

	/** 处理for语句 */
	@Override
	public Object visit(ASTForStatement treenode, Object data) {
		// 处理for-each
		
		ControlFlowData flowdata = (ControlFlowData) data;
		VexNode head=null, init=null, add=null, out=null, subout=null;
		VexNode in = flowdata.vexnode;
		Graph g = flowdata.graph;

		LoopData loopdata = new LoopData();
		loopdata.name = "for";

		// 产生for语句的初始化节点
		List results = treenode.findDirectChildOfType(ASTForInit.class);
		if (!results.isEmpty()) {
			init = g.addVex("for_init_", (SimpleJavaNode) results.get(0));
			if (in != null) {
				g.addEdgeWithFlag(in, init,false);
			}
			
			//处理隐式异常
			addImplicitException((SimpleJavaNode) results.get(0),flowdata,init);
		} else {
			init = in;
		}
		// 产生for语句的头结点head
		head = g.addVex("for_head_", treenode);
		if (init != null) {
			g.addEdgeWithFlag(init, head,false);
		}
		loopdata.head = head;
		
		//处理隐式异常
		addImplicitException(treenode,flowdata,head);

		// 当前循环入栈
		flowdata.loopstack.push(loopdata);

		// 以头节点为入口，调用子语句处理
		head.truetag = true;
		flowdata.vexnode = head;
		// for 语句的初始化、条件和增量子句都可能没有
		JavaNode javanode = (JavaNode) (treenode).jjtGetChild(treenode.jjtGetNumChildren() - 1);
		javanode.jjtAccept(this, flowdata);
		subout = flowdata.vexnode;// 子语句出口

		results = treenode.findDirectChildOfType(ASTForUpdate.class);
		if (!results.isEmpty()) {
			add = g.addVex("for_inc_", (SimpleJavaNode) results.get(0));// 产生for语句的增量结点
			if (subout != null) {
				g.addEdgeWithFlag(subout, add,false);
			}
			//处理隐式异常
			addImplicitException((SimpleJavaNode) results.get(0),flowdata,add);
		} else {
			add = subout;
		}
		if (add != null) {
			g.addEdgeWithFlag(add, head,false);
		}

		// 产生for语句的出口节点
		out = g.addVex("for_out_", treenode);
		head.falsetag = true;
		g.addEdgeWithFlag(head, out,false);

		// 当前循环出栈
		loopdata = flowdata.loopstack.pop();
		// 处理break
		ListIterator<VexNode> i = loopdata.breaknodes.listIterator();
		while (i.hasNext()) {
			VexNode node1 = i.next();
			if (node1 != null) {
				flowdata.graph.addEdgeWithFlag(node1, out,false);
			}
		}

		// 处理continue
		i = loopdata.continuenodes.listIterator();
		while (i.hasNext()) {
			VexNode node1 = i.next();
			if (node1 != null) {
				if(add!=null){
					flowdata.graph.addEdgeWithFlag(node1, add,false);// continue到add节点
				}
				else{
					flowdata.graph.addEdgeWithFlag(node1, head,false);
				}
			}
		}
		flowdata.vexnode = out;
		return null;
	}

	/** 处理标号语句 */
	@Override
	public Object visit(ASTLabeledStatement treenode, Object data) {
		ControlFlowData flowdata = (ControlFlowData) data;
		Graph g = flowdata.graph;
		VexNode in = flowdata.vexnode;

		// 产生头节点
		String name = treenode.getImage();
		VexNode head = g.addVex("lable_head_" + name + "_", treenode);
		if (in != null) {
			g.addEdgeWithFlag(in, head,false);
		}

		// 以头节点为入口，调用子语句处理
		flowdata.vexnode = head;
		JavaNode javanode = (JavaNode) (treenode).jjtGetChild(0);
		javanode.jjtAccept(this, flowdata);
		
		// 增加一个标号语句的出口节点，用于处理java中break 出block的情形
		VexNode out = g.addVex("lable_out_" + name + "_", treenode);
		if (flowdata.vexnode != null) {
			g.addEdgeWithFlag(flowdata.vexnode, out,false);
		}
		flowdata.vexnode = out;

		// 在转跳表中填写标号节点
		// String name=treenode.getImage();
		LabelData labeldata = flowdata.labeltable.get(name);
		if (labeldata == null) {
			labeldata = new LabelData();
			flowdata.labeltable.put(name, labeldata);
		}
		labeldata.labelnode = head;

		// 针对break的处理
		name = "#" + name;
		labeldata = flowdata.labeltable.get(name);
		if (labeldata == null) {
			labeldata = new LabelData();
			flowdata.labeltable.put(name, labeldata);
		}		
		labeldata.labelnode = out;
		
		// 对于java来说此处已经可以处理所有标号转跳了
		name = treenode.getImage();
		labeldata = flowdata.labeltable.get(name);
		if (labeldata != null) {
			ListIterator<VexNode> i = labeldata.jumpnodes.listIterator();
			while (i.hasNext()) {
				VexNode node1 = i.next();
				VexNode node2 = labeldata.labelnode;
				if (node1 != null && node2 != null) {
					flowdata.graph.addEdgeWithFlag(node1, node2,false);
				}
			}
		}
		flowdata.labeltable.remove(name);

		name = "#" + name;
		labeldata = flowdata.labeltable.get(name);
		if (labeldata != null) {
			ListIterator<VexNode> i = labeldata.jumpnodes.listIterator();
			while (i.hasNext()) {
				VexNode node1 = i.next();
				VexNode node2 = labeldata.labelnode;
				if (node1 != null && node2 != null) {
					flowdata.graph.addEdgeWithFlag(node1, node2,false);
				}
			}
		}
		flowdata.labeltable.remove(name);
		return null;
	}

	/** 处理return语句 */
	@Override
	public Object visit(ASTReturnStatement treenode, Object data) {
		ControlFlowData flowdata = (ControlFlowData) data;
		Graph graph = flowdata.graph;
		VexNode in = flowdata.vexnode;

		VexNode out = graph.addVex("return_", treenode);
		if (in != null) {
			graph.addEdgeWithFlag(in, out,false);
		}
		
		//处理隐式异常
		addImplicitException(treenode,flowdata,out);
		
		// 添加节点到当前try的throw列表
		/*
		if ( flowdata.trystack != null && !flowdata.trystack.empty()) {
			List css = treenode.getParentsOfType(ASTCatchStatement.class);
			if (css != null && css.size() > 0) {
				TryData trydata = flowdata.trystack.peek();
				// trydata.thrownodes.add(out);
				trydata.catchouts.add(out);
			} else {
				css = treenode.getParentsOfType(ASTFinallyStatement.class);
				if (css != null && css.size() > 0) {
					TryData trydata = flowdata.trystack.peek();
					// trydata.thrownodes.add(out);
					trydata.catchouts.add(out);
				}
			}
		}*/

		// 在转跳表中相应位置加入转跳节点
		LabelData labeldata = flowdata.labeltable.get("return");
		if (labeldata == null) {
			labeldata = new LabelData();
			flowdata.labeltable.put("return", labeldata);
		}
		labeldata.jumpnodes.add(out);

		// 控制流已经转跳走了
		flowdata.vexnode = null;
		return null;
	}
	
	/**
	 * 判断a是否为b的子类
	 * @param a
	 * @param b
	 * @return
	 */
	private boolean isSelfOrSuperClass(Class a, Class b) {
		do {
			if (a.equals(b)) {
				return true;
			}
			a = a.getSuperclass();
		}
		while (a != null) ;
		return false;
	}
	
	/**
	 * try-catch-finally控制流图
	 * 改为每个可能抛出异常的控制流图节点与相应的捕获异常的catch头结点连接一条边
	 * 未被捕获的异常点传递给上层trydata结构或者函数的eout
	 * 
	 * yangxiu
	 * 20090504
	 * 
	 * 增加2个RL检测用例71,72
	 */
	public Object visit(ASTTryStatement treenode, Object data) {
		ControlFlowData flowdata = (ControlFlowData) data;
		Graph graph = flowdata.graph;
		VexNode in = flowdata.vexnode;

		TryData trydata = new TryData();
		trydata.name = "try";

		// 产生try语句的头结点head
		VexNode head = graph.addVex("try_head_", treenode);
		trydata.head = head;
		if (in != null) {
			graph.addEdgeWithFlag(in, head,false);
		}

		// 当前try入栈
		flowdata.trystack.push(trydata);

		// 以头节点为入口，调用子语句处理
		flowdata.vexnode = head;
		JavaNode javanode = (JavaNode) (treenode).jjtGetChild(0);
		javanode.jjtAccept(this, flowdata);
		trydata.out = flowdata.vexnode;
		
		// 保存try{...}内的异常点
		List<VexNode> throwsInTry = new ArrayList<VexNode>(trydata.thrownodes);
		List<Class> exceptionInTry = new ArrayList<Class>(trydata.exceptions);
		trydata.thrownodes.clear();
		trydata.exceptions.clear();
		
		// 生成catch{...}的控制流图
		// 每个catch头结点都不设置前驱节点
		// 保存在trydata.catchheads中
		for (int i = 0; i < treenode.jjtGetNumChildren(); i++) {
			if (treenode.jjtGetChild(i) instanceof ASTCatchStatement) {
				flowdata.vexnode = null;
				javanode = (JavaNode) (treenode).jjtGetChild(i);
				javanode.jjtAccept(this, flowdata);
				flowdata.vexnode = null;
			}
		}
		
		// 保存所有catch节点的异常点
		List<VexNode> throwsInCatch = new ArrayList<VexNode>(trydata.thrownodes);
		List<Class> exceptionInCatch = new ArrayList<Class>(trydata.exceptions);
		trydata.thrownodes.clear();
		trydata.exceptions.clear();
		
		// 上层try-catch结构
		TryData trydataup = flowdata.trystack.get(flowdata.trystack.size()-2);
		
		if (!treenode.hasFinally()) {
			// 把所有throw节点连接到对应的catch节点
			// throwsInTry删除被捕获的异常，保留未被捕获的异常
			List<Edge> elist = new ArrayList<Edge>();
			for (VexNode cat : trydata.catchheads) {
				ASTCatchStatement cs = (ASTCatchStatement) cat.getTreeNode();
				ASTClassOrInterfaceType except = (ASTClassOrInterfaceType) cs.getFirstChildOfType(ASTClassOrInterfaceType.class);
				Class eclass = TypeSet.getCurrentTypeSet().findClassWithoutEx(except.getImage());
				if (eclass == null) {
					continue;
				}
				
				List<VexNode> throwsInTry1 = new ArrayList<VexNode>();
				List<Class> exceptionInTry1 = new ArrayList<Class>();
				for (int i = 0 ; i < throwsInTry.size() ; ++i) {
					if (exceptionInTry.get(i) != null && isSelfOrSuperClass(exceptionInTry.get(i), eclass)) {
						if (throwsInTry.get(i).getEdgeByHead(cat) == null) {
							flowdata.graph.addEdgeWithFlag(throwsInTry.get(i), cat,true);
						}
					}
					else {
						throwsInTry1.add(throwsInTry.get(i));
						exceptionInTry1.add(exceptionInTry.get(i));
					}
				}
				throwsInTry = throwsInTry1;
				exceptionInTry = exceptionInTry1;
			}
			
		} else {// 有finally,javanode为finally的语法树节点			
			javanode = treenode.getFinally();
			
			// try的出口节点复制finally
			if (trydata.out != null) {
				flowdata.vexnode = trydata.out;
				javanode.jjtAccept(this, flowdata);
				trydata.out = flowdata.vexnode;
			}
			
			if (trydata.catchheads.size() > 0) {// 有catch节点
				// 把所有throw节点连接到对应的catch节点
				// throwsInTry删除被捕获的异常，保留未被捕获的异常
				for (VexNode cat : trydata.catchheads) {
					ASTCatchStatement cs = (ASTCatchStatement) cat.getTreeNode();
					ASTClassOrInterfaceType except = (ASTClassOrInterfaceType) cs.getFirstChildOfType(ASTClassOrInterfaceType.class);
					Class eclass = TypeSet.getCurrentTypeSet().findClassWithoutEx(except.getImage());
					if (eclass == null) {
						continue;
					}
					
					List<VexNode> throwsInTry1 = new ArrayList<VexNode>();
					List<Class> exceptionInTry1 = new ArrayList<Class>();
					for (int i = 0 ; i < throwsInTry.size() ; ++i) {
						if (exceptionInTry.get(i) != null && isSelfOrSuperClass(exceptionInTry.get(i), eclass)) {
							if (throwsInTry.get(i).getEdgeByHead(cat) == null) {
								flowdata.graph.addEdgeWithFlag(throwsInTry.get(i), cat,true);
							}
						}
						else {
							throwsInTry1.add(throwsInTry.get(i));
							exceptionInTry1.add(exceptionInTry.get(i));
						}
					}
					throwsInTry = throwsInTry1;
					exceptionInTry = exceptionInTry1;
				}
				
				// 为catch子句中的异常复制finally
				for (int index = 0; index < throwsInCatch.size(); index++) {
					flowdata.vexnode = throwsInCatch.get(index);
					javanode.jjtAccept(this, flowdata);
					// 重新设置thrownodes
					throwsInCatch.set(index, flowdata.vexnode);
				}

				// 为每个catch子句的正常出口复制finally
				for (int index = 0; index < trydata.catchouts.size(); index++) {
					flowdata.vexnode = trydata.catchouts.get(index);
					javanode.jjtAccept(this, flowdata);
					// 重新设置catchout
					trydata.catchouts.set(index, flowdata.vexnode);
				}
			}
			
			// 为Try中每个未被捕获的异常点复制finally
			for (int index = 0; index < throwsInTry.size(); index++) {
				flowdata.vexnode = throwsInTry.get(index);
				javanode.jjtAccept(this, flowdata);
				// 重新设置thrownodes
				throwsInTry.set(index, flowdata.vexnode);
			}

			// 为每个转跳复制finally
			SimpleJavaNode trytreenode = treenode;
			SimpleJavaNode finallytreenode = (SimpleJavaNode) javanode;
			if (flowdata.loopstack.size() > 0) {
				LoopData loopdata = flowdata.loopstack.peek();
				// 修改break
				for (int index = 0; index < loopdata.breaknodes.size(); index++) {
					VexNode node1 = loopdata.breaknodes.get(index);
					if (isParent(node1.treenode, trytreenode) && !isParent(node1.treenode, finallytreenode)) {
						flowdata.vexnode = node1;
						javanode.jjtAccept(this, flowdata);
						// 修改break转跳
						loopdata.breaknodes.set(index, flowdata.vexnode);
					}
				}
				// 修改continue
				for (int index = 0; index < loopdata.continuenodes.size(); index++) {
					VexNode node1 = loopdata.continuenodes.get(index);
					if (isParent(node1.treenode, trytreenode) && !isParent(node1.treenode, finallytreenode)) {
						flowdata.vexnode = node1;
						javanode.jjtAccept(this, flowdata);
						// 修改continue转跳
						loopdata.continuenodes.set(index, flowdata.vexnode);
					}
				}
			}

			// 修改转跳
			for (Enumeration<LabelData> e = flowdata.labeltable.elements(); e.hasMoreElements();) {
				LabelData labeldata = e.nextElement();
				for (int index = 0; index < labeldata.jumpnodes.size(); index++) {
					VexNode node1 = labeldata.jumpnodes.get(index);
					if (isParent(node1.treenode, trytreenode) && !isParent(node1.treenode, finallytreenode)) {
						flowdata.vexnode = node1;
						javanode.jjtAccept(this, flowdata);
						// 修改转跳
						labeldata.jumpnodes.set(index, flowdata.vexnode);
					}
				}
			}
		}
		
		// try内未被捕获的异常传给上层try
		trydataup.thrownodes.addAll(throwsInTry);
		trydataup.exceptions.addAll(exceptionInTry);
		
		// catch中的异常传递给上层try
		trydataup.thrownodes.addAll(throwsInCatch);
		trydataup.exceptions.addAll(exceptionInCatch);
		
		// finally抛出的异常
		// 若无finally，trydata.thrownodes是空链表
		trydataup.thrownodes.addAll(trydata.thrownodes);
		trydataup.exceptions.addAll(trydata.exceptions);
		
		boolean needtryout = false;
		// 判断是否需要tryout节点
		if (trydata.out != null) {
			needtryout = true;
		}
		ListIterator<VexNode> i = trydata.catchouts.listIterator();
		while (i.hasNext() && !needtryout) {
			VexNode node1 = i.next();
			if (node1 != null) {
				needtryout = true;
				break;
			}
		}

		if (needtryout) {
			// 增加try语句的正常out节点
			VexNode out = graph.addVex("try_out_", treenode);
			// 连接正常出口到out
			if (trydata.out != null) {
				flowdata.graph.addEdgeWithFlag(trydata.out, out,false);
			}
			// 连接所有的catch出口节点到out
			i = trydata.catchouts.listIterator();
			while (i.hasNext()) {
				VexNode node1 = i.next();
				if (node1 != null) {
					flowdata.graph.addEdgeWithFlag(node1, out,false);
				}
			}
			flowdata.vexnode = out;
		} else {
			flowdata.vexnode = null;
		}
		
		//当前try出栈
		trydata = flowdata.trystack.pop();
		return null;
	}

	/** 处理catch语句 */
	@Override
	public Object visit(ASTCatchStatement treenode, Object data) {
		ControlFlowData flowdata = (ControlFlowData) data;
		Graph graph = flowdata.graph;
		VexNode in = flowdata.vexnode;

		VexNode head = graph.addVex("catch_head_", treenode);
		if (in != null) {
			graph.addEdgeWithFlag(in, head,false);
		}
		// 添加头节点到当前try的catch头列表
		TryData trydata = flowdata.trystack.peek();
		trydata.catchheads.add(head);

		// 以头为入口节点调用子句处理
		head.truetag = true;
		flowdata.vexnode = head;
		JavaNode javanode = (JavaNode) (treenode).jjtGetChild(1);
		javanode.jjtAccept(this, flowdata);

		// 保存catch出口到当前try
		if (flowdata.vexnode != null) {
			trydata.catchouts.add(flowdata.vexnode);
		}

		// 设置catch头为出口
		head.falsetag = true;
		flowdata.vexnode = head;
		return null;
	}

	/** 处理throw语句 */
	@Override
	public Object visit(ASTThrowStatement treenode, Object data) {
		ControlFlowData flowdata = (ControlFlowData) data;
		Graph graph = flowdata.graph;
		VexNode in = flowdata.vexnode;

		VexNode head = graph.addVex("throw_", treenode);
		if (in != null) {
			graph.addEdgeWithFlag(in, head,false);
		}
		// 添加节点到当前try的throw列表
		TryData trydata = flowdata.trystack.peek();
		trydata.thrownodes.add(head);
		trydata.exceptions.add((Class)((ExpressionBase)treenode.jjtGetChild(0)).getType());
		//trydata.catchouts.add(head);

		// 设置null为出口
		flowdata.vexnode = null;
		return null;
	}

	/** 处理finally语句 */
	@Override
	public Object visit(ASTFinallyStatement treenode, Object data) {
		ControlFlowData flowdata = (ControlFlowData) data;
		Graph graph = flowdata.graph;
		VexNode in = flowdata.vexnode;

		VexNode head = graph.addVex("Finally_", treenode);
		if (in != null) {
			graph.addEdgeWithFlag(in, head,false);
		}
		TryData trydata = flowdata.trystack.peek();
		trydata.finallyhead = head;
		// 以头为入口节点调用子句处理
		flowdata.vexnode = head;
		JavaNode javanode = (JavaNode) (treenode).jjtGetChild(0);
		javanode.jjtAccept(this, flowdata);
		return null;
	}

	/** 处理assert语句 */
	@Override
	public Object visit(ASTAssertStatement treenode, Object data) {
		ControlFlowData flowdata = (ControlFlowData) data;
		Graph graph = flowdata.graph;
		VexNode in = flowdata.vexnode;
		VexNode out = graph.addVex("Assert_", treenode);
		if (in != null) {
			graph.addEdgeWithFlag(in, out,false);
		}
		
		//处理隐式异常
		addImplicitException(treenode,flowdata,out);		
		
		flowdata.vexnode = out;
		return null;
	}

	/** 处理synchronized语句 */
	@Override
	public Object visit(ASTSynchronizedStatement treenode, Object data) {
		ControlFlowData flowdata = (ControlFlowData) data;
		Graph graph = flowdata.graph;
		VexNode in = flowdata.vexnode;
		VexNode out = graph.addVex("Synchronized_", treenode);
		if (in != null) {
			graph.addEdgeWithFlag(in, out,false);
		}
		//处理隐式异常
		addImplicitException(treenode,flowdata,out);		
		
		// 以out为入口节点调用子句处理
		flowdata.vexnode = out;
		JavaNode javanode = (JavaNode) (treenode).jjtGetChild(1);
		javanode.jjtAccept(this, flowdata);
		return null;
	}
	
	/** 跳过处理静态函数块 */
	@Override
	public Object visit(ASTInitializer treenode,Object data){
		return null;
	}
	
	private void addImplicitException(SimpleJavaNode treenode ,ControlFlowData flowdata,VexNode vexnode){
		SimpleJavaNode connode=(SimpleJavaNode)treenode.getConcreteNode();
		ImplicitExceptionFinder exfinder=new ImplicitExceptionFinder();
		if(connode!=null&&!(connode instanceof ASTConstructorDeclaration)&&!(connode instanceof ASTMethodDeclaration)){
			connode.jjtAccept(exfinder, null);	
			if(exfinder.isHasImplicitException()){
				TryData trdata=flowdata.trystack.peek();
				if(trdata!=null){
					for (Class ec : exfinder.getExceptions()) {
						trdata.thrownodes.add(vexnode);
						trdata.exceptions.add(ec);
					}
				}
				vexnode.addExceptions(exfinder.getExceptions());
			}
		}
	}
}
