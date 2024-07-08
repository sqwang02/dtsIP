package softtest.domain.java;

/** 支持最基本的浮点数数学运算 */
public class DoubleMath {
	/** 正0 */
	static double Zero;

	/** 负0 */
	static double NegZero;

	/** 控制边界精度的标志 */
	static boolean Rounding;

	static {
		Zero = 0.0;
		NegZero = (-1.0) * (0.0);
		Rounding = false;
	}

	/** 返回下一个double符点数,朝正无穷的方 */
	public static double nextfp(double x) {
		double y;
		if (x == 0) {
			return Double.longBitsToDouble(1);
		} else if (x < Double.POSITIVE_INFINITY && x> Double.NEGATIVE_INFINITY) {
			long xx = Double.doubleToLongBits(x);
			if (x > 0) {
				y = Double.longBitsToDouble(xx + 1);
			} else if (x == 0) { // this case should never happen
				y = Double.longBitsToDouble(1);
			} else {
				y = Double.longBitsToDouble(xx - 1);
			}
			return y;
		} else {
			return x;
		}
	}

	/** 返回上一个浮点数,朝负无穷的方向 */
	public static double prevfp(double x) {
		if (x == 0) {
			return (-nextfp(0.0));
		} else {
			return (-nextfp(-x));
		}
	}

	public static double add_lo(double x, double y) {
		if (Rounding) {
			return (prevfp(x + y));
		} else {
			return x + y;
		}
	}

	public static double add_hi(double x, double y) {
		if (Rounding) {
			return (nextfp(x + y));
		} else {
			return x + y;
		}
	}

	public static double sub_lo(double x, double y) {
		if (Rounding) {
			return (prevfp(x - y));
		} else {
			return x - y;
		}
	}

	public static double sub_hi(double x, double y) {
		if (Rounding) {
			return (nextfp(x - y));
		} else {
			return x - y;
		}
	}

	public static double mul_lo(double x, double y) {
		if ((x == 0.0) || (y == 0.0)) {
			return (0.0);
		}
		if (Rounding) {
			return (prevfp(x * y));
		} else {
			return x * y;
		}
	}

	public static double mul_hi(double x, double y) {
		if ((x == 0.0) || (y == 0.0)) {
			return (0.0);
		}
		if (Rounding) {
			return (nextfp(x * y));
		} else {
			return x * y;
		}
	}

	public static double div_lo(double x, double y) {
		if (x == 0.0) {
			return (0.0);
		}
		if (Rounding) {
			return (prevfp(x / y));
		} else {
			return x / y;
		}
	}

	public static double div_hi(double x, double y) {
		if (x == 0.0) {
			return (0.0);
		}
		if (Rounding) {
			return (nextfp(x / y));
		} else {
			return x / y;
		}
	}

	public static double exp_lo(double x) {
		if (x == Double.NEGATIVE_INFINITY) {
			return (0.0);
		} else if (x < Double.POSITIVE_INFINITY) {
			if (Rounding) {
				return (Math.max(0.0, prevfp(Math.exp(x))));
			} else {
				return Math.max(0.0, Math.exp(x));
			}
		} else {
			return (x);
		}
	}

	public static double exp_hi(double x) {
		if (x == Double.NEGATIVE_INFINITY) {
			return (0.0);
		} else if (x < Double.POSITIVE_INFINITY) {
			if (Rounding) {
				return (nextfp(Math.exp(x)));
			} else {
				return Math.exp(x);
			}
		} else {
			return (x);
		}
	}

	public static double log_lo(double x) {
		if (x < 0.0) {
			return (Double.NaN);
		} else if (x < Double.POSITIVE_INFINITY) {
			if (Rounding) {
				return (prevfp(Math.log(x)));
			} else {
				return Math.log(x);
			}
		} else {
			return (x);
		}
	}

