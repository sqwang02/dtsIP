/**
 * SF means Security Features (安全功能部件)
 * Security of Cookies 
 */
package softtest.rules.java.safety.SF;

import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import softtest.ast.java.ASTAdditiveExpression;
import softtest.ast.java.ASTExpression;
import softtest.ast.java.ASTLiteral;
import softtest.ast.java.ASTMultiplicativeExpression;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTPrimaryExpression;
import softtest.ast.java.ASTPrimaryPrefix;
import softtest.ast.java.ASTPrimarySuffix;
import softtest.ast.java.ASTVariableDeclaratorId;
import softtest.ast.java.Node;
import softtest.ast.java.SimpleJavaNode;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;
import softtest.symboltable.java.VariableNameDeclaration;

/**
 * @author pengpinglei
 *
 */
public class SercureFunctionUnit_SecurityOfCookieStateMachine
{
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("安全功能部件模式:在 %d 行上对Cookies的设置存在安全隐患。不安全的Cookie设置可能泄漏出敏感信息，被攻击者窃取后，失去安全性。", errorline);
		}else{
			f.format("Security Features: unproper deal with cookie on line %d",errorline);
		}
		fsmmi.setDescription(f.toString());
		f.close();
	}
	
	public static List<FSMMachineInstance> createSecurityOfCookieStateMachines(SimpleJavaNode node, FSMMachine fsm)
	{
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		
		/**
		 * 获取Cookie变量
		 */
		String xpath=".//LocalVariableDeclaration[./Type[@TypeImage='Cookie']]/VariableDeclarator/VariableDeclaratorId";
		
		List evalRlts = node.findXpath(xpath);
		
		/*
		 * 如果没有Cookie变量的声明，则返回
		 */
		if(evalRlts.size() == 0)
		{
			return list;
		}
		
		Iterator i = evalRlts.iterator();
		/*
		 * 将这些变量存放在缓冲区cookies中。
		 */
		HashMap<VariableNameDeclaration,ASTVariableDeclaratorId> cookies = new HashMap<VariableNameDeclaration,ASTVariableDeclaratorId>();
		
		while(i.hasNext())
		{
			ASTVariableDeclaratorId vd = (ASTVariableDeclaratorId) i.next();
			
			if(!(vd.getNameDeclaration() instanceof VariableNameDeclaration))
			{
				continue;
			}
			
			VariableNameDeclaration v = (VariableNameDeclaration) vd.getNameDeclaration();
			
			cookies.put(v, vd);
		}
		
		/* 
		 * 如果容器cookies的大小为0，则返回
		 */
		if(cookies.size() == 0)
		{
			return list;
		}
		
		/**
		 * 1 Cookie未调用setSecure(true)
		 */
		xpath = ".//PrimaryExpression[./PrimarySuffix/Arguments[.//Literal/BooleanLiteral[@True='true' ]]]/PrimaryPrefix/Name[@MethodName='true' and matches(@Image,'setSecure')]";
		evalRlts = node.findXpath(xpath);
		i = evalRlts.iterator();
		HashMap<VariableNameDeclaration,ASTVariableDeclaratorId> temp = new HashMap<VariableNameDeclaration,ASTVariableDeclaratorId>(cookies);
		
		while(i.hasNext())
		{
			ASTName name = (ASTName) i.next();
			if (!(name.getNameDeclaration() instanceof VariableNameDeclaration)) {
				continue;
			}
			VariableNameDeclaration v = (VariableNameDeclaration) name.getNameDeclaration();
			
			if(cookies.containsKey(v))
			{
				temp.remove(v);
			}
		}
		
			/*
			 * 如果容器temp的大小不为0，则表明存在未设置安全属性的cookie
			 */
		for(Entry<VariableNameDeclaration,ASTVariableDeclaratorId> entry : temp.entrySet())
		{
			FSMMachineInstance fsmInst = fsm.creatInstance();
			
			fsmInst.setRelatedObject(new FSMRelatedCalculation(entry.getValue()));
			fsmInst.setResultString(entry.getValue().getImage());
			
			list.add(fsmInst);
		}
		
		/**
		 * 2 域范围过大的Cookie
		 * e.g:
		 * 1    Cookie cookie = new Cookie("sessionID", sessionID);
		 * 2    cookie.setDomain(".example.com");
		 * 
		 * pattern = "^\"[.]?\\w*([.]\\w*)?\"$"
		 * 
		 */
		String pattern = "^\"[.]?\\w*([.]\\w*)?\"$";
		xpath = ".//PrimaryExpression[./PrimarySuffix/Arguments[.//Literal[matches(@Image,'" +
		pattern +
		"')]]]/PrimaryPrefix/Name[@MethodName='true' and matches(@Image,'setDomain')]";
		
		evalRlts = node.findXpath(xpath);
		i = evalRlts.iterator();
		
		while(i.hasNext())
		{
			ASTName name = (ASTName) i.next();
			if (!(name.getNameDeclaration() instanceof VariableNameDeclaration)) {
				continue;
			}
			VariableNameDeclaration v = (VariableNameDeclaration) name.getNameDeclaration();
			
			if(cookies.containsKey(v))
			{
				FSMMachineInstance fsmInst = fsm.creatInstance();
				
				fsmInst.setRelatedObject(new FSMRelatedCalculation(name));
				fsmInst.setResultString(v.getImage());
				
				list.add(fsmInst);
			}
		}
		
		/**
		 * 3 访问路径范围过大的Cookie
		 * e.g:
		 * 1    Cookie cookie = new Cookie("sessionID", sessionID);
		 * 2    cookie.setPath("/");
		 */
		pattern = "^\"/\"$";
		xpath = ".//PrimaryExpression[./PrimarySuffix/Arguments[.//Literal[matches(@Image,'" +
		pattern +
		"')]]]/PrimaryPrefix/Name[@MethodName='true' and matches(@Image,'setPath')]";
		
		evalRlts = node.findXpath(xpath);
		i = evalRlts.iterator();
		
		while(i.hasNext())
		{
			ASTName name = (ASTName) i.next();
			if (!(name.getNameDeclaration() instanceof VariableNameDeclaration)) {
				continue;
			}
			VariableNameDeclaration v = (VariableNameDeclaration) name.getNameDeclaration();
			
			if(cookies.containsKey(v))
			{
				FSMMachineInstance fsmInst = fsm.creatInstance();
				
				fsmInst.setRelatedObject(new FSMRelatedCalculation(name));
				fsmInst.setResultString(v.getImage());
				
				list.add(fsmInst);
			}
		}
		
		/**
		 * 4 永久性的Cookie
		 * 
		 * e.g:
		 * Cookie cookie = new Cookie("sessionID", sessionID);
		 * cookie.setMaxAge(60*60*24*365*10);
		 * 
		 */
		
		float maxAge = 60*60*24;
		
		xpath = ".//PrimaryExpression/PrimaryPrefix[./Name[@MethodName='true' and matches(@Image,'setMaxAge')]]";
		
		evalRlts = node.findXpath(xpath);
		i = evalRlts.iterator();
		
		while(i.hasNext())
		{
			ASTPrimaryPrefix app = (ASTPrimaryPrefix) i.next();
			
			ASTName name = (ASTName) app.jjtGetChild(0);
			
			if(name == null) continue;
			
			if (!(name.getNameDeclaration() instanceof VariableNameDeclaration)) {
				continue;
			}
			VariableNameDeclaration v = (VariableNameDeclaration) name.getNameDeclaration();
			
			if(!cookies.containsKey(v))
			{
				continue;
			}
			
			Node ageNode = app.getNextSibling();
			
			if( !(ageNode instanceof ASTPrimarySuffix))
			{
				continue;
			}
			
			ASTPrimarySuffix aps = (ASTPrimarySuffix) ageNode;
			
			Node tno = aps.jjtGetChild(0);
			if(tno == null) continue;
			
			tno = tno.jjtGetChild(0);
			if(tno == null) continue;
			
			tno = tno.jjtGetChild(0);
			if(tno == null) continue;
			
			if(!(tno instanceof ASTExpression)) continue;
			
			ASTExpression ae = (ASTExpression) tno;
			ASTCalculator cal = new ASTCalculator();
			float r = 0;
			
			tno = ae.jjtGetChild(0);
			
			if(tno == null) continue;
			else if(tno instanceof ASTAdditiveExpression)
			{
				r = cal.getValueOfAdditiveExpression((ASTAdditiveExpression)tno);
				
			}
			else if(tno instanceof ASTMultiplicativeExpression)
			{
				r = cal.getValueOfMultiplicativeExpression((ASTMultiplicativeExpression)tno);
			}
			else if(tno instanceof ASTPrimaryExpression)
			{
				r = cal.getValueOfPrimaryExpression((ASTPrimaryExpression)tno);
			}
			
			if( r > maxAge)
			{
				FSMMachineInstance fsmInst = fsm.creatInstance();
				
				fsmInst.setRelatedObject(new FSMRelatedCalculation(app));
				fsmInst.setResultString(v.getImage());
				
				list.add(fsmInst);
			}
		}
		
		return list;
	}
}

