package softtest.callgraph.java.method;

import java.util.Hashtable;

/**前置条件集合*/
public class PreconditionSet {
	/**前置条件哈希表*/
	Hashtable<AbstractPrecondition,AbstractPrecondition> table=new Hashtable<AbstractPrecondition,AbstractPrecondition>();
	
	/**添加前置条件*/
	public void addPreconditon(AbstractPrecondition con){
		table.put(con, con);
	}
	
	/**清除所有前置条件*/
	public void clear(){
		table.clear();
	}
	
	/**获得前置条件哈希表*/
	public Hashtable<AbstractPrecondition,AbstractPrecondition> getTable(){
		return table;
	}
	
	/**设置前置条件哈希表*/
	public void setTable(Hashtable<AbstractPrecondition,AbstractPrecondition> table){
		this.table=table;
	}
	
	/**移除前置条件*/
	public void removePreconditon(AbstractPrecondition con){
		table.remove(con);
	}

	@Override
	public String toString() {
		StringBuffer buff=new StringBuffer();
		for(AbstractPrecondition pre:table.values()){
			buff.append(pre.toString()+"\n");
		}
		return buff.toString();
	}
}
