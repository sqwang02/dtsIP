
package softtest.fsm.java;

import org.w3c.dom.Node;
import softtest.cfg.java.*;

/** 永远为真的条件 */
public class AlwaysTrueCondition extends FSMCondition {

	/** 对条件进行计算，判断其是否满足 */
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

	/** 解析xml */
	@Override
	public void loadXML(Node n) {
		if (fsm != null && fsm.getRelatedClass() != null) {
			loadAction(n, fsm.getRelatedClass());
		}
	}
}

