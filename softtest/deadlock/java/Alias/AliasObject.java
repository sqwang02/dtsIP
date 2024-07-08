package softtest.deadlock.java.Alias;

public class AliasObject {
	
	/** ���������ж�������� */
	private String name;
	
	/** �����id */
	private int id;

	
	public AliasObject() {

	}

	public AliasObject(String name, int id) {
		this.setName(name);
		this.setId(id);
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
	
	public boolean isSameObject(AliasObject ao){
		if(this.name .equals(ao.getName())&&this.id==ao.getId()){
			return true;
		}
		return false;
	}
}
