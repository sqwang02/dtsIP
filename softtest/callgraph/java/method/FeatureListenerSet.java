package softtest.callgraph.java.method;

import java.util.*;

import softtest.ast.java.SimpleJavaNode;


public class FeatureListenerSet {
	/**������Ϣ�����߹�ϣ��*/
	Hashtable<AbstractFeatureListener,AbstractFeatureListener> listeners=new Hashtable<AbstractFeatureListener,AbstractFeatureListener>();
	
	/**��Ӽ�����*/
	public void addListener(AbstractFeatureListener listener){
		listeners.put(listener,listener);
	}
	
	/**�Ƴ�������*/
	public void removeListener(AbstractFeatureListener listener){
		listeners.remove(listener);
	}
	
	/**����*/
	public void listen(SimpleJavaNode node){
		for(AbstractFeatureListener listener:listeners.values()){
			listener.listen(node);
		}
	}
	
	/**���õ�ǰ������������Ϣ����*/
	public void setFeatureSetForListeners(FeatureSet set){
		for(AbstractFeatureListener l:listeners.values()){
			l.setFeatureSet(set);
		}
	}
}
