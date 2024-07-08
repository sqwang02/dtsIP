package softtest.cfg.java;

import java.util.*;

/** 用于控制流图生成过程中的数据传递，语义信息传递，请参考ControlFlowVisitor */
public class ControlFlowData {
	/** 当前控制流图的出口节点 */
	VexNode vexnode = null; 

	/** 当前控制流图 */
	Graph graph = null; 

	/** 循环数据栈 */
	Stack<LoopData> loopstack = new Stack<LoopData>(); 

	/** 转跳数据表 */
	Hashtable<String,LabelData> labeltable = new Hashtable<String,LabelData>();

	/** try数据栈 */
	Stack<TryData> trystack = new Stack<TryData>();
}