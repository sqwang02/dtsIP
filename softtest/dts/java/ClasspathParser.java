package softtest.dts.java;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sun.tools.doclets.internal.toolkit.util.SourcePath;

/**
 * For Eclipse
 * @author younix
 *
 */
public class ClasspathParser {
	private List<String> sourcePathList = new ArrayList<String>();
	private List<String> libPathList = new ArrayList<String>();
	private String classpathFile = null;
	private String projectPath = null;
	
	public boolean hasClasspathFile() {
		return classpathFile != null;
	}
	
	/**
	 * 获取eclipse的.classpath文件路径
	 * 
	 * 在manPath下，查找.classpath文件。
	 * 如果不存在则依次向上查找，且至多向上查找2层mainPath的父目录。
	 * 
	 * @param mainPath 输入的目录
	 * @return true 成功得到.classpath
	 *         false 不存在.classpath
	 */
	private String findEclipseClasspath(String mainPath) {
		if (mainPath == null || mainPath.trim().length() == 0) {
			throw new IllegalArgumentException("Param mainPath should not be null or zero-length");
		}
		
		File f = new File(mainPath);
		if (f.isFile()) {
			return null;
		}
		else if (f.isDirectory()) {
			int cnt = 0;
			while (f != null && cnt < 3) {
				File c = new File(f.getAbsolutePath()+File.separator+".classpath");
				if (c.exists()) {
					return c.getAbsolutePath();
				}
				f = c.getParentFile();
				++cnt;
			}
		}
		return null;
	}
	
	/**
	 * 检查是否存在eclipse的.classpath文件
	 * @param mainPath
	 * @return
	 */
	public boolean checkEclipseClasspath(String mainPath) {
		classpathFile = findEclipseClasspath(mainPath);
		if (classpathFile != null) {
			int index = classpathFile.lastIndexOf(File.separatorChar);
			if (index != -1) {
				projectPath = classpathFile.substring(0, index+1);
			}
		}
		
		return classpathFile != null;
	}
	
	/**
	 * 把表中的所有路径用File.pathSeparator连起来
	 * @param pathList
	 * @return
	 */
	private String compose(List<String> pathList) {
		StringBuffer sb = new StringBuffer();
		for (String s : pathList) {
			if (sb.length() == 0) {
				sb.append(s.replace('/', File.separatorChar).replace('\\', File.separatorChar));
			}
			else {
				sb.append(File.pathSeparator);
				sb.append(s.replace('/', File.separatorChar).replace('\\', File.separatorChar));
			}
		}
		return sb.toString();
	}
	
	public String getEclipseSrcPath() {
		return compose(sourcePathList);
	}
	
	public String getEclipseLibPath() {
		return compose(libPathList);
	}
	
	public ClasspathParser(String projectPath) {
		if (checkEclipseClasspath(projectPath)) {
			DocumentBuilderFactory domfac = DocumentBuilderFactory.newInstance();
			try {
				DocumentBuilder dombuilder = domfac.newDocumentBuilder();
				InputStream is = new FileInputStream(classpathFile);
				Document doc = dombuilder.parse(is);
				Element root = doc.getDocumentElement();

				NodeList nodes = root.getChildNodes();
				for (int i = 0; i < nodes.getLength(); i++) {
					Node node = nodes.item(i);
					if (node.getNodeName().equals("classpathentry")) {
						addClasspathEntry(node);
					}
				}
			} catch (IOException e) {
				throw new RuntimeException(
						"Errror in loading the classpath file", e);
			} catch (SAXException e) {
				throw new RuntimeException(
						"Errror in loading the classpath file", e);
			} catch (ParserConfigurationException e) {
				throw new RuntimeException(
						"Errror in loading the classpath file", e);
			}
		}
	}
	
	/**
	 * 根据工程路径，得到path对应的绝对路径
	 * @param path
	 * @return
	 */
	private String getAbsolutePath(String path) {
		if (path == null || path.trim().length() == 0) {
			throw new IllegalArgumentException("Param path should not be null or zero-length");
		}
		
		if (path.startsWith("/")) {
			return path;
		}
		else if (path.matches("^[a-zA-Z]:/")) {
			return path;
		}
		else {
			if (projectPath != null) {
				return projectPath.concat(path);
			}
			else {
				return path;
			}
		}
	}

	/**
	 * 添加一个classpathentry
	 * 目前仅考虑kind为src和lib的entry
	 * @param node
	 */
	private void addClasspathEntry(Node node) {
		// TODO Auto-generated method stub
		Node nkind = node.getAttributes().getNamedItem("kind");
		Node npath = node.getAttributes().getNamedItem("path");
		String kind = null;
		String path = null;
		if (nkind != null) {
			kind = nkind.getNodeValue();
		}
		if (npath != null) {
			path = npath.getNodeValue();
		}
		
		if (kind != null && kind.equals("src")) {
			Node combineaccessrules = node.getAttributes().getNamedItem("combineaccessrules");
			if (combineaccessrules == null || combineaccessrules.getNodeValue().equals("true")) {
				sourcePathList.add(getAbsolutePath(path));
			}
			else { // combineaccessrules = false;
				libPathList.add(getAbsolutePath(path));
			}
		}
		else if (kind != null && kind.equals("lib")) {
			libPathList.add(getAbsolutePath(path));
		}
		else if (kind != null && kind.equals("con")) {
			//libPathList.add(getAbsolutePath(path));
		}
	}
}
