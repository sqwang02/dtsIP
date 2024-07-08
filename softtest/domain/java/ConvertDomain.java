package softtest.domain.java;

import softtest.IntervalAnalysis.java.*;

/** 域转换类 */
public class ConvertDomain {
	/** 将浮点数域转换为整数域 */
	private static IntegerDomain DoubleToInteger(DoubleDomain d) {
		IntegerDomain i = new IntegerDomain();
		i.setUnknown(d.getUnknown());
		for (DoubleInterval interval : d.getIntervals()) {
			i.mergeWith(ConvertInterval.DoubleToInteger(interval));
		}
		return i;
	}

	/** 将整数域转换为浮点数域 */
	private static DoubleDomain IntegerToDouble(IntegerDomain i) {
		DoubleDomain d = new DoubleDomain();
		d.setUnknown(i.getUnknown());
		for (IntegerInterval interval : i.getIntervals()) {
			d.mergeWith(ConvertInterval.IntegerToDouble(interval));
		}
		return d;
	}

	private static DoubleDomain RefToDouble(ReferenceDomain r) {
		return DoubleDomain.getUnknownDomain();
	}

	private static ReferenceDomain DoubleToRef(DoubleDomain d) {
		ReferenceDomain r = new ReferenceDomain(ReferenceValue.NOTNULL);
		return r;
	}

	private static IntegerDomain RefToInteger(ReferenceDomain r) {
		return IntegerDomain.getUnknownDomain();
	}

	private static ReferenceDomain IntegerToRef(IntegerDomain i) {
		ReferenceDomain r = new ReferenceDomain(ReferenceValue.NOTNULL);
		return r;
	}

	private static BooleanDomain RefToBoolean(ReferenceDomain r) {
		return BooleanDomain.getUnknownDomain();
	}

	private static ReferenceDomain BooleanToRef(BooleanDomain b) {
		ReferenceDomain r = new ReferenceDomain(ReferenceValue.NOTNULL);
		return r;
	}

	

	private static ArrayDomain RefToArray(ReferenceDomain r) {	
		/**
		 * @author yang
		 * 2011-05-16 11:25
		 */
		if(r.unknown)//added by yang
		return ArrayDomain.getUnknownDomain();
		else 
			return ArrayDomain.getRefToArrayDomain(r);//added by yang 2011-05-13
		
	}
	
	private static ReferenceDomain ArrayToRef(ArrayDomain a) {
		/**
		 * @author yang
		 * 2011-05-16 11:25
		 */
		if(!a.getvalue().getUnknown())
			return (ReferenceDomain)a.getvalue();
		else//end-yang
			return ReferenceDomain.getUnknownDomain();
	}

	
	private static BooleanDomain IntegerToBoolean(IntegerDomain a){
		return BooleanDomain.getUnknownDomain();
	}
	
	private static ArrayDomain IntegerToArray(IntegerDomain a){
		return ArrayDomain.getUnknownDomain();
	}	
	
	private static IntegerDomain BooleanToInteger(BooleanDomain a){
		return IntegerDomain.getUnknownDomain();
	}		
	
	private static DoubleDomain BooleanToDouble(BooleanDomain a){
		return DoubleDomain.getUnknownDomain();
	}		
	
	private static ArrayDomain BooleanToArray(BooleanDomain a){
		return ArrayDomain.getUnknownDomain();
	}	
	
	private static BooleanDomain DoubleToBoolean(DoubleDomain a){
		return BooleanDomain.getUnknownDomain();
	}
	
	private static ArrayDomain DoubleToArray(DoubleDomain a){
		return ArrayDomain.getUnknownDomain();
	}		
	
	private static IntegerDomain ArrayToInteger(ArrayDomain a){
		return IntegerDomain.getUnknownDomain();
	}
	
	private static DoubleDomain ArrayToDouble(ArrayDomain a){
		return DoubleDomain.getUnknownDomain();
	}
	
	private static BooleanDomain ArrayToBoolean(ArrayDomain a){
		return BooleanDomain.getUnknownDomain();
	}
	
	public static Object DomainUnion(Object d1,Object d2){
		switch (DomainSet.getDomainType(d1)) {
		case INT:
			return IntegerDomain.union((IntegerDomain) d1, (IntegerDomain) d2);
		case DOUBLE:
			return DoubleDomain.union((DoubleDomain) d1, (DoubleDomain) d2);
		case REF:
			return ReferenceDomain.union((ReferenceDomain) d1, (ReferenceDomain) d2);
		case BOOLEAN:
			return BooleanDomain.union((BooleanDomain) d1, (BooleanDomain) d2);
		case ARRAY:
			return ArrayDomain.union((ArrayDomain) d1, (ArrayDomain) d2);
		case ARBITRARY:
			return d1;
		default:
			throw new RuntimeException("do not know the type of Variable");
		}
	}
	
