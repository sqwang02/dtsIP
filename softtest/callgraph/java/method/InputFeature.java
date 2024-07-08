/**
 * 
 */
package softtest.callgraph.java.method;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;


import org.jaxen.JaxenException;

import softtest.ast.java.ASTBlockStatement;
import softtest.ast.java.ASTExpression;
import softtest.ast.java.ASTLocalVariableDeclaration;
import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTPrimaryExpression;
import softtest.ast.java.ASTPrimaryPrefix;
import softtest.ast.java.ASTPrimarySuffix;
import softtest.ast.java.ASTReturnStatement;
import softtest.ast.java.ASTVariableDeclaratorId;
import softtest.ast.java.ExpressionBase;
import softtest.ast.java.JavaParserVisitorAdapter;
import softtest.ast.java.SimpleJavaNode;
import softtest.fsmanalysis.java.ProjectAnalysis;
import softtest.summary.lib.java.TDLibParser;
import softtest.summary.lib.java.TaintedInfo;
import softtest.symboltable.java.NameDeclaration;

/**
 * @author ��ƽ��
 *
 */
public class InputFeature extends AbstractFeature
{
	/**
	 * ���ڴ��JDK���е���ڹ���
	 */
	public static Hashtable<String,TaintedInfo> inputTable = new Hashtable<String,TaintedInfo >();
	
	/**
	 * ���ڴ�Ÿú�����������Ϣ
	 * ����key������Ⱦ��ţ�ע��0��ʾ����ֵ
	 */
	private Hashtable<Integer,List<String> > mapInfo = new Hashtable<Integer,List<String> >();
	
	@Override
	/**
	 * ����ýڵ�ĺ���ժҪ
	 * 1 ���ȼ���JDK���е���ڹ���
	 * 2 ����JDK�����ڹ���Ϊ�Զ��庯�����ɺ���ժҪ
	 */
	public void listen(SimpleJavaNode node, FeatureSet set)
	{
		//����JDK���е���ڹ���
		if(InputFeature.inputTable.size() == 0)
		{
			TDLibParser tdlibParser = new TDLibParser("./cfg/td/TD_entry.xml",null,null);
			InputFeature.inputTable = tdlibParser.parseInputReg();
		}
		
		SimpleJavaNode treenode = node;
		InputFeatureVisitor visitor = new InputFeatureVisitor();
		//����JDK�����ڹ���Ϊ�Զ��庯�����ɺ���ժҪ
		treenode.jjtAccept(visitor, null);
		if(mapInfo.size() > 0)
		{
			set.addFeature(this);
		}
	}
	
