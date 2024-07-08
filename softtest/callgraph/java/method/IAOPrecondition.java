package softtest.callgraph.java.method;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import softtest.IntervalAnalysis.java.ClassType;
import softtest.IntervalAnalysis.java.DomainSet;
import softtest.ast.java.ASTAllocationExpression;
import softtest.ast.java.ASTArguments;
import softtest.ast.java.ASTConstructorDeclaration;
import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.ASTMultiplicativeExpression;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTPrimaryExpression;
import softtest.ast.java.ASTPrimarySuffix;
import softtest.ast.java.ExpressionBase;
import softtest.ast.java.JavaParserVisitorAdapter;
import softtest.ast.java.SimpleJavaNode;
import softtest.cfg.java.VexNode;
import softtest.domain.java.IntegerDomain;
import softtest.fsmanalysis.java.ProjectAnalysis;
import softtest.symboltable.java.ClassScope;
import softtest.symboltable.java.MethodScope;
import softtest.symboltable.java.NameDeclaration;
import softtest.symboltable.java.NameOccurrence;
import softtest.symboltable.java.VariableNameDeclaration;

/**IAOǰ������*/
public class IAOPrecondition extends AbstractPrecondition{
	
	/* (non-Javadoc)
	 * @see softtest.callgraph.java.method.AbstractPrecondition#listen(softtest.cfg.java.VexNode, softtest.callgraph.java.method.PreconditionSet)
	 */
	@Override
	public void listen(SimpleJavaNode node,PreconditionSet set) {
		
		IAOPreconditionVisitor vsitor=new IAOPreconditionVisitor();
		vsitor.root=node;
		node.jjtAccept(vsitor, null);
		
		if(table.size()>0){
			set.addPreconditon(this);
		}
	}
	

