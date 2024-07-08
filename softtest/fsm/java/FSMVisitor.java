package softtest.fsm.java;

/** ״̬���ķ����߽ӿ� */
public interface FSMVisitor {
	/** ״̬������ */
	public void visit(FSMState n, Object data);

	/** ״̬ת������ */
	public void visit(FSMTransition e, Object data);

	/** ״̬�������� */
	public void visit(FSMMachine g, Object data);
	
	/** ���������� */
	public void visit(FSMCondition c, Object data);
}