class ASTCalculator
{
	public float getValueOfMultiplicativeExpression(ASTMultiplicativeExpression me)
	{
		String image = me.getImage();
		String [] symbols = image.split("#");
		for(int i=0; i<symbols.length; i++)
		{
			symbols[i] = symbols[i].trim();
		}
		float [] digits = new float[symbols.length + 1];
		
		Node node = me.getFirstChildOfType(ASTPrimaryExpression.class);
		
		if(node == null)
		{
			return 0;
		}
		
		ASTPrimaryExpression lit = (ASTPrimaryExpression)node;
		
		for(int i=0; i<symbols.length + 1; i++)
		{
			digits[i] = getValueOfPrimaryExpression(lit);
			
			node = lit.getNextSibling();
			
			if(i != symbols.length)
			{
				if(node == null)
				{
					return 0;
				}
				
				if(!(node instanceof ASTPrimaryExpression))
				{
					return 0;
				}
			}
			
			lit = (ASTPrimaryExpression)node;
		}
		
		float d1 = digits[0];
		for(int i=0; i<symbols.length; i++)
		{
			float d2 = digits[i+1];
			String sym = symbols[i];
			
			if(sym.equals("*"))
			{
				d1 = d1 * d2;
			}
			else if(sym.equals("/"))
			{
				d1 = d1 / d2;
			}
			else
			{
				return 0;
			}
		}
		
		return d1;
	}
	
