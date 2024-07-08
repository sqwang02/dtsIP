package softtest.rules.java.sensdt;

import java.util.List;
import java.util.LinkedList;

import org.w3c.dom.Node;

import softtest.config.java.Config;


enum	BoolRelation { AND, OR, XOR  };

enum	EMatch { EQUAL,  MATCHES, LARGE, SMALL };

/**
	<Args  type="String" Sensitive="true">
	</Args>
  
	<Args   Argc="1" >
		<And>
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
			<Arg_0   NeedSensitive="true"  Type="String" >
				<equal>web.root</equal>
				<equal>webroot</equal>
				<matches>*web(.)?root*</matches>
			</Arg_0>
			<Arg_1   NeedSensitive="true"  Type="String" >
				<equal>webroot</equal>
				<matches>*web(.)?root*</matches>
			</Arg_1>
			<Arg_1   NeedSensitive="true"  Type="String" >
				<equal>webroot</equal>
				<matches>*web(.)?root*</matches>
			</Arg_1>
		</And>
	</Args>
 */
public 	class   Signature {

	private  BoolRelation  relation;
	private  List<Argi>    argis = new LinkedList<Argi>();


	public Signature(Node argsNode) {
		Node  rela = argsNode.getFirstChild();
		if( rela == null ) {
			
		}
		/**  There is only one level relation operation, may be in the future,
		 * will add more level, for  <And> <Or> ... </Or> </And>  **/
		while( rela.getNodeType() != Node.ELEMENT_NODE ) {
			rela = rela.getNextSibling();
		}
		String  strrela = rela.getNodeName();
		if( strrela.equals("And") ) {
			relation = BoolRelation.AND;
		} else if( strrela.equals("Or") ){
			relation = BoolRelation.OR;
		} else if( strrela.equals("Xor") ) {
			relation = BoolRelation.XOR;
		}
		int  i = 0;
		for( Node  ndArgi = rela.getFirstChild(); ndArgi != null; ndArgi = ndArgi.getNextSibling() ) {
			if( ndArgi.getNodeType() != Node.ELEMENT_NODE ) {
				continue;
			}
			Argi argi = new Argi( ndArgi, i++ );
			argis.add( argi );
		}
	}

	public boolean  isSensitiveArgs(int [] args) {
		if( relation == BoolRelation.AND ) {
			for( int i = 0; i < argis.size(); i++) {
				if( argis.get(i).needSensitive ) {
					int j = 0;
					for( ; j < args.length; j++) {
						if( args[j] == i ) {
							break;
						}
					}
					if( j >= args.length ) {
						logc("======> False : AND realation not satified");
						return false;
					}
				}
			}
			return true;
		} else if( relation == BoolRelation.OR ) {
			for( int i = 0; i < argis.size(); i++) {
				if( argis.get(i).needSensitive ) {
					int j = 0;
					for( ; j < args.length; j++) {
						if( args[j] == i ) {
							return true;
						}
					}
				}
			}
			return false;
		} else if( relation == BoolRelation.XOR ) {
			logc("????????  Undefine : XOR realation");
		}
		return   false;
	}
	
	/**  (var, "..")  if var's declaration is in the ealias'sensSource or 
	 * ".." is equal to the ealias' resourcename, and the argument need tobe 
	 * sensitive, then returns true  
	//public static boolean  isSensitive(SimpleJavaNode  node,SensClasses sclses, SensClass scls, SensMethod mthd, Signature sign, ExtendAlias ealias) {
	public static boolean  isSensitive(SimpleJavaNode  node,SensClasses sclses, SensClass scls, Signature sign, ExtendAlias ealias) {
		ASTPrimaryExpression  astPrimExpr = (ASTPrimaryExpression) node;
		ASTArguments astArgs = (ASTArguments) astPrimExpr.jjtGetChild(1).jjtGetChild(0);
		ASTArgumentList  astArgList = (ASTArgumentList) astArgs.jjtGetChild(0);
		for(int argi = astArgs.getArgumentCount() - 1; argi >= 0; argi--) {
			if( ! sign.argis.get( argi ).needSensitive ) {
				continue;
			}
			ASTExpression astExpr = (ASTExpression) astArgList.jjtGetChild(argi);
			/**  如果参数是 ASTLiteral or ASTName 则判断参数是否在ealias 
			ASTLiteral astlit = (ASTLiteral)astExpr.getSingleChildofType(ASTLiteral.class);
			if( astlit != null ) {
				if( astlit.isStringLiteral() ) {
					String withquote = astlit.getImage();
					String noquote   = withquote.substring(1, withquote.length()-1);
					if( ealias.getResourceName().equals( noquote ) ) {
						return true;
					}
				}
			}
			/**  上下两个最多只会有一个执行。  
			ASTName astName = (ASTName) astExpr.getSingleChildofType(ASTName.class);
			if( astName != null ) {
				NameDeclaration ndecl = (NameDeclaration) astName.getNameDeclaration();
				if( ndecl instanceof VariableNameDeclaration ) {
					if( ealias.getSensSource().contains((VariableNameDeclaration)ndecl) ) {
						return true;
					}
				}
			}
		}
		return  false;
	}
	*/
	
	/** It seems that it is hard to do this.
	 * */
	public boolean  matchSignature(List<Class>  typeStr) {
		boolean isMatch = false;

		return isMatch;
	}
	
	public int       getArgc() {
		return  argis.size();
	}

	public  void dump() {
		logc("-------------- [ Signature ] -------------[ Begin ]--");
		logc("relation:" + relation);
		for( int i = 0; i < argis.size(); i++ ) {
			(argis.get(i)).dump();
		}
		logc("-------------- [ Signature ] -------------[ End ]--");
	}
	
