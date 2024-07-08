package softtest.domain.java;
/** 引用变量的可能值 */
public enum ReferenceValue {
	/** 只能取null */
	NULL,
	/** 不可能为null */
	NOTNULL,
	/** null和非null都可能 */
	NULL_OR_NOTNULL,
	/** null和非null都不能取，矛盾 */
	EMPTY
}
