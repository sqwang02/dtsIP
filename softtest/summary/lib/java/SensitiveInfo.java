/**
 * 
 */
package softtest.summary.lib.java;

import java.util.ArrayList;
import java.util.List;

/**
 * ���������˺����ж԰�ȫ���еĲ����������ò���������Լ����͡�
 * 
 * @author ��ƽ��
 *
 */
public class SensitiveInfo
{
	/**
	 * ���в��������
	 */
	private List sensitiveSeqs;
	
	/**
	 * ��Ӧ�Ĳ�������
	 */
	private List sensitiveTypes;
	
	/**
	 * ��ע��Ϣ
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
