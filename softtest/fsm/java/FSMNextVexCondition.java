package softtest.fsm.java;

import org.w3c.dom.Node;

import softtest.cfg.java.*;

public class FSMNextVexCondition extends FSMCondition {
	@Override
	public boolean evaluate(FSMMachineInstance fsm, FSMStateInstance state, VexNode vex) {
		boolean b = false;
		if (state.getVexNode() == vex) {
			b = false;
		} else {
			b = true;
		}
		if (b) {
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
					// e.printStackTrace();
					throw new RuntimeException("action error",e);
				}
			}
		}

		return b;
	}

	/** ½âÎöxml */
	@Override
	public void loadXML(Node n) {
		if (fsm != null && fsm.getRelatedClass() != null) {
			loadAction(n, fsm.getRelatedClass());
		}
	}
}
