package softtest.rules.java.sensdt;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.util.Hashtable;
import java.util.List;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import softtest.config.java.Config;




public   class  XMLUtil {
	
	/** <Xxx> <Yyy> <Zzz>Data1</Zzz> <Zzz>Data2</Zzz> </Yyy> </Xxx>  
	 * xtags is : {"Xxx","Yyy","Zzz"}
	 * result is: {"Data1","Data2"}
	 */
	public static List<String>  getStrsFromFile(final String file, String ... xtags)
	throws ParserConfigurationException, SAXException, IOException {
		List<String>  resList = new LinkedList<String>();
		
		final Document document;
		InputStream inputStream = new FileInputStream( file.replace('\\', File.separatorChar).replace('/', File.separatorChar) );
		InputSource input = new InputSource(inputStream);

		final DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
		dfactory.setIgnoringComments(true);
		dfactory.setIgnoringElementContentWhitespace(true);
		final DocumentBuilder builder = dfactory.newDocumentBuilder();

		document = builder.parse(input);

		Node root = document.getElementsByTagName(xtags[0]).item(0);
		Node item = root.getChildNodes().item(0);
		for(int i = 1; item != null && i < xtags.length - 1; i++ ) {
			for( ;null != item; item = item.getNextSibling() ) {
				if( item.getNodeType() == Node.ELEMENT_NODE ) {
					String name = item.getNodeName();
					if( ! name.equals(xtags[i]) ) {
						continue;
					}
					break;
				}
			}
			if( item == null ) {
				break;
			}
			item = item.getFirstChild();
		}
		if( item == null ) {
			return resList;
		}
		for( ; null != item; item = item.getNextSibling() ) {
			if( item.getNodeType() == Node.ELEMENT_NODE ) {
				String name = item.getNodeName();
				if( ! name.equals(xtags[ xtags.length-1]) ) {
					continue;
				}
				resList.add( item.getTextContent() );
			}
		}
		return resList;
	}

