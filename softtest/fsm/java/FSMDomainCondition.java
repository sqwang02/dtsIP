package softtest.fsm.java;

import org.w3c.dom.Node;

import softtest.cfg.java.*;
import softtest.IntervalAnalysis.java.*;
import softtest.domain.java.*;
import softtest.symboltable.java.VariableNameDeclaration;
import softtest.symboltable.java.*;

/** ������ */
public class FSMDomainCondition extends FSMCondition {
	/** ������ */
	private ClassType type = ClassType.INT;

	/** ��ֵ */
	private Object domain = null;

	/** ��Ȼ��ǲ��ȱ�� */
	private boolean isequal = true;

	/** ���������м��㣬�ж����Ƿ����� */
	@Override
	public boolean evaluate(FSMMachineInstance fsmin, FSMStateInstance state, VexNode vex) {
		boolean b = false;
		if (!fsmin.getFSMMachine().isPathSensitive() || !fsmin.getFSMMachine().isVariableRelated()) {
			throw new RuntimeException("Domain condition can not apply to this kind of fsm.");
		}
		VariableNameDeclaration v = fsmin.getRelatedVariable();
		Object d1, d2;
		d1 = ConvertDomain.DomainSwitch(domain, type);
		d2 = ConvertDomain.DomainSwitch(vex.getDomainWithoutNull(v), type);

		if (d1.equals(d2)) {
			b = true;
		}

		if (!isequal) {
			b = !b;
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

	/** ���ַ���ת��Ϊ�ڲ���ֵ����ǰֻ֧���������� */
	public void parseString(String strtype, String strvalue) {
		type = ClassType.valueOf(strtype);
		if (type == ClassType.REF) {
			domain = ReferenceDomain.valueOf(strvalue);
		}
		
		//ADDED by yang
		if (type == ClassType.ARRAY) {
			domain = ReferenceDomain.valueOf(strvalue);
		}
		//END YANG
		
	}

	/** ��ӡ */
	@Override
	public String toString() {
		StringBuffer b = new StringBuffer();
		if (!isequal) {
			b.append("not ");
		}
		b.append(domain);
		return b.toString();
	}

	/** ����xml */
	@Override
	public void loadXML(Node n) {
		Node type = n.getAttributes().getNamedItem("Type");
		Node value = n.getAttributes().getNamedItem("Value");
		if (type == null || value == null) {
			throw new RuntimeException("Domain condition must have a type and a value.");
		}
		Node equal = n.getAttributes().getNamedItem("Equal");
		if (equal != null) {
			if (equal.getNodeValue().equals("false")) {
				isequal = false;
			}
		}
		parseString(type.getNodeValue(), value.getNodeValue());
		if (fsm != null && fsm.getRelatedClass() != null) {
			loadAction(n, fsm.getRelatedClass());
		}
	}
}
