package softtest.callgraph.java.method;

import java.util.*;

/**函数特征分类信息集合类*/
public class FeatureSet {
	/**特征信息哈希表*/
	Hashtable<AbstractFeature,AbstractFeature> table=new Hashtable<AbstractFeature,AbstractFeature>();
	
	/**添加特征信息*/
	public void addFeature(AbstractFeature fea){
		table.put(fea, fea);
	}
	
	/**清除所有特征信息*/
	public void clear(){
		table.clear();
	}
	
	/**获得特征信息哈希表*/
	public Hashtable<AbstractFeature,AbstractFeature> getTable(){
		return table;
	}
	
	/**设置特征信息哈希表*/
	public void setTable(Hashtable<AbstractFeature,AbstractFeature> table){
		this.table=table;
	}
	
	/**移除特征信息*/
	public void removeFeature(AbstractFeature fea){
		table.remove(fea);
	}
}
