package softtest.repair.java.conSynthesis;

public class RestrictTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String e1 = "a";
		String e2 = "b";
		String condition=null;
		long starttime=System.currentTimeMillis();
		condition = RestrictedSet.restrictrule("pow",e1,e2);
		long endtime=System.currentTimeMillis();
		long time =endtime-starttime;
		System.out.println(time);
		System.out.println(condition);
		
		
	}

}
