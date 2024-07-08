/**
 * 
 */
package softtest.summary.lib.java;

import java.util.ArrayList;
import java.util.List;

/**
 * 该类描述了函数中对安全敏感的参数，包括该参数的序号以及类型。
 * 
 * @author 彭平雷
 *
 */
public class SensitiveInfo
{
	/**
	 * 敏感参数的序号
	 */
	private List sensitiveSeqs;
	
	/**
	 * 对应的参数类型
	 */
	private List sensitiveTypes;
	
	/**
	 * 备注信息
	 * 
	 */
	private String otherInfo;
	
	public SensitiveInfo()
	{
		sensitiveSeqs = new ArrayList();
		sensitiveTypes = new ArrayList();
		otherInfo = "";
	}
	
	//accessors
	public List getSensitiveSeqs()
	{
		return sensitiveSeqs;
	}

	public void setSensitiveSeqs(List sensitiveSeqs)
	{
		this.sensitiveSeqs = sensitiveSeqs;
	}

	public List getSensitiveTypes()
	{
		return sensitiveTypes;
	}

	public void setSensitiveTypes(List sensitiveTypes)
	{
		this.sensitiveTypes = sensitiveTypes;
	}
}