	public static void logc(String str) {
		if( Config.DEBUG ) {
			System.out.println("Signature:: - " + str);
		}
	}

	/***
	 * 	<Arg_0   NeedSensitive="true"  Type="String" >
			<Equal>web.root</Equal>
			<Equal>webroot</Equal>
			<matches>*web(.)?root*</matches>
		</Arg_0>
		<Arg_1   NeedSensitive="true"  Type="String" >
			<Equal>webroot</Equal>
			<matches>*web(.)?root*</matches>
		</Arg_1>
		<Arg_2   NeedSensitive="true"  Type="String" >
			<Equal>webroot</Equal>
			<matches>*web(.)?root*</matches>
		</Arg_2>
	 */
	class  Argi {

		private String		type;

		/**  If it is false, values will not need  **/
		private boolean	needSensitive = false;
		private  int 		ith; /**  arg_i  **/

		private List<SensValue>  valueList = new LinkedList<SensValue>(); 


		public Argi(Node argi, int i) {
			Node tpNode = argi.getAttributes().getNamedItem("NeedSensitive");
			if( tpNode.getNodeValue().equals("true") ) {
				needSensitive = true;
			}
			type = argi.getAttributes().getNamedItem("Type").getNodeValue();
			ith = i;
			for( Node valNode = argi.getFirstChild(); valNode != null; valNode = valNode.getNextSibling() ) {
				if( valNode.getNodeType() != Node.ELEMENT_NODE ) {
					continue;
				}
				valueList.add( new SensValue(valNode, type) );
			}
			String name = argi.getNodeName();
			int idx = Integer.parseInt( name.substring( name.length() - 1) );
			if( idx != i) {
				throw new RuntimeException("Error Arg_x format");
			}
		}
		
		public int getIth() {
			return ith;
		}
		
		public  void dump() {
			logc("[ Argi ] -------------[ Begin ]--");
			logc("Arg_" + ith + "  " + type + "  isSens:" + needSensitive);
			for( int i = 0; i < valueList.size(); i++ ) {
				(valueList.get(i)).dump();
			}
			logc("[ Argi ] -------------[  End  ]--");
		}
		
		public void logc(String str) {
			if(Config.DEBUG) {
				System.out.println("Argi:: - " + str);
			}

		}
		
		/**
		<Equal>webroot</Equal>
		<matches>*web(.)?root*</matches>
		 */
		class SensValue {
			private MatchCondition	cond;

			public SensValue(Node valNode, String type) {
				if( type.equals("String") ) {
					cond = new StringMatchCondition();
					cond.setValue( valNode.getTextContent() );
				} else if( type.equals("int") ) {
					cond = new IntMatchCondition();
					String tmp = valNode.getTextContent();
					cond.setValue( Integer.parseInt(tmp) );
				}
				if( valNode.getNodeName().equals("Equal") ) {
					cond.setHowMatch( EMatch.EQUAL );
				} else if( valNode.getNodeName().equals("Matches") ){
					cond.setHowMatch( EMatch.MATCHES );
				} else if( valNode.getNodeName().equals("Larger") ){
					cond.setHowMatch( EMatch.LARGE );
				} else if( valNode.getNodeName().equals("Smaller") ){
					cond.setHowMatch( EMatch.SMALL );
				} else
					throw new RuntimeException("Unsupported Value tag:"+valNode.getNodeName());
			}
			
			public  void dump() {
				cond.dump();
			}
			
			public void logc(String str) {
				if(Config.DEBUG) {
					System.out.println("SensValue:: - " + str);
				}
			}
		} //  class  SensValue

		class StringMatchCondition implements MatchCondition {
			private 	EMatch  howMatch;
			private 	String	value;

			public void setHowMatch(EMatch how) {
				howMatch = how;
			}
			public void setValue(Object  val) {
				value = (String)val;
			}
			public boolean  satisfy(Object obj) {
				String str = (String) obj;
				if( howMatch == EMatch.EQUAL ) {
					if( str.equals(value) ) {
						return true;
					} else {
						return false;
					}
				} else if( howMatch == EMatch.MATCHES ){
					if( str.matches(value) ) {
						return true;
					} else {
						return false;
					}
				}
				throw  new RuntimeException("Error StringMatchCondition.");
			}
			
			public  void dump() {
				logc("how:" + howMatch);
				logc("val:" + value);
			}
			public void logc(String str) {
				if(Config.DEBUG) {
					System.out.println("StringMatchCondition:: - " + str);
				}
			}
		} // class StringMatchCondition

		class IntMatchCondition implements MatchCondition {
			private 	EMatch  howMatch;
			private 	int		value;

			public void setHowMatch(EMatch how) {
				howMatch = how;
			}
			public void setValue(Object  val) {
				value = (Integer)val;
			}
			public boolean  satisfy(Object obj) {
				Integer i = (Integer) obj;
				if( howMatch == EMatch.LARGE ) {
					if( i > value ) {
						return true;
					} else {
						return false;
					}
				} else if( howMatch == EMatch.SMALL ){
					if( i < value) {
						return true;
					} else {
						return false;
					}
				} else if( howMatch == EMatch.EQUAL ) {
					if( i == value ) {
						return true;
					} else {
						return false;
					}
				}
				throw  new RuntimeException("Error IntMatchCondition.");
			}
			public  void dump() {
				logc("how:" + howMatch);
				logc("val:" + value);
			}
			public void logc(String str) {
				if(Config.DEBUG) {
					System.out.println("IntMatchCondition:: - " + str);
				}
			}
		} // class  IntMatchCondition
	} // class  Argi
}

