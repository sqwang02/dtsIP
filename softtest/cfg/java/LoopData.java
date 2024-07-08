package softtest.cfg.java;

import java.util.*;

/** 存循环数据的类 */
public class LoopData {
	/** 名称 */
	String name = null;

	/** 循环语句的头节点 */
	VexNode head = null;
	
	/** switch是否有default标志 */
	boolean hasdefault = false;

	/** break节点集合 */
	LinkedList<VexNode> breaknodes = new LinkedList<VexNode>();

	/** continue节点集合 */
	LinkedList<VexNode> continuenodes = new LinkedList<VexNode>();
}