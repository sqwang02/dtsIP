/**
 * 
 */
package softtest.rules.java.safety.TD;

import java.util.ArrayList;
import java.util.List;

import org.jaxen.BaseXPath;
import org.jaxen.JaxenException;
import org.jaxen.XPath;

import softtest.ast.java.SimpleNode;
import softtest.jaxen.java.DocumentNavigator;

/**
 * @author 彭平雷
 * 
 * 为创建TD状态机实例提供支持
 * 比如： findTreeNodes()
 *
 */
public class TDCreatorTools
{
	/** 在节点node上查找xPath */
	public static List findTreeNodes(SimpleNode node, String xPath) {
		List evaluationResults = new ArrayList();
		try {
			XPath xpath = new BaseXPath(xPath, new DocumentNavigator());
			evaluationResults = xpath.selectNodes(node);
		} catch (JaxenException e) {
			// e.printStackTrace();
			throw new RuntimeException("xpath error",e);
		}
		return evaluationResults;
	}
}
