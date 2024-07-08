package softtest.rules.java.sensdt;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Node;

import softtest.config.java.Config;

public class SensInfo {
	
	private  Hashtable<String, String> equals = new Hashtable<String, String>();
	
	private  List<String>      matches        = new LinkedList<String>();
	
	/**
	<SensResource  Type="String" >
		<Equal Name="designInfo_class">web.root</Equal>
		<Equal Name="mima">password</Equal>
		<Matches Name="mima">*web(.)?root*</Matches>
		<Equal >password</Equal>
		<Matches >*web(.)?root*</Matches>
	</SensResource>	 */
	public void initSensInfo(Node sensNode) {
		Node subs = sensNode.getFirstChild();
		for( ; subs != null; subs = subs.getNextSibling() ) {
			if( subs.getNodeType() != Node.ELEMENT_NODE ) {
				continue;
			}
			if( subs.getNodeName().equals("Equal") ) {
				String  eq = subs.getTextContent();
				equals.put(eq, eq);
			} else if( subs.getNodeName().equals("Matches") ) {
				String  mch = subs.getTextContent();
				matches.add( mch );
			}
		}
	}
	
	public boolean isSensInfo(String  str) {
		boolean sens = false;
		if( equals.containsKey(str) ) {
			sens = true;
		} else {
			for(int len = matches.size(), i = 0; i < len; i++) {
				if( str.matches( matches.get(i) ) ) {
					sens = true;
					break;
				}
			}
		}
		return sens;
	}
	
	public void dump() {
		logc("-----------[ SensInfo ]----------[ begin ]--");
		int  len = matches.size();
		for( int i = 0; i < len; i ++) {
			logc("Matches : " + matches.get(i) );
		}
		for( Enumeration<String> e = equals.elements(); e.hasMoreElements(); ) {
			logc("Equal   : " + e.nextElement());
		}
		logc("-----------[ SensInfo ]----------[  end  ]--");
	}
	public void logc(String str) {
		if( Config.DEBUG ) {
			System.out.println("SensInfo::" + str);
		}
	}
}
