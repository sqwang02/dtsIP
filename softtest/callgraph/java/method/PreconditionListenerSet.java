package softtest.callgraph.java.method;

import java.util.Hashtable;

import softtest.ast.java.SimpleJavaNode;


/**前置条件监听者集合*/
public class PreconditionListenerSet {
	/**前置条件监听者哈希表*/
	Hashtable<AbstractPreconditionListener,AbstractPreconditionListener> listeners=new Hashtable<AbstractPreconditionListener,AbstractPreconditionListener>();
	
	/**添加监听者*/
	public void addListener(AbstractPreconditionListener listener){
		listeners.put(listener,listener);
	}
	
	/**移除监听者*/
	public void removeListener(AbstractPreconditionListener listener){
		listeners.remove(listener);
	}
	
	/**监听*/
	public void listen(SimpleJavaNode node){
		for(AbstractPreconditionListener listener:listeners.values()){
			listener.listen(node);
		}
	}
	
	/**设置当前函数的前置条件集合*/
	public void setPreconditionSetForListeners(PreconditionSet set){
		for(AbstractPreconditionListener l:listeners.values()){
			l.setPreconditionSet(set);
		}
	}
}
