package softtest.callgraph.java.method;

import java.util.Hashtable;

import softtest.ast.java.SimpleJavaNode;


/**ǰ�����������߼���*/
public class PreconditionListenerSet {
	/**ǰ�����������߹�ϣ��*/
	Hashtable<AbstractPreconditionListener,AbstractPreconditionListener> listeners=new Hashtable<AbstractPreconditionListener,AbstractPreconditionListener>();
	
	/**��Ӽ�����*/
	public void addListener(AbstractPreconditionListener listener){
		listeners.put(listener,listener);
	}
	
	/**�Ƴ�������*/
	public void removeListener(AbstractPreconditionListener listener){
		listeners.remove(listener);
	}
	
	/**����*/
	public void listen(SimpleJavaNode node){
		for(AbstractPreconditionListener listener:listeners.values()){
			listener.listen(node);
		}
	}
	
	/**���õ�ǰ������ǰ����������*/
	public void setPreconditionSetForListeners(PreconditionSet set){
		for(AbstractPreconditionListener l:listeners.values()){
			l.setPreconditionSet(set);
		}
	}
}
