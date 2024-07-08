/**
 * 
 */
package softtest.rules.java.safety.AAPI;

import java.io.IOException;
import java.util.Formatter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import softtest.ast.java.ASTPrimaryPrefix;
import softtest.ast.java.ASTPrimarySuffix;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;
import softtest.rules.java.AbstractStateMachine;

/**
 * @author pengpinglei
 *
 */
public class AbuseAPI_DangerousMethodStateMachine extends AbstractStateMachine
{
	private static int count = 0;
	
	private static String jdkLibVersion = "1.6";
	
	private static HashSet<String> dangerMethods = null;
	
	
	/**
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("����APIģʽ:�� %d ��ʹ����Σ�յķ������������һ��©����", errorline);
		}else{
			f.format("Abuse Application Program Interface: Use a dangerous method on line %d.",errorline);
		}
		fsmmi.setDescription(f.toString());
		f.close();
	}
	@Override
	public  void registerPrecondition(PreconditionListenerSet listeners) {
	}
	
	@Override
	public  void registerFeature(FeatureListenerSet listenerSet) {
	}
	/**
	 * Σ�պ�����
	 * String :: toString()
	 * System.gc()
	 * Runtime.getRuntime().gc()
	 * ���л�API -- jdk1.4
	 * У��API -- jdk1.4
	 * 
	 * @param node
	 * @param fsm
	 * @return List<FSMMachineInstance>
	 */
	public static List<FSMMachineInstance> createDangerousMethodStateMachine(SimpleJavaNode node, FSMMachine fsm)
	{
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		
		/**
		 * ͬ����������ȫ
		 */
		synchronized(AbuseAPI_DangerousMethodStateMachine.class)
		{
		if (count == 0)
			{
				/*
				 * 1 ��ȡ�û���lib��������Ϣ
				 */
				UserConfig cfg = new UserConfig();
				String jdklibv = cfg.getJDKlibVersion();
				jdkLibVersion = jdklibv;
				// System.out.println("�û���JDK�汾Ϊ�� " + jdkLibVersion);

				/*
				 * 2 ����JDK�汾����Σ�պ�����ԭ�ʹ����HashSet��
				 */
				DangerMethodConfig dmCfg = new DangerMethodConfig(jdkLibVersion);
				dangerMethods = dmCfg.getDangerMethods();
				if (dangerMethods != null)
				{
					Iterator iter = dangerMethods.iterator();
					while (iter.hasNext())
					{
						String s = (String) iter.next();
					}
				}

				count = 1;

			}
		}
		/**
		 * 3 �﷨ƥ�䣬����״̬��ʵ��
		 */
		
		StringBuffer buffer = new StringBuffer();
		String xpath="";
		List evalRlts = null;
		Iterator i = null;
		
		/**
		 * 3.1 obj.func();
		 */
		buffer = new StringBuffer(".//PrimaryExpression/PrimaryPrefix[");
		xpath = addDangerMethod(buffer);
		xpath += "]";
		
		evalRlts = node.findXpath(xpath);
		
		i=evalRlts.iterator();
		while(i.hasNext()){
			ASTPrimaryPrefix astPrimExpr = ( ASTPrimaryPrefix )i.next();
			FSMMachineInstance fsmInst = fsm.creatInstance();
			fsmInst.setRelatedObject(new FSMRelatedCalculation(astPrimExpr));
			fsmInst.setResultString("dangerous method : " + astPrimExpr.getTypeString());
			list.add(fsmInst);
		}
		
		/**
		 * 3.2 obj.f().g();
		 */
		buffer = new StringBuffer(".//PrimaryExpression/PrimarySuffix[");
		xpath = addDangerMethod(buffer);
		xpath += "]";
		
		evalRlts = node.findXpath(xpath);
		
		i=evalRlts.iterator();
		while(i.hasNext()){
			ASTPrimarySuffix astPrimExpr = ( ASTPrimarySuffix )i.next();
			FSMMachineInstance fsmInst = fsm.creatInstance();
			fsmInst.setRelatedObject(new FSMRelatedCalculation(astPrimExpr));
			fsmInst.setResultString("dangerous method : " + astPrimExpr.getTypeString());
			list.add(fsmInst);
		}
		
		return list;
	}

	private static String addDangerMethod(StringBuffer buffer) {
		
		Iterator iter = dangerMethods.iterator();
		
		while(iter.hasNext())
		{
			String s = (String) iter.next();
			buffer.append("@TypeString= \"");
			buffer.append(s + "\" | ");
		}
		int t = buffer.lastIndexOf("|");
		return buffer.substring(0, t);
	}
	
}

class UserConfig
{
	/**
	 * 
	 */
	private String userConfigPath = "./cfg/user/userConfig.xml";

	/**
	 * jdklib��汾
	 */
	private String jdklibVersion = "1.6";
	
	/**
	 * �Ƿ�ΪJ2EE����
	 */
	private boolean isJ2EEprogram = false;
	
	/**
	 * �Ƿ�ΪJ2EE����
	 */
	private boolean isEJBprogram = false;
	
	
	public UserConfig()
	{
	}
	
