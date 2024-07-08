/**
 * 
 */
package softtest.rules.java.safety.TD;


import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jaxen.BaseXPath;
import org.jaxen.JaxenException;
import org.jaxen.XPath;

import softtest.IntervalAnalysis.java.DomainSet;
import softtest.ast.java.ASTLiteral;
import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTPrimaryExpression;
import softtest.ast.java.ASTPrimarySuffix;
import softtest.ast.java.SimpleNode;
import softtest.cfg.java.VexNode;
import softtest.fsm.java.FSMMachineInstance;
import softtest.jaxen.java.DocumentNavigator;
import softtest.symboltable.java.MethodNameDeclaration;
import softtest.symboltable.java.VariableNameDeclaration;

/**
 * @author limppa
 *
 */
public class PreviousNonFunctionSensitiveChecker implements INonFunctionSensitiveChecker
{
	private static String[] INDEX_FUNCTION = { "get", "setInt" };

	private static String addIndexFunction(StringBuffer buffer) {
		
		for (String s : INDEX_FUNCTION) {
			buffer.append("(" + s + ")|(.+\\." + s + ")|");
		}
		if (INDEX_FUNCTION.length > 0) {
			buffer.deleteCharAt(buffer.length() - 1);
		}
		return buffer.toString();
	}

	private static String[] STRING_USE_FUNCTION = { "getenv","getProperty","loadLibrary","mapLibraryName",
		"executeQuery", "setString", "setProperty", "createTempFile", "exec", "log", "setAttribute", "sendRedirect", };

	private static String addStringUseFunction(StringBuffer buffer) {
		for (String s : STRING_USE_FUNCTION) {
			buffer.append("(" + s + ")|(.+\\." + s + ")|");
		}
		if (STRING_USE_FUNCTION.length > 0) {
			buffer.deleteCharAt(buffer.length() - 1);
		}
		return buffer.toString();
	}
	
	/**
	 * add by ppl
	 */
	private static String[] FILE_USE_FUNCTION = {"mkdirs","mkdir"};
	
	private static String addFileUseFunction(StringBuffer buffer) {
		for (String s : FILE_USE_FUNCTION) {
			buffer.append("(" + s + ")|(.+\\." + s + ")|");
		}
		if (FILE_USE_FUNCTION.length > 0) {
			buffer.deleteCharAt(buffer.length() - 1);
		}
		return buffer.toString();
	}	
	
	

	private static String[] STRING_USE_CONSTRUCTOR = { "", };

	private static String addStringUseConstructor(StringBuffer buffer) {
		
		
		for (String s : STRING_USE_CONSTRUCTOR) {
			buffer.append("(" + s + ")|(.+\\." + s + ")|");
		}
		if (STRING_USE_CONSTRUCTOR.length > 0) {
			buffer.deleteCharAt(buffer.length() - 1);
		}
		return buffer.toString();
	}
	
	private static String[] STRING_ARRAY_USE_FUNCTION = { "exec", };

	private static String addStringArrayUseFunction(StringBuffer buffer) {
		for (String s : STRING_ARRAY_USE_FUNCTION) {
			buffer.append("(" + s + ")|(.+\\." + s + ")|");
		}
		if (STRING_ARRAY_USE_FUNCTION.length > 0) {
			buffer.deleteCharAt(buffer.length() - 1);
		}
		return buffer.toString();
	}

