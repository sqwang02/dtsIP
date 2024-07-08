package softtest.domain.java;

import java.util.*;

/** 数组变量域，跟踪数组的维数 */
public class ArrayDomain implements Domain {
	/** 数组的维数及每一维的长度 */
	ArrayList<IntegerDomain> dimensions;

	/** 数组通过函数返回值的赋值可能为空 */
	//add by zhouhb
	//2011.5.8
	private ReferenceDomain value;

	/** 缺省构造函数 */
	public ArrayDomain() {
		dimensions = new ArrayList<IntegerDomain>();
		value = new ReferenceDomain(ReferenceValue.NULL_OR_NOTNULL);//added by zhb
		value.unknown=true;//added by zhb
	}

	/** 构造维数为num的数组域 */
	public ArrayDomain(int num) {
		this();
		for (int i = 0; i < num; i++) {
			dimensions.add(IntegerDomain.getUnknownDomain());
		}
		//by yang:value = new ReferenceDomain(ReferenceValue.NULL_OR_NOTNULL);//added by zhb
		value.unknown=true;//added by yang 2011-06-07 14:48
		
	}


		
	public static ArrayDomain getUnknownDomain(){
		ArrayDomain r= new ArrayDomain(1);
		r.value=new ReferenceDomain(ReferenceValue.NULL_OR_NOTNULL);//added by zhb
		r.value.unknown=true;//added by zhb
		return r;
	}

	/**
	 * @author yang
	 * @since 2011-05-13 16:48
	 * @param r
	 * @return 转换为数组类型的引用类型的值
	 */
	public static ArrayDomain getRefToArrayDomain(ReferenceDomain r) {
		// TODO Auto-generated method stub
		ArrayDomain array= new ArrayDomain(1);
		array.value=new ReferenceDomain(r);		
		return array;
	}
	
	
	public static ArrayDomain getFullDomain(){
		ArrayDomain r= new ArrayDomain(1);
		r.value=new ReferenceDomain(ReferenceValue.NULL_OR_NOTNULL);
		return r;
	}
	
	public ArrayDomain(ArrayDomain domain){
		this();
		for (int i = 0; i < domain.getAllDimensions().size(); i++) {
			dimensions.add(domain.getDimension(i));
		}
		//value = new ReferenceDomain(ReferenceValue.NOTNULL);//added by zhb
		value = new ReferenceDomain(ReferenceValue.NULL_OR_NOTNULL);//ADDED BY YANG
	}
	


	/** 返回index维的长度，在没有初始化及超过范围时长度返回-1 */
	public IntegerDomain getDimension(int index) {
		if (index >= dimensions.size()) {
			return IntegerDomain.getUnknownDomain();//-1;
		} else {
			return dimensions.get(index);
		}
	}
	
	/** 设置index维的长度为d */
	public void setDimension(int index, IntegerDomain d) {
		if(index>=dimensions.size()){
			for(int i=0;i<=index-dimensions.size();i++){
				dimensions.add(IntegerDomain.getUnknownDomain());
			}
		}
		if(!d.isEmpty()&&d.jointoOneInterval().getMax()==Long.MAX_VALUE){
			d=IntegerDomain.getUnknownDomain();
		}
		dimensions.set(index, d);
	}
	
	/** 返回所有维 */
	public ArrayList<IntegerDomain> getAllDimensions(){
		return dimensions;
	}
	
	/** 设置所有维 */
	public void setAllDimensions(ArrayList<IntegerDomain> dimensions){
	    this.dimensions=dimensions;
	 }

	  //周虹伯 10:12:39 
	public ReferenceDomain getvalue(){
		return value;
		}

		public void setvalue(ReferenceDomain value){
		this.value=value;
		}

	
	
