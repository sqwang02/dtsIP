package softtest.domain.java;

/** 任意域集，用于未定类型 */
public class ArbitraryDomain implements Domain {
	/** 打印 */
	@Override
	public String toString() {
		StringBuffer b = new StringBuffer();
		b.append("Arbitrary");
		return b.toString();
	}
}
