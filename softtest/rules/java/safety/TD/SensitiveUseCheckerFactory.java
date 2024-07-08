/**
 * 
 */
package softtest.rules.java.safety.TD;

/**
 * @author ��ƽ��
 * 
 * ����������ʹ�ü���߼���
 *
 */
public class SensitiveUseCheckerFactory
{
	public SensitiveUseChecker createSensitiveUseChecker()
	{
		/*
		 * 1 ��������ʹ���߼���
		 */
		SensitiveUseChecker checkers = new SensitiveUseChecker();
		
		/*
		 * 2 ����һ�ֺ����������͵����м���� e.g: f(),g(String) ; o.f(), o.g(String);
 		 */
		PlainFunctionInvokationSensitiveChecker pfisChecker = new PlainFunctionInvokationSensitiveChecker();
		checkers.getSensitiveCheckers().add(pfisChecker);
		
		/*
		 * 3 ����һ�ֺ����������͵����м���� e.g: h().f(),h().g(String) ; o.h().f(), o.h().g(String);
 		 */		
		ChainFunctionInvokationSensitiveChecker cfisChecker = new ChainFunctionInvokationSensitiveChecker();
		checkers.getSensitiveCheckers().add(cfisChecker);
		
		/*
		 * 4 ����һ�ֺ����������͵����м���� e.g:new FileInputStream(String);
		 */
		PlainConstructorSensitiveChecker pcsChecker = new PlainConstructorSensitiveChecker();
		checkers.getSensitiveCheckers().add(pcsChecker);
		
		/*
		 * 5 ����һ�ֺ����������͵����м���� e.g:arr[i];
		 */
		PreviousNonFunctionSensitiveChecker pfsChecker = new PreviousNonFunctionSensitiveChecker();
		checkers.getSensitiveCheckers().add(pfsChecker);
		
		return checkers;
	}
}