	public static boolean DomainIsUnknown(Object d1){
		switch (DomainSet.getDomainType(d1)) {
		case INT:
			return ((IntegerDomain) d1).getUnknown();
		case DOUBLE:
			return ((DoubleDomain) d1).getUnknown();
		case REF:
			return ((ReferenceDomain) d1).getUnknown();
		case BOOLEAN:
			return ((BooleanDomain) d1).getUnknown();
		case ARRAY:
			return ((ArrayDomain) d1).getDimension(0).getUnknown();
		case ARBITRARY:
			return true;
		default:
			throw new RuntimeException("do not know the type of Variable");
		}
	}
	
	public static Object DomainSubtract(Object a,Object b){
		switch (DomainSet.getDomainType(a)) {
		case REF:
			return ReferenceDomain.subtract((ReferenceDomain) a, (ReferenceDomain) b);
		case DOUBLE:
			return DoubleDomain.subtract((DoubleDomain) a, (DoubleDomain) b);
		case INT:
			return IntegerDomain.subtract((IntegerDomain) a, (IntegerDomain) b);
		case BOOLEAN:
			return BooleanDomain.subtract((BooleanDomain) a, (BooleanDomain) b);
		case ARRAY:
			return ArrayDomain.subtract((ArrayDomain) a, (ArrayDomain) b);
		case ARBITRARY:
			return a;
		default:
			throw new RuntimeException("do not know the type of Variable");
		}
	}
	
	public static Object GetFullDomain(Object d){
		switch (DomainSet.getDomainType(d)) {
		case REF:
			return ReferenceDomain.getFullDomain();
		case DOUBLE:
			return DoubleDomain.getFullDomain();
		case INT:
			return IntegerDomain.getFullDomain();
		case BOOLEAN:
			return BooleanDomain.getFullDomain();
		case ARRAY:
			return ArrayDomain.getFullDomain();
		case ARBITRARY:
			return d;
		default:
			throw new RuntimeException("do not know the type of Variable");
		}
	}
	
	public static Object GetUnknownDomain(Object d){
		switch (DomainSet.getDomainType(d)) {
		case REF:
			return ReferenceDomain.getUnknownDomain();
		case DOUBLE:
			return DoubleDomain.getUnknownDomain();
		case INT:
			return IntegerDomain.getUnknownDomain();
		case BOOLEAN:
			return BooleanDomain.getUnknownDomain();
		case ARRAY:
			return ArrayDomain.getUnknownDomain();
		case ARBITRARY:
			return d;
		default:
			throw new RuntimeException("do not know the type of Variable");
		}
	}
	
	public static Object GetEmptyDomain(Object d){
		switch (DomainSet.getDomainType(d)) {
		case REF:
			return ReferenceDomain.getEmptyDomain();
		case DOUBLE:
			return DoubleDomain.getEmptyDomain();
		case INT:
			return IntegerDomain.getEmptyDomain();
		case BOOLEAN:
			return BooleanDomain.getEmptyDomain();
		case ARRAY:
			return ArrayDomain.getEmptyDomain();
		case ARBITRARY:
			return d;
		default:
			throw new RuntimeException("do not know the type of Variable");
		}	
	}
	
	public static Object DomainIntersect(Object d1,Object d2){
		switch (DomainSet.getDomainType(d1)) {
		case INT:
			return IntegerDomain.intersect((IntegerDomain) d1, (IntegerDomain) d2);
		case DOUBLE:
			return DoubleDomain.intersect((DoubleDomain) d1, (DoubleDomain) d2);
		case REF:
			return ReferenceDomain.intersect((ReferenceDomain) d1, (ReferenceDomain) d2);
		case BOOLEAN:
			return BooleanDomain.intersect((BooleanDomain) d1, (BooleanDomain) d2);
		case ARRAY:
			return ArrayDomain.intersect((ArrayDomain) d1, (ArrayDomain) d2);
		case ARBITRARY:
			return d1;
		default:
			throw new RuntimeException("do not know the type of Variable");
		}
	}

