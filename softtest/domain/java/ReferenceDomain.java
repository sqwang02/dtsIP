package softtest.domain.java;

/** 引用域 */
public class ReferenceDomain implements Domain {
	/** 值 */
	private ReferenceValue value;
		
	/** unknown标记，代表当前域是否未知*/
	boolean unknown=false;
	
	/** 返回unknown标记*/
	public boolean getUnknown(){
		return this.unknown;
	}
	
	/** 设置unknown标记*/
	public void setUnknown(boolean unknown){
		this.unknown=unknown;
	}
	
	/** 拷贝构造*/
	public ReferenceDomain(ReferenceDomain domain) {
		this.value = domain.value;
		this.unknown=domain.unknown;
	}	
	
	/** 得到一个未知域*/
	public static ReferenceDomain getUnknownDomain(){
		ReferenceDomain r=new ReferenceDomain(ReferenceValue.NULL_OR_NOTNULL);
		r.unknown=true;
		return r;
	}
	
	/** 得到一个全域*/
	public static ReferenceDomain getFullDomain(){
		ReferenceDomain r=new ReferenceDomain(ReferenceValue.NULL_OR_NOTNULL);
		return r;
	}
	
	/** 得到一个空域*/
	public static ReferenceDomain getEmptyDomain(){
		ReferenceDomain r=new ReferenceDomain(ReferenceValue.EMPTY);
		return r;
	}
	
	/** 以value为值构造引用域 */
	public ReferenceDomain(ReferenceValue value) {
		this.value = value;
	}
	
	/** 设置值 */
	public void setValue(ReferenceValue value) {
		this.value = value;
	}
	
	/** 获得值 */
	public ReferenceValue getValue() {
		return value;
	}
	
	/** 域上并运算 a并b */
	public static ReferenceDomain union(ReferenceDomain a, ReferenceDomain b) {
		ReferenceDomain r = null;
		/*if(a.unknown||b.unknown){
			r=ReferenceDomain.getUnknownDomain();
			return r;
		}*/
		if(a.unknown){
			if(!b.unknown&&b.getValue()==ReferenceValue.NOTNULL){
				//notnull与unknown 求并取unknown
				r=ReferenceDomain.getUnknownDomain();
			}else{
				r=new ReferenceDomain(b);
			}
			return r;
		}
		if(b.unknown){
			if(!a.unknown&&a.getValue()==ReferenceValue.NOTNULL){
				//notnull与unknown 求并取unknown
				r=ReferenceDomain.getUnknownDomain();
			}else{
				r=new ReferenceDomain(a);
			}
			return r;
		}
		switch(a.getValue()){
		case EMPTY:
			switch(b.getValue()){
			case EMPTY:
				r = new ReferenceDomain(ReferenceValue.EMPTY);
				break;
			case NULL:
				r = new ReferenceDomain(ReferenceValue.NULL);
				break;
			case NOTNULL:
				r = new ReferenceDomain(ReferenceValue.NOTNULL);
				break;
			case NULL_OR_NOTNULL:
				r = new ReferenceDomain(ReferenceValue.NULL_OR_NOTNULL);
				break;
			}
			break;
		case NULL:
			switch(b.getValue()){
			case EMPTY:
				r = new ReferenceDomain(ReferenceValue.NULL);
				break;
			case NULL:
				r = new ReferenceDomain(ReferenceValue.NULL);
				break;
			case NOTNULL:
				r = new ReferenceDomain(ReferenceValue.NULL_OR_NOTNULL);
				break;
			case NULL_OR_NOTNULL:
				r = new ReferenceDomain(ReferenceValue.NULL_OR_NOTNULL);
				break;
			}
			break;
		case NOTNULL:
			switch(b.getValue()){
			case EMPTY:
				r = new ReferenceDomain(ReferenceValue.NOTNULL);
				break;
			case NULL:
				r = new ReferenceDomain(ReferenceValue.NULL_OR_NOTNULL);
				break;
			case NOTNULL:
				r = new ReferenceDomain(ReferenceValue.NOTNULL);
				break;
			case NULL_OR_NOTNULL:
				r = new ReferenceDomain(ReferenceValue.NULL_OR_NOTNULL);
				break;
			}
			break;
		case NULL_OR_NOTNULL:
			switch(b.getValue()){
			case EMPTY:
				r = new ReferenceDomain(ReferenceValue.NULL_OR_NOTNULL);
				break;
			case NULL:
				r = new ReferenceDomain(ReferenceValue.NULL_OR_NOTNULL);
				break;
			case NOTNULL:
				r = new ReferenceDomain(ReferenceValue.NULL_OR_NOTNULL);
				break;
			case NULL_OR_NOTNULL:
				r = new ReferenceDomain(ReferenceValue.NULL_OR_NOTNULL);
				break;
			}
			break;
		}
		assert (r!=null);
		return r;
	}
	
