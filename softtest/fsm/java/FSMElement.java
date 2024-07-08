package softtest.fsm.java;

import java.lang.reflect.*;
import org.w3c.dom.Node;

/** FSMState FSMTransition FSMMachine FSMCondition�ĳ������ */
public abstract class FSMElement {
	/** Ԫ��������״̬�� */
	protected FSMMachine fsm = null;

	/** Ԫ�ع����Ķ��� */
	protected Method relatedmethod = null;

	/** ���Ԫ������״̬�� */
	public FSMMachine getFSMMachine() {
		return fsm;
	}
	
	/** ����Ԫ������״̬�� */
	public void setFSMMachine(FSMMachine fsm) {
		this.fsm = fsm;
	}

	/** ���Ԫ��Ԫ�ع����Ķ��� */
	public Method getRelatedMethod() {
		return relatedmethod;
	}

	/** ����Ԫ��Ԫ�ع����Ķ��� */
	public void setRelatedMethod(Method relatedmethod) {
		this.relatedmethod = relatedmethod;
	}

	/** ������ģʽ��accept���� */
	public abstract void accept(FSMVisitor visitor, Object data);

	/** ����Ԫ�ض�Ӧ��֧�ֵĽ���xml���� */
	public abstract void loadXML(Node n);

	/** ����Ԫ�ع����Ķ�����relatedclass����Ϊ�ö����������� */
	public void loadAction(Node n, Class<?> relatedclass) {
		if (n.getAttributes().getNamedItem("Action") == null) {
			return;
		}
		String strargs = null;
		if (n.getAttributes().getNamedItem("Args") != null) {
			strargs = n.getAttributes().getNamedItem("Args").getNodeValue();
		}
		Node nodeaction = n.getAttributes().getNamedItem("Action");
		Method relatedmethod = null;
		try {
			if (strargs == null) {
				relatedmethod = relatedclass.getMethod(nodeaction.getNodeValue(), (Class[]) null);
			} else {
				String[] args = strargs.split(",");
				Class[] params = new Class[args.length];
				for (int i = 0; i < args.length; i++) {
					params[i] = Class.forName(args[i]);
				}
				relatedmethod = relatedclass.getMethod(nodeaction.getNodeValue(), params);
			}
		} catch (NoSuchMethodException e) {
			if(softtest.config.java.Config.DEBUG){
				e.printStackTrace();
			}
			throw new RuntimeException("Fail to find the related method.",e);
		} catch (ClassNotFoundException e) {
			if(softtest.config.java.Config.DEBUG){
				e.printStackTrace();
			}
			throw new RuntimeException("Fail to find the related class.",e);
		}
		setRelatedMethod(relatedmethod);
	}
	
}
