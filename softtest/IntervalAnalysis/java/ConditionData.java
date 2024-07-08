package softtest.IntervalAnalysis.java;

import java.util.*;
import softtest.ast.java.*;
import softtest.symboltable.java.*;
import softtest.ast.java.SimpleJavaNode;
import softtest.cfg.java.*;
import softtest.domain.java.*;
import softtest.symboltable.java.VariableNameDeclaration;

/** �����޶��� */
public class ConditionData {
	/** ��ʾ��ǰ�������ڵ� */
	private VexNode currentVex = null;
	
	/** �����޶��� */
	class ConditionDomains {
		/** ������ */
		Object may;

		/** �϶��� */
		Object must;
	}

	/** �ӱ����������޶���Ĺ�ϣ�� */
	private Hashtable<VariableNameDeclaration, ConditionDomains> domainstable = new Hashtable<VariableNameDeclaration, ConditionDomains>();

	/** Ĭ�Ϲ��췽�� */
	public ConditionData() {
		// do nothing
	}
	
	public ConditionData(VexNode v) {
		this.currentVex = v;
	}
	
	/**
	 * @return the currentVex
	 */
	public VexNode getCurrentVex() {
		return currentVex;
	}

	/**
	 * @param currentVex the currentVex to set
	 */
	public void setCurrentVex(VexNode currentVex) {
		this.currentVex = currentVex;
	}

	/** ���ù�ϣ�� */
	public void setDomainsTable(Hashtable<VariableNameDeclaration, ConditionDomains> domainstable) {
		this.domainstable = domainstable;
	}

	/** ��ù�ϣ�� */
	public Hashtable<VariableNameDeclaration, ConditionDomains> getDomainsTable() {
		return domainstable;
	}
	
	/** �ж�ָ�������Ƿ��ڵ�ǰ�����޶����� */
	public boolean isVariableContained(VariableNameDeclaration v){
		return domainstable.containsKey(v);
	}

	/** ���ñ���v�Ŀ����� */
	public void addMayDomain(VariableNameDeclaration v, Object domain) {
		ConditionDomains domains = null;
		if (domainstable.containsKey(v)) {
			domains = domainstable.get(v);
		} else {
			domains = new ConditionDomains();
			domainstable.put(v, domains);
		}
		ClassType type=DomainSet.getDomainType(v.getDomain());
		domains.may = ConvertDomain.DomainSwitch(domain,type);
	}

	/** ���ñ���v�Ŀ϶��� */
	public void addMustDomain(VariableNameDeclaration v, Object domain) {
		ConditionDomains domains = null;
		if (domainstable.containsKey(v)) {
			domains = domainstable.get(v);
		} else {
			domains = new ConditionDomains();
			domainstable.put(v, domains);
		}
		ClassType type=DomainSet.getDomainType(v.getDomain());
		domains.must = ConvertDomain.DomainSwitch(domain,type);
	}

	/** ��ñ���v�Ŀ����� */
	public Object getMayDomain(VariableNameDeclaration v) {
		ConditionDomains domains = domainstable.get(v);
		if (domains == null) {
			return null;
		}
		return domains.may;
	}

	/** ��ñ���v�Ŀ϶��� */
	public Object getMustDomain(VariableNameDeclaration v) {
		ConditionDomains domains = domainstable.get(v);
		if (domains == null) {
			return null;
		}
		return domains.must;
	}

