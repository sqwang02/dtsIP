package softtest.repair.java;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;



import softtest.ast.java.ASTCompilationUnit;
import softtest.ast.java.SimpleJavaNode;
import softtest.ast.java.SimpleNode;

public class ASTraverse {
	public static SimpleJavaNode asTraverse(ASTCompilationUnit astroot,int Id,String dtype,String dvar){
		SimpleJavaNode defectNode=astroot;
		SimpleJavaNode currentNode=astroot;
		List defect=new ArrayList<SimpleJavaNode>(); 
		//ArrayList<Integer> lists=new ArrayList<Integer>();
		if(astroot==null)
			return null;
		Queue<SimpleJavaNode> queue=new LinkedList<SimpleJavaNode>();
		queue.offer(astroot);
		
		while(!queue.isEmpty()){
			//SimpleJavaNode tree=queue.poll();
			for(int i=0;i<currentNode.jjtGetNumChildren();i++){
				if(currentNode.jjtGetChild(i)!=null){
					queue.offer((SimpleJavaNode)currentNode.jjtGetChild(i));
				}
			}
			currentNode=queue.poll();
			if(currentNode.getId()==Id){
				//defectNode = (SimpleJavaNode) currentNode.jjtGetChild(1);
				currentNode.setDefect(true);
				currentNode.setDefectType(dtype);
				currentNode.setDefectVariable(dvar);
				
				defectNode = currentNode;
				defect.add(currentNode);
				//break;
				}
			}
//		defectNode.setDefect(true);
//		defectNode.setDefectType(dtype);
//		defectNode.setDefectVariable(dvar);
		System.out.println(((SimpleNode)defect.get(0)).getImage());
		return defectNode;
		
		
	}
	public static SimpleJavaNode asTraverse(ASTCompilationUnit astroot,int Id){
		SimpleJavaNode defectNode=astroot;
		SimpleJavaNode currentNode=astroot;
		List defect=new ArrayList<SimpleJavaNode>(); 
		//ArrayList<Integer> lists=new ArrayList<Integer>();
		if(astroot==null)
			return null;
		Queue<SimpleJavaNode> queue=new LinkedList<SimpleJavaNode>();
		queue.offer(astroot);
		
		while(!queue.isEmpty()){
			//SimpleJavaNode tree=queue.poll();
			for(int i=0;i<currentNode.jjtGetNumChildren();i++){
				if(currentNode.jjtGetChild(i)!=null){
					queue.offer((SimpleJavaNode)currentNode.jjtGetChild(i));
				}
			}
			currentNode=queue.poll();
			if(currentNode.getId()==Id){
				//defectNode = (SimpleJavaNode) currentNode.jjtGetChild(1);
				currentNode.setDefect(true);
				//currentNode.setDefectType(dtype);
				//currentNode.setDefectVariable(dvar);
				
				defectNode = currentNode;
				//defect.add(currentNode);
				//break;
				}
			}
//		defectNode.setDefect(true);
//		defectNode.setDefectType(dtype);
//		defectNode.setDefectVariable(dvar);
		//System.out.println(((SimpleNode)defect.get(0)).getImage());
		return defectNode;
		
		
	}

}
