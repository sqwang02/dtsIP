package softtest.rules.java.safety.CQ;

import softtest.fsm.java.*;
import softtest.fsmanalysis.java.ProjectAnalysis;
import java.util.*;

import softtest.rules.java.AbstractStateMachine;
import softtest.symboltable.java.MethodNameDeclaration;
import softtest.symboltable.java.NameDeclaration;
import softtest.symboltable.java.VariableNameDeclaration;
import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.ASTName;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.CEdge;
import softtest.callgraph.java.CGraph;
import softtest.callgraph.java.CVexNode;
import softtest.callgraph.java.method.FeatureListenerSet;
import softtest.callgraph.java.method.PreconditionListenerSet;
import softtest.cfg.java.*;

/**
 * 未被使用的方法
 * public class MethodNoUse {
	private void doWork() {
		System.out.println("doing work");
	}
	public static void main(String[] args) {
		System.out.println("running Dead");
	}
}
class DoubleDead {
	private void doTweedledee() {
        doTweedledumb();
    }
    private void doTweedledumb() {
        doTweedledee();
    }
    public static void main(String[] args) {
        System.out.println("running DoubleDead");
    }
}
09.09.23@baigele
 */

public class CQ_MethodNoUseStateMachine extends AbstractStateMachine{
	/**
	 * 设置故障描述
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("代码质量CQ: %d 行定义私有方法没有被使用，可能造成一个漏洞", errorline);
		}else{
			f.format("Code Quality: Private method is not used on line %d",errorline);
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
	public static Set<String> cvpSet=null;
	public static List<FSMMachineInstance> createMethodNoUseStateMachines(SimpleJavaNode node, FSMMachine fsm) {
		List<FSMMachineInstance> list = new LinkedList<FSMMachineInstance>();
		String xPath = "";
		
		cvpSet=new HashSet<String>();
		List evalRlts = null;
		Iterator i = null;
		
		//	变量初始化的使用
		xPath=".//VariableInitializer//Name[@MethodName='true']";
		evalRlts=node.findXpath(xPath);
		i=evalRlts.iterator();
		while(i.hasNext()){
			ASTName aName=(ASTName)i.next();
			cvpSet.add(aName.getImage());
		}
		
//		//	变量调用的情况
		xPath=".//PrimaryExpression//PrimaryPrefix//Name[@MethodName='true']";
		evalRlts=node.findXpath(xPath);
		i=evalRlts.iterator();
		while(i.hasNext()){
			ASTName aName=(ASTName)i.next();
			List<NameDeclaration> ndl=aName.getNameDeclarationList();
			Iterator ndi=ndl.listIterator();
			while(ndi.hasNext()){
				Object nin=ndi.next();
				if(nin instanceof VariableNameDeclaration){
					if(ndi.hasNext()){
						nin=ndi.next();
						if(nin!=null&&nin instanceof MethodNameDeclaration){
							MethodNameDeclaration mnd=(MethodNameDeclaration)nin;
							cvpSet.add(mnd.getImage());
						}
					}
				}
			}
		}
		
				
		//	检查构造函数的情况		
		xPath=".//ConstructorDeclaration//Name[@MethodName='true']";
		evalRlts=node.findXpath(xPath);
		i=evalRlts.iterator();
		while(i.hasNext()){
			ASTName aName=(ASTName)i.next();
			cvpSet.add(aName.getImage());
		}
		
		//	检查调用图内的函数调用关系
		CGraph g = ProjectAnalysis.getCurrent_call_graph();
		List<CVexNode>ln=g.getTopologicalOrderList();
		Iterator j=null;
		j=ln.iterator();
		while(j.hasNext()){
			CVexNode cn=(CVexNode)j.next();
			//检查Method的情况
			//public 方法加入调用集合，protected是否需要？
			if(cn.getMethodDeclaration().isPublic()||cn.getMethodDeclaration().isProtected()){
				Hashtable<String,CEdge> ces=cn.getOutedges();
				for(Iterator it=ces.keySet().iterator();it.hasNext();)   
					{
						String key=(String)it.next();   
                        CEdge ce=ces.get(key);
                        CVexNode cv=ce.getHeadNode();
                        cvpSet.add(cv.getMethodNameDeclaration().getImage());
					} 
			}
			else{
				if(cvpSet.contains(cn.getMethodNameDeclaration().getImage())){
					Hashtable<String,CEdge> ces=cn.getOutedges();
					for(Iterator it=ces.keySet().iterator();it.hasNext();)   
						{
							String key=(String)it.next();   
	                        CEdge ce=ces.get(key);
	                        CVexNode cv=ce.getHeadNode();
	                        cvpSet.add(cv.getMethodNameDeclaration().getImage());
						}
				}
			}
		}
		
		//	检查方法是否被调用
		xPath=".//MethodDeclaration[@Private='true']";
		evalRlts=node.findXpath(xPath);
		i=evalRlts.iterator();
		while(i.hasNext()){
			ASTMethodDeclaration md=(ASTMethodDeclaration)i.next();			
			if(!cvpSet.contains(md.getMethodName())){
					FSMMachineInstance fsminstance = fsm.creatInstance();
					fsminstance.setRelatedObject(new FSMRelatedCalculation(md));
					fsminstance.setResultString("Private method is not used: "+md.getMethodName());
					list.add(fsminstance);
			}
		}
		return list;
	}
		
	
}
