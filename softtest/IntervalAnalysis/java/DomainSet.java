package softtest.IntervalAnalysis.java;

import java.util.*;

import softtest.domain.java.*;
import softtest.symboltable.java.VariableNameDeclaration;
import softtest.ast.java.*;
import softtest.cfg.java.*;
import softtest.symboltable.java.*;

/** �� */
public class DomainSet {
	/** ��һ���ں� */
	private boolean fistmerge = true;

	/** �ӱ�������Ĺ�ϣ�� */
	private Hashtable<VariableNameDeclaration, Object> domaintable = new Hashtable<VariableNameDeclaration, Object>();

	/** ȱʡ���캯�� */
	public DomainSet() {

	}

	/** �������캯�� ,��ǰ��û�н�����㿽��*/
	public DomainSet(DomainSet ds) {
		VariableNameDeclaration v = null;
		Object domain = null;
		Set entryset = ds.domaintable.entrySet();
		Iterator i = entryset.iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			v = (VariableNameDeclaration) e.getKey();
			domain = e.getValue();
			addDomain(v, domain);
		}
	}

	/** ���ñ���v���� */
	public Object addDomain(VariableNameDeclaration v, Object domain) {
		return domaintable.put(v, domain);
	}

	/** ��ñ���v���� */
	public Object getDomain(VariableNameDeclaration v) {
		return domaintable.get(v);
	}

	/** �����ϣ�� */
	public void clearDomainSet() {
		fistmerge = true;
		domaintable.clear();
	}

	/** ���ù�ϣ�� */
	public void setTable(Hashtable<VariableNameDeclaration, Object> domaintable) {
		this.domaintable = domaintable;
	}

	/** ��ù�ϣ�� */
	public Hashtable<VariableNameDeclaration, Object> getTable() {
		return domaintable;
	}

	/** �жϹ�ϣ���Ƿ�Ϊ�� */
	public boolean isEmpty() {
		return domaintable.isEmpty();
	}

	/** ��ӡ */
	@Override
	public String toString() {
		StringBuffer b = new StringBuffer();
		Set entryset = domaintable.entrySet();
		Iterator i = entryset.iterator();
		VariableNameDeclaration v = null;
		Object d = null;
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			v = (VariableNameDeclaration) e.getKey();
			d = e.getValue();
			b.append("" + v.getImage() + ":" + d);
		}
		if (entryset.size() == 0) {
			b.append("Default");
		}
		return b.toString();
	}
	
	/** ��domainset�ںϵ�this���У��ں��߼���ͨ�����ڶ�����������ʱ */
	public void mergeDomainSet(DomainSet domainset,VexNode vex) {
		Hashtable<VariableNameDeclaration, Object> newtable = new Hashtable<VariableNameDeclaration, Object>();
		VariableNameDeclaration v1 = null, v2 = null;
		if (domainset == null) {
			domainset = new DomainSet();
		}
		Object d1 = null, d2 = null;
		Set entryset = domaintable.entrySet();
		Iterator i = entryset.iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			v1 = (VariableNameDeclaration) e.getKey();
			d1 = e.getValue();
			// ��domainset�в���
			d2 = domainset.getDomain(v1);
			if (d2 == null) {
				//TODO���Ƿ���Կ���ȫ��ֱ�Ӽ��룿
				if (fistmerge) {
					newtable.put(v1, d1);
				}else{
					//d2=vex.getDomainWithoutNull(v1);
					d2=v1.getDomain();
					// �ϲ���ͨ��ȡunion
					switch (getDomainType(d1)) {
					case INT:
						newtable.put(v1, IntegerDomain.union((IntegerDomain) d1, (IntegerDomain) d2));
						break;
					case DOUBLE:
						newtable.put(v1, DoubleDomain.union((DoubleDomain) d1, (DoubleDomain) d2));
						break;
					case REF:
						newtable.put(v1, ReferenceDomain.union((ReferenceDomain) d1, (ReferenceDomain) d2));
						break;
					case BOOLEAN:
						newtable.put(v1, BooleanDomain.union((BooleanDomain) d1, (BooleanDomain) d2));
						break;
					case ARRAY:
						newtable.put(v1, ArrayDomain.union((ArrayDomain) d1, (ArrayDomain) d2));
						break;
					case ARBITRARY:
						newtable.put(v1, d1);
						break;
					default:
						throw new RuntimeException("do not know the type of Variable");
					}
				}
			} else {
				// �ϲ���ͨ��ȡunion
				switch (getDomainType(d1)) {
				case INT:
					newtable.put(v1, IntegerDomain.union((IntegerDomain) d1, (IntegerDomain) d2));
					break;
				case DOUBLE:
					newtable.put(v1, DoubleDomain.union((DoubleDomain) d1, (DoubleDomain) d2));
					break;
				case REF:
					newtable.put(v1, ReferenceDomain.union((ReferenceDomain) d1, (ReferenceDomain) d2));
					break;
				case BOOLEAN:
					newtable.put(v1, BooleanDomain.union((BooleanDomain) d1, (BooleanDomain) d2));
					break;
				case ARRAY:
					newtable.put(v1, ArrayDomain.union((ArrayDomain) d1, (ArrayDomain) d2));
					break;
				case ARBITRARY:
					newtable.put(v1, d1);
					break;
				default:
					throw new RuntimeException("do not know the type of Variable");
				}
			}
		}

		// ��domainset����Щthisû�г��ֵĲ��뵽table��
		entryset = domainset.domaintable.entrySet();
		i = entryset.iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			v2 = (VariableNameDeclaration) e.getKey();
			d2 = e.getValue();
			// ��domainset�в���
			d1 = getDomain(v2);
			if (d1 == null) {
				//TODO���Ƿ���Կ���ȫ��ֱ�Ӽ��룿
				if (fistmerge) {
					newtable.put(v2, d2);
				}else{
					//d1=vex.getDomainWithoutNull(v2);
					d1=v2.getDomain();
					// �ϲ���ͨ��ȡunion
					switch (getDomainType(d1)) {
					case INT:
						newtable.put(v2, IntegerDomain.union((IntegerDomain) d1, (IntegerDomain) d2));
						break;
					case DOUBLE:
						newtable.put(v2, DoubleDomain.union((DoubleDomain) d1, (DoubleDomain) d2));
						break;
					case REF:
						newtable.put(v2, ReferenceDomain.union((ReferenceDomain) d1, (ReferenceDomain) d2));
						break;
					case BOOLEAN:
						newtable.put(v2, BooleanDomain.union((BooleanDomain) d1, (BooleanDomain) d2));
						break;
					case ARRAY:
						newtable.put(v2, ArrayDomain.union((ArrayDomain) d1, (ArrayDomain) d2));
						break;
					case ARBITRARY:
						newtable.put(v2, d1);
						break;
					default:
						throw new RuntimeException("do not know the type of Variable");
					}
				}
			}
		}
		domaintable = newtable;
		fistmerge = false;
	}

	/** �����domain�������� */
	public static ClassType getDomainType(Object domain) {
		ClassType r = ClassType.INT;
		if (domain instanceof IntegerDomain) {
			r = ClassType.INT;
		} else if (domain instanceof DoubleDomain) {
			r = ClassType.DOUBLE;
		} else if (domain instanceof BooleanDomain) {
			r = ClassType.BOOLEAN;
		} else if (domain instanceof ArrayDomain) {
			r = ClassType.ARRAY;
		} else if (domain instanceof ReferenceDomain) {
			r = ClassType.REF;
		} else if (domain instanceof ArbitraryDomain) {
			r = ClassType.ARBITRARY;
		} else {
			throw new RuntimeException("do not know the type of Variable");
		}
		return r;
	}

	/** �����a����Щ��b�в����ڵ��Ӽ� */
	public static DomainSet getExtraDomainset(DomainSet a, DomainSet b) {
		DomainSet r = new DomainSet();
		if (a == null) {
			a = new DomainSet();
		}
		if (b == null) {
			b = new DomainSet();
		}
		Set entryset = a.domaintable.entrySet();
		Iterator i = entryset.iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			VariableNameDeclaration v = (VariableNameDeclaration) e.getKey();
			if (!b.domaintable.containsKey(v)) {
				r.addDomain(v, e.getValue());
			}
		}
		return r;
	}

	/** ���󲢣�/����Щa��b���еı������������ڽ�����У��߼�����Ϊ���޹��˻���ȡĬ�ϵ�������� */
	public static DomainSet union(DomainSet a, DomainSet b) {
		DomainSet r = new DomainSet();
		if (a == null) {
			a = new DomainSet();
		}
		if (b == null) {
			b = new DomainSet();
		}
		VariableNameDeclaration v1 = null, v2 = null;
		Object d1 = null, d2 = null;
		Set entryset = a.domaintable.entrySet();
		Iterator i = entryset.iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			v1 = (VariableNameDeclaration) e.getKey();
			d1 = e.getValue();
			// ��domainset�в���
			d2 = b.getDomain(v1);
			if (d2 == null) {
			} else {
				// ȡunion���������⴦��
				switch (getDomainType(d1)) {
				case INT:
					r.addDomain(v1, IntegerDomain.union((IntegerDomain) d1, (IntegerDomain) d2));
					break;
				case DOUBLE:
					r.addDomain(v1, DoubleDomain.union((DoubleDomain) d1, (DoubleDomain) d2));
					break;
				case REF:
					r.addDomain(v1, ReferenceDomain.union((ReferenceDomain) d1, (ReferenceDomain) d2));
					break;
				case BOOLEAN:
					r.addDomain(v1, BooleanDomain.union((BooleanDomain) d1, (BooleanDomain) d2));
					break;
				case ARRAY:
					r.addDomain(v1, ArrayDomain.union((ArrayDomain) d1, (ArrayDomain) d2));
					break;
				case ARBITRARY:
					r.addDomain(v1, d1);
					break;
				default:
					throw new RuntimeException("do not know the type of Variable");
				}
			}
		}

		// ��b����Щaû�г��ֵĲ��뵽table��
		entryset = b.domaintable.entrySet();
		i = entryset.iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			v2 = (VariableNameDeclaration) e.getKey();
			d2 = e.getValue();
			// ��domainset�в���
			d1 = a.getDomain(v2);
			if (d1 == null) {
			}
		}
		return r;
	}

	/** �����߼��� */
	public static DomainSet join(DomainSet a, DomainSet b) {
		DomainSet r = new DomainSet();
		if (a == null) {
			a = new DomainSet();
		}
		if (b == null) {
			b = new DomainSet();
		}
		VariableNameDeclaration v1 = null, v2 = null;
		Object d1 = null, d2 = null;
		Set entryset = a.domaintable.entrySet();
		Iterator i = entryset.iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			v1 = (VariableNameDeclaration) e.getKey();
			d1 = e.getValue();
			// ��domainset�в���
			d2 = b.getDomain(v1);
			if (d2 == null) {
				r.addDomain(v1, d1);
			} else {
				// ȡunion���������⴦��
				switch (getDomainType(d1)) {
				case INT:
					r.addDomain(v1, IntegerDomain.union((IntegerDomain) d1, (IntegerDomain) d2));
					break;
				case DOUBLE:
					r.addDomain(v1, DoubleDomain.union((DoubleDomain) d1, (DoubleDomain) d2));
					break;
				case REF:
					r.addDomain(v1, ReferenceDomain.union((ReferenceDomain) d1, (ReferenceDomain) d2));
					break;
				case BOOLEAN:
					r.addDomain(v1, BooleanDomain.union((BooleanDomain) d1, (BooleanDomain) d2));
					break;
				case ARRAY:
					r.addDomain(v1, ArrayDomain.union((ArrayDomain) d1, (ArrayDomain) d2));
					break;
				case ARBITRARY:
					r.addDomain(v1, d1);
					break;
				default:
					throw new RuntimeException("do not know the type of Variable");
				}
			}
		}

		// ��b����Щaû�г��ֵĲ��뵽table��
		entryset = b.domaintable.entrySet();
		i = entryset.iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			v2 = (VariableNameDeclaration) e.getKey();
			d2 = e.getValue();
			// ��domainset�в���
			d1 = a.getDomain(v2);
			if (d1 == null) {
				r.addDomain(v2, d2);
			}
		}
		return r;
	}

	/** ���� */
	public static DomainSet intersect(DomainSet a, DomainSet b) {
		DomainSet r = new DomainSet();
		if (a == null) {
			a = new DomainSet();
		}
		if (b == null) {
			b = new DomainSet();
		}
		VariableNameDeclaration v1 = null, v2 = null;
		Object d1 = null, d2 = null;
		Set entryset = a.domaintable.entrySet();
		Iterator i = entryset.iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			v1 = (VariableNameDeclaration) e.getKey();
			d1 = e.getValue();
			// ��domainset�в���
			d2 = b.getDomain(v1);
			if (d2 == null) {
				r.addDomain(v1, d1);
			} else {
				// ȡintersect���������⴦��
				switch (getDomainType(d1)) {
				case INT:
					r.addDomain(v1, IntegerDomain.intersect((IntegerDomain) d1, (IntegerDomain) d2));
					break;
				case DOUBLE:
					r.addDomain(v1, DoubleDomain.intersect((DoubleDomain) d1, (DoubleDomain) d2));
					break;
				case REF:
					r.addDomain(v1, ReferenceDomain.intersect((ReferenceDomain) d1, (ReferenceDomain) d2));
					break;
				case BOOLEAN:
					r.addDomain(v1, BooleanDomain.intersect((BooleanDomain) d1, (BooleanDomain) d2));
					break;
				case ARRAY:
					r.addDomain(v1, ArrayDomain.intersect((ArrayDomain) d1, (ArrayDomain) d2));
					break;
				case ARBITRARY:
					r.addDomain(v1, d1);
					break;
				default:
					throw new RuntimeException("do not know the type of Variable");
				}
			}
		}

		// ��b����Щaû�г��ֵĲ��뵽table��
		entryset = b.domaintable.entrySet();
		i = entryset.iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			v2 = (VariableNameDeclaration) e.getKey();
			d2 = e.getValue();
			// ��domainset�в���
			d1 = a.getDomain(v2);
			if (d1 == null) {
				r.addDomain(v2, d2);
			}
		}
		return r;
	}

	/** �����߼��� */
	public static DomainSet common(DomainSet a, DomainSet b) {
		DomainSet r = new DomainSet();
		if (a == null) {
			a = new DomainSet();
		}
		if (b == null) {
			b = new DomainSet();
		}
		VariableNameDeclaration v1 = null, v2 = null;
		Object d1 = null, d2 = null;
		Set entryset = a.domaintable.entrySet();
		Iterator i = entryset.iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			v1 = (VariableNameDeclaration) e.getKey();
			d1 = e.getValue();
			// ��domainset�в���
			d2 = b.getDomain(v1);
			if (d2 == null) {
				if(getDomainType(d1)==ClassType.ARRAY){
					r.addDomain(v1, d1);
				}
			} else {
				// ȡintersect���������⴦��
				switch (getDomainType(d1)) {
				case INT:
					r.addDomain(v1, IntegerDomain.intersect((IntegerDomain) d1, (IntegerDomain) d2));
					break;
				case DOUBLE:
					r.addDomain(v1, DoubleDomain.intersect((DoubleDomain) d1, (DoubleDomain) d2));
					break;
				case REF:
					r.addDomain(v1, ReferenceDomain.intersect((ReferenceDomain) d1, (ReferenceDomain) d2));
					break;
				case BOOLEAN:
					r.addDomain(v1, BooleanDomain.intersect((BooleanDomain) d1, (BooleanDomain) d2));
					break;
				case ARRAY:
					r.addDomain(v1, ArrayDomain.intersect((ArrayDomain) d1, (ArrayDomain) d2));
					break;
				case ARBITRARY:
					r.addDomain(v1, d1);
					break;
				default:
					throw new RuntimeException("do not know the type of Variable");
				}
			}
		}

		// ��b����Щaû�г��ֵĲ��뵽table��
		entryset = b.domaintable.entrySet();
		i = entryset.iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			v2 = (VariableNameDeclaration) e.getKey();
			d2 = e.getValue();
			// ��domainset�в���
			d1 = a.getDomain(v2);
			if (d1 == null) {
				if(getDomainType(d2)==ClassType.ARRAY){
					r.addDomain(v2, d2);
				}
			}
		}
		return r;
	}

	/** �ж����Ƿ����ì�ܣ�������κ�һ����������Ϊ������Ϊ��ì�� */
	public boolean isContradict() {
		Object d = null;
		Set entryset = domaintable.entrySet();
		Iterator i = entryset.iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			d = e.getValue();
			switch (getDomainType(d)) {
			case INT:
				if (((IntegerDomain) d).isEmpty()) {
					return true;
				}
				break;
			case DOUBLE:
				if (((DoubleDomain) d).isEmpty()) {
					return true;
				}
				break;
			case REF:
				if (((ReferenceDomain) d).getValue() == ReferenceValue.EMPTY) {
					return true;
				}
				break;
			case BOOLEAN:
				if (((BooleanDomain) d).getValue() == BooleanValue.EMPTY) {
					return true;
				}
				break;
			case ARRAY:
				if (((ArrayDomain) d).isEmpty()) {
					return true;
				}
				break;
			case ARBITRARY:
				break;
			default:
				throw new RuntimeException("do not know the type");
			}
		}
		return false;
	}
	
	/** ������ʽ��ֵ,�õ��ǿ������ڵ�Out���м��� */
	public static Object calculateOutExprValue(SimpleNode expr,boolean sideefect) {
		Object domain = new ArbitraryDomain();
		try {
			ExpressionDomainVisitor exprvisitor = new ExpressionDomainVisitor();
			DomainData exprdata = new DomainData();
			exprdata.sideeffect = sideefect;
			((SimpleJavaNode) expr).jjtAccept(exprvisitor, exprdata);
			domain = exprdata.domain;
		} catch (Exception e) {

		}
		return domain;
	}
	
	/** ������ʽ��ֵ,�õ��ǿ������ڵ�In���м��� */
	public static Object calculateInExprValue(SimpleNode expr,boolean sideefect) {
		Object domain = new ArbitraryDomain();
		try {
			VexNode vex=expr.getCurrentVexNode();
			DomainSet old=null;
			if(vex!=null){
				old=vex.getDomainSet();
				vex.setDomainSet(vex.getLastDomainSet());
			}
			domain=calculateOutExprValue(expr,sideefect);
			if(vex!=null){
				vex.setDomainSet(old);
			}
		} catch (Exception e) {

		}
		return domain;
	}
	
	/** ɾ����û�б�Ҫ��ŵ��� */
	public void removeRedundantDomain(){
		ArrayList<VariableNameDeclaration> todelete=new ArrayList<VariableNameDeclaration>();
		Set entryset = domaintable.entrySet();
		Iterator i = entryset.iterator();Object d1 = null;
		VariableNameDeclaration v1 = null;
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			v1 = (VariableNameDeclaration) e.getKey();
			d1 = e.getValue();
			if(d1.equals(v1.getDomain())){
				todelete.add(v1);
			}
		}
		for(VariableNameDeclaration v:todelete){
			domaintable.remove(v);
		}
	}
	
}
