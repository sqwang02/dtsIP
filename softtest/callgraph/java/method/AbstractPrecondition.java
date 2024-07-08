package softtest.callgraph.java.method;

import java.util.*;

import softtest.ast.java.SimpleJavaNode;


/**前置条件抽象类*/
abstract public class AbstractPrecondition {
	/**前置条件包含的变量表，key为变量，value当前为跟踪函数调用的trace信息*/
	Hashtable<MapOfVariable,List<String>> table=new Hashtable<MapOfVariable,List<String>>();
	

	/**
	 * 被框架调用的接口，针对每个控制流节点进行处理，框架在遍历控制流图的过程中会用
	 * 当前遍历到的控制流图节点不断调用该接口，set为当前函数的前置条件集合，如果需
	 * 要在摘要中增加前置条件则将this添加到前置条件集合set中。
	 * @param node 方法或者构造函数对应语法树节点
	 * @param set 当前函数的前置条件集合
	 */
	abstract public void listen(SimpleJavaNode node,PreconditionSet set);
	
	public Hashtable<MapOfVariable,List<String>> getTable(){
		return table;
	}
}
