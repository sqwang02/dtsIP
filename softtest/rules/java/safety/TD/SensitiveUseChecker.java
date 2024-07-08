/**
 * 
 */
package softtest.rules.java.safety.TD;

import java.util.HashSet;

/**
 * @author ��ƽ��
 * 
 * ��������������ʹ�õļ����
 *
 */
public class SensitiveUseChecker
{
	/**
	 * ����ʹ�ü��������
	 */
	private HashSet<ISensitiveChecker> sensitiveCheckers;
	
	/**
	 * ��ȾԪ�ؼ���
	 */
	private TaintedSet taintedSet;
	
	public SensitiveUseChecker()
	{
		sensitiveCheckers = new HashSet<ISensitiveChecker>();
	}

	
	//accessors
	public void setSensitiveCheckers(HashSet<ISensitiveChecker> sensitiveCheckers)
	{
		this.sensitiveCheckers = sensitiveCheckers;
	}

	public HashSet<ISensitiveChecker> getSensitiveCheckers()
	{
		return sensitiveCheckers;
	}

	public TaintedSet getTaintedSet()
	{
		return taintedSet;
	}

	public void setTaintedSet(TaintedSet taintedSet)
	{
		this.taintedSet = taintedSet;
	}
}
