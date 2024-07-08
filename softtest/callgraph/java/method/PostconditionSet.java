package softtest.callgraph.java.method;

import java.util.Hashtable;

/**��������������*/
public class PostconditionSet {
	/**ǰ��������ϣ��*/
	Hashtable<AbstractPostcondition,AbstractPostcondition> table=new Hashtable<AbstractPostcondition,AbstractPostcondition>();
	
	/**��Ӻ�������*/
	public void addPreconditon(AbstractPostcondition con){
		table.put(con, con);
	}
	
	/**������к�������*/
	public void clear(){
		table.clear();
	}
	
	/**��ú���������ϣ��*/
	public Hashtable<AbstractPostcondition,AbstractPostcondition> getTable(){
		return table;
	}
	
	/**���ú���������ϣ��*/
	public void setTable(Hashtable<AbstractPostcondition,AbstractPostcondition> table){
		this.table=table;
	}
	
	/**�Ƴ���������*/
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
