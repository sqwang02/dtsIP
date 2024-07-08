package softtest.fsmanalysis.java;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.*;
import javax.tools.*;

public class DTSJavaCompiler {
	private String classpath = null;  // -classpath, -cp
	private String sourcepath = null; // -sourcepath
	private String outputpath = null; // -d
	private JavaCompiler javaCompiler = null;
	private DiagnosticCollector<JavaFileObject> diagnostics = null;
	
	private File getFileObject(String filename) throws IllegalArgumentException,FileNotFoundException {
		if (filename == null || filename.trim().length() == 0) {
			throw new IllegalArgumentException("the argument (String filename) is null or an empty string");
		}
		if (!filename.endsWith(".java")) {
			throw new IllegalArgumentException("the argument (String filename) does not end with \".java\"");
		}
		File javafile = new File(filename);
		if (!javafile.exists()) {
			throw new FileNotFoundException("the file \""+filename+"\" does not exist");
		}
		return javafile;
	}
	
	public boolean compileFiles(Iterable<File> javafiles) {
		this.diagnostics = new DiagnosticCollector<JavaFileObject>();		
		StandardJavaFileManager fileManager = javaCompiler.getStandardFileManager(diagnostics, Locale.CHINA, Charset.defaultCharset());
		Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(javafiles);
		
		List<String> options = new ArrayList<String>();
		if (classpath != null && classpath.trim().length() > 0) {
			options.add("-cp");
			options.add(classpath);
		}
		if (sourcepath != null && sourcepath.trim().length() > 0) {
			options.add("-sourcepath");
			options.add(sourcepath);
		}
		if (outputpath != null && outputpath.trim().length() > 0) {
			options.add("-d");
			options.add(outputpath);
		}
		
		if (softtest.config.java.Config.SOURCE14) {
			options.add("-source");
			options.add("1.4");
		}
		
		JavaCompiler.CompilationTask task = javaCompiler.getTask(null, fileManager,	diagnostics, options, null, compilationUnits);
		boolean succ = task.call();
		try {
			fileManager.close();
		} catch (IOException e) {
			throw new RuntimeException("fileManager can't be closed", e);
		}
		return succ;
	}
	
	public List<Diagnostic<? extends JavaFileObject>> getDiagnostics() {
		if (diagnostics == null) {
			return null;
		} else {
			return diagnostics.getDiagnostics();
		}
	}
	
	public boolean compileFile(File javafile) {
		return compileFiles(Arrays.asList(javafile));
	}
	
	public boolean compileFile(String filename) throws IllegalArgumentException,FileNotFoundException {
		return compileFile(getFileObject(filename));		
	}
	
	private void findAllJavaFileObject(File dir, List<File> fileContainer) {
		File files[] = dir.listFiles();
		for (File f : files) {
			if (f.isDirectory()) {
				findAllJavaFileObject(f, fileContainer);
			}
			if (f.isFile() && f.getName().endsWith(".java")) {
				//¶­ÓñÀ¤ÓÚ2011 10 28ÐÞ¸Ä
				UTFJavaFile ujf=new UTFJavaFile(f);
				ujf.processUTF();
				
				fileContainer.add(f);
			}
		}
	}
	
	public boolean compileProject(String projRootPath, String outputpath) {
		if (outputpath != null) {
			this.outputpath = outputpath;
		}
		return compileProject(projRootPath);
	}
	
	private String findAllJars(String path) {
		if (path == null ) {
			return "";
		}
		StringBuffer classpath = new StringBuffer(path);
		String [] paths = path.split(File.pathSeparator);
		for (String p : paths) {
			File ff = new File(p);
			if (ff.isDirectory()) {
				File[] jars = ff.listFiles();
				for (File jar : jars) {
					if (jar.getName().endsWith(".jar")) {
						classpath.append(File.pathSeparatorChar);
						classpath.append(jar.getAbsolutePath());
					}
				}
			}
		}
		return classpath.toString();
	}
	
	public boolean compileProject(String projRootPath, String classpath, String outputpath) {
		if (outputpath != null) {
			this.outputpath = outputpath;
		}
		if (classpath != null) {
			this.classpath = findAllJars(classpath);
		}
		return compileProject(projRootPath);
	}
	
