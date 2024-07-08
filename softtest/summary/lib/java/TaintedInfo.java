/**
 * 
 */
package softtest.summary.lib.java;

import java.util.ArrayList;
import java.util.List;


/**
 * 
 * 该类描述了可能被污染的参数
 * 
 * @author 彭平雷
 *
 */
public class TaintedInfo
{
	/**
	 * 被污染的参数序号
	 */
	private List taintedSeqs;
	
	/**
	 * 对应的参数类型
	 */
	private List taintedTypes;
	
	/**
	 * 备注信息
	 * 
	 */
	private String otherInfo;
	
	public TaintedInfo()
	{
		taintedSeqs = new ArrayList();
		taintedTypes = new ArrayList();
		otherInfo = "";
	}

	//accessors
	public List getTaintedSeqs()
	{
		return taintedSeqs;
	}

	public void setTaintedSeqs(List taintedSeqs)
	{
		this.taintedSeqs = taintedSeqs;
	}

	public List getTaintedTypes()
	{
		return taintedTypes;
	}

	public void setTaintedTypes(List taintedTypes)
	{
		this.taintedTypes = taintedTypes;
	}
	
	
	
}
