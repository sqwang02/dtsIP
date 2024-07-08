package softtest.fsm.java;

import java.io.IOException;

public class TestFSM {
	public static void main(String args[]){
		FSMMachine fsm= FSMLoader.loadXML("D:\\workspacejava\\DTS_Java1_3\\softtest\\rules\\java\\fault\\NPD\\NPD_ARRAY-0.1.xml");
		String name = "f:\\"+fsm.getName();
		fsm.accept(new DumpFSMVisitor(), name+".dot");
		System.out.println("״̬����������ļ�" + name + ".dot");
		try {
			java.lang.Runtime.getRuntime().exec("dot -Tjpg -o " + name + ".jpg " + name + ".dot").waitFor();
		} catch (IOException e1) {
			System.out.println(e1);
		} catch (InterruptedException e2) {
			System.out.println(e2);
		}
		System.out.println("״̬����ӡ�����ļ�" + name + ".jpg");
	}
}