	public boolean compileProject(String projRootPath, String classpath, String outputpath, String sourcepath) {
		if (classpath != null) {
			this.classpath = findAllJars(classpath);
		}
		if (outputpath != null) {
			this.outputpath = outputpath;
		}
		if (sourcepath != null) {
			this.sourcepath = sourcepath;
		}
		return compileProject(projRootPath);
	}
	
	public boolean compileProject(String projRootPath) {
		String[] dirnames = projRootPath.split(File.pathSeparator);
		List<File> files = new ArrayList<File>();
		for (String dirname : dirnames) {
			File dir = new File(dirname);
			if (dir.isDirectory()) {
				findAllJavaFileObject(dir, files);
			}
			else if (dir.isFile() && dir.getName().endsWith(".java")) {

				//¶­ÓñÀ¤ÓÚ2011 10 28ÐÞ¸Ä
				UTFJavaFile ujf=new UTFJavaFile(dir);
				ujf.processUTF();
				
				files.add(dir);
			}
			}
		if (files.isEmpty()) {
			throw new IllegalArgumentException("the directory \""+projRootPath+"\" does not contain any java source files");
		}
		return compileFiles(files);
	}
	
	public boolean compileFile(String filename, String classpath, String outputpath, String sourcepath) throws IllegalArgumentException,FileNotFoundException {
		if (classpath != null) {
			this.classpath = findAllJars(classpath);
		}
		if (outputpath != null) {
			this.outputpath = outputpath;
		}
		if (sourcepath != null) {
			this.sourcepath = sourcepath;
		}
		return compileFile(filename);
	}
	
	public DTSJavaCompiler(String classpath, String sourcepath, String outputpath) {
		this.classpath = findAllJars(classpath);
		this.sourcepath = sourcepath;
		this.outputpath = outputpath;
		this.javaCompiler = ToolProvider.getSystemJavaCompiler();
	}

	public DTSJavaCompiler() {
		this.javaCompiler = ToolProvider.getSystemJavaCompiler();
	}

	public String getClasspath() {
		return classpath;
	}

	public void setClasspath(String classpath) {
		this.classpath = findAllJars(classpath);
	}

	public String getOutputpath() {
		return outputpath;
	}

	public void setOutputpath(String outputpath) {
		this.outputpath = outputpath;
	}

	public String getSourcepath() {
		return sourcepath;
	}

	public void setSourcepath(String sourcepath) {
		this.sourcepath = sourcepath;
	}
	
	
	public static void printCompileInfo(List<Diagnostic<? extends JavaFileObject>> digs) {
		for (Diagnostic diagnostic : digs){ 
			if (diagnostic.getKind() == Diagnostic.Kind.ERROR)
				System.out.printf(
				"Code: %s%n" +
				"Kind: %s%n" +
				"Position: %s%n" +
				"Start Position: %s%n" +
				"End Position: %s%n" +
				"Source: %s%n" +
				"Message: %s%n",
				diagnostic.getCode(), diagnostic.getKind(),
				diagnostic.getPosition(), diagnostic.getStartPosition(),
				diagnostic.getEndPosition(), diagnostic.getSource(),
				diagnostic.getMessage(null));
		}
	}

	public static void main(String args[]) throws Exception {
		// compilejava();
		// System.out.println("??");
		// runjava();
		DTSJavaCompiler compiler = new DTSJavaCompiler(
				"/usr/lib/eclipse/plugins/org.junit4_4.1.0.1/junit-4.1.jar:/home/younix/workspace/dts_java/lib/ant.jar:/home/younix/workspace/dts_java/lib/asm-3.0.jar:/home/younix/workspace/dts_java/lib/backport-util-concurrent.jar:/home/younix/workspace/dts_java/lib/jaxen-full.jar:/home/younix/workspace/dts_java/lib/log4j-1.2.8.jar:/home/younix/workspace/dts_java/lib/mysql-connector-java-5.0.4-bin.jar:/home/younix/workspace/dts_java/lib/saxpath.jar:/home/younix/workspace/dts_java/lib/servlet-api.jar",
				null, "/home/younix/workspace/javaclass");
		if (compiler.compileProject("/home/younix/workspace/dts_java")) {
			// if
			// (compiler.compileFile("/home/younix/workspace/eclipse/testcompiler/test.java"))
			// {
			System.out.println("compile success");
		} else {
			System.out.println("compile failed");
		}
		printCompileInfo(compiler.getDiagnostics());
	}
}