	/**
	 *  IRNR-4    find method name that is similar to exists methods', for example:
	 *  tostring, compareto
	 */
	public static void fillSuspicious(final String file, Hashtable<String, String> table)
			throws ParserConfigurationException, SAXException, IOException {
		final Document document;
		InputStream inputStream = new FileInputStream( file.replace('\\', File.separatorChar).replace('/', File.separatorChar) );
		InputSource input = new InputSource(inputStream);

		final DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
		dfactory.setIgnoringComments(true);
		dfactory.setIgnoringElementContentWhitespace(true);
		final DocumentBuilder builder = dfactory.newDocumentBuilder();

		document = builder.parse(input);

		Node root = document.getElementsByTagName("IRNR-Data").item(0);

		Node item  =  root.getChildNodes().item(0);
		while( item != null ) {
			//System.out.println( item.getClass() + "__" + item.getTextContent() + "__" );
			if( item.getNodeType() == Node.ELEMENT_NODE ) {
				String  str =  item.getTextContent();
				if( ! table.containsKey( str.toLowerCase())) {
					table.put(str.toLowerCase(), str);
				}
			}
			item = item.getNextSibling();
		}
		/* System.out.println("----------");
		for( Enumeration<String> e = table.elements(); e.hasMoreElements(); ) {
			System.out.println( e.nextElement() );
		}
		System.out.println("----------");  */
	}
	
	
	
	
	/** Init the sInfo, fill it with items stored in the "Item" tags. 
<DsnInfoLeak-Data>
	<SensResource  Type="String" >
		<Equal Name="designInfo_class">web.root</Equal>
		<Equal Name="mima">password</Equal>
		<Matches Name="mima">*web(.)?root*</Matches>
		<Equal >password</Equal>
		<Matches >*web(.)?root*</Matches>
	</SensResource>
</DsnInfoLeak-Data>
	 */
	public static void getSensInfo(final String file, SensInfo sInfo)
			throws ParserConfigurationException, SAXException, IOException {
		final Document document;
		InputStream inputStream = new FileInputStream( file );
		InputSource input = new InputSource(inputStream);

		final DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
		dfactory.setIgnoringComments(true);
		dfactory.setIgnoringElementContentWhitespace(true);
		final DocumentBuilder builder = dfactory.newDocumentBuilder();
		document = builder.parse(input);

		Node root = document.getElementsByTagName("DsnInfoLeak-Data").item(0);

		NodeList list  =  root.getChildNodes();
		int  len = list.getLength();
		Node  sensRes = null;
		for( int i = 0; i < len; i++) {
			sensRes = list.item(i);
			if( sensRes.getNodeType() != Node.ELEMENT_NODE ) {
				continue;
			} else if( sensRes.getNodeName().equals("SensResource")) {
				break;
			}
		}
		sInfo.initSensInfo( sensRes );
	}

	
	/**
<DsnInfoLeak-Data>
	<SensResource  Type="String" >
		<Item Name="designInfo_class">web.root</Item>
		<Item Name="designInfo_class">root</Item>
		<Item Name="mima">password</Item>
		<Item Name="mima">PASSWORD</Item>
	</SensResource>
	<Class Name="File" Package="java.io">
		<Constructor  Transitive="true">
			<Args Argc="2">
				<And>
					<Arg_0  NeedSensitive="true"  Type="String" >
					</Arg_0>
					<Arg_1  NeedSensitive="false"  Type="String" />
				</And>
			</Args>
			<Args  Argc="1" Sensitive="true"  Type="String" >
			</Args>
		</Constructor>
	</Class>
	<Class Name="System" Package="java.lang">
		<Method Name="gc" ReturnType="void" 
		        Sensitive="true">
		</Method>
		<Method Name="getProperty" ReturnType="String"
				Transitive="true">
			<Args   Argc="1" >
				<And>  <!-- using <or> tag is also right when there is only one arg  -->
					<Arg_0  NeedSensitive="true"  Type="String" > <!-- Arg -->
						<Equal>web.root</Equal>
						<Equal>root</Equal>
						<Equal>web_root</Equal>
						<Equal>webroot</Equal>
						<Equal>password</Equal>
						<Matches>*web(.)?root*</Matches>
					</Arg_0>
				</And>
			</Args>
			<Args  Argc="2">
				<Or>
					<Arg_0  NeedSensitive="true"  Type="String" >
						<Equal>password</Equal>
						<Matches>*web(.)?root*</Matches>
					</Arg_0>
					<Arg_1  NeedSensitive="true"  Type="String" >
						<Equal>web.root</Equal>
						<Equal>web_root</Equal>
						<Matches>*web(.)?root*</Matches>
					</Arg_1>
				</Or>
			</Args>
			<Args  Argc="3">
				<And>
					<Arg_0  NeedSensitive="true" Type="String" >
						<Equal>web.root</Equal>
						<Equal>webroot</Equal>
						<Matches>*web(.)?root*</Matches>
					</Arg_0>
					<Arg_1  NeedSensitive="true" Type="String" >
						<Equal>webroot</Equal>
						<Matches>*web(.)?root*</Matches>
					</Arg_1>
					<Arg_2  NeedSensitive="true" Type="String" >
						<Equal>hello</Equal>
					</Arg_2>
				</And>
			</Args>
		</Method>
	</Class>
</DsnInfoLeak-Data>
	 * */
	public static void getSensClasses(final String file, SensClasses scs) 
			throws ParserConfigurationException, SAXException, IOException {
		final Document document;
		InputStream inputStream = new FileInputStream( file );
		InputSource input = new InputSource(inputStream);

		final DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
		dfactory.setIgnoringComments(true);
		dfactory.setIgnoringElementContentWhitespace(true);
		final DocumentBuilder builder = dfactory.newDocumentBuilder();
		document = builder.parse(input);
		
		Node root = document.getElementsByTagName("DsnInfoLeak-Data").item(0);
		NodeList list  =  root.getChildNodes();// .getChildNodes().item(0);
		NodeList  items = null;
		int  len = list.getLength();
		for( int i = 0; i < len; i++) {
			Node  subNode = list.item(i);
			if( subNode.getNodeType() != Node.ELEMENT_NODE ) {
				continue;
			} else if( ! subNode.getNodeName().equals("Class")) {
				continue;
			}
			// must be Class element.
			scs.addSensClass( subNode );
		}
	}
	
	public static void logc(String str) {
		if( Config.DEBUG ) {
			System.out.println("XMLUtil::" + str);
		}
	}
	
	public static void main(String[] args) {
		try{
			/*
			SensClasses s = new SensClasses();
			getSensClasses("softtest\\rules\\java\\DsnInfoLeak-Data.xml", s);
			s.dump(); */
			List<String> res = getStrsFromFile("softtest\\rules\\java\\URLS-Data.xml", "URLS-Data", "Temp", "MethodMaxLength");
			for(int i = 0; i < res.size(); i++) {
				System.out.println( res.get(i) );
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
}