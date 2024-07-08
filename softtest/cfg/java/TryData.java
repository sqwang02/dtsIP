package softtest.cfg.java;

import java.util.*;

/** 存异常数据的类 */
public class TryData {
	/** 名称 */
	String name = null;

	/** try语句的头节点 */
	VexNode head = null;

	/** try语句的出口节点 */
	VexNode out = null;

	/** catch子句头节点集合 */
	LinkedList<VexNode> catchheads = new LinkedList<VexNode>();

	/** catch子句出口节点集合 */
	LinkedList<VexNode> catchouts = new LinkedList<VexNode>();

	/** throw语句节点集合 */
	LinkedList<VexNode> thrownodes = new LinkedList<VexNode>();
	LinkedList<Class> exceptions = new LinkedList<Class>();

	/** finally子句的头节点 */
	VexNode finallyhead = null;
}