	/**
	 * �Ը��﷨�ڵ���н�һ���жϣ���ȷ���Ƿ�Ϊ���뺯��
	 * 
	 * 1 �鿴�Ӻ�����ժҪ��Ϊ���������ɺ���ժҪ��
	 * 2 ������ڹ������ɺ���ժҪ
	 * 
	 * @param pe ASTPrimaryExpression
	 * @return
	 */
	private boolean futherCheck(ASTPrimaryExpression pe) 
	{
		/*
		 * 1 ��֤�ýڵ��Ǻ������ýڵ�
		 */
		if (pe == null || pe.jjtGetNumChildren()<2) {
			return false;
		}
		
		/*
		 * 2 ��ȡ���������β��б�
		 */
		ASTMethodDeclaration md = (ASTMethodDeclaration)pe.getFirstParentOfType(ASTMethodDeclaration.class);
		if (md == null) {
			return false;
		}
			//params���ڴ�Ÿ��������β��б���Ϣ������<x,1>
			//varIds���������б�ʾ�βε��﷨�ڵ㣬��һ������
			//vdi��ʾһ��������β�
		Hashtable<NameDeclaration, Integer> params = new Hashtable<NameDeclaration, Integer>();
		List varIds = null;
		try {
			varIds = md.findChildNodesWithXPath("./MethodDeclarator/FormalParameters/FormalParameter/VariableDeclaratorId");
			int cnt = 0;
			for (Object o : varIds) {
				ASTVariableDeclaratorId vdi = (ASTVariableDeclaratorId) o;
				params.put(vdi.getNameDeclaration(), cnt);
				++cnt;
			}
		} 
		catch (JaxenException e) {
			throw new RuntimeException("xpath error @ ReleaseFeature.java : checkPrimaryExpression(checkPrimaryExpression node)",e);
		}
		
		/*
		 *  3 ���Ӻ�����ʵ���븸�������βζ�Ӧ����
		 */
		List<Integer> argMap = mapChildParamsToParentParams(pe,params);
		//System.out.println(argMap);
		
		/* 
		 * 4 ��ȡtype
		 */
		Object pr = pe.jjtGetChild(pe.jjtGetNumChildren()-2);
		Object type = null;
		if ( (pr instanceof ASTPrimaryPrefix) || (pr instanceof ASTPrimarySuffix) ) {
			type = ((ExpressionBase)pr).getType();
		} else {
			return false;
		}
		
		/*
		 * 5 ���ɺ���ժҪ
		 */
		if (type != null && (type instanceof Method)) {
			MethodNode methodnode=MethodNode.findMethodNode(type);
			if(methodnode != null){
				/*
				 * 5.1 �����Զ��庯������ڹ������ɺ���ժҪ
				 */
				MethodSummary summary = methodnode.getMethodsummary();

				if (summary != null) {
				/* 
				 * �麯��ժҪ���Ƿ�Ϊ���뺯��
				 */
					for (AbstractFeature ff : summary.getFeatrues().getTable().values()) {
						if (!(ff instanceof InputFeature)) {
							continue;
						}
						
						InputFeature rf = (InputFeature) ff;
						
						for (Map.Entry<Integer,List<String>> e : rf.getMapInfo().entrySet()) {
							Integer ide = e.getKey();
							
							List<String> dis = e.getValue();
							
							Integer i = (Integer)argMap.get(ide.intValue());
							
							if (!i.equals(-1)) {
								
								List<String> newlist = new ArrayList<String>(dis);
								if(softtest.config.java.Config.LANGUAGE==0){
									newlist.add("�ļ�:"+ProjectAnalysis.current_file+" ��:"+pe.getBeginLine()+"\n");
								}else{
									newlist.add("file:"+ProjectAnalysis.current_file+" line:"+pe.getBeginLine()+"\n");
								}
								mapInfo.put(i, newlist);
							}
						}
					}
					return false;
				}
			}
			else
				/* 
				 * 5.2 ����JDK�⺯������ڹ������ɺ���ժҪ
				 */
			{
				if(pe.containsChildOfType(ASTPrimaryPrefix.class))
				{
					ASTPrimaryPrefix c = (ASTPrimaryPrefix)pe.getFirstChildOfType(ASTPrimaryPrefix.class);
					String key = c.getTypeString();
					
					TaintedInfo taintedInfo = InputFeature.inputTable.get(key);
					
					if(taintedInfo != null)
					{
						List seqs = taintedInfo.getTaintedSeqs();
						
						for(int i=0; i<seqs.size(); i++)
						{
							int ki = Integer.parseInt((String) seqs.get(i));
							Integer k = argMap.get(ki);
							
							if (! k.equals(-1)) {
								List<String> newlist = new ArrayList<String>();
								if(softtest.config.java.Config.LANGUAGE==0){
									newlist.add("�ļ�:"+ProjectAnalysis.current_file+" ��:"+pe.getBeginLine()+"\n");
								}else{
									newlist.add("file:"+ProjectAnalysis.current_file+" line:"+pe.getBeginLine()+"\n");
								}
								mapInfo.put(k, newlist);
							}
							
						}
					}
//					else
//						//���JDK����û��¼��ú�����ժҪ��Ϣ���򲻴���
//					{
//					}
				}
			}
		}
		return false;
	}
	
