package softtest.rules.java.sensdt;

public interface MatchCondition {

	boolean  satisfy(Object obj);
	
	void    setHowMatch(EMatch how);
	
	void    setValue(Object    val);
	
	public  void dump();
}
