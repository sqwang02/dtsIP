/**
 * 
 */
package softtest.rules.java.safety.TD;

import softtest.cfg.java.VexNode;
import softtest.fsm.java.FSMMachineInstance;

/**
 * @author ��ƽ��
 *
 */
public interface ISensitiveChecker
{
	public boolean checkUsed(VexNode n,FSMMachineInstance fsmin);
}