	public static Object DomainSwitch(Object from, ClassType totype) {
		if(from==null){
			return null;
		}
		Object to = null;
		ClassType fromtype = DomainSet.getDomainType(from);
		switch (fromtype) {
		case INT:
			switch (totype) {
			case INT:
				to = from;
				break;
			case BOOLEAN:
				to= IntegerToBoolean((IntegerDomain) from);
				break;
			case DOUBLE:
				to = IntegerToDouble((IntegerDomain) from);
				break;
			case REF:
				to = IntegerToRef((IntegerDomain) from);
				break;
			case ARRAY:
				to=IntegerToArray((IntegerDomain) from);
				break;
			case ARBITRARY:
				to = new ArbitraryDomain();
				break;
			default:
				throw new RuntimeException("do not know the type of Variable");
			}
			break;
		case BOOLEAN:
			switch (totype) {
			case INT:
				to= BooleanToInteger((BooleanDomain) from);
				break;
			case BOOLEAN:
				to = from;
				break;
			case DOUBLE:
				to= BooleanToDouble((BooleanDomain) from);
				break;
			case REF:
				to = BooleanToRef((BooleanDomain) from);
				break;
			case ARRAY:
				to= BooleanToArray((BooleanDomain) from);
				break;
			case ARBITRARY:
				to = new ArbitraryDomain();
				break;
			default:
				throw new RuntimeException("do not know the type of Variable");
			}
			break;
		case DOUBLE:
			switch (totype) {
			case INT:
				to = DoubleToInteger((DoubleDomain) from);
				break;
			case BOOLEAN:
				to = DoubleToBoolean((DoubleDomain) from);
				break;
			case DOUBLE:
				to = from;
				break;
			case REF:
				to = DoubleToRef((DoubleDomain) from);
				break;
			case ARRAY:
				to = DoubleToArray((DoubleDomain) from);
				break;
			case ARBITRARY:
				to = new ArbitraryDomain();
				break;
			default:
				throw new RuntimeException("do not know the type of Variable");
			}
			break;
		case REF:
			switch (totype) {
			case INT:
				to = RefToInteger((ReferenceDomain) from);
				break;
			case BOOLEAN:
				to = RefToBoolean((ReferenceDomain) from);
				break;
			case DOUBLE:
				to = RefToDouble((ReferenceDomain) from);
				break;
			case REF:
				to = from;
				break;
			case ARRAY:
				to = RefToArray((ReferenceDomain) from);
				break;
			case ARBITRARY:
				to = new ArbitraryDomain();
				break;
			default:
				throw new RuntimeException("do not know the type of Variable");
			}
			break;
		case ARRAY:
			switch (totype) {
			case INT:
				to = ArrayToInteger((ArrayDomain) from);
				break;
			case BOOLEAN:
				to = ArrayToBoolean((ArrayDomain) from);
				break;
			case DOUBLE:
				to = ArrayToDouble((ArrayDomain) from);
				break;
			case REF:
				to = ArrayToRef((ArrayDomain) from);
				break;
			case ARRAY:
				to = from;
				break;
			case ARBITRARY:
				to = new ArbitraryDomain();
				break;
			default:
				throw new RuntimeException("do not know the type of Variable");
			}
			break;
		case ARBITRARY:
			switch (totype) {
			case INT:
				to = IntegerDomain.getUnknownDomain();
				break;
			case BOOLEAN:
				to = BooleanDomain.getUnknownDomain();
				break;
			case DOUBLE:
				to = DoubleDomain.getUnknownDomain();
				break;
			case REF:
				to = ReferenceDomain.getUnknownDomain();
				break;
			case ARRAY:
				to = ArrayDomain.getUnknownDomain();
				break;
			case ARBITRARY:
				to = from;
				break;
			default:
				throw new RuntimeException("do not know the type of Variable");
			}
			break;
		default:
			throw new RuntimeException("do not know the type of Variable");
		}
		return to;
	}
	
	public static boolean isConvertible(ClassType type1,ClassType type2){
		switch(type1){
		case INT:
			switch (type2) {
			case INT:
				return true;
			case BOOLEAN:
				return false;
			case DOUBLE:
				return true;
			case REF:
				return true;
			case ARRAY:
				return false;
			case ARBITRARY:
				return true;
			default:
				throw new RuntimeException("do not know the type of Variable");
			}
		case BOOLEAN:
			switch (type2) {
			case INT:
				return false;
			case BOOLEAN:
				return true;
			case DOUBLE:
				return false;
			case REF:
				return true;
			case ARRAY:
				return false;
			case ARBITRARY:
				return true;
			default:
				throw new RuntimeException("do not know the type of Variable");
			}
		case DOUBLE:
			switch (type2) {
			case INT:
				return true;
			case BOOLEAN:
				return false;
			case DOUBLE:
				return true;
			case REF:
				return true;
			case ARRAY:
				return false;
			case ARBITRARY:
				return true;
			default:
				throw new RuntimeException("do not know the type of Variable");
			}
		case REF:
			switch (type2) {
			case INT:
				return true;
			case BOOLEAN:
				return true;
			case DOUBLE:
				return true;
			case REF:
				return true;
			case ARRAY:
				return true;
			case ARBITRARY:
				return true;
			default:
				throw new RuntimeException("do not know the type of Variable");
			}
		case ARRAY:
			switch (type2) {
			case INT:
				return false;
			case BOOLEAN:
				return false;
			case DOUBLE:
				return false;
			case REF:
				return true;
			case ARRAY:
				return true;
			case ARBITRARY:
				return true;
			default:
				throw new RuntimeException("do not know the type of Variable");
			}
		case ARBITRARY:
			switch (type2) {
			case INT:
				return true;
			case BOOLEAN:
				return true;
			case DOUBLE:
				return true;
			case REF:
				return true;
			case ARRAY:
				return true;
			case ARBITRARY:
				return true;
			default:
				throw new RuntimeException("do not know the type of Variable");
			}
		default:
			throw new RuntimeException("do not know the type of Variable");
		}
	}
}