	public static double log_hi(double x) {
		if (x < 0.0) {
			return (Double.NaN);
		} else if (x < Double.POSITIVE_INFINITY) {
			if (Rounding) {
				return (nextfp(Math.log(x)));
			} else {
				return Math.log(x);
			}
		} else {
			return (x);
		}
	}

	public static double sin_lo(double x) {
		if (Rounding) {
			return (prevfp(Math.sin(x)));
		} else {
			return Math.sin(x);
		}
	}

	public static double sin_hi(double x) {
		if (Rounding) {
			return (nextfp(Math.sin(x)));
		} else {
			return Math.sin(x);
		}
	}

	public static double cos_lo(double x) {
		if (Rounding) {
			return (prevfp(Math.cos(x)));
		} else {
			return Math.cos(x);
		}
	}

	public static double cos_hi(double x) {
		if (Rounding) {
			return (nextfp(Math.cos(x)));
		} else {
			return Math.cos(x);
		}
	}

	public static double tan_lo(double x) {
		if (Rounding) {
			return (prevfp(Math.tan(x)));
		} else {
			return Math.tan(x);
		}
	}

	public static double tan_hi(double x) {
		if (Rounding) {
			return (nextfp(Math.tan(x)));
		} else {
			return Math.tan(x);
		}
	}

	public static double asin_lo(double x) {
		if (Rounding) {
			return (prevfp(Math.asin(x)));
		} else {
			return Math.asin(x);
		}
	}

	public static double asin_hi(double x) {
		if (Rounding) {
			return (nextfp(Math.asin(x)));
		} else {
			return Math.asin(x);
		}
	}

	public static double acos_lo(double x) {
		if (Rounding) {
			return (prevfp(Math.acos(x)));
		} else {
			return Math.acos(x);
		}
	}

	public static double acos_hi(double x) {
		if (Rounding) {
			return (nextfp(Math.acos(x)));
		} else {
			return Math.acos(x);
		}
	}

	public static double atan_lo(double x) {
		if (Rounding) {
			return (prevfp(Math.atan(x)));
		} else {
			return Math.atan(x);
		}
	}

	public static double atan_hi(double x) {
		if (Rounding) {
			return (nextfp(Math.atan(x)));
		} else {
			return Math.atan(x);
		}
	}

	/*
	 * These are meant only to be called with -1/4 <= x < 1/4. They are only to
	 * be used in the ia_math package.
	 */

	static double sin2pi_lo(double x) {
		if (Rounding) {
			return (prevfp(Math.sin(prevfp(Math.PI * 2 * x))));
		} else {
			return Math.sin(Math.PI * 2 * x);
		}
	}

	static double sin2pi_hi(double x) {
		if (Rounding) {
			return (nextfp(Math.sin(nextfp(Math.PI * 2 * x))));
		} else {
			return Math.sin(Math.PI * 2 * x);
		}
	}

	static double cos2pi_lo(double x) {
		if (Rounding) {
			if (x > 0) {
				return (prevfp(Math.cos(nextfp(Math.PI * 2 * x))));
			} else {
				return (prevfp(Math.cos(prevfp(Math.PI * 2 * x))));
			}
		} else {
			return Math.cos(Math.PI * 2 * x);
		}
	}

	static double cos2pi_hi(double x) {
		if (Rounding) {
			if (x > 0) {
				return (nextfp(Math.cos(prevfp(Math.PI * 2 * x))));
			} else {
				return (nextfp(Math.cos(nextfp(Math.PI * 2 * x))));
			}
		} else {
			return Math.cos(Math.PI * 2 * x);
		}
	}

	static double tan2pi_lo(double x) {
		if (Rounding) {
			return (prevfp(Math.tan(prevfp(Math.PI * 2 * x))));
		} else {
			return Math.tan(Math.PI * 2 * x);
		}
	}

	static double tan2pi_hi(double x) {
		if (Rounding) {
			return (nextfp(Math.tan(nextfp(Math.PI * 2 * x))));
		} else {
			return Math.tan(Math.PI * 2 * x);
		}
	}

