package softtest.rules.java.fault.RL;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.jaxen.BaseXPath;
import org.jaxen.JaxenException;
import org.jaxen.XPath;

import softtest.ast.java.ASTAllocationExpression;
import softtest.ast.java.ASTArgumentList;
import softtest.ast.java.ASTConstructorDeclaration;
import softtest.ast.java.ASTMethodDeclarator;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTPrimarySuffix;
import softtest.ast.java.SimpleJavaNode;
import softtest.ast.java.SimpleNode;
import softtest.fsm.java.FSMMachine;
import softtest.fsm.java.FSMMachineInstance;
import softtest.fsm.java.FSMRelatedCalculation;
import softtest.jaxen.java.DocumentNavigator;

public class RL_INStateMachine
{   

	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("资源泄漏:  %d 行上传入的参数资源未被正确释放", errorline ,fsmmi.getResultString());
		}else{
			f.format("Resource Leak: resource passed in method  at line %d cannot be released by any method.", errorline);
		}
		fsmmi.setDescription(f.toString());
		f.close();
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
	private static String[] RES_STRINGS = { "FileOutputStream", "FilterOutputStream", "PipedOutputStream", "ObjectOutputStream", "BufferedOutputStream",
		"CheckedOutputStream", "CipherOutputStream", "DataOutputStream", "DeflaterOutputStream", "DigestOutputStream", "PrintStream", "ZipOutputStream",
		"GZIPOutputStream", "BufferedWriter", "FilterWriter", "OutputStreamWriter", "FileWriter", "PipedWriter", "PrintWriter", "AudioInputStream",
		"FilterInputStream", "BufferedInputStream", "CheckedInputStream", "CipherInputStream", "DataInputStream", "DigestInputStream",
		"InflaterInputStream", "GZIPInputStream", "ZipInputStream", "LineNumberInputStream", "ProgressMonitorInputStream", "PushbackInputStream",
		"ObjectInputStream", "FileInputStream", "PipedInputStream", "SequenceInputStream", "BufferedReader", "LineNumberReader", "FilterReader",
		"PushbackReader", "InputStreamReader", "FileReader", "PipedReader", "RandomAccessFile", "ZipFile", "JNLPRandomAccessFile", "Connection",
		"ResultSet", "Statement", "PooledConnection", "MidiDevice", "Receiver", "Transmitter", "AudioInputStream", "Line", "DatagramSocketImpl",
		"ServerSocket", "Socket", "SocketImpl", "JMXConnector", "RMIConnection", "RMIConnectionImpl", "RMIConnectionImpl_Stub", "RMIConnector",
		"RMIServerImpl", "ConsoleHandler", "FileHandler", "Handler", "MemoryHandler", "SocketHandler", "StreamHandler", "Scanner", "StartTlsResponse",
		"PreparedStatement", "CallableStatement",  "StreamPrintService", "CompositeContext", "Graphics", "InputContext", "InputMethod", "PaintContext", "Window",
		"DebugGraphics", "JInternalFrame", "ImageReader", "ImageWrite", "SaslClient", "SaslServer", "GSSContext", "GSSCredential",
		"Frame","Dialog","HttpURLConnection",};
	private static String[] RES_STRINGS2={ "getConnection", "createStatement", "executeQuery", "getResultSet", "prepareStatement", "prepareCall", "accept",
		"createImageInputStream","open","openStream","getChannel",
		"getGraphics","getResourceAsStream"};
	public static List<FSMMachineInstance> createRL_INStateMachines(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		String xPath =".//ClassOrInterfaceBody //MethodDeclarator" ;
		String xPath2=".//ClassOrInterfaceBody//ArgumentList";
		String xPath3=".//ClassOrInterfaceBody //ConstructorDeclaration";
		List argumentList = node.findXpath(xPath2);
		List methodDeclared = null;
		List constructorList = node.findXpath(xPath3);
		if(argumentList.size()==0)
			return list;
		methodDeclared = node.findXpath(xPath);
		Iterator argumentit = argumentList.iterator();
		Iterator methodit =null;
		Iterator constructorit = null;
		
		boolean flag;
	
		
		while(argumentit.hasNext())
		{   
			flag = true;
			if(methodDeclared.size()!=0)
			{ methodit = methodDeclared.iterator();}
			if(constructorList.size()!=0)
			{
				constructorit = constructorList.iterator();
			}
			String methodUsedImage;
			ASTArgumentList argument =(ASTArgumentList) argumentit.next();
			//System.out.println(argument.getImage());
			if( argument!=null && argument.jjtGetParent()!=null && argument.jjtGetParent() instanceof SimpleJavaNode)
			{    SimpleJavaNode primarysuffix =(SimpleJavaNode )argument.getFirstParentOfType(ASTPrimarySuffix.class);
			if( primarysuffix!=null && primarysuffix.jjtGetParent()!=null && primarysuffix.jjtGetParent() instanceof SimpleJavaNode)
			{
				SimpleJavaNode primaryexpression =(SimpleJavaNode)primarysuffix.jjtGetParent();
			   int childrenNumber = primaryexpression.jjtGetNumChildren();
			   if(childrenNumber-2>=0)
			   { SimpleJavaNode methodUsed = (SimpleJavaNode)primaryexpression.jjtGetChild(childrenNumber-2);
			  if(methodUsed.jjtGetNumChildren()==0)
			{  
				 methodUsedImage =methodUsed.getImage();
			}
			else 
			{
				 methodUsedImage =((SimpleJavaNode)methodUsed.jjtGetChild(0)).getImage();
				
			}
				
			  while(methodit!=null&&methodit.hasNext())
			  {
				  ASTMethodDeclarator methodDeclarator =(ASTMethodDeclarator) methodit.next();
				  if((methodDeclarator.getImage()).equals(methodUsedImage))
				  {  
					  flag = false;
					  break;
				  }
			  }
			  while(constructorit!=null&&constructorit.hasNext())
			  {
				  ASTConstructorDeclaration methodDeclarator =(ASTConstructorDeclaration) methodit.next();
				  if((methodDeclarator.getMethodName()).equals(methodUsedImage))
				  {  
					  flag = false;
					  break;
				  }
			  }
			List argumentName = argument.findChildrenOfType(ASTName.class);
			Class[] argumentClass = argument.getParameterTypes();
			for(Class c:argumentClass)
			{
				for( String a : RES_STRINGS)     
				{
					if(c.toString().endsWith(a)&&flag==true)
					{
						FSMMachineInstance fsminstance = fsm.creatInstance();
						fsminstance.setRelatedObject(new FSMRelatedCalculation(argument));
						fsminstance.setResultString(argument.getImage());
						list.add(fsminstance);
					}
				}
			}
			if(argumentName.size()!=0)
			{
				Iterator it = argumentName.iterator();
				//SimpleJavaNode value =(SimpleJavaNode)it.next()
				String a =((SimpleJavaNode)(it.next())).getImage();
				for(String str :RES_STRINGS2)
				{
					if(a.endsWith(str)&&flag==true)
					{
						FSMMachineInstance fsminstance = fsm.creatInstance();
						fsminstance.setRelatedObject(new FSMRelatedCalculation(argument));
						fsminstance.setResultString(argument.getImage());
						list.add(fsminstance);
					}
				}
			}
			
		
		
		}
		}
			else 
			{
				SimpleJavaNode x =(SimpleJavaNode ) argument.getFirstParentOfType(ASTAllocationExpression.class);
				if(x!=null&&x.jjtGetChild(0)!=null)
				{
					while(methodit!=null&&methodit.hasNext())
					  {
						  ASTMethodDeclarator methodDeclarator =(ASTMethodDeclarator) methodit.next();
						  if((methodDeclarator.getImage()).equals(x.getImage()))
						  {  
							  flag = false;
							  break;
						  }
					  }
					  while(constructorit!=null&&constructorit.hasNext())
					  {
						  ASTConstructorDeclaration methodDeclarator =(ASTConstructorDeclaration) methodit.next();
						  if((methodDeclarator.getMethodName()).equals(x.getImage()))
						  {  
							  flag = false;
							  break;
						  }
					  }
					List argumentName = argument.findChildrenOfType(ASTName.class);
					Class[] argumentClass = argument.getParameterTypes();
					for(Class c:argumentClass)
					{
						for( String a : RES_STRINGS)     
						{
							if(c.toString().endsWith(a)&&flag==true)
							{
								FSMMachineInstance fsminstance = fsm.creatInstance();
								fsminstance.setRelatedObject(new FSMRelatedCalculation(argument));
								fsminstance.setResultString(argument.getImage());
								list.add(fsminstance);
							}
						}
					}
					if(argumentName.size()!=0)
					{
						Iterator it = argumentName.iterator();
						String a =((SimpleJavaNode)(it.next())).getImage();
						for(String str :RES_STRINGS2)
						{
							if(a.endsWith(str)&&flag==true)
							{
								FSMMachineInstance fsminstance = fsm.creatInstance();
								fsminstance.setRelatedObject(new FSMRelatedCalculation(argument));
								fsminstance.setResultString(argument.getImage());
								list.add(fsminstance);
							}
						}
					}
				}
			}
		}
		}
		return list;
		
	}
  

	}
	

