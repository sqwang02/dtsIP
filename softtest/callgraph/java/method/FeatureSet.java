package softtest.callgraph.java.method;

import java.util.*;

/**��������������Ϣ������*/
public class FeatureSet {
	/**������Ϣ��ϣ��*/
	Hashtable<AbstractFeature,AbstractFeature> table=new Hashtable<AbstractFeature,AbstractFeature>();
	
	/**���������Ϣ*/
	public void addFeature(AbstractFeature fea){
		table.put(fea, fea);
	}
	
	/**�������������Ϣ*/
	public void clear(){
		table.clear();
	}
	
	/**���������Ϣ��ϣ��*/
	public Hashtable<AbstractFeature,AbstractFeature> getTable(){
		return table;
	}
	
	/**����������Ϣ��ϣ��*/
	public void setTable(Hashtable<AbstractFeature,AbstractFeature> table){
		this.table=table;
	}
	
	/**�Ƴ�������Ϣ*/
	public void removeFeature(AbstractFeature fea){
		table.remove(fea);
	}
}
