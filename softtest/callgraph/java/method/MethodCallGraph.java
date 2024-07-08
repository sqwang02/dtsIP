package softtest.callgraph.java.method;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Stack;

public class MethodCallGraph {
	/**
	 * ��ǰ������ļ���.
	 */
	private String currentFileName = null;
	
	/**
	 * ���췽��
	 */
	public MethodCallGraph() {
	}
		
	/**
	 * @return ��ǰ�����ļ���
	 */
	public String getCurrentFileName() {
		return currentFileName;
	}

	/**
	 * ���õ�ǰ�����ļ���
	 * @param currentFileName ��ǰ�����ļ���
	 */
	public void setCurrentFileName(String currentFileName) {
		this.currentFileName = currentFileName;
	}
	
	/**
	 * @return ���з����ڵ����ù�ϵ��������������
	 */
	public List<MethodNode> getMethodNodeTopoOrder() {
		List<MethodNode> ret = new ArrayList<MethodNode>();
		class TopoOrderUtils {
			MethodNode node = null;

			int indegree = 0;

			TopoOrderUtils(MethodNode node) {
				this.node = node;
			}
		}
		Hashtable<MethodNode, TopoOrderUtils> table = new Hashtable<MethodNode, TopoOrderUtils>();
		Stack<TopoOrderUtils> stack = new Stack<TopoOrderUtils>();

		for (MethodNode mn : MethodNode.getMethodTable().values()) {
			TopoOrderUtils n = new TopoOrderUtils(mn);
			n.indegree = 0;
			table.put(mn, n);
			for (MethodNode frommn : mn.getCalled().values()) {
				if (frommn != mn) {
					n.indegree++;
				}
			}
			if (n.indegree == 0) {
				// ���Ϊ0�Ľڵ���ջ
				stack.push(n);
			}
		}

		while (ret.size() < MethodNode.getMethodTable().size()) {
			while (!stack.empty()) {
				TopoOrderUtils n = stack.pop();
				ret.add(n.node);

				for (MethodNode tomn : n.node.getCalling().values()) {
					if (tomn != n.node) {
						TopoOrderUtils h = table.get(tomn);
						if (h.indegree > 0) {
							h.indegree--;
							if (h.indegree == 0) {
								// ���Ϊ0����ջ
								stack.push(h);
							}
						}
					}
				}
			}

			if (ret.size() < MethodNode.getMethodTable().size()) {
				// ���ڻ�·
				TopoOrderUtils mindegreenode = null;
				// ѡȡ���>0������С�Ľڵ�

				for (Enumeration<TopoOrderUtils> e = table.elements(); e
						.hasMoreElements();) {
					TopoOrderUtils n = e.nextElement();
					if (n.indegree > 0) {
						if (mindegreenode == null
								|| mindegreenode.indegree > n.indegree) {
							mindegreenode = n;
						}
					}
				}
				// �ƻ���·
				mindegreenode.indegree = 0;
				stack.push(mindegreenode);
			}
		}

		return ret;
	}
	
