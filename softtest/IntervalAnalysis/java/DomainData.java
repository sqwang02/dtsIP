package softtest.IntervalAnalysis.java;

import softtest.cfg.java.VexNode;

/** 用于表达式域处理的数据传递，参见ExpressionDomainVisitor */
public class DomainData {
	private VexNode currentVex = null;
	
	/** 域类型 */
    public ClassType type=null;
    
    /** 域 */
    public Object domain=null;
    
    /** 控制是否产生副作用的标志 */
    public boolean sideeffect=true;
    
    /** 是否第一次的初步区间运算 */
    public boolean firstcal=false;

	/**
	 * @return the currentVex
	 */
	public VexNode getCurrentVex() {
		return currentVex;
	}

	/**
	 * @param currentVex the currentVex to set
	 */
	public void setCurrentVex(VexNode currentVex) {
		this.currentVex = currentVex;
	}
	
	/** 空构造方法 */
	public DomainData() {
		// do nothing
	}

	/** 指定当前控制流节点 */
	public DomainData(VexNode currentVex) {
		this.currentVex = currentVex;
	}
}