	public float getValueOfPrimaryExpression(ASTPrimaryExpression pe)
	{
		float r = 0;
		
		ASTLiteral lit = (ASTLiteral) pe.getFirstChildOfType(ASTLiteral.class);
		
		if(lit == null) return 0;
		
		r = Float.parseFloat(lit.getImage());
		
		return r;
	}
	
	public float getValueOfAdditiveExpression(ASTAdditiveExpression ae)
	{
		float r = 0;
		
		String [] symbols = ae.getImage().split("#");
		
		float [] digits = new float[symbols.length + 1];
		
		for(int i=0; i<symbols.length + 1; i++)
		{
			Node n = ae.jjtGetChild(i);
			
			if(n instanceof ASTPrimaryExpression)
			{
				digits[i] = getValueOfPrimaryExpression((ASTPrimaryExpression)n);
			}
			else if(n instanceof ASTMultiplicativeExpression)
			{
				digits[i] = getValueOfMultiplicativeExpression((ASTMultiplicativeExpression)n);
			}
			else
			{
				return 0;
			}
		}
		
		r = digits[0];
		for(int i=0; i<symbols.length; i++)
		{
			float t = digits[i+1];
			String sym = symbols[i];
			
			if(sym.equals("+"))
			{
				r = r + t;
			}
			else if(sym.equals("-"))
			{
				r = r - t;
			}
			else
			{
				return 0;
			}
		}
		
		return r;
	}
}