	/*
	 * These are meant to be called with 0<=x<=1 where asin2pi(x) =
	 * asin(x)/(2*pi), etc.
	 */

	static double asin2pi_lo(double x) {
		if (Rounding) {
			return (prevfp(Math.asin(x) / nextfp(Math.PI * 2)));
		} else {
			return Math.asin(x) / (Math.PI * 2);
		}
	}

	static double asin2pi_hi(double x) {
		if (Rounding) {
			return (nextfp(Math.asin(x) / prevfp(Math.PI * 2)));
		} else {
			return Math.asin(x) / (Math.PI * 2);
		}
	}

	static double acos2pi_lo(double x) {
		if (Rounding) {
			return (prevfp(Math.acos(x) / nextfp(Math.PI * 2)));
		} else {
			return Math.acos(x) / (Math.PI * 2);
		}
	}

	static double acos2pi_hi(double x) {
		if (Rounding) {
			return (nextfp(Math.acos(x) / prevfp(Math.PI * 2)));
		} else {
			return Math.acos(x) / (Math.PI * 2);
		}
	}

	static double atan2pi_lo(double x) {
		if (Rounding) {
			return (prevfp(Math.atan(x) / nextfp(Math.PI * 2)));
		} else {
			return Math.atan(x) / (Math.PI * 2);
		}
	}

	static double atan2pi_hi(double x) {
		if (Rounding) {
			return (nextfp(Math.atan(x) / prevfp(Math.PI * 2)));
		} else {
			return Math.atan(x) / (Math.PI * 2);
		}
	}

	/**
	 * returns lower bound on x**y assuming x>0
	 */
	public static double pow_lo(double x, double y) {
		if (x < 0)
			return Double.NaN;
		else if (x == 0.0)
			return 0.0;
		else if (y > 0) {
			if (x >= 1)
				return exp_lo(mul_lo(y, log_lo(x)));
			else if (x == 1)
				return 1.0;
			else
				return exp_lo(mul_lo(y, log_hi(x)));
		} else if (y == 0)
			return 1.0;
		else {
			if (x >= 1)
				return exp_lo(mul_lo(y, log_hi(x)));
			else if (x == 1)
				return 1.0;
			else
				return exp_lo(mul_lo(y, log_lo(x)));
		}
	}

	/**
	 * returns upper bound on x**y assuming x>0
	 */
	public static double pow_hi(double x, double y) {
		if (x < 0)
			return Double.NaN;
		else if (x == 0.0)
			return 0.0;
		else if (y > 0) {
			if (x >= 1)
				return exp_hi(mul_hi(y, log_hi(x)));
			else if (x == 1)
				return 1.0;
			else
				return exp_hi(mul_hi(y, log_lo(x)));
		} else if (y == 0)
			return 1.0;
		else {
			if (x >= 1)
				return exp_lo(mul_hi(y, log_lo(x)));
			else if (x == 1)
				return 1.0;
			else
				return exp_lo(mul_hi(y, log_hi(x)));
		}
	}

	/* add by xqing */
	/** 求低位的sqrt */
	public static double sqrt_lo(double x) {
		if (x < 0) {
			return Double.NaN;
		} else if (x < Double.POSITIVE_INFINITY && x > 0) {
			if (Rounding) {
				return (Math.max(0.0, prevfp(Math.sqrt(x))));
			} else {
				return Math.max(0.0, Math.sqrt(x));
			}
		} else {
			return (x);
		}
	}

	/** 求高位的sqrt */
	public static double sqrt_hi(double x) {
		if (x < 0) {
			return Double.NaN;
		} else if (x < Double.POSITIVE_INFINITY && x > 0) {
			if (Rounding) {
				return (nextfp(Math.sqrt(x)));
			} else {
				return Math.sqrt(x);
			}
		} else {
			return (x);
		}
	}
}
