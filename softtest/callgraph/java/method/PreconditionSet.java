package softtest.callgraph.java.method;

import java.util.Hashtable;

/**ǰ����������*/
public class PreconditionSet {
	/**ǰ��������ϣ��*/
	Hashtable<AbstractPrecondition,AbstractPrecondition> table=new Hashtable<AbstractPrecondition,AbstractPrecondition>();
	
	/**���ǰ������*/
	public void addPreconditon(AbstractPrecondition con){
		table.put(con, con);
	}
	
	/**�������ǰ������*/
	public void clear(){
		table.clear();
	}
	
	/**���ǰ��������ϣ��*/
	public Hashtable<AbstractPrecondition,AbstractPrecondition> getTable(){
		return table;
	}
	
	/**����ǰ��������ϣ��*/
	public void setTable(Hashtable<AbstractPrecondition,AbstractPrecondition> table){
		this.table=table;
	}
	
	/**�Ƴ�ǰ������*/
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