	/** 数组域的交，维数取最大值，在每一维上取最小值 */
	public static ArrayDomain intersect(ArrayDomain a, ArrayDomain b) {
		ArrayDomain r=null;
		// 取维数最大的那个，在每一维上取交
		int max = a.dimensions.size() < b.dimensions.size() ? b.dimensions.size() : a.dimensions.size();
		int min = a.dimensions.size() > b.dimensions.size() ? b.dimensions.size() : a.dimensions.size();
		
		r = new ArrayDomain(max);

		for (int i = 0; i < min; i++) {
			r.setDimension(i, IntegerDomain.intersect(a.dimensions.get(i),b.dimensions.get(i)));
		}
		
		for(int i=min;i<max;i++){
			r.setDimension(i,a.dimensions.size() > b.dimensions.size()?a.dimensions.get(i):b.dimensions.get(i));
		}
		r.value=ReferenceDomain.intersect(a.value, b.value);//added by zhb
		return r;
	}
	
	/** 数组域的并，维数取最大值，在每一维上取最大值 */
	public static ArrayDomain union(ArrayDomain a, ArrayDomain b) {
		ArrayDomain r=null;
		// 取维数最大的那个，在每一维上取并
		int max = a.dimensions.size() < b.dimensions.size() ? b.dimensions.size() : a.dimensions.size();
		int min = a.dimensions.size() > b.dimensions.size() ? b.dimensions.size() : a.dimensions.size();
		
		r = new ArrayDomain(max);

		for (int i = 0; i < min; i++) {
			r.setDimension(i, IntegerDomain.union(a.dimensions.get(i),b.dimensions.get(i)));
		}
		
		for(int i=min;i<max;i++){
			r.setDimension(i,a.dimensions.size() > b.dimensions.size()?a.dimensions.get(i):b.dimensions.get(i));
		}
		r.value=ReferenceDomain.union(a.value, b.value);//added by zhb
		return r;
	}
	

	
	public static ArrayDomain subtract(ArrayDomain a,ArrayDomain b){
		ArrayDomain r=null;

		int max = a.dimensions.size() < b.dimensions.size() ? b.dimensions.size() : a.dimensions.size();
		int min = a.dimensions.size() > b.dimensions.size() ? b.dimensions.size() : a.dimensions.size();
		
		r = new ArrayDomain(max);

		for (int i = 0; i < min; i++) {
			r.setDimension(i, IntegerDomain.subtract(a.dimensions.get(i),b.dimensions.get(i)));
		}
		
		for(int i=min;i<max;i++){
			r.setDimension(i,a.dimensions.size() > b.dimensions.size()?a.dimensions.get(i):IntegerDomain.getUnknownDomain());
		}
		r.value=ReferenceDomain.subtract(a.value, b.value);
		return r;
	}
	

	
	/** 打印 */
	@Override
	public String toString() {
		StringBuffer b = new StringBuffer();
		b.append("Array");
		for (int i = 0; i < dimensions.size(); i++) {
			b.append("{"+dimensions.get(i)+"}");
		}
		b.append(value.toString());//added by zhb
		return b.toString();
	}
	


	
	public boolean isEmpty(){
		for(int i=0;i < dimensions.size(); i++){
			if(dimensions.get(i).isEmpty()){
				return true;
			}
		}
		return false;
	}


	
	public static ArrayDomain getEmptyDomain(){
		ArrayDomain ret=new ArrayDomain(1);
		ret.setDimension(0, IntegerDomain.getEmptyDomain());
		return ret;
	}


	/** 判断两个数组域是否相等，维数相等及每一维长度也相等的数组域被认为相等
	 * 添加的代码用来判断两个数组域的引用域是否相等 added by yang */
	@Override
	public boolean equals(Object o) {
		if ((o == null) || !(o instanceof ArrayDomain)) {
			return false;
		}
		if (this == o) {
			return true;
		}
		ArrayDomain x = (ArrayDomain) o;
		
		//added by yang 2011-05-13
		if(dimensions.get(0)!=null && x.dimensions.get(0)!=null && dimensions.get(0).getUnknown()&& x.dimensions.get(0).getUnknown()){
			if(this.value.equals(x.value))
				return true;
		}
			
		
		else
		//end yang
		{
			if (dimensions.size() == x.dimensions.size()) {
			for (int i = 0; i < dimensions.size(); i++) {
				if (!dimensions.get(i).equals(x.dimensions.get(i))) {
					return false;
				}
			}
			return true;
		}//added by yang
		}
		return false;
	}

	
}
