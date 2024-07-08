package softtest.callgraph.java.method;

import java.util.*;

import softtest.ast.java.SimpleJavaNode;


public class PostconditionListenerSet {
	/**前置条件监听者哈希表*/
	Hashtable<AbstractPostconditionListener,AbstractPostconditionListener> listeners=new Hashtable<AbstractPostconditionListener,AbstractPostconditionListener>();
	
	/**添加监听者*/
	public void addListener(AbstractPostconditionListener listener){
		listeners.put(listener,listener);
	}
	
	/**移除监听者*/
	public void removeListener(AbstractPostconditionListener listener){
		listeners.remove(listener);
	}
	
	/**监听*/
	public void listen(SimpleJavaNode node){
		for(AbstractPostconditionListener listener:listeners.values()){
			listener.listen(node);
		}
	}
	
	/**设置当前函数的前置条件集合*/
	public void setPostconditionSetForListeners(PostconditionSet set){
		for(AbstractPostconditionListener l:listeners.values()){
			l.setPostconditionSet(set);
		}
	}
}
