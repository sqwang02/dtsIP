package softtest.fsm.java;
import softtest.cfg.java.*;

/** 转换条件 */
public abstract class  FSMCondition extends FSMElement{
	/** 对条件进行计算，判断其是否满足 */
	public abstract boolean evaluate(FSMMachineInstance fsm,FSMStateInstance state,VexNode vex);
	
	/** 状态机访问者的accept方法 */
	@Override
	public void accept(FSMVisitor visitor, Object data) {
		visitor.visit(this, data);
	}
}
