package softtest.dts.java;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import softtest.database.java.DBAccess;


class K8Result{
	String type;
	String kind;
	String level;
	int number;
	String project;
	
	@Override
	public boolean equals(Object o) {
		if ((o == null) || !(o instanceof K8Result)) {
			return false;
		}
		if (this == o) {
			return true;
		}
		K8Result x = (K8Result) o;
		if(type.equals(x.type)&&kind.equals(x.kind)&&level.equals(x.level)&&project.equals(x.project)){
			return true;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return type.hashCode()+kind.hashCode()+project.hashCode()+level.hashCode();
	}
}

public class ConverK7Report {
	private List<File> fsl = new LinkedList<File>();
	static String FTYPE = ".txt";
	
	private Hashtable<K8Result,K8Result> table=new Hashtable<K8Result,K8Result>();

	public boolean chksubdir = false;
	public static void main(String[] args) {
		ConverK7Report test=new ConverK7Report();
		if (args.length != 1) {
			System.out.println("Usage£ºDTSJava \"directory or filename\" ");
			return;
		}
		test.fsl.clear();
		File f=new File(args[0]);
		if(f.isDirectory()){
			test.check(f);
		}else if(f.isFile()){
			test.fsl.add(f);
		}
		test.convert();
		//test.report();
		System.out.println("Convertion completed!");
	}

	private void check(File path) {
		File[] ffs = path.listFiles();
		for (int i = 0; i < ffs.length; i++) {
			if (ffs[i].getName().endsWith(FTYPE)) {
				fsl.add(ffs[i]);
			} else if (chksubdir && ffs[i].isDirectory()) {
				check(ffs[i]);
			}
		}
	}
	
	public void report(){
		for(File f:fsl){
			BufferedReader is=null;
			try{
				is=new BufferedReader(new FileReader(f));
				String line=is.readLine();
				while(line!=null){
					String cells[]=line.split(";");
					if(cells.length>=9){
						K8Result r=new K8Result();
						r.project=f.getAbsolutePath();
						r.type=cells[7];
						r.kind=cells[6];
						r.level=cells[3];
						r.number=1;
						if(table.contains(r)){
							table.get(r).number++;
						}else{
							table.put(r, r);
						}	
					}
					line=is.readLine();
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		Hashtable<String,Integer> projects=new Hashtable<String,Integer>(); 
		for(Enumeration<K8Result> e=table.elements();e.hasMoreElements();){
			K8Result r=e.nextElement();
			if(!projects.containsKey(r.project)){
				projects.put(r.project,r.number);
			}else{
				int number=projects.get(r.project);
				number+=r.number;
				projects.put(r.project, number);
			}
		}
		
		for(String project:projects.keySet()){
			int number=projects.get(project);
			//System.out.println("__________________________________________________________");
			//System.out.println("project:"+project);
			//System.out.println("IP num:"+number);
			for(Enumeration<K8Result> e=table.elements();e.hasMoreElements();){
				K8Result r=e.nextElement();
				if(project.equals(r.project)){
					System.out.printf("%s;%s;%s;%s;%d;%.4f\n",project,r.type,r.level,r.kind,r.number,(double)r.number/(double)number);
					//System.out.println("type:"+r.type);
					//System.out.println("level:"+r.level);
					//System.out.println("kind:"+r.kind);
					//System.out.println("num:"+r.number);
					//System.out.printf("ratio:%.4f\n",(double)r.number/(double)number);
				}
			}
			//System.out.println("__________________________________________________________");
		}
	}
	
	public void convert(){
		DBAccess db = new DBAccess();
		for(File f:fsl){
			String name=f.getAbsolutePath();
			name=name.substring(0, name.length()-4)+"_K8.mdb";
			db.openDataBase(name);
			BufferedReader is;
			try{
				is=new BufferedReader(new FileReader(f));
				String line=is.readLine();
				while(line!=null){
				//	System.out.println(line);
					String cells[]=line.split(";");
					if(cells.length>=9&&cells[7].equals("Error")){
						int beginline=Integer.parseInt(cells[1]);
						db.exportErrorData("error", cells[6], cells[0], cells[8], beginline, beginline, cells[10],"","","");
					}
					line=is.readLine();
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
			
			db.closeDataBase();
		}
	}
}
