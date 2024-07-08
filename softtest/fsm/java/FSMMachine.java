package softtest.fsm.java;

import java.io.IOException;
import java.util.*;
import javax.tools.*;
import org.w3c.dom.*;

import softtest.fsmanalysis.java.ProjectAnalysis;
import softtest.rules.java.AbstractStateMachine;

/** 故障模式状态机 */
public class FSMMachine extends FSMElement {
	/** 模型分类 */
	private String modeltype="error";
	/** 内部编号 */
	private int statecount = 0;

	/** 内部编号 */
	private int transitioncount = 0;
	private int id;

	/** 名称 */
	private String name;

	/** 是否路径敏感的标志 */
	private boolean ispathsensitive = false;

	/** 是否变量相关的标志 */
	private boolean isvariablerelated = false;
	
	/** 是否以文件为单位创建状态机实例*/
	private boolean iscreatedbyfile=false;

	/** 状态集合 */
	private Hashtable<String, FSMState> states = new Hashtable<String, FSMState>();

	/** 转换集合 */
	private Hashtable<String, FSMTransition> transitions = new Hashtable<String, FSMTransition>();

	/** 状态机中所有关联动作所属的类 */
	private Class relatedclass = null;

	/** 获得状态机的关联类 */
	public Class getRelatedClass() {
		return relatedclass;
	}

	/** 设置状态机的关联类 */
	public void setRelatedClass(Class relatedclass) {
		this.relatedclass = relatedclass;
	}

	/** 获得状态集合 */
	public Hashtable<String, FSMState> getStates() {
		return states;
	}

	/** 设置状态集合 */
	public void setStates(Hashtable<String, FSMState> states) {
		this.states = states;
	}	

	/** 获得转换集合 */
	public Hashtable<String, FSMTransition> getTransitions() {
		return transitions;
	}
	
	/** 设置转换集合 */
	public void setTransitions(Hashtable<String, FSMTransition> transitions) {
		this.transitions = transitions;
	}		
	
	/** 缺省参数构造函数 */
	public FSMMachine() {
		name = "";
		fsm = this;
	}

	/** 指定名称构造状态机 */
	public FSMMachine(String name) {
		this.name = name;
	}

	/** 获得名称 */
	public String getName() {
		return name;
	}

	/** 设置名称 */
	public void setName(String name) {
		this.name = name;
	}
	
	/** 获得模型分类 */
	public String getModelType() {
		return modeltype;
	}

	/** 设置模型分类 */
	public void setModelType(String modeltype) {
		this.modeltype = modeltype;
	}

	/** 设置变量相关标志 */
	public void setVariableRelated(boolean isvariablerelated) {
		this.isvariablerelated = isvariablerelated;
	}

	/** 获得变量相关标志 */
	public boolean isVariableRelated() {
		return isvariablerelated;
	}

	/** 设置路径敏感标志 */
	public void setPathSensitive(boolean ispathsensitive) {
		this.ispathsensitive = ispathsensitive;
	}

	/** 获得路径敏感标志 */
	public boolean isPathSensitive() {
		return ispathsensitive;
	}
	
	
	/** 设置是否以文件位单位创建状态机的标志 */
	public void setCreatedByFile(boolean iscreatedbyfile) {
		this.iscreatedbyfile = iscreatedbyfile;
	}

	/** 获得是否以文件位单位创建状态机的标志 */
	public boolean isCreatedByFile() {
		return iscreatedbyfile;
	}

	/** 状态机访问者的accept方法 */
	@Override
	public void accept(FSMVisitor visitor, Object data) {
		visitor.visit(this, data);
	}

	/** 增加一个状态，如果该状态已经存在，则抛出异常 */
	public FSMState addState(FSMState state) {
		if (states.get(state.getName()) != null) {
			throw new RuntimeException("The state has already existed.");
		}
		states.put(state.getName(), state);
		state.setSnumber(statecount++);
		state.fsm = this;
		return state;
	}

	/** 增加一个指定名称的状态，最终的名称将为name */
	public FSMState addState(String name) {
		FSMState state = new FSMState(name);
		return addState(state);
	}

	/** 增加一个没有指定名称的状态，最终的名称将为statecount */
	public FSMState addState() {
		String name = "" + statecount;
		return addState(name);
	}

