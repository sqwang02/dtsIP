/**
 * 
 */
package softtest.rules.java.safety.TD;

import java.util.HashSet;

/**
 * @author 彭平雷
 * 
 * 包含了所有敏感使用的检测者
 *
 */
public class SensitiveUseChecker
{
	/**
	 * 敏感使用检测者容器
	 */
	private HashSet<ISensitiveChecker> sensitiveCheckers;
	
	/**
	 * 污染元素集合
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
