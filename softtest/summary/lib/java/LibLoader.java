package softtest.summary.lib.java;


import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import softtest.callgraph.java.method.AbstractPrecondition;
import softtest.callgraph.java.method.MapOfVariable;
import softtest.callgraph.java.method.MethodNode;
import softtest.callgraph.java.method.MethodSummary;
import softtest.callgraph.java.method.NpdPrecondition;
import softtest.domain.java.Domain;
import softtest.domain.java.ReferenceDomain;
import softtest.symboltable.java.ExpressionTypeFinder;
import softtest.symboltable.java.TypeSet;
import softtest.symboltable.java.VariableNameDeclaration;

/**
 * 
 * 从XML配置文件中加载库函数函数摘要信息
 * 
 * @author cjie
 *
 */
public class LibLoader {

	/**
	 * <p>加载XML描述文件中的库函数摘要信息</p>
	 * 
	 * @param path	库函数摘要描述文件路径
	 * @return 成功加载的库函数摘要集合
	 */
	public static void loadLibSummarys(String path) {
		DocumentBuilderFactory domfac = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder dombuilder = domfac.newDocumentBuilder();
			InputStream is = new FileInputStream(path);
			Document doc = dombuilder.parse(is);
			Element root = doc.getDocumentElement();
			if (root == null) {
				throw new RuntimeException("This is not a legal lib summary define file.");
			}
			NodeList nodes = root.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				if (node.getNodeName().equals("Method")) {
					loadMethodLibSummary(node);
				}
				if (node.getNodeName().equals("Constructor")) {
					loadConstructorLibSummary(node);
				}
			}
			
		} catch (Exception e) {
			//e.printStackTrace();
			//throw new RuntimeException("Errror in loading the lib methods summarys", e);
		}
	}
	/**
	 * 将String 类型转化为class
	 * @param className
	 * @return
	 * @throws ClassNotFoundException 
	 */
	private static Class loadClass(String className) {
		if(className != null && className.trim().length() !=0) {
			try {
				if("int".equalsIgnoreCase(className)) {
					return int.class;
				} else if("char".equalsIgnoreCase(className)) {
					return char.class;
				} else if("byte".equalsIgnoreCase(className)) {
					return byte.class;
				} else if("boolean".equalsIgnoreCase(className)) {
					return boolean.class;
				} else if("double".equalsIgnoreCase(className)) {
					return double.class;
				} else if("long".equalsIgnoreCase(className)) {
					return long.class;
				} else if("short".equalsIgnoreCase(className)) {
					return long.class;
				} else if("float".equalsIgnoreCase(className)) {
					return float.class;
				}  else if("[char".equalsIgnoreCase(className)) {
					return char[].class;
				} else if("[byte".equalsIgnoreCase(className)) {
					return byte[].class;
				} else {
					return Class.forName(className);
				}
			} catch (Exception e) {
				//System.out.println("Class Name:" + className);
			}
		}
		return null;
	}
	/**
	 * 加载库函数摘要信息，包括前置约束，特征信息，返回值区间等
	 * @param libSet
	 * @param node
	 */
	public static void loadMethodLibSummary(Node node) {
		try {
			Node methodNameNode = node.getAttributes().getNamedItem("name");
			Node classNameNode = node.getAttributes().getNamedItem("className");
			Node paramsNode = node.getAttributes().getNamedItem("params");
			
			if (methodNameNode != null && methodNameNode != null && paramsNode != null) {
				TypeSet typeset=TypeSet.getCurrentTypeSet();
				Class<?> clazz = null;
				if (null != typeset){
					typeset.findClass(classNameNode.getNodeValue());
				}
				if  (null == clazz) {
					clazz = Class.forName(classNameNode.getNodeValue());
				}
				String[] para = paramsNode.getNodeValue().split(",");
				
				Class<?>[] params = new Class[para.length];
				if (paramsNode.getNodeValue() == null
						|| paramsNode.getNodeValue().trim().length() == 0) {
					 params = new Class[0];
				}
				for (int i=0; i< params.length; i++) {
					params[i] = loadClass(para[i]);
				}
				Method method = ExpressionTypeFinder.getMethodOfClass(methodNameNode.getNodeValue(), params, clazz);
				MethodNode methodNode = new MethodNode(method, classNameNode.getNodeValue(), -1);
				MethodSummary methodsummary = methodNode.getMethodsummary();
				if (null == methodsummary) {
					methodsummary = new MethodSummary();
					methodNode.setMethodsummary(methodsummary);
				}
				if (method == null) {
					return;
				}
				MethodNode.getMethodTable().put(method, methodNode);
						
				NodeList nodes = node.getChildNodes();
				for (int i = 0; i < nodes.getLength(); i++) {
					Node item = nodes.item(i);
					if (item.getNodeName().equals("Feature")) {
						Node typeNode = item.getAttributes().getNamedItem("type");
						if (typeNode != null) {
							if (typeNode.getNodeValue().equals("PRECOND_NPD")) {
								Node valueNode = item.getAttributes().getNamedItem("value");
								if (valueNode != null) {
									Integer value = Integer.valueOf(valueNode.getNodeValue());
								    MapOfVariable mapOfVariable = new MapOfVariable(new VariableNameDeclaration(null), value);
									Map<AbstractPrecondition, AbstractPrecondition> preconditions = methodsummary.getPreconditons().getTable();
									AbstractPrecondition precondition = null;
									for (AbstractPrecondition p : preconditions.keySet()) {
										if (p instanceof NpdPrecondition) {
											precondition = p;
										}
									}
									if (precondition == null) {
										precondition = new NpdPrecondition();
										methodsummary.getPreconditons().getTable().put(precondition, precondition);
									}
									List<String> lists = new ArrayList<String>();
									precondition.getTable().put(mapOfVariable, lists);
								}
							} 
						}
					} else if (item.getNodeName().equals("Return")) {
						Node typeNode = item.getAttributes().getNamedItem("type");
						Node valueNode = item.getAttributes().getNamedItem("value");
						if (typeNode != null && valueNode != null) {
							Domain retDomain = loadDomain(typeNode.getNodeValue(), valueNode.getNodeValue());
							if (retDomain != null) {
								methodNode.setDomain(retDomain);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			//throw new RuntimeException("This is illegal lib method summary node", e);
			//e.printStackTrace();
		}
	}
	/**
	 * 加载库构造函数摘要信息，包括前置约束，特征信息，返回值区间等
	 * @param libSet
	 * @param node
	 */
	public static void loadConstructorLibSummary(Node node) {
		try {
			Node methodNameNode = node.getAttributes().getNamedItem("name");
			Node classNameNode = node.getAttributes().getNamedItem("className");
			Node paramsNode = node.getAttributes().getNamedItem("params");
			
			if (methodNameNode != null && methodNameNode != null && paramsNode != null) {
				TypeSet typeset=TypeSet.getCurrentTypeSet();
				Class<?> clazz = null;
				if (null != typeset){
					typeset.findClass(classNameNode.getNodeValue());
				}
				if  (null == clazz) {
					clazz = Class.forName(classNameNode.getNodeValue());
				}
				String[] para = paramsNode.getNodeValue().split(",");
				
				Class<?>[] params = new Class[para.length];
				if (paramsNode.getNodeValue() == null
						|| paramsNode.getNodeValue().trim().length() == 0) {
					 params = new Class[0];
				}
				for (int i=0; i< params.length; i++) {
					params[i] = loadClass(para[i]);
				}
				Constructor<?> constructor = ExpressionTypeFinder.getConstructorOfClass(params, clazz);
				MethodNode methodNode = new MethodNode(constructor, classNameNode.getNodeValue(), -1);
				MethodSummary methodsummary = methodNode.getMethodsummary();
				if (null == methodsummary) {
					methodsummary = new MethodSummary();
					methodNode.setMethodsummary(methodsummary);
				}
				if (constructor == null) {
					return;
				}
				MethodNode.getMethodTable().put(constructor, methodNode);
						
				NodeList nodes = node.getChildNodes();
				for (int i = 0; i < nodes.getLength(); i++) {
					Node item = nodes.item(i);
					if (item.getNodeName().equals("Feature")) {
						Node typeNode = item.getAttributes().getNamedItem("type");
						if (typeNode != null) {
							if (typeNode.getNodeValue().equals("PRECOND_NPD")) {
								Node valueNode = item.getAttributes().getNamedItem("value");
								if (valueNode != null) {
									Integer value = Integer.valueOf(valueNode.getNodeValue());
								    MapOfVariable mapOfVariable = new MapOfVariable(new VariableNameDeclaration(null), value);
									Map<AbstractPrecondition, AbstractPrecondition> preconditions = methodsummary.getPreconditons().getTable();
									AbstractPrecondition precondition = null;
									for (AbstractPrecondition p : preconditions.keySet()) {
										if (p instanceof NpdPrecondition) {
											precondition = p;
										}
									}
									if (precondition == null) {
										precondition = new NpdPrecondition();
										methodsummary.getPreconditons().getTable().put(precondition, precondition);
									}
									List<String> lists = new ArrayList<String>();
									precondition.getTable().put(mapOfVariable, lists);
								}
							} 
						}
					}
				}
			}
		} catch (Exception e) {
			//throw new RuntimeException("This is illegal lib method summary node", e);
			e.printStackTrace();
		}
	}
	/**
	 * 加载库函数返回值区间
	 * @param type
	 * @param value
	 * @return
	 */
	public static Domain loadDomain(String type, String value) {
		if (type.equalsIgnoreCase("Ref")) {
			return ReferenceDomain.valueOf(value);
		}
		return null;
	}
	
	public static void main(String[] args) {
		loadLibSummarys("javalib/npd_summary.xml");
		Hashtable<Object,MethodNode> methods = MethodNode.getMethodTable();
		System.out.println(methods.size());
		for (Map.Entry<Object, MethodNode> entry : methods.entrySet()) {
			System.out.println(entry.getKey());
			System.out.println(entry.getValue());
		}
	}
}