	/** 增加一个指定出发、到达状态的转换，并设定名称为name */
	public FSMTransition addTransition(FSMState fromstate, FSMState tostate, String name) {
		if (fromstate == null || tostate == null) {
			throw new RuntimeException("An transition's fromstate or tostate cannot be null.");
		}
		if (transitions.get(name) != null) {
			throw new RuntimeException("The transition has already existed.");
		}
		if (states.get(fromstate.getName()) != fromstate || states.get(tostate.getName()) != tostate) {
			throw new RuntimeException("There is a contradiction.");
		}

		if (tostate.getInTransitions().get(name) != null) {
			throw new RuntimeException("There is a contradiction.");
		}

		if (fromstate.getOutTransitions().get(name) != null) {
			throw new RuntimeException("There is a contradiction.");
		}

		FSMTransition e = new FSMTransition(name, fromstate, tostate);
		transitions.put(name, e);
		e.setSnumber(transitioncount++);
		e.fsm = this;

		return e;
	}

	/** 增加一个指定出发、到达状态的转换，并设定名称 */
	public FSMTransition addTransition(String from, String to, String name) {
		FSMState fromstate = states.get(from);
		FSMState tostate = states.get(to);
		return addTransition(fromstate, tostate, name);
	}

	/** 增加一个指定出发、到达状态的转换,名称默认为transitioncount */
	public FSMTransition addTransition(String from, String to) {
		String name = "" + transitioncount;
		return addTransition(from, to, name);
	}

	/** 增加一个指定出发、到达状态的转换,名称默认为transitioncount */
	public FSMTransition addTransition(FSMState fromstate, FSMState tostate) {
		String name = "" + transitioncount;
		return addTransition(fromstate, tostate, name);
	}

	/** 增加一个指定尾、头节点的边，并设定名称为name+edgecount */
	public FSMTransition addTransition(String name, FSMState fromstate, FSMState tostate) {
		name = name + transitioncount;
		return addTransition(fromstate, tostate, name);
	}

	/** 删除指定的转换，如果找不到该转换或者该转换不是状态机中的转换则抛出异常 */
	public void removeTransition(FSMTransition e) {
		if (transitions.get(e.getName()) != e || e == null) {
			throw new RuntimeException("Cannot find the transition.");
		}
		if (e.getFromState() == null || e.getToState() == null) {
			throw new RuntimeException("There is a contradiction.");
		}
		if (e.getToState().getInTransitions().get(e.getName()) != e || e.getFromState().getOutTransitions().get(e.getName()) != e) {
			throw new RuntimeException("There is a contradiction.");
		}

		e.getToState().getInTransitions().remove(e.getName());
		e.getFromState().getOutTransitions().remove(e.getName());
		transitions.remove(e.getName());

	}

	/** 删除指定的转换 */
	public void removeTransition(String name) {
		FSMTransition e = transitions.get(name);
		removeTransition(e);
	}

	/** 删除指定状态的所有进入转换 */
	public void removeInTransitions(FSMState state) {
		LinkedList<FSMTransition> temp = new LinkedList<FSMTransition>();
		temp.clear();
		for (Enumeration<FSMTransition> e = state.getInTransitions().elements(); e.hasMoreElements();) {
			temp.add(e.nextElement());
		}
		ListIterator<FSMTransition> i = temp.listIterator();
		while (i.hasNext()) {
			FSMTransition tran = i.next();
			removeTransition(tran);
		}
	}

	/** 删除指定状态的所有出发转换 */
	public void removeOutTransitions(FSMState state) {
		LinkedList<FSMTransition> temp = new LinkedList<FSMTransition>();
		temp.clear();
		for (Enumeration<FSMTransition> e = state.getOutTransitions().elements(); e.hasMoreElements();) {
			temp.add(e.nextElement());
		}
		ListIterator<FSMTransition> i = temp.listIterator();
		while (i.hasNext()) {
			FSMTransition tran = i.next();
			removeTransition(tran);
		}
	}

	/** 删除指定状态及其关联的转换 */
	public void removeState(FSMState state) {
		if (states.get(state.getName()) != state || state == null) {
			throw new RuntimeException("Cannot find the state.");
		}
		removeInTransitions(state);
		removeOutTransitions(state);
		states.remove(state.getName());
	}

	/** 删除指定状态及其关联的转换 */
	public void removeState(String name) {
		FSMState state = states.get(name);
		removeState(state);
	}

