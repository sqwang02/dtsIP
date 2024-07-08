package softtest.callgraph.java.method;

import softtest.ast.java.SimpleJavaNode;

public abstract class AbstractFeatureListener {
	/**当前函数的特征信息集合*/
	protected FeatureSet set=null;
	
	/**获得当前函数的特征信息集合*/
	public FeatureSet getFeatureSet() {
		return set;
	}
	
	/**设置当前函数的特征信息集合*/
	public void setFeatureSet(FeatureSet set) {
		this.set = set;
	}
	
	/**可以忽略存在的抽象类构造方法*/
	protected AbstractFeatureListener(){}
	
	/**监听接口*/
	abstract public void listen(SimpleJavaNode node);
}
