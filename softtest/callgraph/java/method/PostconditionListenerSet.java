package softtest.callgraph.java.method;

import java.util.*;

import softtest.ast.java.SimpleJavaNode;


public class PostconditionListenerSet {
	/**ǰ�����������߹�ϣ��*/
	Hashtable<AbstractPostconditionListener,AbstractPostconditionListener> listeners=new Hashtable<AbstractPostconditionListener,AbstractPostconditionListener>();
	
	/**��Ӽ�����*/
	public void addListener(AbstractPostconditionListener listener){
		listeners.put(listener,listener);
	}
	
	/**�Ƴ�������*/
	public void removeListener(AbstractPostconditionListener listener){
		listeners.remove(listener);
	}
	
	/**����*/
	public void listen(SimpleJavaNode node){
		for(AbstractPostconditionListener listener:listeners.values()){
			listener.listen(node);
		}
	}
	
	/**���õ�ǰ������ǰ����������*/
	public void setPostconditionSetForListeners(PostconditionSet set){
		for(AbstractPostconditionListener l:listeners.values()){
			l.setPostconditionSet(set);
		}
	}
}
