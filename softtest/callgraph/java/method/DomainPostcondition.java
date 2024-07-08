package softtest.callgraph.java.method;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import softtest.IntervalAnalysis.java.*;
import softtest.ast.java.SimpleJavaNode;
import softtest.cfg.java.*;
import softtest.domain.java.ConvertDomain;
import softtest.domain.java.ReferenceDomain;
import softtest.domain.java.ReferenceValue;
import softtest.symboltable.java.NameOccurrence;
import softtest.symboltable.java.VariableNameDeclaration;

/**��������������������������ö������Ļ������޸ģ��������õĸ�����*/
public class DomainPostcondition extends AbstractPostcondition {
	/**���溯���������Ļ������޸�*/
	private Hashtable<MapOfVariable, Object> domaintable = new Hashtable<MapOfVariable, Object>();

	@Override
	public void listen(SimpleJavaNode node, PostconditionSet set) {
		List<VexNode> list=node.getVexNode();
		if(list==null){
			return;
		}
		VexNode vex=null;
		for(int i=0;i<list.size();i++){
			if(list.get(i).getName().startsWith("func_out")){
				vex=list.get(i);
				break;
			}
		}
		if(vex==null){
			return ;
		}
		//�ں���������ͼ�ĳ��ڴ��ռ� �Ժ����ⲿ�������޸�
		if(vex.getDomainSet()!=null){
		
			VariableNameDeclaration v = null;
			Object domain = null;
			DomainSet ds=vex.getDomainSet();
			Set entryset = ds.getTable().entrySet();
			Iterator i = entryset.iterator();
			while (i.hasNext()) {
				Map.Entry e = (Map.Entry) i.next();
				v = (VariableNameDeclaration) e.getKey();
				if(v.getDeclareScope().isSelfOrAncestor(vex.getTreeNode().getScope())){
					continue;
				}
			
				domain = e.getValue();
				if(DomainSet.getDomainType(domain)==ClassType.REF){
					ReferenceDomain r = (ReferenceDomain) ConvertDomain.DomainSwitch(domain, ClassType.REF);
					if(r.getValue()== ReferenceValue.NULL_OR_NOTNULL){
						//�������ñ��� ���������ж���ɵ������������
						boolean find=false;
						for (Object o : v.getOccs()) {
							NameOccurrence occ = (NameOccurrence) o;
							if(occ.getOccurrenceType() ==NameOccurrence.OccurrenceType.DEF && occ.getLocation().isSelOrAncestor(node)){
								find=true;
								break;
							}
						}
						if(!find){
							continue;
						}
					}
				}
				domaintable.put(new MapOfVariable(v), domain);
			}
			
		}else{
			domaintable.clear();
		}
		if(!domaintable.isEmpty()){
			set.addPreconditon(this);
		}		
	}

	/**�����*/
	public Hashtable<MapOfVariable, Object> getTable() {
		return domaintable;
	}

	@Override
	public String toString() {
		StringBuffer buff=new StringBuffer("Domain-Postcondition:");
		Iterator<Map.Entry<MapOfVariable, Object>> i=domaintable.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry<MapOfVariable,Object> entry=i.next();
			MapOfVariable m=entry.getKey();
			Object domain=entry.getValue();
			buff.append("("+m.toString()+":"+domain+")");
		}	
		return buff.toString();
	}
}
