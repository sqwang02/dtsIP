package softtest.fsm.java;

import java.lang.reflect.*;
import org.w3c.dom.Node;

/** FSMState FSMTransition FSMMachine FSMCondition的抽象基类 */
public abstract class FSMElement {
	/** 元素所属的状态机 */
	protected FSMMachine fsm = null;

	/** 元素关联的动作 */
	protected Method relatedmethod = null;

	/** 获得元素所属状态机 */
	public FSMMachine getFSMMachine() {
		return fsm;
	}
	
	/** 设置元素所属状态机 */
	public void setFSMMachine(FSMMachine fsm) {
		this.fsm = fsm;
	}

	/** 获得元素元素关联的动作 */
	public Method getRelatedMethod() {
		return relatedmethod;
	}

	/** 设置元素元素关联的动作 */
	public void setRelatedMethod(Method relatedmethod) {
		this.relatedmethod = relatedmethod;
	}

	/** 访问者模式的accept方法 */
	public abstract void accept(FSMVisitor visitor, Object data);

	/** 所有元素都应该支持的解析xml方法 */
	public abstract void loadXML(Node n);

	/** 解析元素关联的动作，relatedclass参数为该动作所属的类 */
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
