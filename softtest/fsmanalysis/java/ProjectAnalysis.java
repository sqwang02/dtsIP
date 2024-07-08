package softtest.fsmanalysis.java;

import static org.junit.Assert.fail;
import softtest.DefUseAnalysis.java.DUAnaysisVistor;
import softtest.IntervalAnalysis.java.*;
import softtest.ast.java.ASTCompilationUnit;
import softtest.ast.java.ASTConstructorDeclaration;
import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.JavaCharStream;
import softtest.ast.java.JavaParser;
import softtest.ast.java.JavaParserVisitorAdapter;
import softtest.ast.java.SimpleJavaNode;
import softtest.callgraph.java.*;
import softtest.callgraph.java.method.*;
import softtest.cfg.java.*;
import softtest.config.java.Config;
import softtest.fsm.java.*;
import softtest.jaxen.java.MatchesFunction;
import softtest.ast.java.*;
import softtest.registery.SuccessRe;
import softtest.summary.lib.java.LibLoader;
import softtest.summary.lib.java.LibManager;
import softtest.symboltable.java.*;

import java.awt.Container;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

import softtest.jaxen.java.*;
import softtest.symboltable.java.ExpressionTypeFinder;
import softtest.symboltable.java.OccurrenceFinder;
import softtest.symboltable.java.PakageAndImportVisitor;
import softtest.symboltable.java.ScopeAndDeclarationFinder;
import softtest.symboltable.java.TypeSet;

import java.util.*;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.tools.Diagnostic;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import softtest.database.java.*;

/**
 * 
 * @author 杨绣，肖庆
 * 
 */
public class ProjectAnalysis {
	/**
	 * log文件存放路径
	 */
	private static final String LOG_DIR = "log";
	
	/**
	 * 显示当前分析的方法 add by Ruiqiang 2013-04-01 暂时没使用
	 */
	//private static String current_analysingMethod = null;
	/**
	 * 存放所有的状态机对象
	 */
	private static List<FSMMachine> fsms = new ArrayList<FSMMachine>();

	/**
	 * 存放数据库配置
	 */
	private static List<DBConfig> list_dbconfig = null;

	/**
	 * 当前文件的内部调用关系图
	 */
	public static CGraph current_call_graph = null;

	/**
	 * 被测项目信息
	 */
	public static String project = "";

	public static long usedtime = 0;

	public static int filecount = 0;

	public static int linecount = 0;

	/**
	 * 当前文件对应的语法树根节点
	 */
	public static ASTCompilationUnit current_astroot = null;

	/**
	 * 当前文件名
	 */
	public static String current_file = null;

	/**
	 * 方法调用关系图
	 */
	private static MethodCallGraph mcgraph = new MethodCallGraph();

	/**
	 * 函数摘要前置条件监听者集合
	 */
	private static PreconditionListenerSet prelisteners = new PreconditionListenerSet();

	/**
	 * 函数摘要后置条件监听者集合
	 */
	private static PostconditionListenerSet postlisteners = new PostconditionListenerSet();

	/**
	 * 函数摘要特征信息监听者集合
	 */
	private static FeatureListenerSet featurelisteners = new FeatureListenerSet();

	/**
	 * @return 前置条件监听者集合
	 */
	public static PreconditionListenerSet getPreconditionListener() {
		return prelisteners;
	}

	/**
	 * @return 后置条件监听者集合
	 */
	public static PostconditionListenerSet getPostconditionListener() {
		return postlisteners;
	}

	/**
	 * @return 函数摘要特征信息
	 */
	public static FeatureListenerSet getFeatureListenerSet() {
		return featurelisteners;
	}
	
	/*
	 * @method 方法名称 add by Ruiqiang 2013-04-01 暂时没投入使用
	
	public static void setShowMethod(String method){
		current_analysingMethod = method;
	} */

	/**
	 * @param 需要递归读取的目录路径
	 */
	private static void loadAll(String pathname) {
		File path = new File(pathname);
		if (!path.isDirectory()) {
			return;
		}
		File[] ffs = path.listFiles();
		for (int i = 0; i < ffs.length; i++) {
			if (!ffs[i].isDirectory() && !ffs[i].getName().endsWith(".xml")) {
				continue;
			}
			if (ffs[i].isDirectory()) {
				loadAll(ffs[i].getAbsolutePath());
				continue;
			}

			for (DBConfig c : list_dbconfig) {
				String str = c.category;
				str = str + "-";
				if (!ffs[i].getName().startsWith(str)) {
					continue;
				}
				FSMMachine fsm = FSMLoader.loadXML(ffs[i].getAbsolutePath());
				if (softtest.config.java.Config.TRACE) {
					fsm.dump();
				}
				fsm.setModelType(c.defect);
				fsms.add(fsm);

				logger.debug(ffs[i].getAbsolutePath());
				break;
			}
		}
	}

