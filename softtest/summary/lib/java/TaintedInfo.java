/**
 * 
 */
package softtest.summary.lib.java;

import java.util.ArrayList;
import java.util.List;


/**
 * 
 * ���������˿��ܱ���Ⱦ�Ĳ���
 * 
 * @author ��ƽ��
 *
 */
public class TaintedInfo
{
	/**
	 * ����Ⱦ�Ĳ������
	 */
	private List taintedSeqs;
	
	/**
	 * ��Ӧ�Ĳ�������
	 */
	private List taintedTypes;
	
	/**
	 * ��ע��Ϣ
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
