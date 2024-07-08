/**
 * 
 */
package softtest.summary.lib.java;

import java.io.IOException;
import java.util.Hashtable;

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

/**
 * ������Ҫ������TD��صĿ⺯����ժҪ������
 * 
 * @author ��ƽ��
 *
 */
public class TDLibParser
{
	/**
	 * ��ڹ����ĵ���ַ
	 */
	private String inputLocation;
	
	/**
	 * �������ĵ���ַ
	 */
	private String sinkLocation;
	
	/**
	 * ���������ĵ���ַ
	 */
	private String filterLocation;

	public TDLibParser(String input, String sink, String filter)
	{
		this.inputLocation = input;
		this.sinkLocation = sink;
		this.filterLocation = filter;
	}
	
	/**
	 * ����ڹ�����н���
	 * 
	 * @return Hashtable
	 */
	public Hashtable<String,TaintedInfo> parseInputReg()
	{
		
		Hashtable<String,TaintedInfo> table = new Hashtable<String,TaintedInfo>();
		
		try
		{
			/*
			 * ��ʼ����
			 */
			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
	        domFactory.setNamespaceAware(true); 
	        DocumentBuilder builder;
			builder = domFactory.newDocumentBuilder();
			Document doc = builder.parse(inputLocation);
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			
			/*
			 * ����������
			 */
			XPathExpression expr = xpath.compile("//method");
			Object result = expr.evaluate(doc, XPathConstants.NODESET);
	        NodeList nodes = (NodeList) result;
	        
	        for(int i=0; i<nodes.getLength(); i++)
	        {
	        	String sigInfo = null;
	        	
	        	Node nodei = nodes.item(i);
	        	
	        	//�����ڵ�i�ĺ�����������:public java.lang.String java.io.DataInputStream.readLine()
	        	for(int j=0; j<nodei.getAttributes().getLength(); j++)
	        	{
	        		Node nodej = nodei.getAttributes().item(j);
	        		
	        		if(nodej.getNodeName().equalsIgnoreCase("signature"))
	        		{
	        			sigInfo = nodej.getNodeValue();
	        			//System.out.println("sigInfo = " + sigInfo);
	        		}
	        	}
	        	
	        	//�����ڵ�i����ȾԴ����
	        	TaintedInfo taintedInfo = new TaintedInfo();
	        	NodeList nodeks = nodei.getChildNodes();
	        	
	        	for(int k=0; k<nodeks.getLength(); k++)
	        	{
	        		Node nodek = nodeks.item(k);
	        		if(nodek.getNodeName().equalsIgnoreCase("taintedParam"))
	        		{
	        			int count = 0;
	        			for(int l=0; l<nodek.getAttributes().getLength(); l++)
	        			{
	        				Node nodel = nodek.getAttributes().item(l);
	        				if(nodel.getNodeName().equalsIgnoreCase("seqNum"))
	        				{
	        					taintedInfo.getTaintedSeqs().add(nodel.getNodeValue());
	        					//taintedInfo.getTaintedSeqs().set(count,nodel.getNodeValue());
	        					//System.out.println("tainted seq : " + nodel.getNodeValue());
	        				}
	        				else if(nodel.getNodeName().equalsIgnoreCase("type"))
	        				{
	        					taintedInfo.getTaintedTypes().add(nodel.getNodeValue());
	        					//taintedInfo.getTaintedTypes().set(count, nodel.getNodeValue());
	        					//System.out.println("tainted type : " + nodel.getNodeValue()+ "\n");
	        				}
	        				count ++;
	        			}
	        		}
	        	}
	        	//��������������ӵ�table��
	        	table.put(sigInfo, taintedInfo);
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
		
		return table;
	}
	
	/**
	 * �Ի�������н���
	 * 
	 * @return Hashtable
	 */
	public Hashtable<String,SensitiveInfo> parseSinkReg()
	{
		
		Hashtable<String,SensitiveInfo> table = new Hashtable<String,SensitiveInfo>();
		
		try
		{
			/*
			 * ��ʼ����
			 */
			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
	        domFactory.setNamespaceAware(true); 
	        DocumentBuilder builder;
			builder = domFactory.newDocumentBuilder();
			Document doc = builder.parse(sinkLocation);
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			
			/*
			 * ����������
			 */
			XPathExpression expr = xpath.compile("//method");
			Object result = expr.evaluate(doc, XPathConstants.NODESET);
	        NodeList nodes = (NodeList) result;
	        
	        for(int i=0; i<nodes.getLength(); i++)
	        {
	        	String sigInfo = null;
	        	
	        	Node nodei = nodes.item(i);
	        	
	        	//�����ڵ�i�ĺ�����������:public java.lang.String java.io.DataInputStream.readLine()
	        	for(int j=0; j<nodei.getAttributes().getLength(); j++)
	        	{
	        		Node nodej = nodei.getAttributes().item(j);
	        		
	        		if(nodej.getNodeName().equalsIgnoreCase("signature"))
	        		{
	        			sigInfo = nodej.getNodeValue();
//	        			System.out.println("sigInfo = " + sigInfo);
	        		}
	        	}
	        	
	        	//�����ڵ�i����������
	        	SensitiveInfo sensitiveInfo = new SensitiveInfo();
	        	NodeList nodeks = nodei.getChildNodes();
	        	
	        	for(int k=0; k<nodeks.getLength(); k++)
	        	{
	        		Node nodek = nodeks.item(k);
	        		if(nodek.getNodeName().equalsIgnoreCase("sensitiveParam"))
	        		{
	        			int count = 0;
	        			for(int l=0; l<nodek.getAttributes().getLength(); l++)
	        			{
	        				Node nodel = nodek.getAttributes().item(l);
	        				if(nodel.getNodeName().equalsIgnoreCase("seqNum"))
	        				{
	        					sensitiveInfo.getSensitiveSeqs().add(nodel.getNodeValue());
//	        					System.out.println("sensitive seq : " + nodel.getNodeValue());
	        				}
	        				else if(nodel.getNodeName().equalsIgnoreCase("type"))
	        				{
	        					sensitiveInfo.getSensitiveTypes().add(nodel.getNodeValue());
//	        					System.out.println("sensitive type : " + nodel.getNodeValue()+ "\n");
	        				}
	        				count ++;
	        			}
	        		}
	        	}
	        	
	        	//��������������ӵ�table��
	        	table.put(sigInfo, sensitiveInfo);
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
		
		return table;
	}
	
	//accessor
	public String getInputLocation()
	{
		return inputLocation;
	}

	public void setInputLocation(String inputLocation)
	{
		this.inputLocation = inputLocation;
	}

	public String getSinkLocation()
	{
		return sinkLocation;
	}

	public void setSinkLocation(String sinkLocation)
	{
		this.sinkLocation = sinkLocation;
	}

	public String getFilterLocation()
	{
		return filterLocation;
	}

	public void setFilterLocation(String filterLocation)
	{
		this.filterLocation = filterLocation;
	}
	
}
