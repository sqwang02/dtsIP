package softtest.fsm.java;

import java.util.*;
import softtest.cfg.java.*;
import org.w3c.dom.*;

/** 故障模式状态机状态 */
public class FSMState extends FSMElement implements Comparable<FSMState>{
	/** 名称 */
	private String name;
	
	/** 是否为开始状态的标志 */
	private boolean isstart = false;
	
	/** 是否为终结状态的标志 */
	private boolean isfinal = false;
	
	/** 是否为错误状态的标志 */
	private boolean iserror = false;
	
	/** 入边集合 */
	private Hashtable<String, FSMTransition> intrans = new Hashtable<String, FSMTransition>();

	/** 出边集合 */
	private Hashtable<String, FSMTransition> outtrans = new Hashtable<String, FSMTransition>();

	/** 访问标志 */
	private boolean visited = false;
	
	/** 编号，也用于比较的数字 */
	private int snumber = 0;
	
	/** 状态机访问者的accept */
	@Override
	public void accept(FSMVisitor visitor, Object data) {
		visitor.visit(this, data);
	}
	
	/** 比较区间的顺序，用于排序 */
	public int compareTo(FSMState e) {
		if (snumber == e.snumber) {
			return 0;
		} else if (snumber > e.snumber) {
			return 1;
		} else {
			return -1;
		}
	}
	
	/** 设置序号 */
	public void setSnumber(int snumber){
		this.snumber=snumber;
	}
	
	/** 获得序号 */
	public int getSnumber(){
		return snumber;
	}
	
	/** 以指定的名字创建状态 */
	public FSMState(String name) {
		this.name = name;
	}
	
	/** 设置状态访问标志 */
	public void setVisited(boolean visited) {
		this.visited = visited;
	}

	/** 获得状态访问标志 */
	public boolean getVisited() {
		return visited;
	}
	
	/** 获得节点名称*/
	public String getName(){
		return name;
	}
	
	public void setName(String name){
		this.name=name;
	}
	
	/** 获得是否为开始状态标志 */
	public boolean isStart(){
		return isstart;
	}
	
	/** 设置是否为开始状态标志 */
	public void setStart(boolean isstart){
		this.isstart=isstart;
	}
	
	/** 获得是否为终结状态标志 */
	public boolean isFinal(){
		return isfinal;
	}
	
	/** 设置是否为终结状态标志 */
	public void setFinal(boolean isfinal){
		this.isfinal=isfinal;
	}
	
	/** 获得是否为错误状态标志 */
	public boolean isError(){
		return iserror;
	}
	
	/** 设置是否为错误状态标志 */
	public void setError(boolean iserror){
		this.iserror=iserror;
	}
	
	/** 获得进入状态集合*/
	public Hashtable<String,FSMTransition> getInTransitions(){
		return intrans;
	}
	
	/** 设置进入状态集合*/
	public void setInTransitions(Hashtable<String,FSMTransition> intrans){
		this.intrans= intrans;
	}
	
	/** 获得出发状态集合*/
	public Hashtable<String,FSMTransition> getOutTransitions(){
		return outtrans;
	}
	
	/** 设置出发状态集合*/
	public void setOutTransitions(Hashtable<String,FSMTransition> outtrans){
		this.outtrans=outtrans;
	}
	
	@Override
	public void loadXML(Node n){
		if (n.getAttributes().getNamedItem("isStartState") != null && n.getAttributes().getNamedItem("isStartState").getNodeValue().equals("true")) {
			isstart=true;
		} else {
			isstart=false;
		}

		if (n.getAttributes().getNamedItem("isErrorState") != null && n.getAttributes().getNamedItem("isErrorState").getNodeValue().equals("true")) {
			iserror=true;
		} else {
			iserror=false;
		}

		if (n.getAttributes().getNamedItem("isFinalState") != null && n.getAttributes().getNamedItem("isFinalState").getNodeValue().equals("true")) {
			isfinal=true;
		} else {
			isfinal=false;
		}
		//设置每个变量的相关函数
		if(fsm!=null&&fsm.getRelatedClass()!=null){
			loadAction(n,fsm.getRelatedClass());
		}
	}
	
	/** 打印 */
	@Override
	public String toString() {
		return name;
	}
	
	public boolean invokeRelatedMethod(VexNode n,FSMMachineInstance fsm){
		boolean r=true;
		if(getRelatedMethod()!=null){
			try {
				Object[] args = new Object[2];
				args[0] = n;
				args[1] = fsm;
				r=(Boolean) getRelatedMethod().invoke(null, args);
			} catch (Exception ex) {
				if(softtest.config.java.Config.DEBUG){
					ex.printStackTrace();
				}
				throw new RuntimeException("Can't invoke state method.",ex);
			}
		}
		return r;
	}
}