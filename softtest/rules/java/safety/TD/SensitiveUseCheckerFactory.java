/**
 * 
 */
package softtest.rules.java.safety.TD;

/**
 * @author 彭平雷
 * 
 * 创建了敏感使用检测者集合
 *
 */
public class SensitiveUseCheckerFactory
{
	public SensitiveUseChecker createSensitiveUseChecker()
	{
		/*
		 * 1 创建敏感使用者集合
		 */
		SensitiveUseChecker checkers = new SensitiveUseChecker();
		
		/*
		 * 2 创建一种函数调用类型的敏感检测者 e.g: f(),g(String) ; o.f(), o.g(String);
 		 */
		PlainFunctionInvokationSensitiveChecker pfisChecker = new PlainFunctionInvokationSensitiveChecker();
		checkers.getSensitiveCheckers().add(pfisChecker);
		
		/*
		 * 3 创建一种函数调用类型的敏感检测者 e.g: h().f(),h().g(String) ; o.h().f(), o.h().g(String);
 		 */		
		ChainFunctionInvokationSensitiveChecker cfisChecker = new ChainFunctionInvokationSensitiveChecker();
		checkers.getSensitiveCheckers().add(cfisChecker);
		
		/*
		 * 4 创建一种函数调用类型的敏感检测者 e.g:new FileInputStream(String);
		 */
		PlainConstructorSensitiveChecker pcsChecker = new PlainConstructorSensitiveChecker();
		checkers.getSensitiveCheckers().add(pcsChecker);
		
		/*
		 * 5 创建一种函数调用类型的敏感检测者 e.g:arr[i];
		 */
		PreviousNonFunctionSensitiveChecker pfsChecker = new PreviousNonFunctionSensitiveChecker();
		checkers.getSensitiveCheckers().add(pfsChecker);
		
		return checkers;
	}
}
