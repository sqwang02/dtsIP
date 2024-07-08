package softtest.rules.java.fault.RL;

import softtest.fsm.java.*;

import java.util.*;

import org.jaxen.*;

import softtest.ast.java.*;
import softtest.jaxen.java.*;
import softtest.ast.java.ASTArgumentList;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTVariableDeclaratorId;
import softtest.ast.java.SimpleJavaNode;
import softtest.ast.java.SimpleNode;
import softtest.callgraph.java.method.AllocateFeatureListener;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.callgraph.java.method.ReleaseFeatureListener;
import softtest.config.java.Config;
import softtest.rules.java.AbstractStateMachine;
import softtest.symboltable.java.*;
import softtest.fsmanalysis.java.*;
import softtest.jaxen.java.DocumentNavigator;
import softtest.symboltable.java.NameOccurrence;
import softtest.symboltable.java.VariableNameDeclaration;

public class RLFieldStateMachine extends AbstractStateMachine{
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("资源泄漏:  %d 行上分配给私有成员 \'%s\' 的资源无法通过任何方法进行释放", errorline ,fsmmi.getResultString());
		}else{
			f.format("Resource Leak: resource stored in field \'%s\' at line %d cannot be released by any method.", fsmmi.getResultString(),errorline);
		}
		fsmmi.setDescription(f.toString());
		f.close();
	}
	
	@Override
	public void registerFeature(FeatureListenerSet listeners) {
		// TODO Auto-generated method stub
	}

	@Override
	public void registerPrecondition(PreconditionListenerSet listeners) {
		// TODO Auto-generated method stub		
	}
	/** 在节点node上查找xPath */
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

	// 释放函数为close的可能资源
	private static String[] RES_STRINGS1 = { "FileOutputStream", "FilterOutputStream", "PipedOutputStream", "ObjectOutputStream", "BufferedOutputStream",
			"CheckedOutputStream", "CipherOutputStream", "DataOutputStream", "DeflaterOutputStream", "DigestOutputStream", "PrintStream", "ZipOutputStream",
			"GZIPOutputStream", "BufferedWriter", "FilterWriter", "OutputStreamWriter", "FileWriter", "PipedWriter", "PrintWriter", "AudioInputStream",
			"FilterInputStream", "BufferedInputStream", "CheckedInputStream", "CipherInputStream", "DataInputStream", "DigestInputStream",
			"InflaterInputStream", "GZIPInputStream", "ZipInputStream", "LineNumberInputStream", "ProgressMonitorInputStream", "PushbackInputStream",
			"ObjectInputStream", "FileInputStream", "PipedInputStream", "SequenceInputStream", "BufferedReader", "LineNumberReader", "FilterReader",
			"PushbackReader", "InputStreamReader", "FileReader", "PipedReader", "RandomAccessFile", "ZipFile", "JNLPRandomAccessFile", "Connection",
			"ResultSet", "Statement", "PooledConnection", "MidiDevice", "Receiver", "Transmitter", "AudioInputStream", "Line", "DatagramSocketImpl",
			"ServerSocket", "Socket", "SocketImpl", "JMXConnector", "RMIConnection", "RMIConnectionImpl", "RMIConnectionImpl_Stub", "RMIConnector",
			"RMIServerImpl", "ConsoleHandler", "FileHandler", "Handler", "MemoryHandler", "SocketHandler", "StreamHandler", "Scanner", "StartTlsResponse",
			"PreparedStatement", "CallableStatement", };

	// 释放函数为dispose的资源
	private static String RES_STRINGS2[] = { "StreamPrintService", "CompositeContext", "Graphics", "InputContext", "InputMethod", "PaintContext", "Window",
			"DebugGraphics", "JInternalFrame", "ImageReader", "ImageWrite", "SaslClient", "SaslServer", "GSSContext", "GSSCredential",
			"Frame","Dialog",
	/* "Color","Font","Cursor","GC","Display","Image","Printer","Region", */
	};

	// 释放函数为disconnect的资源
	private static String RES_STRINGS3[] = { "HttpURLConnection",
	/* "URLConnection","HttpsURLConnection","JarURLConnection", */
	};

	private static String RES_STRINGS4[] = { "getConnection", "createStatement", "executeQuery", "getResultSet", "prepareStatement", "prepareCall", "accept",
		"createImageInputStream","open","openStream","getChannel",
		"getGraphics","getResourceAsStream"};

	// 应该去除的资源
	private static String RES_STRINGS5[] = { "ByteArrayOutputStream", "CharArrayReader", "CharArrayWriter", "StringWriter", "ByteArrayInputStream",
			"StringBufferInputStream", "StringReader", };

	// 不应该去除得资源
	private static String RES_STRINGS6[] = { "FileOutputStream", "PipedOutputStream", "FileWriter", "PipedWriter", "FileInputStream", "PipedInputStream",
			"FileReader", "PipedReader", "RandomAccessFile",
			"Dialog","Frame",};

	private static String addResString(StringBuffer buffer) {
		for (String s : RES_STRINGS1) {
			buffer.append("(" + s + ")|(.+\\." + s + ")|");
		}
		for (String s : RES_STRINGS2) {
			buffer.append("(" + s + ")|(.+\\." + s + ")|");
		}
		for (String s : RES_STRINGS3) {
			buffer.append("(" + s + ")|(.+\\." + s + ")|");
		}
		if (RES_STRINGS1.length + RES_STRINGS2.length + RES_STRINGS3.length > 0) {
			buffer.deleteCharAt(buffer.length() - 1);
		}
		return buffer.toString();
	}

	private static String addResString4(StringBuffer buffer) {
		for (String s : RES_STRINGS4) {
			buffer.append("(" + s + ")|(.+\\." + s + ")|");
		}
		if (RES_STRINGS4.length > 0) {
			buffer.deleteCharAt(buffer.length() - 1);
		}
		return buffer.toString();
	}

	private static String addResString5(StringBuffer buffer) {
		for (String s : RES_STRINGS5) {
			buffer.append("(" + s + ")|(.+\\." + s + ")|");
		}
		if (RES_STRINGS5.length > 0) {
			buffer.deleteCharAt(buffer.length() - 1);
		}
		return buffer.toString();
	}

	private static String addResString6(StringBuffer buffer) {
		for (String s : RES_STRINGS6) {
			buffer.append("(" + s + ")|(.+\\." + s + ")|");
		}
		if (RES_STRINGS6.length > 0) {
			buffer.deleteCharAt(buffer.length() - 1);
		}
		return buffer.toString();
	}

	private static boolean checkRes(VariableNameDeclaration v) {
		String image = v.getTypeImage();
		String regex = "^(";
		regex = addResString6(new StringBuffer(regex));
		regex += ")$";
		if (image == null) {
			return false;
		}

		if (image.matches(regex)) {
			/* 形如：
			 * FileOutputStream v; 
			 * */
			return true;
		} else {
			regex = "^(";
			regex = addResString5(new StringBuffer(regex));
			regex += ")$";
			if (image.matches(regex)) {
				/*形如：
				 * 	ByteArrayOutputStream os=new ByteArrayOutputStream();
				 *  ObjectOutputStream v=new ObjectOutputStream(os); 
				 *  */
				return false;
			}
			
			if (!(v.getNode() instanceof ASTVariableDeclaratorId)) {
				return false;
			}

			ASTVariableDeclaratorId id = (ASTVariableDeclaratorId) v.getNode();
			
			StringBuffer buffer = new StringBuffer(
					"../VariableInitializer/Expression/PrimaryExpression/PrimaryPrefix/AllocationExpression/ClassOrInterfaceType[matches(@Image,\'^(");
			String xPath = addResString5(buffer);
			xPath += ")$\')]";
			List temp = findTreeNodes(id, xPath);
			if (temp.size() != 0) {
				/*
				 * OutputStream os=new ByteArrayOutputStream();
				 * 形如： ObjectOutputStream v=new ObjectOutputStream(os);
				 */
				return false;
			}			

//			buffer = new StringBuffer(
//			"../VariableInitializer/Expression/PrimaryExpression/PrimaryPrefix/AllocationExpression/ClassOrInterfaceType[matches(@Image,\'^(");
//			xPath = addResString6(buffer);
//			xPath += ")$\')]";
//			temp = findTreeNodes(id, xPath);
//			if (temp.size() != 0) {
//				/*
//				 * OutputStream os=new FileOutputStream("a.txt");
//				 * 形如： ObjectOutputStream v=new ObjectOutputStream(os);
//				 */
//				return true;
//			}				
			
			buffer = new StringBuffer(
					"../VariableInitializer/Expression/PrimaryExpression/PrimaryPrefix/AllocationExpression/Arguments/ArgumentList/Expression//AllocationExpression/ClassOrInterfaceType[matches(@Image,\'^(");
			xPath = addResString5(buffer);
			xPath += ")$\')]";
			temp = findTreeNodes(id, xPath);
			if (temp.size() != 0) {
				/*形如：
				 *  ObjectOutputStream v=new ObjectOutputStream(new ByteArrayOutputStream()); 
				 *  */
				return false;
			}

//			buffer = new StringBuffer(
//					"../VariableInitializer/Expression/PrimaryExpression/PrimaryPrefix/AllocationExpression/Arguments/ArgumentList/Expression//AllocationExpression/ClassOrInterfaceType[matches(@Image,\'^(");
//			xPath = addResString6(buffer);
//			xPath += ")$\')]";
//			temp = findTreeNodes(id, xPath);
//			if (temp.size() != 0) {
//				/*形如：
//				 *  ObjectOutputStream v=new ObjectOutputStream(new FileOutputStream("a.txt")); 
//				 *  */
//				return true;
//			}

			xPath = "../VariableInitializer/Expression/PrimaryExpression/PrimaryPrefix/AllocationExpression/Arguments/ArgumentList/Expression/PrimaryExpression/PrimaryPrefix/Name";
			temp = findTreeNodes(id, xPath);
			if (temp.size() > 0) {
				ASTName tname = (ASTName) temp.get(0);
				if (tname.getNameDeclaration() instanceof VariableNameDeclaration) {
					/*形如：
					 *  ObjectOutputStream v=new ObjectOutputStream(os); 
					 *  应该继续检查变量os
					 *  */
					VariableNameDeclaration tv = (VariableNameDeclaration) tname.getNameDeclaration();
					if (tv!=v&&checkRes(tv)) {
						return true;
					} else {
						return false;
					}
				} else {
					return false;
				}
			}

			Map variableNames = null;
			variableNames = v.getDeclareScope().getVariableDeclarations();

			if (variableNames == null) {
				return false;
			}
			ArrayList occs = (ArrayList) variableNames.get(v);
			if (occs == null) {
				return false;
			}
			for (Object o : occs) {
				NameOccurrence occ = (NameOccurrence) o;
				if (occ.getOccurrenceType() == NameOccurrence.OccurrenceType.DEF) {
					buffer = new StringBuffer(
							"../../../Expression/PrimaryExpression/PrimaryPrefix/AllocationExpression/Arguments/ArgumentList/Expression//AllocationExpression/ClassOrInterfaceType[matches(@Image,\'^(");
					xPath = addResString5(buffer);
					xPath += ")$\')]";
					temp = findTreeNodes(occ.getLocation(), xPath);
					if (temp.size() != 0) {
						/*形如：
						 *  ObjectOutputStream v;
						 *  v=new ObjectOutputStream(new ByteArrayOutputStream()); 
						 *  */
						return false;
					}

//					buffer = new StringBuffer(
//							"../../../Expression/PrimaryExpression/PrimaryPrefix//AllocationExpression/ClassOrInterfaceType[matches(@Image,\'^(");
//					xPath = addResString6(buffer);
//					xPath += ")$\')]";
//					temp = findTreeNodes(occ.getLocation(), xPath);
//					if (temp.size() != 0) {
//						/*形如：
//						 *  ObjectOutputStream v;
//						 *  v=new ObjectOutputStream(new FileOutputStream()); 
//						 *  */
//						return true;
//					}

					xPath = "../../../Expression/PrimaryExpression/PrimaryPrefix/AllocationExpression/Arguments/ArgumentList/Expression/PrimaryExpression/PrimaryPrefix/Name";
					temp = findTreeNodes(occ.getLocation(), xPath);
					if (temp.size() > 0) {
						ASTName tname = (ASTName) temp.get(0);
						if (tname.getNameDeclaration() instanceof VariableNameDeclaration) {
							VariableNameDeclaration tv = (VariableNameDeclaration) tname.getNameDeclaration();
							/*形如：
							 *  ObjectOutputStream tv;
							 *  tv==new ObjectOutputStream(os); 
							 *  应该继续检查变量os
							 *  */
							if (tv!=v&&checkRes(tv)) {
								return true;
							} else {
								return false;
							}
						} else {
							return false;
						}
					}
					break;
				}
			}
		}
		return true;
	}

	public static List<FSMMachineInstance> createRLFieldStateMachines(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		String xPath = "";
		List evaluationResults = null;

		StringBuffer buffer = new StringBuffer(".//FieldDeclaration[@Private='true' and ./Type/ReferenceType/ClassOrInterfaceType[matches(@Image,\'^(");
		xPath = addResString(buffer);
		xPath += ")$\')]]/VariableDeclarator/VariableDeclaratorId";

		evaluationResults = node.findXpath(xPath);
		Iterator iout = evaluationResults.iterator();
		out: while (iout.hasNext()) {
			ASTVariableDeclaratorId idout = (ASTVariableDeclaratorId) iout.next();
			FSMMachineInstance fsminstance = fsm.creatInstance();
			fsminstance.setRelatedObject(new FSMRelatedCalculation(idout));
			fsminstance.setResultString(idout.getImage());

			buffer = new StringBuffer(
					".//VariableDeclaratorId[../VariableInitializer/Expression/PrimaryExpression/PrimaryPrefix/AllocationExpression/ClassOrInterfaceType[matches(@Image,\'^(");
			xPath = addResString(buffer);
			xPath += ")$\')]]";

			evaluationResults = node.findXpath(xPath);
			Iterator i = evaluationResults.iterator();
			while (i.hasNext()) {
				ASTVariableDeclaratorId id = (ASTVariableDeclaratorId) i.next();
				if (id.getNameDeclaration() != idout.getNameDeclaration()) {
					continue;
				}

				// 去除那些非资源
				buffer = new StringBuffer(
						"../VariableInitializer/Expression/PrimaryExpression/PrimaryPrefix/AllocationExpression/Arguments/ArgumentList/Expression//AllocationExpression/ClassOrInterfaceType[matches(@Image,\'^(");
				xPath = addResString5(buffer);
				xPath += ")$\')]";
				List temp = findTreeNodes(id, xPath);
				if (temp.size() != 0) {
					continue;
				}

				buffer = new StringBuffer(
						"../VariableInitializer/Expression/PrimaryExpression/PrimaryPrefix//AllocationExpression/ClassOrInterfaceType[matches(@Image,\'^(");
				xPath = addResString6(buffer);
				xPath += ")$\')]";
				temp = findTreeNodes(id, xPath);
				if (temp.size() == 0) {
					xPath = "../VariableInitializer/Expression/PrimaryExpression/PrimaryPrefix/AllocationExpression/Arguments/ArgumentList/Expression//PrimaryExpression/PrimaryPrefix/Name";
					temp = findTreeNodes(id, xPath);
					if (temp.size() > 0) {
						ASTName tname = (ASTName) temp.get(0);
						if (tname.getNameDeclaration() instanceof VariableNameDeclaration) {
							VariableNameDeclaration tv = (VariableNameDeclaration) tname.getNameDeclaration();
							if (!checkRes(tv)) {
								continue;
							}
						} else {
							continue;
						}
					}
				}
				list.add(fsminstance);
				continue out;
			}

			// 不在在句柄变量声明时，分配资源，表达式语句
			buffer = new StringBuffer(
					".//StatementExpression[./AssignmentOperator[@Image=\'=\'] and ./Expression/PrimaryExpression/PrimaryPrefix/AllocationExpression/ClassOrInterfaceType[matches(@Image,\'^(");
			xPath = addResString(buffer);
			xPath += ")$\')]]/PrimaryExpression/PrimaryPrefix/Name";

			evaluationResults = findTreeNodes(node, xPath);
			i = evaluationResults.iterator();
			while (i.hasNext()) {
				ASTName name = (ASTName) i.next();

				if (name.getNameDeclaration() != idout.getNameDeclaration()) {
					continue;
				}

				// 去除那些非资源
				buffer = new StringBuffer(
						"../../../Expression/PrimaryExpression/PrimaryPrefix/AllocationExpression/Arguments/ArgumentList/Expression//AllocationExpression/ClassOrInterfaceType[matches(@Image,\'^(");
				xPath = addResString5(buffer);
				xPath += ")$\')]";
				List temp = findTreeNodes(name, xPath);
				if (temp.size() != 0) {
					continue;
				}

				buffer = new StringBuffer("../../../Expression/PrimaryExpression/PrimaryPrefix//AllocationExpression/ClassOrInterfaceType[matches(@Image,\'^(");
				xPath = addResString6(buffer);
				xPath += ")$\')]";
				temp = findTreeNodes(name, xPath);
				if (temp.size() == 0) {
					xPath = "../../../Expression/PrimaryExpression/PrimaryPrefix/AllocationExpression/Arguments/ArgumentList/Expression//PrimaryExpression/PrimaryPrefix/Name";
					temp = findTreeNodes(name, xPath);
					if (temp.size() > 0) {
						ASTName tname = (ASTName) temp.get(0);
						if (tname.getNameDeclaration() instanceof VariableNameDeclaration) {
							VariableNameDeclaration tv = (VariableNameDeclaration) tname.getNameDeclaration();
							if (!checkRes(tv)) {
								continue;
							}
						} else {
							continue;
						}
					}
				}

				list.add(fsminstance);
				continue out;
			}
			// 表达式
			buffer = new StringBuffer(
					".//Expression[./AssignmentOperator[@Image=\'=\'] and ./Expression/PrimaryExpression/PrimaryPrefix/AllocationExpression/ClassOrInterfaceType[matches(@Image,\'^(");
			xPath = addResString(buffer);
			xPath += ")$\')]]/PrimaryExpression/PrimaryPrefix/Name";

			evaluationResults = findTreeNodes(node, xPath);
			i = evaluationResults.iterator();
			while (i.hasNext()) {
				ASTName name = (ASTName) i.next();

				if (name.getNameDeclaration() != idout.getNameDeclaration()) {
					continue;
				}

				// 去除那些非资源
				buffer = new StringBuffer(
						"../../../Expression/PrimaryExpression/PrimaryPrefix/AllocationExpression/Arguments/ArgumentList/Expression//AllocationExpression/ClassOrInterfaceType[matches(@Image,\'^(");
				xPath = addResString5(buffer);
				xPath += ")$\')]";
				List temp = findTreeNodes(name, xPath);
				if (temp.size() != 0) {
					continue;
				}

				buffer = new StringBuffer("../../../Expression/PrimaryExpression/PrimaryPrefix//AllocationExpression/ClassOrInterfaceType[matches(@Image,\'^(");
				xPath = addResString6(buffer);
				xPath += ")$\')]";
				temp = findTreeNodes(name, xPath);
				if (temp.size() == 0) {
					xPath = "../../../Expression/PrimaryExpression/PrimaryPrefix/AllocationExpression/Arguments/ArgumentList/Expression//PrimaryExpression/PrimaryPrefix/Name";
					temp = findTreeNodes(name, xPath);
					if (temp.size() > 0) {
						ASTName tname = (ASTName) temp.get(0);
						if (tname.getNameDeclaration() instanceof VariableNameDeclaration) {
							VariableNameDeclaration tv = (VariableNameDeclaration) tname.getNameDeclaration();
							if (!checkRes(tv)) {
								continue;
							}
						} else {
							continue;
						}
					}
				}

				list.add(fsminstance);
				continue out;
			}

			buffer = new StringBuffer(".//VariableDeclaratorId[../VariableInitializer/Expression/PrimaryExpression/PrimaryPrefix/Name[matches(@Image,\'^(");
			xPath = addResString4(buffer);
			xPath += ")$\')]]";
			evaluationResults = findTreeNodes(node, xPath);
			i = evaluationResults.iterator();
			while (i.hasNext()) {
				ASTVariableDeclaratorId id = (ASTVariableDeclaratorId) i.next();
				if (id.getNameDeclaration() != idout.getNameDeclaration()) {
					continue;
				}
				VariableNameDeclaration v = (VariableNameDeclaration) id.getNameDeclaration();
				String image = v.getTypeImage();
				String regex = "^(";
				regex = addResString(new StringBuffer(regex));
				regex += ")$";
				if (image == null || !image.matches(regex)) {
					continue;
				}
				
				list.add(fsminstance);
				continue out;
			}

			buffer = new StringBuffer(
					".//StatementExpression[./AssignmentOperator[@Image=\'=\'] and ./Expression/PrimaryExpression/PrimaryPrefix/Name[matches(@Image,\'^(");
			xPath = addResString4(buffer);
			xPath += ")$\')]]/PrimaryExpression/PrimaryPrefix/Name";

			evaluationResults = findTreeNodes(node, xPath);
			i = evaluationResults.iterator();
			while (i.hasNext()) {
				ASTName name = (ASTName) i.next();

				if (name.getNameDeclaration() != idout.getNameDeclaration()) {
					continue;
				}

				VariableNameDeclaration v = (VariableNameDeclaration) name.getNameDeclaration();
				String image = v.getTypeImage();
				String regex = "^(";
				regex = addResString(new StringBuffer(regex));
				regex += ")$";
				if (image == null || !image.matches(regex)) {
					continue;
				}

				list.add(fsminstance);
				continue out;
			}

			buffer = new StringBuffer(
					".//Expression[./AssignmentOperator[@Image=\'=\'] and ./Expression/PrimaryExpression/PrimaryPrefix/Name[matches(@Image,\'^(");
			xPath = addResString4(buffer);
			xPath += ")$\')]]/PrimaryExpression/PrimaryPrefix/Name";

			evaluationResults = findTreeNodes(node, xPath);
			i = evaluationResults.iterator();
			while (i.hasNext()) {
				ASTName name = (ASTName) i.next();
				if (name.getNameDeclaration() != idout.getNameDeclaration()) {
					continue;
				}

				VariableNameDeclaration v = (VariableNameDeclaration) name.getNameDeclaration();
				String image = v.getTypeImage();
				String regex = "^(";
				regex = addResString(new StringBuffer(regex));
				regex += ")$";
				if (image == null || !image.matches(regex)) {
					continue;
				}
				
				list.add(fsminstance);
				continue out;
			}
		}

		List templist = new ArrayList();
		iout = list.iterator();
		out2: while (iout.hasNext()) {
		//	if(iout.hasNext()){				
			FSMMachineInstance fsminstance = (FSMMachineInstance) iout.next();
			ASTVariableDeclaratorId idout = (ASTVariableDeclaratorId) fsminstance.getRelatedObject().getTagTreeNode();
			VariableNameDeclaration v = (VariableNameDeclaration) idout.getNameDeclaration();
			Map variableNames = null;
			try {
				variableNames = v.getDeclareScope().getVariableDeclarations();
			} catch (RuntimeException e) {
			}
			if (variableNames == null) {
				continue;
			}
			ArrayList occs = (ArrayList) variableNames.get(v);
			if (occs == null) {
				continue;
			}
			for (Object o : occs) {
				NameOccurrence occ = (NameOccurrence) o;
				if (occ.getLocation() instanceof ASTName) {
					String image = occ.getLocation().getImage();
					if (image != null) {
						if (image.endsWith(".close")) {
							buffer = new StringBuffer("^(");
							for (String s : RES_STRINGS1) {
								buffer.append("(" + s + ")|(.+\\." + s + ")|");
							}
							if (RES_STRINGS1.length > 0) {
								buffer.deleteCharAt(buffer.length() - 1);
							}
							buffer.append(")$");
							if (v.getTypeImage().matches(buffer.toString())) {
								continue out2;
							}
						} else if (image.endsWith("dispose")) {
							buffer = new StringBuffer("^(");
							for (String s : RES_STRINGS2) {
								buffer.append("(" + s + ")|(.+\\." + s + ")|");
							}
							if (RES_STRINGS2.length > 0) {
								buffer.deleteCharAt(buffer.length() - 1);
							}
							buffer.append(")$");
							if (v.getTypeImage().matches(buffer.toString())) {
								continue out2;
							}
						} else if (image.endsWith("disconnect")) {
							buffer = new StringBuffer("^(");
							for (String s : RES_STRINGS3) {
								buffer.append("(" + s + ")|(.+\\." + s + ")|");
							}
							if (RES_STRINGS3.length > 0) {
								buffer.deleteCharAt(buffer.length() - 1);
							}
							buffer.append(")$");
							if (v.getTypeImage().matches(buffer.toString())) {
								continue out2;
							}
						}
					}
				}
				
			}
			if (!Config.RL_USEASPARAM) {
				String xpath=".//ArgumentList";
				List nodes= findTreeNodes( ProjectAnalysis.getCurrent_astroot(),xpath);
				Iterator i = nodes.iterator();
				while (i.hasNext()) {
					Object o = i.next();
					ASTArgumentList arglist = (ASTArgumentList) o;
					for (int k = 0; k < arglist.jjtGetNumChildren(); k++) {
						ASTName name = (ASTName) ((SimpleNode) arglist.jjtGetChild(k)).getSingleChildofType(ASTName.class);
						if (name != null && name.getNameDeclaration() instanceof VariableNameDeclaration) {
							if(name.getImage()!=null&&name.getImage().contains(".")){
								continue;
							}							
							if(name.getNameDeclaration()==v){
//								iout.remove();//added by yang
//						         list.remove(fsminstance);//added by yang
								continue out2;						
							}
						}
					}
				}
			}
			templist.add(fsminstance);//remarked by yang 
		}
		
		return templist; //remarked by yang
		//return list;//added by yang
	}
}
