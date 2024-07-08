package softtest.callgraph.java.method;

import java.util.Hashtable;

/**后置条件集合类*/
public class PostconditionSet {
	/**前置条件哈希表*/
	Hashtable<AbstractPostcondition,AbstractPostcondition> table=new Hashtable<AbstractPostcondition,AbstractPostcondition>();
	
	/**添加后置条件*/
	public void addPreconditon(AbstractPostcondition con){
		table.put(con, con);
	}
	
	/**清除所有后置条件*/
	public void clear(){
		table.clear();
	}
	
	/**获得后置条件哈希表*/
	public Hashtable<AbstractPostcondition,AbstractPostcondition> getTable(){
		return table;
	}
	
	/**设置后置条件哈希表*/
	public void setTable(Hashtable<AbstractPostcondition,AbstractPostcondition> table){
		this.table=table;
	}
	
	/**移除后置条件*/
	public void removePreconditon(AbstractPostcondition con){
		table.remove(con);
	}
	
	@Override
	public String toString() {
		StringBuffer buff=new StringBuffer();
		for(AbstractPostcondition post:table.values()){
			buff.append(post.toString()+"\n");
		}
		return buff.toString();
	}
}
