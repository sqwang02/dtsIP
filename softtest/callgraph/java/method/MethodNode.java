package softtest.callgraph.java.method;

import java.lang.reflect.*;
import java.util.*;

import softtest.config.java.Config;
import softtest.domain.java.*;

public class MethodNode {
	
	/**
	 * ��̬��Java���Ͷ��󵽷���ӳ���
	 */
	private static Hashtable<Object,MethodNode> METHOD_TABLE=new Hashtable<Object,MethodNode> ();
	
	
	/**
	 * ������Ӧ��Java���Ͷ��󣬿���ΪMethod����Ҳ����ΪConstructor�������������Ϣ��ȫ��Ϊnull
	 */
	Object methodInstance = null;
	
	/**
	 * �����ķ���ֵ���䣬���÷�����û�з�����ʱΪnull
	 */
	private Object domain = new ArbitraryDomain();
	
	/**
	 * ����Դ�������ļ���ȫ·��
	 */
	private String fileName = null;
	
	/**
	 * ����Դ�������к�
	 */
	private int linenum=0;
	
	/**
	 * �����ĺ���ժҪ�����÷�����û�з�����ʱΪnull
	 */
	private MethodSummary methodsummary=null;
	
	/**
	 * �����˵ķ�����
	 */
	Hashtable<MethodNode, MethodNode> calling = new Hashtable<MethodNode, MethodNode>();
	
	/**
	 * �����õķ�����
	 */
	Hashtable<MethodNode, MethodNode> called = new Hashtable<MethodNode, MethodNode>();

	
	/**
	 * ���캯��
	 * @param methodInstance ������Ӧ��Java���Ͷ��󣬿���ΪMethod����Ҳ����ΪConstructor����
	 * @param fileName ����Դ�������ļ���ȫ·����
	 */
	public MethodNode(Object methodInstance, String fileName ,int linenum) {
		if (methodInstance != null) {
			if (!(methodInstance instanceof Method)&& !(methodInstance instanceof Constructor)) {
				throw new IllegalArgumentException(
						"methodInstance isn't a Method or Constructor instance");
			}
		}
		this.methodInstance = methodInstance;
		this.fileName = fileName;
		this.linenum = linenum;
		if (methodInstance != null && !METHOD_TABLE.containsKey(methodInstance)) {
			METHOD_TABLE.put(methodInstance, this);
		}
	}
	
	/**
	 * @param methodInstance Ҫ���ҷ�����Ӧ��Java���Ͷ���
	 * @return ���ҵ��ķ��������û���ҵ���Ϊnull
	 */
	public static MethodNode findMethodNode(Object methodInstance){
		MethodNode ret=null;
		if(methodInstance!=null && METHOD_TABLE.get(methodInstance) != null){
			ret=METHOD_TABLE.get(methodInstance);
		} else if (Config.USE_LIBSUMMARY){
			Set<Object> keys = METHOD_TABLE.keySet();
			for (Object o: keys) {
				if (o.equals(methodInstance) && METHOD_TABLE.get(o) != null) {
					ret=METHOD_TABLE.get(o);
				}
			}
		}
		return ret;
	}
	
	
	/**
	 * @return Java���Ͷ��󵽷���ӳ���
	 */
	public static Hashtable<Object,MethodNode> getMethodTable(){
		return METHOD_TABLE;
	}
	
	/**
	 * @param obj ������Ӧ��Java���Ͷ���
	 * @param filename ���������ļ�
	 */
	public void addCallingByObject(Object obj,String filename,int linenum){
		MethodNode mn=findMethodNode(obj);
		if(mn==null){
			mn=new MethodNode(obj,filename,linenum);
		}
		addCalling(mn);
	}
	
	/**
	 * @param mn ��Ҫ��ӵ����ñ�ķ����ڵ�
	 */
	public void addCalling(MethodNode mn){
		calling.put(mn, mn);
	}
	
	/**
	 * @param obj ������Ӧ��Java���Ͷ���
	 * @param filename ���������ļ�
	 */
	public void addCalledByObject(Object obj,String filename,int linenum){
		MethodNode mn=findMethodNode(obj);
		if(mn==null){
			mn=new MethodNode(obj,filename,linenum);
		}
		addCalled(mn);
	}
	
	/**
	 * @param mn ��Ҫ��ӵ����ñ�ķ����ڵ�
	 */
	public void addCalled(MethodNode mn){
		called.put(mn, mn);
	}	
	
	/**
	 * @return ������Ӧ��java���Ͷ���
	 */
	public Object getMethodInstance() {
		return methodInstance;
	}

	/**
	 * ����java���Ͷ���
	 * @param methodInstance java���Ͷ���
	 */
	public void setMethodInstance(Object methodInstance) {
		if (!(methodInstance instanceof Method) && !(methodInstance instanceof Constructor)) {
			throw new IllegalArgumentException("methodInstance isn't a Method or Constructor instance");
		}
		this.methodInstance = methodInstance;
	}

	/**
	 * @return ���������ļ���
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * ���÷��������ļ���
	 * @param fileName ���������ļ���
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return ���÷�����
	 */
	public Hashtable<MethodNode, MethodNode> getCalling() {
		return calling;
	}

	/**
	 * @return �����÷�����
	 */
	public Hashtable<MethodNode, MethodNode> getCalled() {
		return called;
	}

	/**
	 * @return ����ժҪ
	 */
	public MethodSummary getMethodsummary() {
		return methodsummary;
	}

	/**
	 * ���ú���ժҪ
	 * @param methodsummary MethodNode
	 */
	public void setMethodsummary(MethodSummary methodsummary) {
		this.methodsummary = methodsummary;
	}
	
	/**
	 * ���÷���ֵ����
	 * @param domain ��������ֵ����
	 */
	public void setDomain(Object domain) {
		this.domain = domain;
	}

	/**
	 * @return ����ֵ����
	 */
	public Object getDomain() {
		return domain;
	}

	public int getLinenum() {
		return linenum;
	}

	public void setLinenum(int linenum) {
		this.linenum = linenum;
	}	
}