	/**
	 * @param 需要递归读取的目录路径
	 */
	private static void loadAll(String pathname, String defect, String category) {
		File path = new File(pathname);
		if (!path.isDirectory()) {
			return;
		}
		File[] ffs = path.listFiles();
		for (int i = 0; i < ffs.length; i++) {
			if (!ffs[i].isDirectory() && !ffs[i].getName().endsWith(".xml")) {
				continue;
			}
			if (ffs[i].isDirectory()) {
				loadAll(ffs[i].getAbsolutePath(), defect, category);
				continue;
			}

			for (DBConfig c : list_dbconfig) {
				if (!ffs[i].getName().startsWith(c.category)) {
					continue;
				}
				FSMMachine fsm = FSMLoader.loadXML(ffs[i].getAbsolutePath());
				if (fsm.getName().equals(c.category)) {
					fsm.setModelType(c.defect);
					fsms.add(fsm);
					logger.debug(ffs[i].getAbsolutePath());
					break;
				}
			}
		}
	}

	/**
	 * 根据config.mdb中的配置载入待分析的状态机文件
	 */
	public static void loadFSM() {
		list_dbconfig = DBAccess.getScanTypes(".." + File.separator + "set"
				+ File.separator + "config.mdb");
		fsms.clear();
		loadAll("softtest" + File.separator + "rules" + File.separator + "java");
	}

	/**
	 * 根据defect,category载入待分析的状态机文件
	 */
	public static void loadFSM(String defect, String category) {
		list_dbconfig = Arrays.asList(new DBConfig[] { new DBConfig(defect,
				category) });
		fsms.clear();
		loadAll(
				"softtest" + File.separator + "rules" + File.separator + "java",
				defect, category);
	}

	public static void clearFSM() {
		fsms.clear();
	}

	public static void addFSM(String path) {
		FSMMachine fsm = FSMLoader.loadXML(path);
		fsms.add(fsm);
	}

	/**
	 * 查找目录下的所有java文件，并放入容器fileContainer中
	 * 
	 * @param dir
	 *            待查找的目录名
	 * @param fileContainer
	 *            存放结果的容器
	 */
	private static void findAllJavaFileObject(File dir, List<File> fileContainer) {
		
		if (dir.isFile()) {
			if (dir.getName().endsWith(".java")) {
				fileContainer.add(dir);
			}
			return;
		}
		//try{
		for (File f : dir.listFiles()) {
			if (f.isDirectory()) {
				findAllJavaFileObject(f, fileContainer);
			} else if (f.isFile() && f.getName().endsWith(".java")) {
				fileContainer.add(f);
			}
		}
		//}
		//catch(NullPointerException e){
		//	
	//	}
	}

	/**
	 * 删除一个目录及其包含的所有内容
	 * 
	 * @param dir
	 *            目录名
	 * @throws IOException
	 *             删除失败
	 */
	private static void deleteDirectory(File dir) throws IOException {
		if ((dir == null) || !dir.isDirectory()) {
			throw new IllegalArgumentException("Argument " + dir
					+ " is not a directory. ");
		}
		File[] entries = dir.listFiles();
		int sz = entries.length;
		for (int i = 0; i < sz; i++) {
			if (entries[i].isDirectory()) {
				deleteDirectory(entries[i]);
			} else {
				entries[i].delete();
			}
		}
		dir.delete();
	}

	/**
	 * 删除已有的temp目录，然后创建一个新的空的temp目录
	 */
	private static void deleteAndCreateTemp() {
		File file = new File("temp");
		if (file.exists()) {
			try {
				deleteDirectory(file);
			} catch (IOException e) {
				fail("fail to delete temp file");
			}
		}

		file = new File("temp");
		file.mkdirs();
	}

	private static final Logger logger = Logger.getLogger(ProjectAnalysis.class);
	static {
		File file = new File("log/"+Config.LOG_FILE);
		file.delete();
		BasicConfigurator.configure();
		PropertyConfigurator.configure("log4j.properties");
		// logger = Logger.getRootLogger();
	}

	private static int percent = 0;

	/*
	 * private static JFrame jf=new JFrame(); private static JProgressBar
	 * jpb=new JProgressBar(); private static JLabel jlfilecount=new JLabel();
	 * private static JLabel jltime=new JLabel(); private static JLabel
	 * jllinenumber=new JLabel();
	 */

	class ProgressThread extends Thread {

		private int percent = 0;
		
		private int prePercent = 0;

		private JFrame jf = new JFrame();

		private JProgressBar jpb = new JProgressBar();

		private JLabel jlfilecount = new JLabel();
		
		//private JLabel jshowfile = new JLabel(); add by Ruiqiang 2013-04-02

		private JLabel jltime = new JLabel();

		private JLabel jllinenumber = new JLabel();

		private JLabel jltrial = new JLabel();

		private JLabel jlallip = new JLabel();

		private JLabel jresult = new JLabel();

		private JButton jButton = new JButton("查看日志");
		
		//private TextArea textArea = new TextArea(); add by Ruiqiang 2013-04-02
		//private JLabel jltextlabel = new JLabel();
		
 		public ProgressThread(){
			if (softtest.config.java.Config.LANGUAGE != 0) {
				jButton.setText("Check log");
			}
		}

		public void setPercent(int p) {
			this.percent = p;
		}
		
		public void setPrePercent(int p){
			this.prePercent = p;
		}