	/**
	 * ���Ӻ�����ʵ���븸�������βζ�Ӧ���������а�������ֵ
	 * @param pe    ASTPrimaryExpression
	 * @param params  Hashtable<NameDeclaration, Integer>
	 * @return
	 */
	public List<Integer> mapChildParamsToParentParams(ASTPrimaryExpression pe,Hashtable<NameDeclaration, Integer> params)
	{
		List<Integer> argMap = null;
		int numOfRealArgs = 0;
		
		/*
		 * 0 ��ʼ��
		 */
		try
		{
			List realargs = pe.findChildNodesWithXPath("./PrimarySuffix[last()]/Arguments/ArgumentList/Expression");
			
			numOfRealArgs = realargs.size();
			argMap = new ArrayList<Integer>(numOfRealArgs + 1);
			
			argMap.add(0,Integer.valueOf(-1));
			for (int count = 1 ; count <= numOfRealArgs; count++)
			{
				argMap.add(count,Integer.valueOf(-1));
			}
			
		} 
		catch (JaxenException e)
		{
			throw new RuntimeException("xpath error @ InputFeature.java : mapChildParamsToParentParams(ASTPrimaryExpression pe,Hashtable<NameDeclaration, Integer> params)",e);
		}
		
		
		/*
		 *  1 ����ֵ�ڵ��ӳ���ϵ 
		 *  
		 *  case1 : return g(i)
		 *  case2 : String s = g(i); return s;
		 *  case3 : str = g(i); return str;
		 *  
		 */
			//���Ȳ鿴����ֵ����
		String returnTypeString = pe.getTypeString();
		String retName = "";
		
			//�������ֵ���Ͳ�Ϊvoid
		if( !returnTypeString.contains("void") )
		{
			ASTBlockStatement blockStatement = (ASTBlockStatement) pe.getFirstParentOfType(ASTBlockStatement.class);
			
			try
			{
				//case1 : return g(i);
				if(blockStatement.findChildNodesWithXPath(".//ReturnStatement").size() != 0)
				{
					argMap.add(0, Integer.valueOf(0));
				}
				else
				{
					//case2 : String s = g(i);
					List lvds;
					lvds = blockStatement.findChildNodesWithXPath(".//LocalVariableDeclaration");
					boolean flag = false;
					
					if(lvds.size() != 0)
					{
						ASTLocalVariableDeclaration lvd = (ASTLocalVariableDeclaration)lvds.get(0);
						List vids = lvd.findChildNodesWithXPath(".//VariableDeclaratorId");
						ASTVariableDeclaratorId vid = (ASTVariableDeclaratorId)vids.get(0);

						if(vid != null)
						{
							retName = vid.getImage();
							flag = true;
						}
					}
					//case3 : str = g(i);
					else if(blockStatement.findChildNodesWithXPath(".//AssignmentOperator").size() != 0)
					{
						ASTPrimaryExpression asp = (ASTPrimaryExpression) blockStatement.getFirstChildOfType(ASTPrimaryExpression.class);
						ASTName aName = (ASTName) asp.getFirstChildOfType(ASTName.class);
						if(aName != null)
						{
							retName = aName.getImage();
							flag = true;
						}
					}
					
					if(flag)
					{
						ASTMethodDeclaration md = (ASTMethodDeclaration) blockStatement.getFirstParentOfType(ASTMethodDeclaration.class);
						
						List rss = md.findChildNodesWithXPath(".//ReturnStatement");
						
out1:					for(int i=0; i<rss.size(); i++)
						{
							ASTReturnStatement rs = (ASTReturnStatement) rss.get(i);
							List nms = rs.findChildNodesWithXPath(".//Name");
							for(int j=0; j<nms.size(); j++)
							{
								ASTName nm = (ASTName) nms.get(j);
								if(nm.getImage().equalsIgnoreCase(retName))
								{
									argMap.add(0, Integer.valueOf(0));
									break out1;
								}
							}
						}
						
					}
					else
					{
						argMap.add(0, Integer.valueOf(-1));
					}
				}
			} catch (JaxenException e)
			{
				e.printStackTrace();
			} 
		}
		
		/*
		 * 2 ����ֵ��ӳ���ϵ
		 * 
		 */
		List realargs = null;
		try
		{
			realargs = pe.findChildNodesWithXPath("./PrimarySuffix[last()]/Arguments/ArgumentList/Expression");
			
			for(int i=1; i<=numOfRealArgs; i++)
			{
				ASTExpression expr = (ASTExpression)realargs.get(i-1);
				
				List names = expr.findChildNodesWithXPath(".//Name");
				NameDeclaration nd = null;
				int flag = 0;
				
				for(int j=0; j<names.size(); j++)
				{
					ASTName name = (ASTName) names.get(j);
					nd = name.getNameDeclaration();
					
					if(nd != null && params.containsKey(nd))
					{
						argMap.add(i, params.get(nd)+1);
						flag = 1;
						break;
					}
				}
				
				if(flag == 0)
				{
					argMap.add(i,-1);
				}
				
			}
		}
		catch (JaxenException e) 
		{
			throw new RuntimeException("xpath error @ InputFeature.java : mapChildParamsToParentParams(ASTPrimaryExpression pe,Hashtable<NameDeclaration, Integer> params)",e);
		}
		

		
		
//		try {			
//			realargs = pe.findChildNodesWithXPath("./PrimarySuffix[last()]/Arguments/ArgumentList/Expression[./PrimaryExpression/PrimaryPrefix/Name]");
//			int c = 1;
//			for (Object o : realargs) {
//				ASTExpression expr = (ASTExpression) o;
//				List name = expr.findChildNodesWithXPath("./PrimaryExpression[count(PrimarySuffix)=0]/PrimaryPrefix/Name");
//				List suffix = expr.findChildNodesWithXPath("./PrimaryExpression[count(PrimarySuffix)=1 and count(PrimaryPrefix)=1 and ./PrimaryPrefix[count(*)=0]]/PrimarySuffix[count(*)=0]");
//				NameDeclaration nd = null;
//				
//				if (name.size() == 1) {
//					ASTName nn = (ASTName) name.get(0);
//					nd = nn.getNameDeclaration();
//				} else if (suffix.size() == 1) {
//					ASTPrimarySuffix ps1 = (ASTPrimarySuffix) suffix.get(0);
//					nd = ps1.getNameDeclaration();					
//				}
//				//����β��а����ñ����Ķ���
//				if (nd != null && params.containsKey(nd)) {
//					argMap.add(c, params.get(nd)+1);
//				} else {
//					argMap.add(c,-1);
//				}
//				c++;
//			}
//		} catch (JaxenException e) {
//			throw new RuntimeException("xpath error @ InputFeature.java : futherCheck(checkPrimaryExpression node)",e);
//		}
		
		return argMap;
	}
	
	
	/**
	 * �����������ڵ��Ӧ���﷨���ڵ㣬��ȡ���뺯��������
	 * 
	 * @author ��ƽ��
	 *
	 */
	private class InputFeatureVisitor extends JavaParserVisitorAdapter {
		/**
		 * ͨ����鵱ǰ���������õ������������Ծ����Ƿ�Ϊ�����ɺ���ժҪ��Ϣ��
		 *
		 * 1. this.zzz(x,y)
		 * 2. t.zzz(x,y);
		 * 3. zzz(x,y);
		 */
		public Object visit(ASTPrimaryExpression node, Object data) {
			if (node == null) {
				return null;
			}
			//������
			futherCheck(node);
			return null;
		}
	}

	//accessors
	public Hashtable<Integer, List<String>> getMapInfo()
	{
		return mapInfo;
	}
	public void setMapInfo(Hashtable<Integer, List<String>> mapInfo)
	{
		this.mapInfo = mapInfo;
	}
}
