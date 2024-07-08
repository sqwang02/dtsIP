package softtest.rules.java.safety.ICE;

import softtest.fsm.java.*;
import softtest.jaxen.java.DocumentNavigator;

import java.util.*;

import org.jaxen.*;

import softtest.ast.java.*;
import softtest.jaxen.java.*;
import softtest.rules.java.AbstractStateMachine;
import softtest.ast.java.ASTClassOrInterfaceDeclaration;
import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.cfg.java.*;
import softtest.config.java.Config;


/** （2）	故障名称：方法权限设置
故障描述：这类错误发生在当方法没有定义为私有时。这错误不会在接口和方法重载或被重载时报告。范围解释规则限定了类的可用范围，默认情况下范围是对Applet类的扩展。
举例： 
public class Example_218 extends Applet {
private void methodA() {
}
public void methodB() {
}
}
解决办法：
针对继承自Applet, Servlet的类中的共有方法，若没有出现在其父类中，则认为是不恰当的。

2008-9-22
 */

public class MethodAccessStateMachine extends AbstractStateMachine{
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("封装不当模式: %d 行方法没有定义为私有，可能造漏洞", errorline);
		}else{
			f.format("Incorrect Encapsule:method on line %d should be private",errorline);
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
	
	static Set appletMthds;
	static Set servletMthds;
	
	static {
		appletMthds = new HashSet();
		
		appletMthds.add("destroy");
		appletMthds.add("getAccessibleContext");
		appletMthds.add("getAppletContext");
		appletMthds.add("getAppletInfo");
		appletMthds.add("getAudioClip");
		appletMthds.add("getCodeBase");
		appletMthds.add("getDocumentBase");
		appletMthds.add("getImage");
		appletMthds.add("getLocale");
		appletMthds.add("getParameter");
		appletMthds.add("getParameterInfo");
		appletMthds.add("init");
		appletMthds.add("isActive");
		appletMthds.add("newAudioClip");
		appletMthds.add("play");
		appletMthds.add("resize");
		appletMthds.add("resize");
		appletMthds.add("setStub");
		appletMthds.add("showStatus");
		appletMthds.add("start");
		appletMthds.add("stop");
		appletMthds.add("addNotify");
		appletMthds.add("getAccessibleContext");
		appletMthds.add("getComponentCount");
		appletMthds.add("countComponents");
		appletMthds.add("getComponent");
		appletMthds.add("getInsets");
		appletMthds.add("insets");
		appletMthds.add("add");
		appletMthds.add("setComponentZOrder");
		appletMthds.add("getComponentZOrder");
		appletMthds.add("remove");
		appletMthds.add("remvoeall");
		appletMthds.add("layout");
		appletMthds.add("doLayout");
		appletMthds.add("setLayout");
		appletMthds.add("getLayout");
		appletMthds.add("setFront");
		appletMthds.add("preferredSize");
		appletMthds.add("getPreferredSize");
		appletMthds.add("getMinimumSize");
		appletMthds.add("minimumSize");
		appletMthds.add("getMaximumSize");
		appletMthds.add("getAlignmentX");
		appletMthds.add("validate");
		appletMthds.add("addContainerListener");
		appletMthds.add("printComponents");
		appletMthds.add("paintComponents");
		appletMthds.add("print");
		appletMthds.add("update");
		appletMthds.add("paint");
		appletMthds.add("getAlignmentY");
		appletMthds.add("removeContainerListener");
		appletMthds.add("getContainerListeners");
		appletMthds.add("getListeners");
		appletMthds.add("deliverEvent");
		appletMthds.add("getComponentAt");
		appletMthds.add("locate");
		appletMthds.add("getMousePosition");
		appletMthds.add("findComponentAt");
		appletMthds.add("addNotify");
		appletMthds.add("removeNotify");
		appletMthds.add("isAncestorOf");
		appletMthds.add("list");
		appletMthds.add("setFocusTraversalKeys");
		appletMthds.add("getFocusTraversalKeys");
		appletMthds.add("areFocusTraversalKeysSet");
		appletMthds.add("isFocusCycleRoot");
		appletMthds.add("transferFocusBackward");
		appletMthds.add("setFocusTraversalPolicy");
		appletMthds.add("getFocusTraversalPolicy");
		appletMthds.add("isFocusTraversalPolicy");
		appletMthds.add("isFocusTraversalPolicyProvider");
		appletMthds.add("setFocusTraversalPolicyProvider");
		appletMthds.add("isFocusCycleRoot");
		appletMthds.add("setFocusCycleRoot");
		appletMthds.add("transferFocusDownCycle");
		appletMthds.add("applyComponentOrientation");
		appletMthds.add("addPropertyChangeListener");
		
		////////////////////////// Object
		appletMthds.add("getClass");
		appletMthds.add("hashCode");
		appletMthds.add("equals");
		appletMthds.add("toString");
		
		servletMthds = new HashSet();
		servletMthds.add("service");
		servletMthds.add("toString");
		servletMthds.add("equals");
		servletMthds.add("hashCode");
		servletMthds.add("getClass");
	}
	
	private static List getTreeNode(SimpleJavaNode node, String xStr) {
		List evalRlts = new ArrayList();
		try {
			XPath xpath = new BaseXPath(xStr, new DocumentNavigator());
			evalRlts = xpath.selectNodes(node);
		} catch (JaxenException e) {
			if (softtest.config.java.Config.DEBUG) {
				e.printStackTrace();
			}
			throw new RuntimeException("xpath error",e);
		}
		return evalRlts;
	}
	
	// 返回node下的所有公共方法的节点
	private static List<ASTMethodDeclaration> getPublicMethod(SimpleJavaNode clsRoot) {
		String xpathPubMthd = ".//MethodDeclaration[ @Public='true']";
		return getTreeNode(clsRoot, xpathPubMthd);
	}
	
	private static ASTMethodDeclaration getMethodDeclaration(SimpleJavaNode node) {
		SimpleJavaNode par = node;
		while(par != null && !(par instanceof ASTMethodDeclaration)) {
			par = (SimpleJavaNode)par.jjtGetParent();
		}
		return (ASTMethodDeclaration)par;
	}
	
	public static List<FSMMachineInstance> createMAStateMachines(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		
		// Applet
		String xpathApplet = ".//ClassOrInterfaceDeclaration[./ExtendsList/ClassOrInterfaceType[@Image='Applet'] and @Nested='false' and @Interface='false' and @Native='false' ]";
		List<ASTClassOrInterfaceDeclaration> astCls = getTreeNode(node, xpathApplet);
		for(ASTClassOrInterfaceDeclaration astC : astCls) {
			if(! (astC.getImage().contains("applet"))
			&& ! (astC.getImage().contains("Applet"))
			&& ! (astC.getImage().contains("JApplet"))
			) {
				continue;
			}
			List<ASTMethodDeclaration> astMthds = getPublicMethod(astC);
			for(ASTMethodDeclaration mthd : astMthds) {
				if(0 != mthd.getScope().getEnclosingClassScope().getClassName().compareTo(astC.getImage())) {
					continue;
				}
				String mname = mthd.getMethodName();
				if( ! appletMthds.contains(mname) ) {
					newFSM(fsm, list, "public method declaration not inherited from Applet", mthd);
				}
			}
		}
		/*
		// Servlet
		String xpathServlet = ".//ClassOrInterfaceDeclaration[./ExtendsList/ClassOrInterfaceType[@Image='HttpServlet'] and @Nested='false' and @Interface='false' and @Native='false' ]";
		astCls = getTreeNode(node, xpathServlet);
		for(ASTClassOrInterfaceDeclaration astC : astCls) {
			List<ASTMethodDeclaration> astMthds = getPublicMethod(astC);
			for(ASTMethodDeclaration mthd : astMthds) {
				if(0 != mthd.getScope().getEnclosingClassScope().getClassName().compareTo(astC.getImage())) {
					continue;
				}
				String mname = mthd.getMethodName();
				if( ! servletMthds.contains(mname) ) {
					newFSM(fsm, list, "public method declaration not inherited from HttpServlet", mthd);
				}
			}
		}
		*/
		
		
		
		return list;
	}
	
	
	public static boolean checkICMA(VexNode vex,FSMMachineInstance fsmInst) {
		
		return true;
	}
	
	private static void newFSM(FSMMachine fsm, List fsms, String result, SimpleJavaNode ast) {
		FSMMachineInstance fsminstance = fsm.creatInstance();
		fsminstance.setRelatedObject(new FSMRelatedCalculation(ast));
		fsminstance.setResultString(result);
		fsms.add(fsminstance);
	}
	
	
	public static void logc1(String str) {
		logc("createURLSFSM(..) - " + str);
	}

	public static void logc(String str) {
		if( Config.DEBUG ) {
			System.out.println("MethodAccessStateMechine::" + str);
		}
	}
}