	//@Override
	public boolean checkUsed(VexNode n, FSMMachineInstance fsmin)
	{
		String xpath = "";
		StringBuffer buffer = null;
		List list = null;
		Iterator i = null;
		if (n.isBackNode()) {
			return false;
		}
		
		SimpleNode treenode=n.getTreeNode().getConcreteNode();
		if(treenode==null){
			return false;
		}
		
		Hashtable<VariableNameDeclaration, VariableNameDeclaration> table = ((TaintedSet)fsmin.getRelatedObject()).getTable();
		
		for (Enumeration<VariableNameDeclaration> e = table.elements(); e.hasMoreElements();) {
			VariableNameDeclaration v = e.nextElement();
			switch (DomainSet.getDomainType(v.getDomain())) {
			case INT:
				/**
				 * 形如： list.get(v);
				 */
				buffer = new StringBuffer(".//PrimaryExpression[./PrimaryPrefix/Name[matches(@Image,\'^(");
				xpath = addIndexFunction(buffer);
				xpath += ")$\')]]/PrimarySuffix[@Arguments=\'true\']//Name";
				list = findTreeNodes(treenode, xpath);
				i = list.iterator();
				while (i.hasNext()) {
					ASTName name = (ASTName) i.next();
					if (v == name.getNameDeclaration()) {
						fsmin.setResultString(v.getImage());
						return true;
					}
				}
				
				/**
				 * 形如： f().get(v);
				 */
				buffer = new StringBuffer(".//PrimaryExpression[./PrimarySuffix[@Arguments=\'false\' and matches(@Image,\'^(");
				xpath = addIndexFunction(buffer);
				xpath += ")$\')]]/PrimarySuffix[@Arguments=\'true\']//Name";
				list = findTreeNodes(treenode, xpath);
				i = list.iterator();
				while (i.hasNext()) {
					ASTName name = (ASTName) i.next();
					if (v == name.getNameDeclaration()) {
						fsmin.setResultString(v.getImage());
						return true;
					}
				}
				
				/**
				 * 形如： a[v];
				 */
				xpath = ".//PrimarySuffix[@ArrayDereference=\'true\']//Name";
				list = findTreeNodes(treenode, xpath);
				i = list.iterator();
				while (i.hasNext()) {
					ASTName name = (ASTName) i.next();
					if (v == name.getNameDeclaration()) {
						fsmin.setResultString(v.getImage());
						return true;
					}
				}
				
				/**
				 *  形如： int a[]=new int[v];
				 * */
				xpath = ".//ArrayDimsAndInits//Name";
				list = findTreeNodes(treenode, xpath);
				i = list.iterator();
				while (i.hasNext()) {
					ASTName name = (ASTName) i.next();
					if (v == name.getNameDeclaration()) {
						fsmin.setResultString(v.getImage());
						return true;
					}
				}
				
				/**
				 * add by ppl
				 * 
				 * 形如： out = input + 150
				 * 
				 */
				xpath = ".//AdditiveExpression//Name | .//AdditiveExpression//Literal";
				list = findTreeNodes(treenode,xpath);
				i = list.iterator();
				while(i.hasNext()) 
				{
					Object obj = i.next();
					String typeString = null;
					if(obj instanceof ASTName)
					{
						ASTName name = (ASTName) obj;
						typeString = name.getTypeString();
					}
					else if(obj instanceof ASTLiteral)
					{
						ASTLiteral literal = (ASTLiteral)obj;
						typeString = literal.getTypeString();
					}
					
					if(typeString == null)
					{
						return false;
					}
					
					if(!(typeString.equalsIgnoreCase("int")))
					{
						return false;
					}
				}
				
				xpath = ".//AdditiveExpression//Name";
				list = findTreeNodes(treenode,xpath);
				i = list.iterator();
				
				while (i.hasNext()) {
					ASTName name = (ASTName) i.next();
					if (v == name.getNameDeclaration()) {
						fsmin.setResultString(v.getImage());
						return true;
					}
				}
				
				/**
				 * add by ppl
				 * 
				 * 形如： out = input * 150
				 * 
				 */
				xpath = ".//MultiplicativeExpression//Name | .//MultiplicativeExpression//Literal";
				list = findTreeNodes(treenode,xpath);
				i = list.iterator();
				while(i.hasNext()) 
				{
					Object obj = i.next();
					String typeString = null;
					if(obj instanceof ASTName)
					{
						ASTName name = (ASTName) obj;
						typeString = name.getTypeString();
					}
					else if(obj instanceof ASTLiteral)
					{
						ASTLiteral literal = (ASTLiteral)obj;
						typeString = literal.getTypeString();
					}
					
					if(typeString == null)
					{
						return false;
					}
					
					if(!(typeString.equalsIgnoreCase("int")))
					{
						return false;
					}
				}
				
				xpath = ".//MultiplicativeExpression//Name";
				list = findTreeNodes(treenode,xpath);
				i = list.iterator();
				
				while (i.hasNext()) {
					ASTName name = (ASTName) i.next();
					if (v == name.getNameDeclaration()) {
						fsmin.setResultString(v.getImage());
						return true;
					}
				}
				
				/**
				 * add by ppl
				 * 
				 * 形如： input++ 或者 input--
				 * 
				 */
				xpath = ".//PostfixExpression//Name";
				list = findTreeNodes(treenode,xpath);
				i = list.iterator();
				
				while (i.hasNext()) {
					ASTName name = (ASTName) i.next();
					if (v == name.getNameDeclaration()) {
						fsmin.setResultString(v.getImage());
						return true;
					}
				}
				
				/**
				 * add by ppl
				 * 
				 * 形如： ++input 或者 --input
				 * 
				 */
				xpath = ".//PreIncrementExpression//Name | .//PreDecrementExpression//Name";
				list = findTreeNodes(treenode,xpath);
				i = list.iterator();
				
				while (i.hasNext()) {
					ASTName name = (ASTName) i.next();
					if (v == name.getNameDeclaration()) {
						fsmin.setResultString(v.getImage());
						return true;
					}
				}
				
				break;
				
			case DOUBLE:
				/**
				 * add by ppl
				 * 
				 * 形如： out = input + 150
				 * 
				 */
				xpath = ".//AdditiveExpression//Name | .//AdditiveExpression//Literal";
				list = findTreeNodes(treenode,xpath);
				i = list.iterator();
				while(i.hasNext()) 
				{
					Object obj = i.next();
					String typeString = null;
					if(obj instanceof ASTName)
					{
						ASTName name = (ASTName) obj;
						typeString = name.getTypeString();
					}
					else if(obj instanceof ASTLiteral)
					{
						ASTLiteral literal = (ASTLiteral)obj;
						typeString = literal.getTypeString();
					}
					
					if(typeString == null)
					{
						return false;
					}
					
					if(!(typeString.equalsIgnoreCase("double")))
					{
						return false;
					}
				}
				
				xpath = ".//AdditiveExpression//Name";
				list = findTreeNodes(treenode,xpath);
				i = list.iterator();
				
				while (i.hasNext()) {
					ASTName name = (ASTName) i.next();
					if (v == name.getNameDeclaration()) {
						fsmin.setResultString(v.getImage());
						return true;
					}
				}
				
				/**
				 * add by ppl
				 * 
				 * 形如： out = input * 150
				 * 
				 */
				xpath = ".//MultiplicativeExpression//Name | .//MultiplicativeExpression//Literal";
				list = findTreeNodes(treenode,xpath);
				i = list.iterator();
				while(i.hasNext()) 
				{
					Object obj = i.next();
					String typeString = null;
					if(obj instanceof ASTName)
					{
						ASTName name = (ASTName) obj;
						typeString = name.getTypeString();
					}
					else if(obj instanceof ASTLiteral)
					{
						ASTLiteral literal = (ASTLiteral)obj;
						typeString = literal.getTypeString();
					}
					
					if(typeString == null)
					{
						return false;
					}
					
					if(!(typeString.equalsIgnoreCase("double")))
					{
						return false;
					}
				}
				
				xpath = ".//MultiplicativeExpression//Name";
				list = findTreeNodes(treenode,xpath);
				i = list.iterator();
				
				while (i.hasNext()) {
					ASTName name = (ASTName) i.next();
					if (v == name.getNameDeclaration()) {
						fsmin.setResultString(v.getImage());
						return true;
					}
				}
				break;
			
			case REF:
				/**
				 * 形如： new ServletException(v);
				 */
//				xpath = ".//AllocationExpression[./ClassOrInterfaceType[matches(@Image,\'^(.*Exception)$\')]]/Arguments//Name";
//				list = findTreeNodes(treenode, xpath);
//				i = list.iterator();
//				while (i.hasNext()) {
//					ASTName name = (ASTName) i.next();
//					if (v == name.getNameDeclaration()) {
//						fsmin.setResultString(v.getImage());
//						return true;
//					}
//				}

				/**
				 * 形如： stmt.executeQuery(v);
				 */
				buffer = new StringBuffer(".//PrimaryExpression[./PrimaryPrefix/Name[matches(@Image,\'^(");
				xpath = addStringUseFunction(buffer);
				xpath += ")$\')]]/PrimarySuffix[@Arguments=\'true\']//Name";
				list = findTreeNodes(treenode, xpath);
				i = list.iterator();
				while (i.hasNext()) {
					ASTName name = (ASTName) i.next();
					if (v == name.getNameDeclaration()) {
						fsmin.setResultString(v.getImage());
						return true;
					}
				}
				
				/**
				 * add by ppl
				 * 
				 * 形如： 
				 * File dir = ...;
				 * dir.mkdirs();
				 */
				buffer = new StringBuffer(".//PrimaryExpression[./PrimaryPrefix/Name[matches(@Image,\'^(");
				xpath = addFileUseFunction(buffer);
				xpath += ")$\') and @MethodName='true'] and ./PrimarySuffix/Arguments[count(*) = 0]]/PrimaryPrefix/Name";
				list = findTreeNodes(treenode, xpath);
				i = list.iterator();
				while (i.hasNext()) {
					ASTName name = (ASTName) i.next();
					Set varDels = table.keySet();
					Iterator iter = varDels.iterator();
					
					while(iter.hasNext())
					{
						v = (VariableNameDeclaration)iter.next();
						if(v == name.getNameDeclaration())
						{
							fsmin.setResultString(v.getImage());
							return true;
						}
					}
				}
				
				/**
				 * 形如： f().executeQuery(v);
				 */
				buffer = new StringBuffer(".//PrimaryExpression[./PrimarySuffix[@Arguments=\'false\' and matches(@Image,\'^(");
				xpath = addStringUseFunction(buffer);
				xpath += ")$\')]]/PrimarySuffix[@Arguments=\'true\']//Name";
				list = findTreeNodes(treenode, xpath);
				i = list.iterator();
				while (i.hasNext()) {
					ASTName name = (ASTName) i.next();
					if (v == name.getNameDeclaration()) {
						fsmin.setResultString(v.getImage());
						return true;
					}
				}

				/**
				 * 形如： private native void veryOptimalNativeProcess(String
				 * name); ...... veryOptimalNativeProcess(v);
				 */
				xpath = ".//PrimaryExpression[./PrimaryPrefix/Name]/PrimarySuffix[@Arguments=\'true\']//Name";
				list = findTreeNodes(treenode, xpath);
				i = list.iterator();
				while (i.hasNext()) {
					ASTName name = (ASTName) i.next();
					if (v != name.getNameDeclaration()) {
						continue;
					}
					ASTPrimarySuffix suffix = (ASTPrimarySuffix) name.getFirstParentOfType(ASTPrimarySuffix.class);
					if (suffix == null || !suffix.isArguments() || !(suffix.jjtGetParent() instanceof ASTPrimaryExpression)
							|| !(suffix.jjtGetParent().jjtGetChild(0).jjtGetChild(0) instanceof ASTName)) {
						continue;
					}
					ASTName fun = (ASTName) suffix.jjtGetParent().jjtGetChild(0).jjtGetChild(0);
					if (fun.getNameDeclaration() instanceof MethodNameDeclaration) {
						MethodNameDeclaration decl = (MethodNameDeclaration) fun.getNameDeclaration();
						ASTMethodDeclaration m = (ASTMethodDeclaration) decl.getMethodNameDeclaratorNode().jjtGetParent();
						if (m.isNative()) {
							fsmin.setResultString(v.getImage());
							return true;
						}
					}
				}

				/**
				 * 形如： new InternetAddress(v);
				 */
				buffer = new StringBuffer(".//AllocationExpression[./ClassOrInterfaceType[matches(@Image,\'^(");
				xpath = addStringUseConstructor(buffer);
				xpath += ")$\')]]//Arguments//Name";
				list = findTreeNodes(treenode, xpath);
				i = list.iterator();
				while (i.hasNext()) {
					ASTName name = (ASTName) i.next();
					if (v == name.getNameDeclaration()) {
						fsmin.setResultString(v.getImage());
						return true;
					}
				}

				break;
			case ARRAY:
				/**
				 * 形如： stmt.exec(a,v);
				 */
				buffer = new StringBuffer(".//PrimaryExpression[./PrimaryPrefix/Name[matches(@Image,\'^(");
				xpath = addStringArrayUseFunction(buffer);
				xpath += ")$\')]]/PrimarySuffix[@Arguments=\'true\']//Name";
				list = findTreeNodes(treenode, xpath);
				i = list.iterator();
				while (i.hasNext()) {
					ASTName name = (ASTName) i.next();
					if (v == name.getNameDeclaration()) {
						fsmin.setResultString(v.getImage());
						return true;
					}
				}
				
				/**
				 * 形如： f().exec(a,v);
				 */
				buffer = new StringBuffer(".//PrimaryExpression[./PrimarySuffix[@Arguments=\'false\' and matches(@Image,\'^(");
				xpath =addStringArrayUseFunction(buffer);
				xpath += ")$\')]]/PrimarySuffix[@Arguments=\'true\']//Name";
				list = findTreeNodes(treenode, xpath);
				i = list.iterator();
				while (i.hasNext()) {
					ASTName name = (ASTName) i.next();
					if (v == name.getNameDeclaration()) {
						fsmin.setResultString(v.getImage());
						return true;
					}
				}
				break;
			}
		}
		return false;
	}
	
	private static List findTreeNodes(SimpleNode node, String xPath) {
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