		@Override
		public void run() {
			jf.setResizable(false);
			jf.setLocation(400, 300);
			if (Config.ISTRIAL) {
				jf.setSize(600, 260);
			} else {
				jf.setSize(525, 225);
				
			}
			Container jp = jf.getContentPane();

			jf.setVisible(true);
			jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			
			if (Config.ISTRIAL) {
				if (softtest.config.java.Config.LANGUAGE == 0) {
					jf.setTitle("(试用版)正在分析");
				} else {
					jf.setTitle("DTSJava(Trial Version) is Analyzing......");
				}
			} else {
				if (softtest.config.java.Config.LANGUAGE == 0) {
					jf.setTitle("正在加载");
				} else {
					jf.setTitle("DTSJava is Analyzing......");
				}
			}
			
			
			jp.setLayout(null);
			
			/* modified by 2013-05-19
			jpb.setBounds(10, 10, 500, 30);
			jlfilecount.setBounds(10, 40, 500, 30);			
			jllinenumber.setBounds(10, 80, 500, 30);
			jltime.setBounds(10, 120, 500, 30);
			jButton.setBounds(400, 150, 100, 30);
			jresult.setBounds(10, 150, 350, 35);
			*/
			jpb.setBounds(10,40, 500, 30);
			jlfilecount.setBounds(10,80, 500, 30);
			//jlfilecount.setHorizontalAlignment(SwingConstants.CENTER);
			//jlfilecount.setText("正在加载文件，请稍后...");
			
			jButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					Process rt = null;
					try {
						rt = Runtime.getRuntime().exec("notepad "+Config.LOG_FILE,
								null, new File(LOG_DIR));
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			});
			if (Config.ISTRIAL) {
				jltrial.setBounds(10, 180, 500, 30);
				jlallip.setBounds(10, 200, 500, 30);
			}

			jp.add(jpb);
			jp.add(jlfilecount);
			jp.add(jllinenumber);
			jp.add(jltime);
			jp.add(jButton);
			jp.add(jresult);
			
			//jButton.setVisible(false);
			if (Config.ISTRIAL) {
				jp.add(jltrial);
				jp.add(jlallip);
			}

			if (softtest.config.java.Config.LANGUAGE == 0) {
				//jlfilecount.setText("文件数: "); modified by Ruiqiang 2013-04-02
				//jllinenumber.setText("代码总行数: ");
				//jltime.setText("分析时间: ");

				if (Config.ISTRIAL) {
					jltrial.setText("报告IP数: ");
					jlallip.setText("总共IP数: ");
				}
				//2013-05-19 by Ruiqiang
				while (percent < 100 || prePercent < 100) {
					if(percent ==0 && prePercent <100 )
						jpb.setValue(prePercent);
					else 
						jpb.setValue(percent);
					
					jpb.setStringPainted(true);
					// add by Ruiqiang 2013-04-02
					//在进度条上显示当前分析的文件信息，对于path长的显示不下的，暂时采取只显示后面部分信息
					if(current_file != null){
						/*
						if(current_file.length()>70){
							int i =0, pIndex =0;
							int tempIndex = current_file.lastIndexOf(File.separator);
							while(i<5 && tempIndex!=-1){
								pIndex = tempIndex;
								tempIndex = current_file.lastIndexOf(File.separator, pIndex-1);
								i++;
							}
							String stemp = current_file.substring(pIndex);
							//System.out.println(stemp + "   pIndex:" + pIndex);
							
							jlfilecount.setText("正在分析文件： \\..."+ stemp);
						}else{
							jlfilecount.setText("正在分析文件：" + current_file);
						}
						*/
						//2013-05-19 by Ruiqiang 
						if(current_file.length()>60){
							String temp = current_file;
							while(temp.length() > 60 ){
								int index = temp.indexOf(File.separator);
								temp = temp.substring(index+1);
							}
							int diskIndex = current_file.indexOf(":");
							if(prePercent <100){								
								jlfilecount.setText("正在预处理文件：" + current_file.substring(0, diskIndex+1)+"\\...\\" + temp);
							}else 
								jlfilecount.setText("正在分析文件：" + current_file.substring(0, diskIndex+1) +"\\...\\" + temp);
						}else{
							if(prePercent <100){
								jlfilecount.setText("正在预处理文件：" + current_file);
							}else
								jlfilecount.setText("正在分析文件：" + current_file);
						}
					}	

					try {
						Thread.sleep(500);
					} catch (Exception e) { }
				}
				//modified by Ruiqiang 2013-5-19
				jpb.setBounds(10, 10, 500, 30);
				jlfilecount.setBounds(10, 40, 500, 30);	
				jllinenumber.setBounds(10, 80, 500, 30);
				jltime.setBounds(10, 120, 500, 30);
				jButton.setBounds(400, 150, 100, 30);
				jresult.setBounds(10, 150, 350, 35);
				
				jpb.setValue(percent);
				jpb.setStringPainted(true);
				jlfilecount.setText("文件数: " + filecount + "  个");
				jllinenumber.setText("代码总行数: " + linecount + "  行");
				jltime.setText("分析时间: " + usedtime + "  秒");
				

				if (Config.ISTRIAL) {
					jltrial.setText("报告IP数: " + loopdata.db.writecount);
					jlallip.setText("总共IP数: " + loopdata.db.list_ip.size());
				}
				if (Config.ISTRIAL) {
					jf.setTitle("DTSJava(试用版)分析结束");
				} else {
					jf.setTitle("DTSJava分析结束");
				}
			} else {
				jlfilecount.setText("Files number: ");
				jllinenumber.setText("Total code lines number: ");
				jltime.setText("Analysis used time: ");

				if (Config.ISTRIAL) {
					jltrial.setText("Report IP number: ");
					jlallip.setText("Total IP number: ");
				}
				while (percent < 100) {
					jpb.setValue(percent);
					jpb.setStringPainted(true);
					try {
						Thread.sleep(500);
					} catch (Exception e) {
					}
				}
				jpb.setValue(percent);
				jpb.setStringPainted(true);
				jlfilecount.setText("Files number: " + filecount + "  pieces");
				jllinenumber.setText("Total code lines number: " + linecount
						+ "  lines");
				jltime.setText("Analysis used time: " + usedtime + "  seconds");

				if (Config.ISTRIAL) {
					jltrial.setText("Report IP number: "
							+ loopdata.db.writecount);
					jlallip.setText("Total IP number: "
							+ loopdata.db.list_ip.size());
				}
				if (Config.ISTRIAL) {
					jf.setTitle("DTSJava(Trial Version)Analysis finish");
				} else {
					jf.setTitle("DTSJava Analysis finish");
				}
			}
			