	/**
	 * ����ǰǰ��������ʹ��ǰ���������溯������
	 * @param node node ����Mehtod���ͻ�Constructor���͵ı��ʽ�ڵ㣬ͨ��������ASTPrimarySuffix����ASTPrimaryPrefix����ASTAllocationExpression
	 * @return �ں������ù����лᱻֱ�����õ�ʵ�ʲ�����ϣ��keyΪʵ�α�����valueΪ���ٵĺ�������trace��Ϣ
	 */
	public Hashtable<VariableNameDeclaration,List<String>> check(SimpleJavaNode node){
		Hashtable<VariableNameDeclaration,List<String>> ret= new Hashtable<VariableNameDeclaration,List<String>>();
		Iterator<Map.Entry<MapOfVariable,List<String>>> i=table.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry<MapOfVariable,List<String>> entry=i.next();
			MapOfVariable m=entry.getKey();
			List<String> list=entry.getValue();
			VariableNameDeclaration v=m.findVariable(node);
			if(v!=null){
				ret.put(v, list);
			}
		}	
		return ret;
	}
	
	/**
	 * �����﷨���ڵ㣬��麯���ڲ������ñ�����ֱ��ʹ��p.f()��ͬʱҲ������㺯�����ù�ϵf(p,a)����f
	 * ժҪ�д��ڶ�p��ֱ��ʹ��
	 */
	private class IAOPreconditionVisitor extends JavaParserVisitorAdapter{
		SimpleJavaNode root;
		HashSet<VariableNameDeclaration> set = new HashSet<VariableNameDeclaration>();
	 
		@Override
		public Object visit(ASTName node, Object data) {
			if(node.hasLocalMethod(root)){
				return null;
			}
			return null;
		}

		@Override
		public Object visit(ASTMultiplicativeExpression node, Object data) {
			if(node.hasLocalMethod(root)){
				return null;
			}
			if(node.getImage().equals("/") || node.getImage().equals("%")) {
            	List names = ((SimpleJavaNode)node.jjtGetChild(1)).findXpath(".//Name");
            	if (null != names) {
            		for(int i=0; i<names.size(); i++) {
            			ASTName name = (ASTName) names.get(i);
            			List<NameDeclaration> decllist = name.getNameDeclarationList();
            			Iterator<NameDeclaration> decliter = decllist.iterator();
            			MethodScope methodscope=node.getScope().getEnclosingMethodScope();
            out:		while (decliter.hasNext()) {
            				NameDeclaration decl = decliter.next();
            				if (!(decl instanceof VariableNameDeclaration)){
            					//����Ƿ�Ϊ����
            					continue;
            				}
            				VariableNameDeclaration v = (VariableNameDeclaration) decl;
            				
            				ClassScope classcope=v.getDeclareScope().getEnclosingClassScope();
            				if(set.contains(v)){
            					//����Ƿ��Ѿ���ӹ�
            					continue;
            				}
            				//����Ƿ�Ϊ ��Ա������������
            				//if(!methodscope.isSelfOrAncestor(v.getDeclareScope()) /*|| !(v.getNode().jjtGetParent() instanceof ASTFormalParameter)*/){
            				if(!methodscope.isSelfOrAncestor(v.getDeclareScope())){
//            				if(methodscope!=v.getDeclareScope()){
            					//����Ƿ�Ϊ��ǰ��������
            					continue;
            				}
            				VexNode vex=node.getCurrentVexNode();
            				if(vex==null){
            					//��鵱ǰ������ͼ�ڵ��Ƿ�Ϊ��
            					continue;
            				}
            				
            				if(vex.getContradict()){
            					//���ɴ�ڵ�
            					continue;
            				}
            				
            				Object domain = vex.getDomainWithoutNull(v);
            				if (domain == null || DomainSet.getDomainType(domain) != ClassType.INT) {
            					//����Ƿ�Ϊ �����ͱ���
            					continue;
            				}
            				
            				IntegerDomain intdomain=(IntegerDomain)domain;
            				if(!intdomain.getUnknown()/*&&refdomain.getValue()!=ReferenceValue.NULL_OR_NOTNULL*/){
            					continue;
            				}
            				
            				for(NameOccurrence occ:vex.getOccurrences()){
            					if(occ.getDeclaration()==v){
            						if(occ.getUseDefList()!=null&&!occ.getUseDefList().isEmpty()){
            							continue out;
            						}
            					}
            				}
            				List<String> list = new ArrayList<String>();
            				ASTMethodDeclaration currentmethoddecl=(ASTMethodDeclaration)node.getFirstParentOfType(ASTMethodDeclaration.class);
            				ASTConstructorDeclaration currentConstructordecl=(ASTConstructorDeclaration)node.getFirstParentOfType(ASTConstructorDeclaration.class);
            				String methodname="";
            				if(currentmethoddecl!=null){
            					methodname= currentmethoddecl.getMethodName();
            				} else if(currentConstructordecl!=null){
            					methodname= currentConstructordecl.getMethodName();
            				} 
            				if(softtest.config.java.Config.LANGUAGE==0){
            					list.add(0,"�ļ�:"+ProjectAnalysis.current_file+" ��:"+node.getBeginLine()+" ����:"+methodname+"\n");
            				}else{
            					list.add(0,"file:"+ProjectAnalysis.current_file+" line:"+node.getBeginLine()+" Method:"+methodname+"\n");
            				}
            				table.put(new MapOfVariable(v), list);
            				set.add(v);
            			}
            		}
            	}
            }
			return super.visit(node, data);
		}
		public Object visit(ASTArguments node, Object data) {
			if(node.hasLocalMethod(root)){
				return null;
			}
			//f(p,a);
			Object type=null;
			ExpressionBase last=null;
			if(node.jjtGetParent() instanceof ASTPrimarySuffix &&
					node.jjtGetParent().jjtGetParent() instanceof ASTPrimaryExpression){
				ASTPrimarySuffix suffix=(ASTPrimarySuffix)node.jjtGetParent();
				ASTPrimaryExpression primary=(ASTPrimaryExpression)node.jjtGetParent().jjtGetParent();
				last=(ExpressionBase)primary.jjtGetChild(suffix.getIndexOfParent()-1);
				type=last.getType();
			}else if(node.jjtGetParent() instanceof ASTAllocationExpression){
				ASTAllocationExpression allo=(ASTAllocationExpression)node.jjtGetParent();
				type=allo.getType();
				last=allo;
			}
			if(type instanceof Method||type instanceof Constructor){
				MethodNode methodnode=MethodNode.findMethodNode(type);
				if(methodnode==null){
					return super.visit(node, data);
				}
				MethodSummary summary = methodnode.getMethodsummary();
				if (summary == null) {
					return super.visit(node, data);
				}

				for (AbstractPrecondition pre : summary.getPreconditons().getTable().values()) {
					if (!(pre instanceof IAOPrecondition)) {
						continue;
					}
					IAOPrecondition iaopre = (IAOPrecondition) pre;
					Hashtable<VariableNameDeclaration,List<String>> usedvar = iaopre.check(last);
					Iterator<Map.Entry<VariableNameDeclaration,List<String>>> i=usedvar.entrySet().iterator();
	out:			while (i.hasNext()) {
						Map.Entry<VariableNameDeclaration,List<String>> entry=i.next();
						VariableNameDeclaration v=entry.getKey();
						List<String> list=entry.getValue();
							
						if(set.contains(v)){
							//����Ƿ��Ѿ���ӹ�
							continue;
						}
						MethodScope methodscope=node.getScope().getEnclosingMethodScope();
						//if(!methodscope.isSelfOrAncestor(v.getDeclareScope()) /*|| !(v.getNode().jjtGetParent() instanceof ASTFormalParameter)*/){
						if(!methodscope.isSelfOrAncestor(v.getDeclareScope())){
//						if(methodscope!=v.getDeclareScope()){
							//����Ƿ�Ϊ��ǰ��������
							continue;
						}
							
						VexNode vex=node.getCurrentVexNode();
						if(vex==null){
							//��鵱ǰ������ͼ�ڵ��Ƿ�Ϊ��
							continue;
						}
							
						if(vex.getContradict()){
							//���ɴ�ڵ�
							continue;
						}
							
							
						Object domain = vex.getDomainWithoutNull(v);
						if (domain == null || DomainSet.getDomainType(domain) != ClassType.INT) {
							//����Ƿ�Ϊ �����ͱ���
							continue;
						}
						IntegerDomain intdomain=(IntegerDomain)domain;
						if(!intdomain.getUnknown()/*&&refdomain.getValue()!=ReferenceValue.NULL_OR_NOTNULL*/){
							continue;
						}
														
						for(NameOccurrence occ:vex.getOccurrences()){
							if(occ.getDeclaration()==v){
								if(occ.getUseDefList()!=null&&!occ.getUseDefList().isEmpty()){
									continue out;
								}
							}
						}
							
						ASTMethodDeclaration currentmethoddecl=(ASTMethodDeclaration)node.getFirstParentOfType(ASTMethodDeclaration.class);
						ASTConstructorDeclaration currentConstructordecl=(ASTConstructorDeclaration)node.getFirstParentOfType(ASTConstructorDeclaration.class);
						String methodname="";
						if(currentmethoddecl!=null){
							methodname= currentmethoddecl.getMethodName();
						} else if(currentConstructordecl!=null){
							methodname= currentConstructordecl.getMethodName();
						} 
						List<String> newlist=new ArrayList<String>();
						for(String s:list){
							newlist.add(s);
						}
						if(softtest.config.java.Config.LANGUAGE==0){
							newlist.add(0,"�ļ�:"+ProjectAnalysis.current_file+" ��:"+node.getBeginLine()+" ����:"+methodname+"\n");
						}else{
							newlist.add(0,"file:"+ProjectAnalysis.current_file+" line:"+node.getBeginLine()+" Method:"+methodname+"\n");
						}
						//Config.OOB_PRE_INSTANCE_COUNT++;
						table.put(new MapOfVariable(v), newlist);
						set.add(v);
					}
				}
			}
			return super.visit(node, data);
		}	
	}

	@Override
	public String toString() {
		StringBuffer buff=new StringBuffer("IAO-Precondition:");
		Iterator<Map.Entry<MapOfVariable,List<String>>> i=table.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry<MapOfVariable,List<String>> entry=i.next();
			MapOfVariable m=entry.getKey();
			List<String> list=entry.getValue();
			buff.append("("+m.toString()+")");
		}	
		return buff.toString();
	}
}

