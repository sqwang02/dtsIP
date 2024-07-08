package softtest.fsm.java;

/** 状态机的访问者接口 */
public interface FSMVisitor {
	/** 状态访问者 */
	public void visit(FSMState n, Object data);

	/** 状态转换问者 */
	public void visit(FSMTransition e, Object data);

	/** 状态机访问者 */
	public void visit(FSMMachine g, Object data);
	
	/** 条件访问者 */
	public void visit(FSMCondition c, Object data);
}