	/** 域上交运算 a交b */
	public static ReferenceDomain intersect(ReferenceDomain a, ReferenceDomain b) {
		ReferenceDomain r = null;
		if(a.unknown){
			r=new ReferenceDomain(b);
			return r;
		}
		if(b.unknown){
			r=new ReferenceDomain(a);
			return r;
		}
		switch(a.getValue()){
		case EMPTY:
			switch(b.getValue()){
			case EMPTY:
				r = new ReferenceDomain(ReferenceValue.EMPTY);
				break;
			case NULL:
				r = new ReferenceDomain(ReferenceValue.EMPTY);
				break;
			case NOTNULL:
				r = new ReferenceDomain(ReferenceValue.EMPTY);
				break;
			case NULL_OR_NOTNULL:
				r = new ReferenceDomain(ReferenceValue.EMPTY);
				break;
			}
			break;
		case NULL:
			switch(b.getValue()){
			case EMPTY:
				r = new ReferenceDomain(ReferenceValue.EMPTY);
				break;
			case NULL:
				r = new ReferenceDomain(ReferenceValue.NULL);
				break;
			case NOTNULL:
				r = new ReferenceDomain(ReferenceValue.EMPTY);
				break;
			case NULL_OR_NOTNULL:
				r = new ReferenceDomain(ReferenceValue.NULL);
				break;
			}
			break;
		case NOTNULL:
			switch(b.getValue()){
			case EMPTY:
				r = new ReferenceDomain(ReferenceValue.EMPTY);
				break;
			case NULL:
				r = new ReferenceDomain(ReferenceValue.EMPTY);
				break;
			case NOTNULL:
				r = new ReferenceDomain(ReferenceValue.NOTNULL);
				break;
			case NULL_OR_NOTNULL:
				r = new ReferenceDomain(ReferenceValue.NOTNULL);
				break;
			}
			break;
		case NULL_OR_NOTNULL:
			switch(b.getValue()){
			case EMPTY:
				r = new ReferenceDomain(ReferenceValue.EMPTY);
				break;
			case NULL:
				r = new ReferenceDomain(ReferenceValue.NULL);
				break;
			case NOTNULL:
				r = new ReferenceDomain(ReferenceValue.NOTNULL);
				break;
			case NULL_OR_NOTNULL:
				r = new ReferenceDomain(ReferenceValue.NULL_OR_NOTNULL);
				break;
			}
			break;
		}
		assert (r!=null);
		return r;
	}
	
	/** 域上减法 e-a */
	public static ReferenceDomain subtract(ReferenceDomain e, ReferenceDomain a) {
		ReferenceDomain r = inverse(a);
		r = intersect(e, r);
		return r;
	}	
	
	/** 域上取反 ~a */
	public static ReferenceDomain inverse(ReferenceDomain a){
		ReferenceDomain r = null;
		if(a.unknown){
			r=new ReferenceDomain(a);
			return r;
		}
		switch (a.getValue()){
		case EMPTY:
			r = new ReferenceDomain(ReferenceValue.NULL_OR_NOTNULL);
			break;
		case NULL:
			r = new ReferenceDomain(ReferenceValue.NOTNULL);
			break;
		case NOTNULL:
			r = new ReferenceDomain(ReferenceValue.NULL);
			break;
		case NULL_OR_NOTNULL:
			r = new ReferenceDomain(ReferenceValue.EMPTY);
			break;
		}		
		assert (r!=null);
		return r;
	}
	
	/** 打印 */
	@Override
	public String toString() {
		StringBuffer b = new StringBuffer();
		if(this.unknown){
			b.append("unknown");
		}else{
			b.append(value);
		}
		return b.toString();
	}
	
	/** 从字符串产生域 */
	public static ReferenceDomain valueOf(String str){
		ReferenceDomain r=null;
		if(str.equalsIgnoreCase("unknown")){
			r=ReferenceDomain.getUnknownDomain();
		}else{
			r =new ReferenceDomain(ReferenceValue.valueOf(str));
		}
		return r;
	}
	
	/** 如果值相等，则被认为相等 */
	@Override
	public boolean equals(Object o) {
		if ((o == null) || !(o instanceof ReferenceDomain)) {
			return false;
		}
		if (this == o) {
			return true;
		}
		ReferenceDomain x = (ReferenceDomain) o;
		if(x.unknown&&this.unknown){
			return true;
		}
		if(!x.unknown&&!this.unknown&&value == x.value) {
			return true;
		}
		return false;
	}
}
