package softtest.fsm.java;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import org.xml.sax.*;

/** 解析xml，生成状态机的类 */
public class FSMLoader {
	private static String escapeFilePath(String path) {
		String ret1 = path.replace('/', File.separatorChar);
		String ret2 = ret1.replace('\\', File.separatorChar);
		return ret2;
	}
	/** 解析xml，得到状态机数据结构 */
	public static FSMMachine loadXML(String path) {
		path = escapeFilePath(path);
		
		FSMMachine fsm = new FSMMachine();
		DocumentBuilderFactory domfac = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder dombuilder = domfac.newDocumentBuilder();
			InputStream is = new FileInputStream(path);
			Document doc = dombuilder.parse(is);
			Element root = doc.getDocumentElement();

			if (root == null) {
				throw new RuntimeException("This is not a legal satemachine define file.");
			}
			//解析状态机
			fsm.loadXML(root);

			// 解析所有的状态，所有状态都解析了才能进一步解析转换，因为要查询获得转换的tostate
			NodeList list = root.getChildNodes();
			for (int i = 0; i < list.getLength(); i++) {
				Node n = list.item(i);
				if (n.getNodeName().equals("State")) {
					Node nameatt = n.getAttributes().getNamedItem("Name");
					if (nameatt != null) {
						FSMState state = fsm.addState(nameatt.getNodeValue());
						state.loadXML(n);
					} else {
						throw new RuntimeException("State node must have a name.");
					}
				}
			}

			// 解析所有的转换
			for (int i = 0; i < list.getLength(); i++) {
				Node n = list.item(i);
				if (n.getNodeName().equals("State")) {
					String name = n.getAttributes().getNamedItem("Name").getNodeValue();
					//获得fromstate
					FSMState fromstate = fsm.getStates().get(name);
					if (fromstate == null) {
						throw new RuntimeException("State error.");
					}
					//得到当前State结点的所有Transition结点
					NodeList tranlist = n.getChildNodes();
					for (int j = 0; j < tranlist.getLength(); j++) {
						Node tran = tranlist.item(j);
						if (tran.getNodeName().equals("Transition")) {
							//获得tostate
							Node tostateatt = tran.getAttributes().getNamedItem("ToState");
							FSMState tostate = null;
							if (tostateatt != null) {
								tostate = fsm.getStates().get(tostateatt.getNodeValue());
								if (tostate == null) {
									throw new RuntimeException("State error.");
								}
							} else {
								throw new RuntimeException("Transition must have a tostate.");
							}

							FSMTransition transition = fsm.addTransition(fromstate, tostate);
							transition.loadXML(tran);

							// 解析当前转换的所有的条件，当前支持五种条件：Domain，Scope，Xpath，Nextvex ,AlwaysTrue
							NodeList conlist = tran.getChildNodes();
							for (int k = 0; k < conlist.getLength(); k++) {
								Node con = conlist.item(k);
								if (con.getNodeName().equals("Domain")) {
									FSMDomainCondition domaincon = new FSMDomainCondition();
									domaincon.setFSMMachine(fsm);
									domaincon.loadXML(con);
									transition.addCondition(domaincon);
								} else if (con.getNodeName().equals("Scope")) {
									FSMScopeCondition scopecon = new FSMScopeCondition();
									scopecon.setFSMMachine(fsm);
									scopecon.loadXML(con);
									transition.addCondition(scopecon);
								} else if (con.getNodeName().equals("Xpath")) {
									FSMXpathCondition xpathcon = new FSMXpathCondition("");
									xpathcon.setFSMMachine(fsm);
									xpathcon.loadXML(con);
									transition.addCondition(xpathcon);
								} else if (con.getNodeName().equals("Nextvex")) {
									FSMNextVexCondition vexcon = new FSMNextVexCondition();
									vexcon.setFSMMachine(fsm);
									vexcon.loadXML(con);
									transition.addCondition(vexcon);
								} else if (con.getNodeName().equals("AlwaysTrue")) {
									AlwaysTrueCondition truecon = new AlwaysTrueCondition();
									truecon.setFSMMachine(fsm);
									truecon.loadXML(con);
									transition.addCondition(truecon);
								}
							}
						}
					}
				}
			}

		} catch (ParserConfigurationException e) {
			if(softtest.config.java.Config.DEBUG){
				e.printStackTrace();
			}
			throw new RuntimeException("This is not a legal satemachine define file.",e);
		} catch (FileNotFoundException e) {
			if(softtest.config.java.Config.DEBUG){
				e.printStackTrace();
			}
			throw new RuntimeException("This is not a legal satemachine define file.",e);
		} catch (SAXException e) {
			if(softtest.config.java.Config.DEBUG){
				e.printStackTrace();
			}
			throw new RuntimeException("This is not a legal satemachine define file.",e);
		} catch (IOException e) {
			if(softtest.config.java.Config.DEBUG){
				e.printStackTrace();
			}
			throw new RuntimeException("This is not a legal satemachine define file.",e);
		}

		return fsm;
	}
}
