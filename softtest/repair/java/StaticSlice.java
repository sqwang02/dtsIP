package softtest.repair.java;

/*import ASTNode;
import IdentifierCollectVisitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;*/
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import softtest.ast.java.*;

/*import org.eclipse.core.contenttype.jar;
import org.eclipse.core.jobs.jar;
import org.eclipse.core.resources.jar;
import org.eclipse.core.runtime.jar;
import org.eclipse.equinox.common.jar;
import org.eclipse.equinox.preferences.jar;
import org.eclipse.jdt.core.jar;
import org.eclipse.osgi.jar;*/


public class StaticSlice{
	public static void main(String []args) throws IOException{
		File file = new File("F:\\javaio\\Test.java");
		File tmp = File.createTempFile("tmp", ".java", file.getParentFile());	
		String statement=null;
		
		FileReader in =new FileReader(file);
		FileWriter out=new FileWriter(tmp);
		BufferedReader bi =new BufferedReader(in);
		BufferedWriter bo =  new BufferedWriter(out);
			
		while((statement=bi.readLine())!= null){
			if(!(statement.contains("if")||statement.contains("for")||statement.contains("while"))){
				if(statement.contains("x")){
					bo.write(statement);
					bo.newLine();
					bo.flush();
				}
				else
					continue;
			}
			else if(statement!=null){
				//遍历抽象语法树，语句孩子结点，看这个语句块中有没有相关变量
				
				
			}
			
		}
		
	}
	private List<String> statementList;
	private Map<String, Set<String>> dependencyMap;
	private Set<String> allVariableInMethod;
	private Map<String, Integer> inDegree;
	private void getDependency() {
		Set<String> leftHands = new HashSet<String>();
	    for (String statement : statementList) {
	        if (statement == null) {
	            continue;
	        }
	        if (!statement.contains("=") || statement.contains(">=") || statement.contains("<=") || statement.contains("==") || statement.contains("!=")) {
	            continue;
	        }

	        String oper = getOper(statement);
	        int index = statement.indexOf(oper);
	        String leftHand = getLeftHand(statement, index);//获取左侧表达式
	        allVariableInMethod.add(leftHand);

	            if (leftHands.contains(leftHand)) {
	                continue;
	            }

	            String rightHandExpression = getRightHand(statement, index + oper.length());

	            Set<String> rightHandSet = new HashSet<String>();
/*	            List<String> identifierInRightHand = getIdentifierList(rightHandExpression, ASTParser.K_EXPRESSION);
	            for (String identifier : identifierInRightHand) {
	                if (leftHand.equals(identifier)) {
	                    continue;
	                }
	                rightHandSet.add(identifier);
	                allVariableInMethod.addAll(rightHandSet);
	            }

	            if (rightHandSet.size() == 0) {
	                continue;
	            }

	            leftHands.add(leftHand);*/

	            for (String rightHand : rightHandSet) {
	                if (dependencyMap.containsKey(rightHand) && dependencyMap.get(rightHand).contains(leftHand)) {
	                    continue;
	                }
	                if (dependencyMap.containsKey(leftHand)) {
	                    dependencyMap.get(leftHand).add(rightHand);
	                } else {
	                    Set<String> set = new HashSet<String>();
	                    set.add(rightHand);
	                    dependencyMap.put(leftHand, set);
	                }
	                if (inDegree.containsKey(rightHand)) {
	                    inDegree.put(rightHand, inDegree.get(rightHand) + 1);
	                } else {
	                    inDegree.put(rightHand, 1);
	                }
	                if(!inDegree.containsKey(leftHand)) {
	                    inDegree.put(leftHand, 0);
	                }
	            }

	        }
	    }
	 private String getOper(String statement){
	        if(statement.contains("+=")){
	            return "+=";
	        }
	        else if(statement.contains("-=")){
	            return "-=";
	        }
	        else if(statement.contains("*=")){
	            return "*=";
	        }
	        else if(statement.contains("/=")){
	            return "/=";
	        }
	        else if(statement.contains("=")){
	            return "=";
	        }
	        return null;
	    }
	 private String getLeftHand(String statement, int index) {
	        String left = statement.substring(0, index).trim();
	        if (!left.contains(" ")) {
	            return left;
	        } else {
	            int index2 = left.lastIndexOf(" ");
	            return left.substring(index2 + 1, left.length());
	        }
	    }
	 private String getRightHand(String statement, int index) {
	        String right = statement.substring(index, statement.length()).trim();
	        if (right.contains(";")) {
	            right = right.replace(";", "");
	        }
	        return right;
	    }
/*	    private ArrayList<String> getIdentifierList(String source, int kind) {
	        ASTNode root = JDTUtils.createASTForSource(source, kind);
	        IdentifierCollectVisitor identifierCollectVisitor = new IdentifierCollectVisitor();
	        root.accept(identifierCollectVisitor);
	        return identifierCollectVisitor.getIdentifierList();
	    }*/


}