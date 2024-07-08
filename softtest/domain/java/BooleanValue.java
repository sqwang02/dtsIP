package softtest.domain.java;
/** 布尔变量的可能值 */
public enum BooleanValue {
	/** 只能取true */
	TRUE,
	/** 只能取false */
	FALSE,
	/** true和false都可能取到 */
	TRUE_OR_FALSE,
	/** true和false都不能取，矛盾 */
	EMPTY
}
