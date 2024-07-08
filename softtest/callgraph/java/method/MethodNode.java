package softtest.callgraph.java.method;

import java.lang.reflect.*;
import java.util.*;

import softtest.config.java.Config;
import softtest.domain.java.*;

public class MethodNode {
	
	/**
	 * 静态的Java类型对象到方法映射表
	 */
	private static Hashtable<Object,MethodNode> METHOD_TABLE=new Hashtable<Object,MethodNode> ();
	
	
	/**
	 * 方法对应的Java类型对象，可能为Method对象，也可能为Constructor对象，如果类型信息不全则为null
	 */
	Object methodInstance = null;
	
	/**
	 * 方法的返回值区间，当该方法还没有分析过时为null
	 */
	private Object domain = new ArbitraryDomain();
	
	/**
	 * 方法源码所属文件的全路径
	 */
	private String fileName = null;
	
	/**
	 * 方法源码所在行号
	 */
	private int linenum=0;
	
	/**
	 * 方法的函数摘要，当该方法还没有分析过时为null
	 */
	private MethodSummary methodsummary=null;
	
	/**
	 * 调用了的方法表
	 */
	Hashtable<MethodNode, MethodNode> calling = new Hashtable<MethodNode, MethodNode>();
	
	/**
	 * 被调用的方法表
	 */
	Hashtable<MethodNode, MethodNode> called = new Hashtable<MethodNode, MethodNode>();

	
	/**
	 * 构造函数
	 * @param methodInstance 方法对应的Java类型对象，可能为Method对象，也可能为Constructor对象
	 * @param fileName 方法源码所属文件的全路径名
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
	 * @param methodInstance 要查找方法对应的Java类型对象
	 * @return 查找到的方法，如果没有找到则为null
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
	 * @return Java类型对象到方法映射表
	 */
	public static Hashtable<Object,MethodNode> getMethodTable(){
		return METHOD_TABLE;
	}
	
	/**
	 * @param obj 方法对应的Java类型对象
	 * @param filename 方法所属文件
	 */
	public void addCallingByObject(Object obj,String filename,int linenum){
		MethodNode mn=findMethodNode(obj);
		if(mn==null){
			mn=new MethodNode(obj,filename,linenum);
		}
		addCalling(mn);
	}
	
	/**
	 * @param mn 需要添加到调用表的方法节点
	 */
	public void addCalling(MethodNode mn){
		calling.put(mn, mn);
	}
	
	/**
	 * @param obj 方法对应的Java类型对象
	 * @param filename 方法所属文件
	 */
	public void addCalledByObject(Object obj,String filename,int linenum){
		MethodNode mn=findMethodNode(obj);
		if(mn==null){
			mn=new MethodNode(obj,filename,linenum);
		}
		addCalled(mn);
	}
	
	/**
	 * @param mn 需要添加到调用表的方法节点
	 */
	public void addCalled(MethodNode mn){
		called.put(mn, mn);
	}	
	
	/**
	 * @return 方法对应的java类型对象
	 */
	public Object getMethodInstance() {
		return methodInstance;
	}

	/**
	 * 设置java类型对象
	 * @param methodInstance java类型对象
	 */
	public void setMethodInstance(Object methodInstance) {
		if (!(methodInstance instanceof Method) && !(methodInstance instanceof Constructor)) {
			throw new IllegalArgumentException("methodInstance isn't a Method or Constructor instance");
		}
		this.methodInstance = methodInstance;
	}

	/**
	 * @return 方法所属文件名
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * 设置方法所属文件名
	 * @param fileName 方法所属文件名
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return 调用方法表
	 */
	public Hashtable<MethodNode, MethodNode> getCalling() {
		return calling;
	}

	/**
	 * @return 被调用方法表
	 */
	public Hashtable<MethodNode, MethodNode> getCalled() {
		return called;
	}

	/**
	 * @return 函数摘要
	 */
	public MethodSummary getMethodsummary() {
		return methodsummary;
	}

	/**
	 * 设置函数摘要
	 * @param methodsummary MethodNode
	 */
	public void setMethodsummary(MethodSummary methodsummary) {
		this.methodsummary = methodsummary;
	}
	
	/**
	 * 设置返回值区间
	 * @param domain 方法返回值区间
	 */
	public void setDomain(Object domain) {
		this.domain = domain;
	}

	/**
	 * @return 返回值区间
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