	/**
	 * @param �����ļ����б���Ϊ���ǵ��ڵ��ù�ϵͼ�п��ܴ���ĳЩû�г��ֵ��ļ���������Ҫ�ò���
	 * @return �ļ����������������
	 */
	public List<String> getReverseFileTopoOrder(List<File> filelist) {
		//		���ڼ����ļ���������ĸ�����
		class FileTopOrderUtils{
			int indegree = 0;
			int outdegree = 0;
			boolean available = true;
			String filename = null;
			HashSet<String> caller = new HashSet<String>();
			HashSet<String> callee = new HashSet<String>();
			FileTopOrderUtils(String filename){
				this.filename=filename;
			}
		}
		
		if (softtest.config.java.Config.DEBUG) {
			System.out.println("SIZE(filelist) : " + filelist.size());
		}
		Hashtable<String,FileTopOrderUtils> table=new Hashtable<String,FileTopOrderUtils>();
		
		for (MethodNode tomn : MethodNode.getMethodTable().values()) {
			for (MethodNode frommn : tomn.getCalled().values()) {
				if (frommn != tomn) {
					FileTopOrderUtils fromfile=table.get(frommn.getFileName());
					if(fromfile==null){
						fromfile=new FileTopOrderUtils(frommn.getFileName());
						table.put(frommn.getFileName(), fromfile);
					}
					
					FileTopOrderUtils tofile=table.get(tomn.getFileName());
					if(tofile==null){
						tofile=new FileTopOrderUtils(tomn.getFileName());
						table.put(tomn.getFileName(), tofile);
					}
					
					if(fromfile!=tofile){	
						if(!fromfile.callee.contains(tomn.getFileName())) {
							++fromfile.outdegree;
							fromfile.callee.add(tomn.getFileName());
						}
						
						if (!tofile.caller.contains(frommn.getFileName())) {
							tofile.caller.add(frommn.getFileName());
							++tofile.indegree;
						}
					}
				}
			}
		}
		
		if (softtest.config.java.Config.DEBUG) {
			System.out.println("SIZE(table) : "+table.size());
		}
		
		List<String> rootTopo = new ArrayList<String>();
		List<String> leafTopo = new ArrayList<String>();
		
		int temp = 0;
		while (temp < table.size()) {			
			// ����������
			{				
				Stack<FileTopOrderUtils> queue = new Stack<FileTopOrderUtils>();
				for (FileTopOrderUtils f : table.values()) {
					if (f.available && f.indegree == 0) {
						queue.push(f);
					}
				}
				
				while (!queue.empty()) {
					FileTopOrderUtils n = queue.pop();
					n.available = false;
					rootTopo.add(n.filename);
					if (softtest.config.java.Config.DEBUG) {
						System.out.println("ROOT : "+n.filename);
					}
					++temp;

					for (String tofile : n.callee) {
						FileTopOrderUtils ftou = table.get(tofile);
						if (ftou == null) continue;
						if (ftou.available && ftou.indegree > 0) {
							ftou.indegree--;
							if (ftou.indegree == 0) {
								// ���Ϊ0�����
								queue.push(ftou);
							}
						}
					}
				}
			}
			
			// ����������
			{				
				Stack<FileTopOrderUtils> queue = new Stack<FileTopOrderUtils>();
				for (FileTopOrderUtils f : table.values()) {
					if (f.available && f.outdegree == 0) {
						queue.push(f);
					}
				}
				
				while (!queue.empty()) {
					FileTopOrderUtils n = queue.pop();
					n.available = false;
					leafTopo.add(n.filename);
					if (softtest.config.java.Config.DEBUG) {
						System.out.println("LEAF : "+n.filename);
					}
					
					++temp;

					for (String fromfile : n.caller) {
						FileTopOrderUtils ftou = table.get(fromfile);
						if (ftou == null) continue;
						if (ftou.available && ftou.outdegree > 0) {
							ftou.outdegree--;
							if (ftou.outdegree == 0) {
								// ���Ϊ0�����
								queue.push(ftou);
							}
						}
					}
				}
			}

			if (temp < table.size()) {
				// ���ڻ�·
				FileTopOrderUtils mindegreenode = null;
				// ѡȡ���>0������С�Ľڵ�

				for (Enumeration<FileTopOrderUtils> e = table.elements(); e.hasMoreElements();) {
					FileTopOrderUtils n = e.nextElement();
					/*
					//�����С
					if (n.available && n.indegree > 0) {
						if (mindegreenode == null || mindegreenode.indegree > n.indegree) {
							mindegreenode = n;
						}
					}
					*/
					
					//������С
					if (n.available && n.outdegree > 0) {
						if (mindegreenode == null 
								|| mindegreenode.outdegree > n.outdegree 
								|| mindegreenode.outdegree == n.outdegree && mindegreenode.indegree < n.indegree) {
							mindegreenode = n;
						} 
					}
				}
				if (softtest.config.java.Config.DEBUG) {
					System.out.println("BREAK : "+mindegreenode.filename);
				}
				
				// �ƻ���·
				/*
				//�����С
				mindegreenode.indegree = 0;
				for (String caller : mindegreenode.caller) {
					FileTopOrderUtils ftou = table.get(caller);
					if (ftou.available && ftou.outdegree>0) {
						--ftou.outdegree;
					}
				}
				*/
				
				//������С
				mindegreenode.outdegree = 0;
				for (String callee : mindegreenode.callee) {
					FileTopOrderUtils ftou = table.get(callee);
					if (ftou.available && ftou.indegree>0) {
						--ftou.indegree;
					}
				}
				
			}
		}
		
		
		List<String> ret = new ArrayList<String>();
		
		//�����ù�ϵͼ�е���Щû�г��ֵ��ļ����ŵ��б����
		for(File file:filelist){
			String filename=file.getAbsolutePath();
			if(table.containsKey(filename)){
				continue;
			}
			table.put(filename, new FileTopOrderUtils(filename));
			ret.add(filename);
		}
		if (softtest.config.java.Config.DEBUG) {
			System.out.println("SIZE(leafTopo) : " + leafTopo.size());
			System.out.println("SIZE(rootTopo) : " + rootTopo.size());
			System.out.println("SIZE(ret) : " + ret.size());
		}
		
		ret.addAll(leafTopo);
		Collections.reverse(rootTopo);
		ret.addAll(rootTopo);
				
		return ret;
	}
}