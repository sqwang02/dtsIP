package softtest.IntervalAnalysis.java;

import java.util.*;

import softtest.domain.java.*;
import softtest.symboltable.java.VariableNameDeclaration;
import softtest.ast.java.*;
import softtest.cfg.java.*;
import softtest.symboltable.java.*;

/** 域集 */
public class DomainSet {
	/** 第一次融合 */
	private boolean fistmerge = true;

	/** 从变量到域的哈希表 */
	private Hashtable<VariableNameDeclaration, Object> domaintable = new Hashtable<VariableNameDeclaration, Object>();

	/** 缺省构造函数 */
	public DomainSet() {

	}

	/** 拷贝构造函数 ,当前并没有进行深层拷贝*/
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

	/** 设置变量v的域 */
	public Object addDomain(VariableNameDeclaration v, Object domain) {
		return domaintable.put(v, domain);
	}

	/** 获得变量v的域 */
	public Object getDomain(VariableNameDeclaration v) {
		return domaintable.get(v);
	}

	/** 清除哈希表 */
	public void clearDomainSet() {
		fistmerge = true;
		domaintable.clear();
	}

	/** 设置哈希表 */
	public void setTable(Hashtable<VariableNameDeclaration, Object> domaintable) {
		this.domaintable = domaintable;
	}

	/** 获得哈希表 */
	public Hashtable<VariableNameDeclaration, Object> getTable() {
		return domaintable;
	}

	/** 判断哈希表是否为空 */
	public boolean isEmpty() {
		return domaintable.isEmpty();
	}

	/** 打印 */
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
	
	/** 将domainset融合到this域集中，融合逻辑上通常用于多个控制流会合时 */
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
			// 在domainset中查找
			d2 = domainset.getDomain(v1);
			if (d2 == null) {
				//TODO：是否可以考虑全部直接加入？
				if (fistmerge) {
					newtable.put(v1, d1);
				}else{
					//d2=vex.getDomainWithoutNull(v1);
					d2=v1.getDomain();
					// 合并，通常取union
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
				// 合并，通常取union
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

		// 将domainset的那些this没有出现的插入到table中
		entryset = domainset.domaintable.entrySet();
		i = entryset.iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			v2 = (VariableNameDeclaration) e.getKey();
			d2 = e.getValue();
			// 在domainset中查找
			d1 = getDomain(v2);
			if (d1 == null) {
				//TODO：是否可以考虑全部直接加入？
				if (fistmerge) {
					newtable.put(v2, d2);
				}else{
					//d1=vex.getDomainWithoutNull(v2);
					d1=v2.getDomain();
					// 合并，通常取union
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

	/** 获得域domain的域类型 */
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

	/** 获得域集a中那些在b中不存在的子集 */
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

	/** 域集求并，/对那些a或b独有的变量将不出现在结果域集中，逻辑上认为是无关了或者取默认的最大域了 */
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
			// 在domainset中查找
			d2 = b.getDomain(v1);
			if (d2 == null) {
			} else {
				// 取union，数组特殊处理
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

		// 将b的那些a没有出现的插入到table中
		entryset = b.domaintable.entrySet();
		i = entryset.iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			v2 = (VariableNameDeclaration) e.getKey();
			d2 = e.getValue();
			// 在domainset中查找
			d1 = a.getDomain(v2);
			if (d1 == null) {
			}
		}
		return r;
	}

	/** 域集求逻辑并 */
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
			// 在domainset中查找
			d2 = b.getDomain(v1);
			if (d2 == null) {
				r.addDomain(v1, d1);
			} else {
				// 取union，数组特殊处理
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

		// 将b的那些a没有出现的插入到table中
		entryset = b.domaintable.entrySet();
		i = entryset.iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			v2 = (VariableNameDeclaration) e.getKey();
			d2 = e.getValue();
			// 在domainset中查找
			d1 = a.getDomain(v2);
			if (d1 == null) {
				r.addDomain(v2, d2);
			}
		}
		return r;
	}

	/** 域集求交 */
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
			// 在domainset中查找
			d2 = b.getDomain(v1);
			if (d2 == null) {
				r.addDomain(v1, d1);
			} else {
				// 取intersect，数组特殊处理
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

		// 将b的那些a没有出现的插入到table中
		entryset = b.domaintable.entrySet();
		i = entryset.iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			v2 = (VariableNameDeclaration) e.getKey();
			d2 = e.getValue();
			// 在domainset中查找
			d1 = a.getDomain(v2);
			if (d1 == null) {
				r.addDomain(v2, d2);
			}
		}
		return r;
	}

	/** 域集求逻辑交 */
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
			// 在domainset中查找
			d2 = b.getDomain(v1);
			if (d2 == null) {
				if(getDomainType(d1)==ClassType.ARRAY){
					r.addDomain(v1, d1);
				}
			} else {
				// 取intersect，数组特殊处理
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

		// 将b的那些a没有出现的插入到table中
		entryset = b.domaintable.entrySet();
		i = entryset.iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			v2 = (VariableNameDeclaration) e.getKey();
			d2 = e.getValue();
			// 在domainset中查找
			d1 = a.getDomain(v2);
			if (d1 == null) {
				if(getDomainType(d2)==ClassType.ARRAY){
					r.addDomain(v2, d2);
				}
			}
		}
		return r;
	}

	/** 判断域集是否存在矛盾，如果有任何一个变量的域为空则认为是矛盾 */
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
	
	/** 计算表达式的值,用的是控制流节点Out进行计算 */
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
	
	/** 计算表达式的值,用的是控制流节点In进行计算 */
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
	
	/** 删除那没有必要存放的域 */
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