			if(Config.AUTOCLOSE){
				jf.dispose();
			    jf.setVisible(false);
			}
		}
	}

	class AnalysisThread extends Thread {
		private FSMControlFlowData data;

		private String path;

		public AnalysisThread(FSMControlFlowData data, String path) {
			this.data = data;
			this.path = path;
		}

		@Override
		public void run() {
			synchronized (AnalysisThread.class) {
				SuccessRe.check();//added by yang:2011-07-06
				try {
					linecount += ProjectAnalysis.fileAnalysis(data, path);
					//System.out.println("在线程 Analysis里面的输出");
				} catch (Exception e) {
					if (softtest.config.java.Config.TESTING
							|| softtest.config.java.Config.DEBUG) {
						e.printStackTrace();
					}
					 //System.out.println(path+" encounter an analysis error!");

					ByteArrayOutputStream bo = new ByteArrayOutputStream();
					PrintStream po = new PrintStream(bo);
					e.printStackTrace(po);
					ProjectAnalysis.logger.fatal(path
							+ " encounter an analysis error!\n" + bo);
					// throw new RuntimeException(e);
				} catch (Error er) {
					ProjectAnalysis.logger.fatal(path
							+ " encounter an analysis error!\n"
							+ er.getMessage());
					MapOfVariable.clear();
					TypeSet.clear();
					// throw new RuntimeException(er);
				} finally {
					synchronized (data) {
						data.notifyAll();
					}
				}
			}
		}
	}

	/**
	 * 按照文件调用关系次序分析项目，用于单元测试
	 * 
	 * @param projectpath
	 *            项目路径
	 * @param classpath
	 *            项目依赖的classpath
	 * @throws FileNotFoundException
	 *             找不到某个分析文件
	 */
	public static int fileAnalysisForTest(String projectpath, String classpath,
			FSMControlFlowData loopdata) {
		// BasicConfigurator.configure();
		// PropertyConfigurator.configure( "log4j.properties" ) ;

		MethodNode.getMethodTable().clear();

		// 清空临时工作目录"temp"
		deleteAndCreateTemp();

		// 尝试编译被分析项目，输出的class文件存放在"temp"目录下
		DTSJavaCompiler compiler = new DTSJavaCompiler();
		if (!compiler.compileProject(projectpath, classpath, "temp")) {
			DTSJavaCompiler.printCompileInfo(compiler.getDiagnostics());
		}

		MatchesFunction.registerSelfInSimpleContext();

		// 将temp目录注册为一个新的classpath
		new TypeSet(classpath + File.pathSeparator + "temp");

		// 查找项目中所有的java文件放到fileList中
		List<File> filelist = new LinkedList<File>();
		for (String pp : projectpath.split(File.pathSeparator)) {
			findAllJavaFileObject(new File(pp), filelist);
		}

		// 预分析
		for (File sourceFile : filelist) {
			try {
				TypeSet.clear();
				JavaParser parser = new JavaParser(new JavaCharStream(
						new FileInputStream(sourceFile)));
				parser.setJDK15();
				ASTCompilationUnit astroot = parser.CompilationUnit();

				// 符号表分析
				// 1. 作用域和声明分析
				// 2. 表达式类型分析
				// 3. 标记符出现分析
				ScopeAndDeclarationFinder sc = new ScopeAndDeclarationFinder();
				astroot.jjtAccept(sc, null);
				astroot.jjtAccept(new PakageAndImportVisitor(), TypeSet
						.getCurrentTypeSet());
				astroot.getScope().resolveTypes(TypeSet.getCurrentTypeSet());
				astroot.jjtAccept(new ExpressionTypeFinder(), TypeSet
						.getCurrentTypeSet());
				OccurrenceFinder of = new OccurrenceFinder();
				astroot.jjtAccept(of, null);

				// 方法间调用关系分析
				mcgraph.setCurrentFileName(sourceFile.getAbsolutePath());
				astroot.jjtAccept(new MethodNodeVisitor(), mcgraph);
				mcgraph.setCurrentFileName(null);
			} catch (Exception e) {
				if (softtest.config.java.Config.TESTING
						|| softtest.config.java.Config.DEBUG) {
					e.printStackTrace();
				}
				ProjectAnalysis.logger.fatal(sourceFile
						+ " encounter an pre-analysis error!\n"
						+ e.getMessage());
			}
		}

		for (File sourceFile : filelist) {
			try {
				TypeSet.clear();
				JavaParser parser = new JavaParser(new JavaCharStream(
						new FileInputStream(sourceFile)));
				parser.setJDK15();
				ASTCompilationUnit astroot = parser.CompilationUnit();

				// 符号表分析
				// 1. 作用域和声明分析
				// 2. 表达式类型分析
				// 3. 标记符出现分析
				ScopeAndDeclarationFinder sc = new ScopeAndDeclarationFinder();
				astroot.jjtAccept(sc, null);
				astroot.jjtAccept(new PakageAndImportVisitor(), TypeSet
						.getCurrentTypeSet());
				astroot.getScope().resolveTypes(TypeSet.getCurrentTypeSet());
				astroot.jjtAccept(new ExpressionTypeFinder(), TypeSet
						.getCurrentTypeSet());
				OccurrenceFinder of = new OccurrenceFinder();
				astroot.jjtAccept(of, null);

				// 方法间调用关系分析
				mcgraph.setCurrentFileName(sourceFile.getAbsolutePath());
				astroot.jjtAccept(new MethodCallVisitor(), mcgraph);
				mcgraph.setCurrentFileName(null);
			} catch (Exception e) {
				if (softtest.config.java.Config.TESTING
						|| softtest.config.java.Config.DEBUG) {
					e.printStackTrace();
				}
				ProjectAnalysis.logger.fatal(sourceFile
						+ " encounter an pre-analysis error!\n"
						+ e.getMessage());
			}
		}

		// 得到应该的文件分析顺序，拓扑逆序
		List<String> analysislist = mcgraph.getReverseFileTopoOrder(filelist);
		// Collections.reverse(analysislist);

		// 正式分析
		double count = 0;
		linecount = 0;
		for (String filename : analysislist) {
			count++;
			try {
				linecount += ProjectAnalysis.fileAnalysis(loopdata, filename);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}

		return analysislist.size();
	}

	private static FSMControlFlowData loopdata = null;

	/**
	 * 按照文件调用关系次序分析项目
	 * 
	 * @param projectpath
	 *            项目路径
	 * @param classpath
	 *            项目依赖的classpath
	 * @throws FileNotFoundException
	 *             找不到某个分析文件
	 */
	public static int projectAnalysisByFile(String projectpath,
			String classpath, FSMControlFlowData loopdata) {
		ProjectAnalysis.loopdata = loopdata;
		project = projectpath;

		// BasicConfigurator.configure();
		// PropertyConfigurator.configure( "log4j.properties" ) ;
		Date t1, t2;
		t1 = new Date();
		
		// 查找项目中所有的java文件放到fileList中
		List<File> filelist = new LinkedList<File>();
		for (String pp : projectpath.split(File.pathSeparator)) {
			findAllJavaFileObject(new File(pp), filelist);
		}
		
		// 启动线程分析进度条,并设置其为守护进程			
		ProjectAnalysis test = new ProjectAnalysis();
		ProgressThread pthread = null;
		if (!Config.TESTING) {
			pthread = test.new ProgressThread();
			pthread.setDaemon(true);
			pthread.start();
		}

		MethodNode.getMethodTable().clear();

		// 清空临时工作目录"temp"
		deleteAndCreateTemp();

		// 尝试编译被分析项目，输出的class文件存放在"temp"目录下
		DTSJavaCompiler compiler = new DTSJavaCompiler();
		try {
			if (!compiler.compileProject(projectpath, classpath, "temp")) {
				DTSJavaCompiler.printCompileInfo(compiler.getDiagnostics());
				ProjectAnalysis.logger.fatal(projectpath
						+ " encounter a compile error!\n");
				int temp = 0;
				for (Diagnostic diagnostic : compiler.getDiagnostics()) {
					if (diagnostic.getKind() == Diagnostic.Kind.ERROR) {
						String str = "";
						str = String.format("Code: %s%n" + "Kind: %s%n"
								+ "Position: %s%n" + "Start Position: %s%n"
								+ "End Position: %s%n" + "Source: %s%n"
								+ "Message: %s%n", diagnostic.getCode(),
								diagnostic.getKind(), diagnostic.getPosition(),
								diagnostic.getStartPosition(), diagnostic
										.getEndPosition(), diagnostic
										.getSource(), diagnostic
										.getMessage(null));
						ProjectAnalysis.logger.fatal(str + "\n");
						temp++;
						if (temp == 3) {
							break;
						}
					}
				}
			}
		} catch (Exception e) {
			ProjectAnalysis.logger.fatal(projectpath + " access error!\n"
					+ e.getMessage());
		}

		MatchesFunction.registerSelfInSimpleContext();

		// 将temp目录注册为一个新的classpath
		new TypeSet(classpath + File.pathSeparator + "temp");

		boolean DD = true;
		// String TF="VisualHandler.java";
		String TF = "Controller.java";
		// String TF = "PlayListIO.java";
		String TF1 = "TableRenderState.java";
		int num = 15;
		int count1 = 0;

		int LOC = 0;
		
		SuccessRe.check();//added by yang:2011-07-06

		//2013-05-03
		pthread.jf.setTitle("正在预处理");
		// 预分析
		logger.info("BEGIN : pre-analysis1 : FindAllMethods");
		int count = 0;
		for (File sourceFile : filelist) {
			if (softtest.config.java.Config.DEBUG) {
				if (DD && !sourceFile.getName().endsWith(TF)
						&& !sourceFile.getName().endsWith(TF1)) {
					continue;
				}
				if (DD && count1 > num) {
					count1 = 0;
					break;
				}
				count1++;
			}

			try {
				logger
						.debug("FindAllMethods : "
								+ sourceFile.getAbsolutePath());

				TypeSet.clear();

				JavaParser parser = new JavaParser(new JavaCharStream(
						new FileInputStream(sourceFile)));
				parser.setJDK15();
				ASTCompilationUnit astroot = parser.CompilationUnit();

				ProjectAnalysis.setCurrent_file(sourceFile.getAbsolutePath());
				ProjectAnalysis.setCurrent_astroot(astroot);

				// 符号表分析
				// 1. 作用域和声明分析
				// 2. 表达式类型分析
				// 3. 标记符出现分析
				ScopeAndDeclarationFinder sc = new ScopeAndDeclarationFinder();
				astroot.jjtAccept(sc, null);
				astroot.jjtAccept(new PakageAndImportVisitor(), TypeSet
						.getCurrentTypeSet());
				astroot.getScope().resolveTypes(TypeSet.getCurrentTypeSet());
				astroot.jjtAccept(new ExpressionTypeFinder(), TypeSet
						.getCurrentTypeSet());
				OccurrenceFinder of = new OccurrenceFinder();
				astroot.jjtAccept(of, null);

				// 方法间调用关系分析
				mcgraph.setCurrentFileName(sourceFile.getAbsolutePath());
				astroot.jjtAccept(new MethodNodeVisitor(), mcgraph);
				mcgraph.setCurrentFileName(null);

				LOC += astroot.getEndLine();

			} catch (Exception e) {
				ProjectAnalysis.logger.fatal(sourceFile
						+ " encounter an pre-analysis error!\n"
						+ e.getMessage());
			} catch (Error er) {
				ProjectAnalysis.logger.fatal(sourceFile
						+ " encounter an pre-analysis error!\n"
						+ er.getMessage());
			}

			++count;
			//设置进度比例
			if (!Config.TESTING) {
				//2013-05-19 by Ruiqiang
				//pthread.setPercent((int) (count * 20.0 / filelist.size()));	
				//System.out.println((int) (count * 50.0 / filelist.size()));
				pthread.setPrePercent((int) (count * 50.0 / filelist.size()));
			}
		}//for
		
		logger.info("END : pre-analysis1 : FindAllMethods");
		//2013-05-19 by Ruiqiang
		
		if (!Config.TESTING) {
			pthread.setPrePercent(50);
		}
		
		
		logger.info("BEGIN : pre-analysis2 : FindTheRelationOfCaller-Callee");
		
		count = 0;
		
		for (File sourceFile : filelist) {
			if (softtest.config.java.Config.DEBUG) {
				if (DD && !sourceFile.getName().endsWith(TF)
						&& !sourceFile.getName().endsWith(TF1)) {
					continue;
				}
				if (DD && count1 > num) {
					count1 = 0;
					break;
				}
				count1++;
			}

			try {
				logger.debug("FindTheRelationOfCaller-Callee : "
						+ sourceFile.getAbsolutePath());

				TypeSet.clear();

				JavaParser parser = new JavaParser(new JavaCharStream(
						new FileInputStream(sourceFile)));
				parser.setJDK15();
				ASTCompilationUnit astroot = parser.CompilationUnit();

				ProjectAnalysis.setCurrent_file(sourceFile.getAbsolutePath());
				ProjectAnalysis.setCurrent_astroot(astroot);

				// 符号表分析
				// 1. 作用域和声明分析
				// 2. 表达式类型分析
				// 3. 标记符出现分析
				ScopeAndDeclarationFinder sc = new ScopeAndDeclarationFinder();
				astroot.jjtAccept(sc, null);
				astroot.jjtAccept(new PakageAndImportVisitor(), TypeSet
						.getCurrentTypeSet());
				astroot.getScope().resolveTypes(TypeSet.getCurrentTypeSet());
				astroot.jjtAccept(new ExpressionTypeFinder(), TypeSet
						.getCurrentTypeSet());
				OccurrenceFinder of = new OccurrenceFinder();
				astroot.jjtAccept(of, null);

				// 方法间调用关系分析
				mcgraph.setCurrentFileName(sourceFile.getAbsolutePath());
				astroot.jjtAccept(new MethodCallVisitor(), mcgraph);
				mcgraph.setCurrentFileName(null);

			} catch (Exception e) {
				ProjectAnalysis.logger.fatal(sourceFile
						+ " encounter an pre-analysis error!\n"
						+ e.getMessage());
			} catch (Error er) {
				ProjectAnalysis.logger.fatal(sourceFile
						+ " encounter an pre-analysis error!\n"
						+ er.getMessage());
			}
			++count;
			if (!Config.TESTING) {
				//2013-05-19 by Ruiqiang
				//pthread.setPercent((int) (count * 20.0 / filelist.size()) + 20);
				//System.out.println((int) (count * 50.0 / filelist.size()) + 50);
				pthread.setPrePercent((int) (count * 50.0 / filelist.size()) +50);
			}
		}//for
		
		logger.info("END : pre-analysis2 : FindTheRelationOfCaller-Callee");
		//2013-05-19 by Ruiqiang
		/*
		if (!Config.TESTING) {
			pthread.setPercent(40);
		}
		*/
		// 得到应该的文件分析顺序，拓扑逆序
		List<String> analysislist = mcgraph.getReverseFileTopoOrder(filelist);
		/*
		 * List<String> analysislist =
		 * MethodCallGraph.getFileTopoOrder(filelist);
		 * Collections.reverse(analysislist); TreeSet<String> set = new TreeSet<String>(analysislist);
		 * System.out.println(set.size());
		 */

		if (!Config.USE_SUMMARY) {
			MethodNode.getMethodTable().clear();
		}

		SuccessRe.check();//added by yang:2011-07-06
		// 正式分析
		linecount = 0;
		count = 0;
		logger.info("BEGIN : FSM Calculating");
		//2013-04-07
		pthread.jf.setTitle("正式分析");
		pthread.jpb.setValue(0);
		
		for (String filename : analysislist) {
			if (softtest.config.java.Config.DEBUG) {
				if (/* DD && */!filename.endsWith(TF)
						&& !filename.endsWith(TF1)) {
					continue;
				}
				if (DD && count1 > num) {
					count1 = 0;
					break;
				}
				count1++;
			}

			logger.debug("FSMCalculating : " + filename);
			AnalysisThread t = test.new AnalysisThread(loopdata, filename);
			long start = System.currentTimeMillis();
			t.setDaemon(true);
			synchronized (loopdata) {
				t.start();
				try {
 					loopdata.wait(Config.TIMEOUT);
				} catch (InterruptedException e) {
					// System.out.println("dfa");
					// logger.error("timeout for : "+filename);
				}
			}
			long end = System.currentTimeMillis();
			//int time=(int) (end-start);
			//System.out.println("分析时间"+time);
			if (end - start >= Config.TIMEOUT) {
				
				// System.out.println("stop a thread");
				t.stop();
				//t.interrupt();
			
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {

				}
			}

			++count;
			if (!Config.TESTING) {
				/*
				int tempp = (int) (count * 60.0 / filelist.size()) + 40;
				pthread.setPercent(tempp >= 100 ? 99 : tempp);
				*/
				//System.out.println((int) (count * 100.0 / filelist.size()));
				pthread.setPercent((int) (count * 100.0 / filelist.size()));
			}
		}
		if (Config.ISTRIAL) {
			loopdata.db.writeIP();
		}
		logger.info("END : FSM Calculating");

		t2 = new Date();
		usedtime = (t2.getTime() - t1.getTime()) / 1000;
		filecount = analysislist.size();
		if (!Config.TESTING) {
			pthread.setPercent(100);
		}
		loopdata.db.writeResult((int) usedtime, filecount, linecount);
		try {
			if (readFile(LOG_DIR + File.separator + Config.LOG_FILE).trim().length() == 0) {
				if (softtest.config.java.Config.LANGUAGE == 0) {
					pthread.jresult.setText("文件分析正常结束。");
				} else {
					pthread.jresult
							.setText("Files analysis normally finished.");
				}
			} else {
				if (softtest.config.java.Config.LANGUAGE == 0) {
					pthread.jresult.setText("分析过程中出现错误信息，请查看日志。");
				} else {
					pthread.jresult
							.setText("<html>There is error information in the process of analysis,<br>please check the log.</html>");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return analysislist.size();
	}

	/**
	 * <p>
	 * Reads the content of a given file.
	 * </p>
	 * 
	 * @param fileName
	 *            the name of the file to read.
	 * 
	 * @return a string represents the content.
	 * 
	 * @throws IOException
	 *             when error occurs during reading.
	 */
	public static String readFile(String fileName) throws IOException {
		Reader reader = new FileReader(fileName);

		try {
			// create a StringBuffer instance.
			StringBuffer sb = new StringBuffer();

			// buffer for reading.
			char[] buffer = new char[1024];

			// number of read chars.
			int k = 0;

			// Read characters and append to string buffer
			while ((k = reader.read(buffer)) != -1) {
				sb.append(buffer, 0, k);
			}

			// return read content
			return new String(sb);
		} finally {
			try {
				reader.close();
			} catch (IOException ioe) {
				// ignore
			}
		}
	}

	private static int fileAnalysis(FSMControlFlowData loopdata, String filename)
			throws FileNotFoundException {
		// 清除函数摘要和其他分析信息的关联
		MapOfVariable.clear();
		TypeSet.clear();
		
		JavaParser parser = new JavaParser(new JavaCharStream(
				new FileInputStream(filename)));
		parser.setJDK15();
		ASTCompilationUnit astroot = parser.CompilationUnit();
		current_file = filename;
		current_astroot = astroot;
		
			
		// 符号表分析
		// 1. 作用域和声明分析
		// 2. 表达式类型分析
		// 3. 标记符出现分析
		ScopeAndDeclarationFinder sc = new ScopeAndDeclarationFinder();
		astroot.jjtAccept(sc, null);
		astroot.jjtAccept(new PakageAndImportVisitor(), TypeSet
				.getCurrentTypeSet());
		astroot.getScope().resolveTypes(TypeSet.getCurrentTypeSet());		
		/*
		 * 第二遍遍历语法树:将函数调用对应到正确的函数版本(能够将函数标记符的出现对应到正确的声明处),
		 * 需要先对函数形参和实参的类型进行分析并进行对应
		 */
		astroot.jjtAccept(new ExpressionTypeFinder(), TypeSet
				.getCurrentTypeSet());
		/*
		 * 第三遍遍历语法树:对所有标记符出现的地方进行处理，将每一个标记符的出现和相应的正确声明进行联系
		 */
		OccurrenceFinder of = new OccurrenceFinder();
		astroot.jjtAccept(of, null);

		// 产生控制流图
		astroot.jjtAccept(new ControlFlowVisitor(), null); 

		// 处理定义使用
		astroot.jjtAccept(new DUAnaysisVistor(), null);

		// 初始化区间
		astroot.getScope().initDomains();

		// 分析文件内部调用关系
		current_call_graph = new CGraph();
		astroot.getScope().resolveCallRelation(current_call_graph);

		List<CVexNode> list = current_call_graph.getTopologicalOrderList();
		Collections.reverse(list);

		// 计算函数摘要
		if (Config.USE_LIBSUMMARY) {
			LibManager libManager = LibManager.getInstance();
			libManager.loadLib("cfg/npd");
		}
		ControFlowDomainVisitor tempvisitor = new ControFlowDomainVisitor();
		ArrayList<SimpleJavaNode> methodlist = new ArrayList<SimpleJavaNode>();
		HashSet<SimpleJavaNode> methodset = new HashSet<SimpleJavaNode>();
		for (CVexNode n : list) {
			ASTMethodDeclaration method = (ASTMethodDeclaration) n
					.getMethodNameDeclaration().getMethodNameDeclaratorNode()
					.jjtGetParent();
			method.jjtAccept(tempvisitor, null);
			methodset.add(method);
			genMethodSummary(method);
		}
		astroot.jjtAccept(new MethodAndConstructorFinder(), methodlist);

		for (SimpleJavaNode method : methodlist) {
			if (!methodset.contains(method)) {
				method.jjtAccept(tempvisitor, null);
				genMethodSummary(method);
			}
		}

		for (SimpleJavaNode n : methodlist) {
			n.jjtAccept(new PathCountVisitor(true), loopdata);
		}

		FSMAnalysisVisitor fsmvisitor = new FSMAnalysisVisitor();
		for (FSMMachine fsm : fsms) {
			fsmvisitor.addFSMS(fsm);
		}

		loopdata.parsefilename = filename;
		astroot.jjtAccept(fsmvisitor, loopdata);

		current_file = null;
		current_astroot = null;
		current_call_graph = null;

		return astroot.getEndLine();
	}

	private static void genMethodSummary(SimpleJavaNode treenode) {
		Object type = null;
		if (treenode instanceof ASTConstructorDeclaration) {
			type = ((ASTConstructorDeclaration) treenode).getType();
		}
		if (treenode instanceof ASTMethodDeclaration) {
			type = ((ASTMethodDeclaration) treenode).getType();
		}
		MethodNode mn = MethodNode.findMethodNode(type);
		if (mn == null || mn.getMethodsummary() != null) {
			// 已经计算过该函数的摘要了,当前未考虑摘要的比较
			return;
		}
		MethodSummary summary = new MethodSummary();
		// if (mn != null) {
		mn.setMethodsummary(summary);
		// }

		ProjectAnalysis.getPreconditionListener()
				.setPreconditionSetForListeners(summary.getPreconditons());
		ProjectAnalysis.getPostconditionListener()
				.setPostconditionSetForListeners(summary.getPostconditons());
		ProjectAnalysis.getFeatureListenerSet().setFeatureSetForListeners(
				summary.getFeatrues());

		ProjectAnalysis.getPreconditionListener().listen(treenode);
		ProjectAnalysis.getPostconditionListener().listen(treenode);
		ProjectAnalysis.getFeatureListenerSet().listen(treenode);
	}

	public static MethodCallGraph getMcgraph() {
		return mcgraph;
	}

	public static void setMcgraph(MethodCallGraph mcgraph) {
		ProjectAnalysis.mcgraph = mcgraph;
	}

	public static ASTCompilationUnit getCurrent_astroot() {
		return current_astroot;
	}

	public static void setCurrent_astroot(ASTCompilationUnit current_astroot) {
		ProjectAnalysis.current_astroot = current_astroot;
	}

	public static CGraph getCurrent_call_graph() {
		return current_call_graph;
	}

	public static void setCurrent_call_graph(CGraph current_call_graph) {
		ProjectAnalysis.current_call_graph = current_call_graph;
	}

	public static String getCurrent_file() {
		return current_file;
	}

	public static void setCurrent_file(String current_file) {
		ProjectAnalysis.current_file = current_file;
	}

}

class MethodAndConstructorFinder extends JavaParserVisitorAdapter {
	@Override
	public Object visit(ASTConstructorDeclaration treenode, Object data) {
		ArrayList<SimpleJavaNode> methodlist = (ArrayList<SimpleJavaNode>) data;
		methodlist.add(treenode);
		return null;
	}

	@Override
	public Object visit(ASTMethodDeclaration treenode, Object data) {
		ArrayList<SimpleJavaNode> methodlist = (ArrayList<SimpleJavaNode>) data;
		methodlist.add(treenode);
		return null;
	}
}