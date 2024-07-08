package softtest.callgraph.java.method;

import java.util.*;

import softtest.ast.java.SimpleJavaNode;


public class FeatureListenerSet {
	/**特征信息监听者哈希表*/
	Hashtable<AbstractFeatureListener,AbstractFeatureListener> listeners=new Hashtable<AbstractFeatureListener,AbstractFeatureListener>();
	
	/**添加监听者*/
	public void addListener(AbstractFeatureListener listener){
		listeners.put(listener,listener);
	}
	
	/**移除监听者*/
	public void removeListener(AbstractFeatureListener listener){
		listeners.remove(listener);
	}
	
	/**监听*/
	public void listen(SimpleJavaNode node){
		for(AbstractFeatureListener listener:listeners.values()){
			listener.listen(node);
		}
	}
	
	/**设置当前函数的特征信息集合*/
	public void setFeatureSetForListeners(FeatureSet set){
		for(AbstractFeatureListener l:listeners.values()){
			l.setFeatureSet(set);
		}
	}
}