	/** 获得状态机的开始状态 */
	public FSMState getStartState() {
		FSMState state = null;
		for (Enumeration<FSMState> e = states.elements(); e.hasMoreElements();) {
			FSMState temp = e.nextElement();
			if (temp.isStart()) {
				state = temp;
				break;
			}
		}
		return state;
	}

	/** 创建一个状态机实例，并将该实例的状态机集合加入一个开始状态实例 */
	public FSMMachineInstance creatInstance() {
		FSMMachineInstance instance = new FSMMachineInstance();
		instance.setFSMMachine(this);
		FSMState start = getStartState();
		if (start != null) {
			FSMStateInstance e = new FSMStateInstance(start);
			instance.addStateInstance(e);
		}
		return instance;
	}

	/** 解析xml */
	@Override
	public void loadXML(Node n) {
		setName(n.getNodeName());
		//xml中Type的值为category的值，比如Type="NPD"
		if (n.getAttributes().getNamedItem("Type") != null) {
			setName(n.getAttributes().getNamedItem("Type").getNodeValue());
		}
		//设置状态机是否为路径敏感
		if (n.getAttributes().getNamedItem("isPathSensitive") != null && n.getAttributes().getNamedItem("isPathSensitive").getNodeValue().equals("true")) {
			setPathSensitive(true);
		} else {
			setPathSensitive(false);
		}
		//设置是否以文件创建状态机
		if (n.getAttributes().getNamedItem("isCreatedByFile") != null && n.getAttributes().getNamedItem("isCreatedByFile").getNodeValue().equals("true")) {
			setCreatedByFile(true);
		} else {
			setCreatedByFile(false);
		}
		Node nodeclass = n.getAttributes().getNamedItem("Class");
		Node nodeaction = n.getAttributes().getNamedItem("Action");
		
		if (n.getAttributes().getNamedItem("isVariableRelated") != null && n.getAttributes().getNamedItem("isVariableRelated").getNodeValue().equals("true")) {
			setVariableRelated(true);
			// 变量和路径相关的状态机必须有动作，通常该动作用于状态机实例的创建
			if (nodeclass == null || nodeaction == null) {
				throw new RuntimeException("VariableRelated fsm must has \"Class\" and \"Action\" attributes.");
			}
		} else {
			setVariableRelated(false);
		}
		
		// 解析状态机关联动作的所属类
		if (nodeclass != null) {
			if (softtest.config.java.Config.COMPILEFSM) {
				JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
				if (compiler != null) {
					// 编译指定的类的源码，这里的速度较慢
					int result = compiler.run(null, null, null, nodeclass.getNodeValue().replace('.', '\\') + ".java");
					if (result != 0) {
						throw new RuntimeException("Fail to compile the related action file.");
					}
				} else {
					System.out.println("编译动作程序失败");
					throw new RuntimeException("Fail to compile the related action file.");
				}
			}
			try {
				Class<?> relatedclass = Class.forName(nodeclass.getNodeValue());
				setRelatedClass(relatedclass);
			} catch (ClassNotFoundException e) {
				if(softtest.config.java.Config.DEBUG){
					e.printStackTrace();
				}
				throw new RuntimeException("Fail to find the related class.",e);
			}
			//为了分析函数摘要，注册状态机需要的函数摘要前置条件、后置条件、函数特征信息
			if(relatedclass!=null){
				try{
					AbstractStateMachine instance=(AbstractStateMachine)relatedclass.newInstance();
					instance.registerPrecondition(ProjectAnalysis.getPreconditionListener());
					instance.registerPostcondition(ProjectAnalysis.getPostconditionListener());
					instance.registerFeature(ProjectAnalysis.getFeatureListenerSet());
				}catch(Exception e){
					
				}
			}
		}
		loadAction(n, this.relatedclass);
	}
	
	/** 打印 */
	@Override
	public String toString() {
		return name;
	}
	
	/** 输出到文件 */
	public void dump(){
		String name = softtest.config.java.Config.DEBUGPATH+getName();
		accept(new DumpFSMVisitor(), name+".dot");
		System.out.println("状态机输出到了文件" + name + ".dot");
		try {
			java.lang.Runtime.getRuntime().exec("dot -Tjpg -o " + name + ".jpg " + name + ".dot").waitFor();
		} catch (IOException e1) {
			System.out.println(e1);
		} catch (InterruptedException e2) {
			System.out.println(e2);
		}
		System.out.println("状态机打印到了文件" + name + ".jpg");
	}
}
