
package softtest.fsm.java;

import org.w3c.dom.Node;
import softtest.cfg.java.*;

/** ��ԶΪ������� */
public class AlwaysTrueCondition extends FSMCondition {

	/** ���������м��㣬�ж����Ƿ����� */
	@Override
	public boolean evaluate(FSMMachineInstance fsm,FSMStateInstance state, VexNode vex) {
		boolean b = true;
		if (relatedmethod == null) {
			b = true;
		} else {
			Object[] args = new Object[2];
			args[0] = vex;
			args[1] = fsm;
			try {
				Boolean r = (Boolean) relatedmethod.invoke(null, args);
				b = r;
			} catch (Exception e) {
				if(softtest.config.java.Config.DEBUG){
					e.printStackTrace();
				}
				throw new RuntimeException("action error",e);
			}
		}
		return b;
	}

	/** ����xml */
	@Override
	public void loadXML(Node n) {
		if (fsm != null && fsm.getRelatedClass() != null) {
			loadAction(n, fsm.getRelatedClass());
		}
	}
}