	/** �����е�����������Ϊ��Ӧ�������޶��϶��� */
	public void addMustDomain(DomainSet ds) {
		Set entryset = ds.getTable().entrySet();
		Iterator i = entryset.iterator();
		VariableNameDeclaration v = null;
		Object d = null;
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			v = (VariableNameDeclaration) e.getKey();
			d = e.getValue();
			addMustDomain(v, d);
		}
	}

	/** �����е�����������Ϊ��Ӧ�������޶������� */
	public void addMayDomain(DomainSet ds) {
		Set entryset = ds.getTable().entrySet();
		Iterator i = entryset.iterator();
		VariableNameDeclaration v = null;
		Object d = null;
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			v = (VariableNameDeclaration) e.getKey();
			d = e.getValue();
			addMayDomain(v, d);
		}
	}

	/** �����ϣ�� */
	public void clearDomains() {
		domainstable.clear();
	}

	/** �������Ϊ��ǰ���µ������޶������� */
	public DomainSet getTrueMayDomainSet() {
		DomainSet domainset = new DomainSet();
		Set<Map.Entry<VariableNameDeclaration, ConditionDomains>> entry = domainstable.entrySet();
		Iterator<Map.Entry<VariableNameDeclaration, ConditionDomains>> i = entry.iterator();
		while (i.hasNext()) {
			Map.Entry<VariableNameDeclaration, ConditionDomains> e = i.next();
			if (e.getValue().may != null) {
				domainset.addDomain(e.getKey(), e.getValue().may);
			}
		}
		return domainset;
	}

	/** �������Ϊ��ǰ���µ������޶������� */
	public DomainSet getFalseMayDomainSet(VexNode vex) {
		DomainSet domainset = new DomainSet();
		Set<Map.Entry<VariableNameDeclaration, ConditionDomains>> entry = domainstable.entrySet();
		Iterator<Map.Entry<VariableNameDeclaration, ConditionDomains>> i = entry.iterator();
		while (i.hasNext()) {
			Map.Entry<VariableNameDeclaration, ConditionDomains> e = i.next();
			if (e.getValue().must != null) {
				Object b = e.getValue().must;
				Object a = vex.getDomainWithoutNull(e.getKey());
				switch (DomainSet.getDomainType(a)) {
				case REF:
					domainset.addDomain(e.getKey(), ReferenceDomain.subtract((ReferenceDomain) a, (ReferenceDomain) b));
					break;
				case DOUBLE:
					domainset.addDomain(e.getKey(), DoubleDomain.subtract((DoubleDomain) a, (DoubleDomain) b));
					break;
				case INT:
					domainset.addDomain(e.getKey(), IntegerDomain.subtract((IntegerDomain) a, (IntegerDomain) b));
					break;
				case BOOLEAN:
					domainset.addDomain(e.getKey(), BooleanDomain.subtract((BooleanDomain) a, (BooleanDomain) b));
					break;
				case ARRAY:
					domainset.addDomain(e.getKey(), ArrayDomain.subtract((ArrayDomain) a, (ArrayDomain) b));
					break;
				case ARBITRARY:
					break;
				}
				// domainset.addDomain(e.getKey(),e.getValue().may);
			}else{
				Object a = vex.getDomainWithoutNull(e.getKey());
				domainset.addDomain(e.getKey(), ConvertDomain.DomainIntersect(a,ConvertDomain.GetFullDomain(a)));
			}
		}
		return domainset;
	}

	/** �������Ϊ��ǰ���µ������޶��϶��� */
	public DomainSet getTrueMustDomainSet() {
		DomainSet domainset = new DomainSet();
		Set<Map.Entry<VariableNameDeclaration, ConditionDomains>> entry = domainstable.entrySet();
		Iterator<Map.Entry<VariableNameDeclaration, ConditionDomains>> i = entry.iterator();
		while (i.hasNext()) {
			Map.Entry<VariableNameDeclaration, ConditionDomains> e = i.next();
			if (e.getValue().must != null) {
				domainset.addDomain(e.getKey(), e.getValue().must);
			}
		}
		return domainset;
	}

	/** �������Ϊ��ǰ���µ������޶��϶��� */
	public DomainSet getFalseMustDomainSet(VexNode vex) {
		DomainSet domainset = new DomainSet();
		Set<Map.Entry<VariableNameDeclaration, ConditionDomains>> entry = domainstable.entrySet();
		Iterator<Map.Entry<VariableNameDeclaration, ConditionDomains>> i = entry.iterator();
		while (i.hasNext()) {
			Map.Entry<VariableNameDeclaration, ConditionDomains> e = i.next();
			if (e.getValue().may != null) {
				Object b = e.getValue().may;
				Object a =vex.getDomainWithoutNull(e.getKey());
				switch (DomainSet.getDomainType(a)) {
				case REF:
					domainset.addDomain(e.getKey(), ReferenceDomain.subtract((ReferenceDomain) a, (ReferenceDomain) b));
					break;
				case DOUBLE:
					domainset.addDomain(e.getKey(), DoubleDomain.subtract((DoubleDomain) a, (DoubleDomain) b));
					break;
				case INT:
					domainset.addDomain(e.getKey(), IntegerDomain.subtract((IntegerDomain) a, (IntegerDomain) b));
					break;
				case BOOLEAN:
					domainset.addDomain(e.getKey(), BooleanDomain.subtract((BooleanDomain) a, (BooleanDomain) b));
					break;
				case ARRAY:
					domainset.addDomain(e.getKey(), ArrayDomain.subtract((ArrayDomain) a, (ArrayDomain) b));
					break;
				case ARBITRARY:
					break;
				}
				// domainset.addDomain(e.getKey(),e.getValue().may);
			}else{
				
			}
		}
		return domainset;
	}

	/** �ж������޶��������Ƿ�ì�� */
	public boolean isMayContradict() {
		Set<Map.Entry<VariableNameDeclaration, ConditionDomains>> entryset = domainstable.entrySet();
		Iterator<Map.Entry<VariableNameDeclaration, ConditionDomains>> i = entryset.iterator();
		ConditionDomains d = null;
		while (i.hasNext()) {
			Map.Entry<VariableNameDeclaration, ConditionDomains> e = i.next();
			d = e.getValue();
			ClassType type = DomainSet.getDomainType(d.may);
			switch (type) {
			case ARRAY:
				if(((ArrayDomain)d.may).isEmpty()){
					return true;
				}
				break;
			case ARBITRARY:
				break;
			case REF:
				if (((ReferenceDomain) d.may).getValue() == ReferenceValue.EMPTY) {
					return true;
				}
				break;
			case BOOLEAN:
				if (((BooleanDomain) d.may).getValue() == BooleanValue.EMPTY) {
					return true;
				}
				break;
			case INT:
				if (((IntegerDomain) d.may).isEmpty()) {
					return true;
				}
				break;
			case DOUBLE:
				if (((DoubleDomain) d.may).isEmpty()) {
					return true;
				}
				break;
			}
		}
		return false;
	}

	/** �ж������޶��϶����Ƿ�ì�� */
	public boolean isMustContradict() {
		Set<Map.Entry<VariableNameDeclaration, ConditionDomains>> entryset = domainstable.entrySet();
		Iterator<Map.Entry<VariableNameDeclaration, ConditionDomains>> i = entryset.iterator();
		ConditionDomains d = null;
		while (i.hasNext()) {
			Map.Entry<VariableNameDeclaration, ConditionDomains> e = i.next();
			d = e.getValue();
			if(d.must==null){
				continue;
			}
			ClassType type = DomainSet.getDomainType(d.must);
			switch (type) {
			case ARRAY:
				if(((ArrayDomain)d.must).isEmpty()){
					return true;
				}
				break;
			case ARBITRARY:
				break;
			case REF:
				if (((ReferenceDomain) d.must).getValue() == ReferenceValue.EMPTY) {
					return true;
				}
				break;
			case BOOLEAN:
				if (((BooleanDomain) d.must).getValue() == BooleanValue.EMPTY) {
					return true;
				}
				break;
			case INT:
				if (((IntegerDomain) d.must).isEmpty()) {
					return true;
				}
				break;
			case DOUBLE:
				if (((DoubleDomain) d.must).isEmpty()) {
					return true;
				}
				break;
			}
		}
		return false;
	}
	
	/** ��ӡ */
	@Override
	public String toString() {
		StringBuffer b = new StringBuffer();
		Set<Map.Entry<VariableNameDeclaration, ConditionDomains>> entryset = domainstable.entrySet();
		Iterator<Map.Entry<VariableNameDeclaration, ConditionDomains>> i = entryset.iterator();
		VariableNameDeclaration v = null;
		ConditionDomains d = null;
		while (i.hasNext()) {
			Map.Entry<VariableNameDeclaration, ConditionDomains> e = i.next();
			v = e.getKey();
			d = e.getValue();
			b.append("" + v.getImage() + ":{may:" + d.may + "must:" + d.must + "}");
		}
		return b.toString();
	}
	
	private static boolean isLogicallyTrueBut(DomainSet ds,VariableNameDeclaration v,VexNode vex){
		VariableNameDeclaration v1 = null;
		Object d1 = null;
		Set entryset = ds.getTable().entrySet();
		Iterator i = entryset.iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			v1 = (VariableNameDeclaration) e.getKey();
			d1= e.getValue();
			if(v1==v){
				continue;
			}
			if(d1.equals(vex.getDomain(v1))){
				return true;
			}
		}		
		return false;
	}
	
	private static boolean isContradicBut(DomainSet ds,VariableNameDeclaration v){
		VariableNameDeclaration v1 = null;
		Object d1 = null;
		Set entryset = ds.getTable().entrySet();
		Iterator i = entryset.iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			v1 = (VariableNameDeclaration) e.getKey();
			d1=e.getValue();
			if(v1==v){
				continue;
			}
			switch (DomainSet.getDomainType(d1)) {
			case INT:
				if (((IntegerDomain) d1).isEmpty()) {
					return true;
				}
				break;
			case DOUBLE:
				if (((DoubleDomain) d1).isEmpty()) {
					return true;
				}
				break;
			case REF:
				if (((ReferenceDomain) d1).getValue() == ReferenceValue.EMPTY) {
					return true;
				}
				break;
			case BOOLEAN:
				if (((BooleanDomain) d1).getValue() == BooleanValue.EMPTY) {
					return true;
				}
				break;
			case ARRAY:
				if (((ArrayDomain) d1).isEmpty()) {
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
	
	public static ConditionData calConditionalAndExpression(ConditionData leftdata,ConditionData rightdata,VexNode vex){
		ConditionData r=new ConditionData(vex);
		DomainSet may1 = leftdata.getTrueMayDomainSet();
		DomainSet may2 = rightdata.getTrueMayDomainSet();

		DomainSet must1 = leftdata.getTrueMustDomainSet();
		DomainSet must2 = rightdata.getTrueMustDomainSet();
		
		DomainSet may=new DomainSet(),must=new DomainSet();
		
		VariableNameDeclaration v1 = null, v2 = null;
		Object d1 = null, d2 = null;
		Set entryset = may1.getTable().entrySet();
		Iterator i = entryset.iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			v1 = (VariableNameDeclaration) e.getKey();
			d1 = e.getValue();
			// ��domainset�в���
			d2 = may2.getDomain(v1);
			if (d2 == null) {
				if(!isContradicBut(may2,v1)){
					Object d=vex.getDomainWithoutNull(v1);
					may.addDomain(v1,ConvertDomain.DomainIntersect(d,d1));
				}else{
					//always FALSE, add EMPTY
					may.addDomain(v1, ConvertDomain.GetEmptyDomain(d1));
				}
			} else {
				// ȡintersect
				may.addDomain(v1, ConvertDomain.DomainIntersect(d1, d2));
			}
		}

		// ��b����Щaû�г��ֵĲ��뵽table��
		entryset = may2.getTable().entrySet();
		i = entryset.iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			v2 = (VariableNameDeclaration) e.getKey();
			d2 = e.getValue();
			// ��domainset�в���
			d1 = may1.getDomain(v2);
			if (d1 == null) {
				if(!isContradicBut(may1,v2)){
					Object d=vex.getDomainWithoutNull(v2);
					may.addDomain(v2,ConvertDomain.DomainIntersect(d,d2));
				}else{
					//always FALSE, add EMPTY
					may.addDomain(v2, ConvertDomain.GetEmptyDomain(d2));
				}				
			}
		}		
		
		r.addMayDomain(may);
		//����must
		entryset = must1.getTable().entrySet();
		i = entryset.iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			v1 = (VariableNameDeclaration) e.getKey();
			d1 = e.getValue();
			// ��domainset�в���
			d2 = must2.getDomain(v1);
			if (d2 == null) {
				if(isLogicallyTrueBut(must2,v1,vex)){
					//���棬ȡ��ǰȡ���������must
					must.addDomain(v1, d1);
				}else{
					if(!ConvertDomain.DomainIsUnknown(d1)){
						if(ConvertDomain.DomainIsUnknown(may1.getDomain(v1))){
							must.addDomain(v1,ConvertDomain.GetUnknownDomain(d1));
						}else{
							must.addDomain(v1, ConvertDomain.GetEmptyDomain(d1));
						}
					}else{
						must.addDomain(v1,d1);
					}
				}
			} else {
				// ȡintersect
				must.addDomain(v1,ConvertDomain.DomainIntersect(d1, d2));
			}
		}

		// ��b����Щaû�г��ֵĲ��뵽table��
		entryset = must2.getTable().entrySet();
		i = entryset.iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			v2 = (VariableNameDeclaration) e.getKey();
			d2 = e.getValue();
			// ��domainset�в���
			d1 = must1.getDomain(v2);
			if (d1 == null) {
				if(isLogicallyTrueBut(must1,v2,vex)){
					//���棬ȡ���������must
					must.addDomain(v2, d2);
				}else{
					if(!ConvertDomain.DomainIsUnknown(d2)){
						if(ConvertDomain.DomainIsUnknown(may2.getDomain(v2))){
							must.addDomain(v2,ConvertDomain.GetUnknownDomain(d2));
						}else{
							must.addDomain(v2, ConvertDomain.GetEmptyDomain(d2));
						}
					}else{
						must.addDomain(v2,d2);
					}
				}
			}
		}		
		
		r.addMustDomain(must);			
		return r;
	}
	
	public static ConditionData calLogicConditionalOrExpression(ConditionData leftdata,ConditionData rightdata,VexNode vex){
		ConditionData r=new ConditionData(vex);
		DomainSet may1 = leftdata.getTrueMayDomainSet();
		DomainSet may2 = rightdata.getTrueMayDomainSet();

		DomainSet must1 = leftdata.getTrueMustDomainSet();
		DomainSet must2 = rightdata.getTrueMustDomainSet();
		
		DomainSet may=new DomainSet(),must=new DomainSet();
		
		
		VariableNameDeclaration v1 = null, v2 = null;
		Object d1 = null, d2 = null;
		Set entryset = may1.getTable().entrySet();
		Iterator i = entryset.iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			v1 = (VariableNameDeclaration) e.getKey();
			d1 = e.getValue();
			// ��domainset�в���
			d2 = may2.getDomain(v1);
			if (d2 == null) {
				if(!isContradicBut(may2,v1)){
					//��ì�ܣ�ȡ��ǰ��ȫ���Ĳ�
					Object d;
					if(!ConvertDomain.DomainIsUnknown(d1)){
						if(ConvertDomain.DomainIsUnknown(must1.getDomain(v1))){
							d=ConvertDomain.GetUnknownDomain(d1);
						}else{
							d=ConvertDomain.DomainIntersect(vex.getDomainWithoutNull(v1),ConvertDomain.GetFullDomain(d1));
						}
						//d=ConvertDomain.DomainUnion(vex.getDomainWithoutNull(v1),
						//	ConvertDomain.GetFullDomain(d1));
					}else{
						d=vex.getDomainWithoutNull(v1);
					}
					may.addDomain(v1,d );
				}else{
					//ì�ܣ�ȡ���������may
					may.addDomain(v1, d1);
				}
			} else {
				// ȡunion��
				may.addDomain(v1, ConvertDomain.DomainUnion(d1, d2));
			}
		}

		// ��b����Щaû�г��ֵĲ��뵽table��
		entryset = may2.getTable().entrySet();
		i = entryset.iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			v2 = (VariableNameDeclaration) e.getKey();
			d2 = e.getValue();
			// ��domainset�в���
			d1 = may1.getDomain(v2);
			if (d1 == null) {
				if(!isContradicBut(may1,v2)){
					//��ì�ܣ�ȡ��ǰ��ȫ���Ĳ�
					Object d;
					if(!ConvertDomain.DomainIsUnknown(d2)){
						if(ConvertDomain.DomainIsUnknown(must2.getDomain(v2))){
							d=ConvertDomain.GetUnknownDomain(d2);
						}else{
							d=ConvertDomain.DomainIntersect(vex.getDomainWithoutNull(v2),ConvertDomain.GetFullDomain(d2));
						}
						//d=ConvertDomain.DomainUnion(vex.getDomainWithoutNull(v2),
						//	ConvertDomain.GetFullDomain(d2));
					}else{
						d=vex.getDomainWithoutNull(v2);
					}
					may.addDomain(v2, d);
				}else{
					//ì�ܣ�ȡ���������may
					may.addDomain(v2, d2);
				}
			}
		}		
		
		r.addMayDomain(may);
		//����must
		entryset = must1.getTable().entrySet();
		i = entryset.iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			v1 = (VariableNameDeclaration) e.getKey();
			d1 = e.getValue();
			// ��domainset�в���
			d2 = must2.getDomain(v1);
			if (d2 == null) {
				if (isLogicallyTrueBut(must2, v1, vex)) {
					// ���棬ȡ��ǰȫ��
					Object d;
					if(!ConvertDomain.DomainIsUnknown(d1)){
						d=ConvertDomain.DomainIntersect(vex.getDomainWithoutNull(v1),ConvertDomain.GetFullDomain(d1));						
						//d=ConvertDomain.DomainUnion(vex.getDomainWithoutNull(v1),
							//ConvertDomain.GetFullDomain(d1));
					}else{
						d=vex.getDomainWithoutNull(v1);
					}
					must.addDomain(v1, d);
				}else{
					//�����棬ȡ���������must
					must.addDomain(v1, d1);
				}
			} else {
				// ȡunion���������⴦��
				must.addDomain(v1, ConvertDomain.DomainUnion(d1, d2));
			}
		}

		// ��b����Щaû�г��ֵĲ��뵽table��
		entryset = must2.getTable().entrySet();
		i = entryset.iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			v2 = (VariableNameDeclaration) e.getKey();
			d2 = e.getValue();
			// ��domainset�в���
			d1 = must1.getDomain(v2);
			if (d1 == null) {
				if(isLogicallyTrueBut(must1,v2,vex)){
					//���棬ȡ��ǰȫ��	
					Object d;
					if(!ConvertDomain.DomainIsUnknown(d2)){
						d=ConvertDomain.DomainIntersect(vex.getDomainWithoutNull(v2),ConvertDomain.GetFullDomain(d2));						
						//d=ConvertDomain.DomainUnion(vex.getDomainWithoutNull(v2),
						//	ConvertDomain.GetFullDomain(d2));
					}else{
						d=vex.getDomainWithoutNull(v2);
					}
					must.addDomain(v2,d);
				}else{
					//�����棬ȡ���������must
					must.addDomain(v2, d2);
				}
			}
		}		
		
		r.addMustDomain(must);		
		return r;
	}
	
	public static ConditionData calLoopCondtion(ConditionData data,VexNode vex,ConditionDomainVisitor convisitor,SimpleJavaNode treenode){
		DomainSet old=vex.getDomainSet();
		treenode.jjtAccept(convisitor, data);

		ConditionData data1=new ConditionData(vex);
		vex.setDomainSet(null);
		treenode.jjtAccept(convisitor, data1);
		vex.setDomainSet(old);
		//������ȷ��ѭ������
		
		Set entryset = data1.domainstable.entrySet();
		Iterator i = entryset.iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			VariableNameDeclaration v = (VariableNameDeclaration) e.getKey();
			ConditionDomains d = (ConditionDomains)e.getValue();
			data.addMustDomain(v, d.must);
		}
		return data;
	}
}
