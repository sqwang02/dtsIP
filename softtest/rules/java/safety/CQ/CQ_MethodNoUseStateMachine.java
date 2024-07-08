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
 * δ��ʹ�õķ���
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
	 * ���ù�������
	 */
	public void fillDescription(FSMMachineInstance fsmmi, int beginline, int errorline) {
		Formatter f = new Formatter();
		if(softtest.config.java.Config.LANGUAGE==0){
			f.format("��������CQ: %d �ж���˽�з���û�б�ʹ�ã��������һ��©��", errorline);
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
		
		//	������ʼ����ʹ��
		xPath=".//VariableInitializer//Name[@MethodName='true']";
		evalRlts=node.findXpath(xPath);
		i=evalRlts.iterator();
		while(i.hasNext()){
			ASTName aName=(ASTName)i.next();
			cvpSet.add(aName.getImage());
		}
		
//		//	�������õ����
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
		
				
		//	��鹹�캯�������		
		xPath=".//ConstructorDeclaration//Name[@MethodName='true']";
		evalRlts=node.findXpath(xPath);
		i=evalRlts.iterator();
		while(i.hasNext()){
			ASTName aName=(ASTName)i.next();
			cvpSet.add(aName.getImage());
		}
		
		//	������ͼ�ڵĺ������ù�ϵ
		CGraph g = ProjectAnalysis.getCurrent_call_graph();
		List<CVexNode>ln=g.getTopologicalOrderList();
		Iterator j=null;
		j=ln.iterator();
		while(j.hasNext()){
			CVexNode cn=(CVexNode)j.next();
			//���Method�����
			//public ����������ü��ϣ�protected�Ƿ���Ҫ��
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
		
		//	��鷽���Ƿ񱻵���
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
