package softtest.domain.java;

/** 布尔域 */
public class BooleanDomain implements Domain {
	/** 值 */
	private BooleanValue value;
	
	/** unknown标记，代表当前域是否未知*/
	boolean unknown=false;
	
	/** 得到一个未知域*/
	public static BooleanDomain getUnknownDomain(){
		BooleanDomain r= new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
		r.setUnknown(true);
		return r;
	}
	
	/** 得到一个全域*/
	public static BooleanDomain getFullDomain(){
		BooleanDomain r= new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
		return r;
	}
	
	/** 得到一个空域*/
	public static BooleanDomain getEmptyDomain(){
		BooleanDomain r= new BooleanDomain(BooleanValue.EMPTY);
		return r;
	}
	
	/** 设置unknown标记*/
	public void setUnknown(boolean unknown){
		this.unknown=unknown;
	}
	
	/** 拷贝构造*/
	public BooleanDomain(BooleanDomain domain){
		this.value=domain.value;
		this.unknown=domain.unknown;
	}
	
	/** 返回unknown标记*/
	public boolean getUnknown(){
		return unknown;
	}

	/** 以value为值构造布尔域 */
	public BooleanDomain(BooleanValue value) {
		this.value = value;
	}

	/** 获得值 */
	public BooleanValue getValue() {
		return value;
	}

	/** 设置值 */
	public void setValue(BooleanValue value) {
		this.value = value;
		unknown=false;
	}

	/** 域上并运算 a并b */
	public static BooleanDomain union(BooleanDomain a, BooleanDomain b) {
		BooleanDomain r = null;
		/*if(a.unknown||b.unknown){
			r=BooleanDomain.getUnknownDomain();
			return r;
		}*/
		if(a.unknown){
			r=new BooleanDomain(b);
			return r;
		}
		if(b.unknown){
			r=new BooleanDomain(a);
			return r;
		}
		switch (a.getValue()){
		case EMPTY:
			switch (b.getValue()){
			case EMPTY:
				r = new BooleanDomain(BooleanValue.EMPTY);
				break;
			case FALSE:
				r = new BooleanDomain(BooleanValue.FALSE);
				break;
			case TRUE:
				r = new BooleanDomain(BooleanValue.TRUE);
				break;
			case TRUE_OR_FALSE:
				r = new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
				break;
			}
			break;
		case FALSE:
			switch (b.getValue()){
			case EMPTY:
				r = new BooleanDomain(BooleanValue.FALSE);
				break;
			case FALSE:
				r = new BooleanDomain(BooleanValue.FALSE);
				break;
			case TRUE:
				r = new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
				break;
			case TRUE_OR_FALSE:
				r = new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
				break;
			}
			break;
		case TRUE:
			switch (b.getValue()){
			case EMPTY:
				r = new BooleanDomain(BooleanValue.TRUE);
				break;
			case FALSE:
				r = new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
				break;
			case TRUE:
				r = new BooleanDomain(BooleanValue.TRUE);
				break;
			case TRUE_OR_FALSE:
				r = new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
				break;
			}
			break;
		case TRUE_OR_FALSE:
			switch (b.getValue()){
			case EMPTY:
				r = new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
				break;
			case FALSE:
				r = new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
				break;
			case TRUE:
				r = new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
				break;
			case TRUE_OR_FALSE:
				r = new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
				break;
			}
			break;
		}
		assert (r!=null);
		return r;
	}
	
	/** 域上交运算 a交b */
	public static BooleanDomain intersect(BooleanDomain a, BooleanDomain b) {
		BooleanDomain r = null;
		if(a.unknown){
			r=new BooleanDomain(b);
			return r;
		}
		if(b.unknown){
			r=new BooleanDomain(a);
			return r;
		}
		switch (a.getValue()){
		case EMPTY:
			switch (b.getValue()){
			case EMPTY:
				r = new BooleanDomain(BooleanValue.EMPTY);
				break;
			case FALSE:
				r = new BooleanDomain(BooleanValue.EMPTY);
				break;
			case TRUE:
				r = new BooleanDomain(BooleanValue.EMPTY);
				break;
			case TRUE_OR_FALSE:
				r = new BooleanDomain(BooleanValue.EMPTY);
				break;
			}
			break;
		case FALSE:
			switch (b.getValue()){
			case EMPTY:
				r = new BooleanDomain(BooleanValue.EMPTY);
				break;
			case FALSE:
				r = new BooleanDomain(BooleanValue.FALSE);
				break;
			case TRUE:
				r = new BooleanDomain(BooleanValue.EMPTY);
				break;
			case TRUE_OR_FALSE:
				r = new BooleanDomain(BooleanValue.FALSE);
				break;
			}
			break;
		case TRUE:
			switch (b.getValue()){
			case EMPTY:
				r = new BooleanDomain(BooleanValue.EMPTY);
				break;
			case FALSE:
				r = new BooleanDomain(BooleanValue.EMPTY);
				break;
			case TRUE:
				r = new BooleanDomain(BooleanValue.TRUE);
				break;
			case TRUE_OR_FALSE:
				r = new BooleanDomain(BooleanValue.TRUE);
				break;
			}
			break;
		case TRUE_OR_FALSE:
			switch (b.getValue()){
			case EMPTY:
				r = new BooleanDomain(BooleanValue.EMPTY);
				break;
			case FALSE:
				r = new BooleanDomain(BooleanValue.FALSE);
				break;
			case TRUE:
				r = new BooleanDomain(BooleanValue.TRUE);
				break;
			case TRUE_OR_FALSE:
				r = new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
				break;
			}
			break;
		}
		assert (r!=null);
		return r;
	}

	/** 域上减法 e-a */
	public static BooleanDomain subtract(BooleanDomain e, BooleanDomain a) {
		BooleanDomain r = inverse(a);
		r = intersect(e, r);
		return r;
	}	
	
	/** 域上取反 ~a */
	public static BooleanDomain inverse(BooleanDomain a){
		BooleanDomain r = null;
		if(a.unknown){
			r=new BooleanDomain(a);
			return r;
		}
		switch (a.getValue()){
		case EMPTY:
			r = new BooleanDomain(BooleanValue.TRUE_OR_FALSE);
			break;
		case FALSE:
			r = new BooleanDomain(BooleanValue.TRUE);
			break;
		case TRUE:
			r = new BooleanDomain(BooleanValue.FALSE);
			break;
		case TRUE_OR_FALSE:
			r = new BooleanDomain(BooleanValue.EMPTY);
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
	public static BooleanDomain valueOf(String str){
		BooleanDomain r=null;
		if(str.equalsIgnoreCase("unknown")){
			r=BooleanDomain.getUnknownDomain();
		}else{
			r =new BooleanDomain(BooleanValue.valueOf(str));
		}
		return r;
	}
	
	/** 如果布尔域的值相等，则被认为相等 */
	@Override
	public boolean equals(Object o) {
		if ((o == null) || !(o instanceof BooleanDomain)) {
			return false;
		}
		if (this == o) {
			return true;
		}
		BooleanDomain x = (BooleanDomain) o;
		if(x.unknown&&this.unknown){
			return true;
		}
		if (!x.unknown&&!this.unknown&&value == x.value) {
			return true;
		}
		return false;
	}
}
