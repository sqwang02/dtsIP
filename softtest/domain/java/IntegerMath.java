package softtest.domain.java;

/** 支持最基本的整型数数学运算 */
public class IntegerMath {
	/** 返回下一个整数,最大不超过正无穷 */
	public static long nextInt(long x) {
		if (x == Long.MAX_VALUE) return x;
		else return x+1;
	}

	/** 返回上一个浮点数,朝负无穷的方向 */
	public static long prevInt(long x) {
		if (x == Long.MIN_VALUE) return x;
		else return x-1;
	}
	
	public static double add(double x, double y) {
		double z = x + y;
		if (z >= Long.MAX_VALUE) return Double.POSITIVE_INFINITY;
		else if (z <= Long.MIN_VALUE) return Double.NEGATIVE_INFINITY;
		else return x+y;
	}

	public static long add(long x, long y) {
		double z = (double)x + y;
		if (z >= Long.MAX_VALUE) return Long.MAX_VALUE;
		else if (z <= Long.MIN_VALUE) return Long.MIN_VALUE;
		else return x+y;
	}

	public static double sub(double x, double y) {
		double z = x - y;
		if (z >= Long.MAX_VALUE) return Double.POSITIVE_INFINITY;
		else if (z <= Long.MIN_VALUE) return Double.NEGATIVE_INFINITY;
		else return x-y;
	}
	
	public static long sub(long x, long y) {
		double z = (double)x - y;
		if (z >= Long.MAX_VALUE) return Long.MAX_VALUE;
		else if (z <= Long.MIN_VALUE) return Long.MIN_VALUE;
		else return x-y;
	}
	
	public static double mul(double x, double y) {
		double z = x * y;
		if (z >= Long.MAX_VALUE) return Double.POSITIVE_INFINITY;
		else if (z <= Long.MIN_VALUE) return Double.NEGATIVE_INFINITY;
		else return x*y;
	}

	public static long mul(long x, long y) {
		double z = (double)x * y;
		if (z >= Long.MAX_VALUE) return Long.MAX_VALUE;
		else if (z <= Long.MIN_VALUE) return Long.MIN_VALUE;
		else return x*y;
	}
	
	public static double div(double x, double y) {
		if (x == 0) return 0;
		if (y == 0 && x > 0) return Double.POSITIVE_INFINITY;
		if (y == 0 && x < 0) return Double.NEGATIVE_INFINITY;
		
		double z = x / y;
		if (z >= Long.MAX_VALUE) return Double.POSITIVE_INFINITY;
		else if (z <= Long.MIN_VALUE) return Double.NEGATIVE_INFINITY;
		else return x/y;
	}

	public static long div(long x, long y) {
		if (x == 0) return 0;
		if (y == 0 && x > 0) return Long.MAX_VALUE;
		if (y == 0 && x < 0) return Long.MIN_VALUE;
		return x/y;
	}
}
