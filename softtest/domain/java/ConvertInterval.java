package softtest.domain.java;

/** 区间转换 */
public class ConvertInterval {
	/** 将浮点区间转换为整数区间 */
	public static IntegerInterval DoubleToInteger(DoubleInterval d) {
		IntegerInterval i = new IntegerInterval(Math.round(d.getMin()), Math.round(d.getMax()));
		return i;
	}

	/** 将整数区间转换为浮点区间 */
	public static DoubleInterval IntegerToDouble(IntegerInterval i) {
		DoubleInterval d = new DoubleInterval(i.getRealMin(), i.getRealMax());
		return d;
	}

	public static void main(String args[]) {
		DoubleInterval d = new DoubleInterval(-3.3, 5.5);
		IntegerInterval i = DoubleToInteger(d);
		System.out.println(i);
	}
}
