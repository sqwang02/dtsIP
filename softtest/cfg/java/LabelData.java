package softtest.cfg.java;

import java.util.*;

/**  存储标号数据的类 */
public class LabelData {
	/** 标号语句对应控制流图节点 */
	VexNode labelnode = null;

	/** 转跳语句对应控制流图节点列表 */
	LinkedList<VexNode> jumpnodes = new LinkedList<VexNode>();
}