	public String getJDKlibVersion()
	{	
		try
		{
			/*
			 * ��ʼ����
			 */
			DocumentBuilderFactory domFactory = DocumentBuilderFactory
					.newInstance();
			domFactory.setNamespaceAware(true);
			DocumentBuilder builder;
			builder = domFactory.newDocumentBuilder();
			Document doc = builder.parse(userConfigPath);
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();

			/*
			 * ����JDK��汾
			 */
			XPathExpression expr = xpath.compile("//jdkLibVersion");
			Object result = expr.evaluate(doc, XPathConstants.NODESET);
			NodeList nodes = (NodeList) result;
        
			for(int i=0; i<nodes.getLength(); i++)
			{
				Node node = nodes.item(i);
				
				String s = node.getTextContent();
				if(s.length() == 0)
				{
					return jdklibVersion;
				}
				jdklibVersion = s;
			}
        
		}
		catch (ParserConfigurationException e)
		{
			e.printStackTrace();
		} 
		catch (SAXException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		} 
		catch (XPathExpressionException e)
		{
			e.printStackTrace();
		}
		
		return jdklibVersion;
	}
	
	public boolean isJ2EEprogram()
	{
		try
		{
			/*
			 * ��ʼ����
			 */
			DocumentBuilderFactory domFactory = DocumentBuilderFactory
					.newInstance();
			domFactory.setNamespaceAware(true);
			DocumentBuilder builder;
			builder = domFactory.newDocumentBuilder();
			Document doc = builder.parse(userConfigPath);
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();

			/*
			 * ����
			 */
			XPathExpression expr = xpath.compile("//isJ2EEprogram");
			Object result = expr.evaluate(doc, XPathConstants.NODESET);
			NodeList nodes = (NodeList) result;
        
			for(int i=0; i<nodes.getLength(); i++)
			{
				Node node = nodes.item(i);
				
				String s = node.getTextContent();
				if(s.length() == 0)
				{
					return isJ2EEprogram;
				}
				if(s.equalsIgnoreCase("true"))
				{
					isJ2EEprogram = true;
					return true;
				}
				else
				{
					isJ2EEprogram = false;
					return false;
				}
			}
        
		}
		catch (ParserConfigurationException e)
		{
			e.printStackTrace();
		} 
		catch (SAXException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		} 
		catch (XPathExpressionException e)
		{
			e.printStackTrace();
		}
		
		return isJ2EEprogram;
	}
	
	
	public boolean isEJBprogram()
	{
		try
		{
			/*
			 * ��ʼ����
			 */
			DocumentBuilderFactory domFactory = DocumentBuilderFactory
					.newInstance();
			domFactory.setNamespaceAware(true);
			DocumentBuilder builder;
			builder = domFactory.newDocumentBuilder();
			Document doc = builder.parse(userConfigPath);
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();

			/*
			 * ����
			 */
			XPathExpression expr = xpath.compile("//isEJBprogram");
			Object result = expr.evaluate(doc, XPathConstants.NODESET);
			NodeList nodes = (NodeList) result;
        
			for(int i=0; i<nodes.getLength(); i++)
			{
				Node node = nodes.item(i);
				
				String s = node.getTextContent();
				if(s.length() == 0)
				{
					return isEJBprogram;
				}
				if(s.equalsIgnoreCase("true"))
				{
					isEJBprogram = true;
					return true;
				}
				else
				{
					isEJBprogram = false;
					return false;
				}
			}
        
		}
		catch (ParserConfigurationException e)
		{
			e.printStackTrace();
		} 
		catch (SAXException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		} 
		catch (XPathExpressionException e)
		{
			e.printStackTrace();
		}
		
		return isEJBprogram;
	}
}

class DangerMethodConfig
{
	private String dangerMethodPath = "./cfg/aapi/dangerousMethod/dangerousMethods.xml";
	
	private HashSet<String> dangerMethods = new HashSet<String>();
	
	private String version = "1.6";
	
	public DangerMethodConfig(String version)
	{
		this.version = version;
	}
	
	public HashSet<String> getDangerMethods()
	{
		try
		{
			/*
			 * ��ʼ����
			 */
			DocumentBuilderFactory domFactory = DocumentBuilderFactory
					.newInstance();
			domFactory.setNamespaceAware(true);
			DocumentBuilder builder;
			builder = domFactory.newDocumentBuilder();
			Document doc = builder.parse(dangerMethodPath);
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();

			/*
			 * ��ȡΣ�պ����б�
			 */
			String v = "//" + "v" + version.trim();
			XPathExpression expr = xpath.compile(v);
			Object result = expr.evaluate(doc, XPathConstants.NODESET);
			NodeList nodes = (NodeList) result;
			
			for(int i=0; i<nodes.getLength(); i++)
			{
				Node node = nodes.item(i);
				
				NodeList methodList = node.getChildNodes();
								
				for(int j=0; j<methodList.getLength(); j++)
				{
					Node method = methodList.item(j);
					
					if(method.getNodeName().equalsIgnoreCase("method"))
					{
						for(int k=0; k<method.getAttributes().getLength(); k++)
						{
							Node attr = method.getAttributes().item(k);
							
							if(attr.getNodeName().equalsIgnoreCase("signature"))
							{
								dangerMethods.add(attr.getNodeValue());
							}
						}
					}
				}
			}
        
		}
		catch (ParserConfigurationException e)
		{
			e.printStackTrace();
		} 
		catch (SAXException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		} 
		catch (XPathExpressionException e)
		{
			e.printStackTrace();
		}
		
		return dangerMethods;
	}
	
	
}
