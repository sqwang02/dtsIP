package softtest.symboltable.java;

import java.lang.reflect.*;
import java.util.*;

import softtest.ast.java.ASTAdditiveExpression;
import softtest.ast.java.ASTAllocationExpression;
import softtest.ast.java.ASTAndExpression;
import softtest.ast.java.ASTArguments;
import softtest.ast.java.ASTArrayDimsAndInits;
import softtest.ast.java.ASTBooleanLiteral;
import softtest.ast.java.ASTCastExpression;
import softtest.ast.java.ASTClassOrInterfaceBody;
import softtest.ast.java.ASTClassOrInterfaceDeclaration;
import softtest.ast.java.ASTClassOrInterfaceType;
import softtest.ast.java.ASTConditionalAndExpression;
import softtest.ast.java.ASTConditionalExpression;
import softtest.ast.java.ASTConditionalOrExpression;
import softtest.ast.java.ASTConstructorDeclaration;
import softtest.ast.java.ASTEqualityExpression;
import softtest.ast.java.ASTExclusiveOrExpression;
import softtest.ast.java.ASTExpression;
import softtest.ast.java.ASTInclusiveOrExpression;
import softtest.ast.java.ASTInstanceOfExpression;
import softtest.ast.java.ASTLiteral;
import softtest.ast.java.ASTMemberSelector;
import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.ASTMultiplicativeExpression;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTNullLiteral;
import softtest.ast.java.ASTPostfixExpression;
import softtest.ast.java.ASTPreDecrementExpression;
import softtest.ast.java.ASTPreIncrementExpression;
import softtest.ast.java.ASTPrimaryExpression;
import softtest.ast.java.ASTPrimaryPrefix;
import softtest.ast.java.ASTPrimarySuffix;
import softtest.ast.java.ASTPrimitiveType;
import softtest.ast.java.ASTReferenceType;
import softtest.ast.java.ASTRelationalExpression;
import softtest.ast.java.ASTResultType;
import softtest.ast.java.ASTShiftExpression;
import softtest.ast.java.ASTType;
import softtest.ast.java.ASTUnaryExpression;
import softtest.ast.java.ASTUnaryExpressionNotPlusMinus;
import softtest.ast.java.ExpressionBase;
import softtest.ast.java.JavaNode;
import softtest.ast.java.JavaParserVisitorAdapter;
import softtest.ast.java.Node;
import softtest.ast.java.SimpleJavaNode;

import softtest.ast.java.*;

public class ExpressionTypeFinder extends JavaParserVisitorAdapter {
	@Override
	public Object visit(ASTMethodDeclaration node, Object data) {
		Class[] parameterTypes=node.getParameterTypes();
		String classname = node.getScope().getEnclosingClassScope().getClassName();
		Class type = TypeSet.getCurrentTypeSet().findClassWithoutEx(classname);
		Method method = null;
		
		if (type != null) {
			method = getMethodOfClass(node.getMethodName(), parameterTypes, type);
			
			if (method != null) {
				node.setType(method);
			}
		}
						
		super.visit(node, data);
		
		return null;
	}
	
	@Override
	public Object visit(ASTConstructorDeclaration node, Object data) {
		Class[] parameterTypes=node.getParameterTypes();
		String classname = node.getScope().getEnclosingClassScope().getClassName();
		Class type = TypeSet.getCurrentTypeSet().findClassWithoutEx(classname);
		Constructor cons = null;
		
		if (type != null) {
			cons = getConstructorOfClass(parameterTypes, type);
			if (cons != null) {
				node.setType(cons);
			}
		}
		
		super.visit(node, data);
		
		return null;
	}
	
	@Override
	public Object visit(ASTClassOrInterfaceDeclaration node, Object data) {
		TypeSet.pushClassName(node.getImage());
		super.visit(node, data);
		TypeSet.popClassName();		
		return null;
	}
	
	@Override
	public Object visit(ASTClassOrInterfaceBody node, Object data) {
        if (node.jjtGetParent() instanceof ASTAllocationExpression ){//|| node.jjtGetParent() instanceof ASTEnumConstant) {
        	TypeSet.pushClassName(node.getScope().getEnclosingClassScope().getClassName());
        	super.visit(node, data);
            TypeSet.popClassName();
        } else {
            super.visit(node, data);
        }
        return data;
    }
	
	@Override
	public Object visit(ASTAdditiveExpression node, Object data) {
		//加法表达式，通过比较两个操作数的类型级别，设定加法表达式最终类型
		ArrayList list=new ArrayList();
		super.visit(node, data);
		Class current=null;
		for(int i=0;i<node.jjtGetNumChildren();i++){
			ExpressionBase e=(ExpressionBase)node.jjtGetChild(i);
			if(e.getType() instanceof Class){
				Class c=(Class)e.getType();
				if(current!=null){
					current=checkPriority(current,c); 
				}else{
					current=c;
				}
			}
		}
		node.setType(current);
		return null;
	}

	@Override
	public Object visit(ASTAllocationExpression node, Object data) {
		//对象分配表达式
		super.visit(node, data);
		String typeimage=null;
		int arrayDepth=0;
		if(node.jjtGetChild(0) instanceof ASTPrimitiveType){
			//简单类型
			ASTPrimitiveType pritype=(ASTPrimitiveType)node.jjtGetChild(0);
			typeimage=pritype.getImage();
			if(node.jjtGetNumChildren()==2&&node.jjtGetChild(1) instanceof ASTArrayDimsAndInits){
				//数组情况
				ASTArrayDimsAndInits arrayandinits=(ASTArrayDimsAndInits)node.jjtGetChild(1);
				arrayDepth=arrayandinits.getArrayDepth();
			}
		}else{
			//非简单类型
			ASTClassOrInterfaceType coritype=(ASTClassOrInterfaceType)node.jjtGetChild(0);
			typeimage=coritype.getImage();
			if(node.jjtGetChild(node.jjtGetNumChildren()-1) instanceof ASTArrayDimsAndInits){
				//数组情况
				ASTArrayDimsAndInits arrayandinits=(ASTArrayDimsAndInits)node.jjtGetChild(node.jjtGetNumChildren()-1);
				arrayDepth=arrayandinits.getArrayDepth();
			} else if(node.jjtGetChild(node.jjtGetNumChildren()-1) instanceof ASTClassOrInterfaceBody){
				//匿名类，匿名类名称由；文件名+"$"+匿名类计数
				String currentfilename=softtest.fsmanalysis.java.ProjectAnalysis.getCurrent_file();
				//added by yang 2011-06-28 10:49  在对currentfilename操作之前增加了判空条件
				if(currentfilename!=null){
				  int pos=currentfilename.lastIndexOf('\\');
				  if(pos==-1){
					pos=currentfilename.lastIndexOf('/');
				  }
				  currentfilename=currentfilename.substring(pos+1);
				  typeimage=currentfilename.substring(0, currentfilename.indexOf('.'))+"$"+node.getAnonymousNum();}
			} else if(node.jjtGetNumChildren()>1 && node.jjtGetChild(1) instanceof ASTArguments){
				ASTArguments arg=(ASTArguments)node.jjtGetChild(1);
				super.visit(arg, data);
				Class [] types = ((ASTArguments)node.jjtGetChild(1)).getParameterTypes();
				try {
					Class cls = TypeSet.getCurrentTypeSet().findClass(typeimage);
					Constructor cons = getConstructorOfClass(types,cls);
					node.setType(cons);
				} catch (ClassNotFoundException e) {
					node.setType(null);
				} 
				return null;
			}
		}
		Class type=findClassWithArray(typeimage,arrayDepth);
		node.setType(type);
		return null;
	}

	@Override
	public Object visit(ASTAndExpression node, Object data) {
		super.visit(node, data);
		ExpressionBase e=(ExpressionBase)node.jjtGetChild(0);
		if(e.getType()==boolean.class||e.getType()==Boolean.class){
			node.setType(boolean.class);
		}else{
			node.setType(int.class);
		}
		return null;
	}

	@Override
	public Object visit(ASTArguments node, Object data) {
		//在ASTPrimaryExpression中已经处理过了
		super.visit(node, data);
		return null;
	}

	@Override
	public Object visit(ASTBooleanLiteral node, Object data) {
		node.setType(boolean.class);
		return null;
	}

	@Override
	public Object visit(ASTCastExpression node, Object data) {
		super.visit(node, data);
		Class type=null;
		String typeimage=null;
		int arrayDepth=0;
		if(node.jjtGetNumChildren()==2&&node.jjtGetChild(0)instanceof ASTType){
			ASTType t=(ASTType)node.jjtGetChild(0);
			if(t.jjtGetChild(0) instanceof ASTPrimitiveType){
				ASTPrimitiveType pri=(ASTPrimitiveType)t.jjtGetChild(0);
				typeimage=pri.getImage();
			}else{
				ASTReferenceType ref=(ASTReferenceType)t.jjtGetChild(0);
				arrayDepth=ref.getArrayDepth();
				typeimage=((SimpleJavaNode)(ref.jjtGetChild(0))).getImage();
			}
			type=findClassWithArray(typeimage,arrayDepth);
			node.setType(type);
		}
		return null;
	}

	@Override
	public Object visit(ASTConditionalAndExpression node, Object data) {
		super.visit(node, data);
		node.setType(boolean.class);
		return null;
	}

	@Override
	public Object visit(ASTConditionalExpression node, Object data) {
		super.visit(node, data);
		ExpressionBase e=(ExpressionBase)node.jjtGetChild(1);
		node.setType(e.getType());
		return null;
	}

	@Override
	public Object visit(ASTConditionalOrExpression node, Object data) {
		super.visit(node, data);
		node.setType(boolean.class);
		return null;
	}

	@Override
	public Object visit(ASTEqualityExpression node, Object data) {
		super.visit(node, data);
		node.setType(boolean.class);
		return null;
	}

	@Override
	public Object visit(ASTExclusiveOrExpression node, Object data) {
		super.visit(node, data);
		ExpressionBase e=(ExpressionBase)node.jjtGetChild(0);
		if(e.getType()==boolean.class||e.getType()==Boolean.class){
			node.setType(boolean.class);
		}else{
			node.setType(int.class);
		}
		return null;
	}

	@Override
	public Object visit(ASTExpression node, Object data) {
		super.visit(node, data);
		ExpressionBase e=(ExpressionBase)node.jjtGetChild(0);
		node.setType(e.getType());
		return null;
	}

	@Override
	public Object visit(ASTInclusiveOrExpression node, Object data) {
		super.visit(node, data);
		ExpressionBase e=(ExpressionBase)node.jjtGetChild(0);
		if(e.getType()==boolean.class||e.getType()==Boolean.class){
			node.setType(boolean.class);
		}else{
			node.setType(int.class);
		}
		return null;
	}

	@Override
	public Object visit(ASTInstanceOfExpression node, Object data) {
		super.visit(node, data);
		node.setType(boolean.class);
		return null;
	}

	@Override
	public Object visit(ASTLiteral node, Object data) {
		super.visit(node, data);
		if (node.jjtGetNumChildren() > 0) {
			// null 和布尔常量的情况
			ExpressionBase e=(ExpressionBase)node.jjtGetChild(0);
			node.setType(e.getType());
			return null;
		}
		String image=node.getImage();
		if (image.startsWith("\"")) {
			node.setType(String.class);
		} else if (image.startsWith("\'")) {
			node.setType(char.class);
		} else {
			boolean isInteger = false;
			if (image.endsWith("l") || image.endsWith("L")) {
				image = image.substring(0, image.length() - 1);
			}
			char[] source = image.toCharArray();
			int length = source.length;
			int intValue = 0;
			long computeValue = 0L;
			double doubleValue = 0;
			try {
				if (source[0] == '0') {
					if (length == 1) {
						computeValue = 0;
					} else {
						final int shift, radix;
						int j;
						if ((source[1] == 'x') || (source[1] == 'X')) {
							shift = 4;
							j = 2;
							radix = 16;
						} else {
							shift = 3;
							j = 1;
							radix = 8;
						}
						while (source[j] == '0') {
							j++; // jump over redondant zero
							if (j == length) { // watch for 000000000000000000
								computeValue = 0;
								break;
							}
						}
						while (j < length) {
							int digitValue = 0;
							if (radix == 8) {
								if ('0' <= source[j] && source[j] <= '7') {
									digitValue = source[j++] - '0';
								} else {
									throw new RuntimeException("This is not a legal integer");
								}
							} else {
								if ('0' <= source[j] && source[j] <= '9') {
									digitValue = source[j++] - '0';
								} else if ('a' <= source[j] && source[j] <= 'f') {
									digitValue = source[j++] - 'a' + 10;
								} else if ('A' <= source[j] && source[j] <= 'F') {
									digitValue = source[j++] - 'A' + 10;
								} else {
									throw new RuntimeException("This is not a legal integer");
								}
							}
							computeValue = (computeValue << shift) | digitValue;

						}
					}
				} else { // -----------regular case : radix = 10-----------
					for (int i = 0; i < length; i++) {
						int digitValue;
						if ('0' <= source[i] && source[i] <= '9') {
							digitValue = source[i] - '0';
						} else {
							throw new RuntimeException("This is not a legal integer");
						}
						computeValue = 10 * computeValue + digitValue;
					}
				}
				intValue = (int) computeValue;
				isInteger = true;
			} catch (RuntimeException e) {
			}
			if (isInteger) {
				node.setType(int.class);
			} else {
				node.setType(float.class);
			}
		}
		return null;
	}

	@Override
	public Object visit(ASTMultiplicativeExpression node, Object data) {
		//乘法表达式，通过比较两个操作数的类型级别，设定加法表达式最终类型
		ArrayList list=new ArrayList();
		super.visit(node, data);
		Class current=null;
		for(int i=0;i<node.jjtGetNumChildren();i++){
			ExpressionBase e=(ExpressionBase)node.jjtGetChild(i);
			if(e.getType() instanceof Class){
				Class c=(Class)e.getType();
				if(current!=null){
					current=checkPriority(current,c); 
				}else{
					current=c;
				}
			}
		}
		node.setType(current);
		return null;
	}

	@Override
	public Object visit(ASTName node, Object data) {
		String nameimage = node.getImage();
		if(nameimage==null||!(node.jjtGetParent() instanceof ASTPrimaryPrefix)){
			return null;
		}
		Class type=null;
		TypeSet typeset=TypeSet.getCurrentTypeSet();
		String names[]=nameimage.split("\\.");
		Search t = new Search(names[0]);
		t.execute(node.getScope());
		NameDeclaration decl=t.getResult();
		if (decl == null) {
			try {
				Class cla = typeset.findClass(node.getScope().getEnclosingClassScope().getClassName());
				type = getFieldTypeOfClass(names[0], cla);
				if (type != null) {
					node.setType(type);
					for (int i = 1; i < names.length - 1; i++) {
						if (type != null) {
							String name = names[i];
							type = getFieldTypeOfClass(name, type);
							node.setType(type);
						}
					}
					if (names.length > 1 && type != null) {
						if (node.isMethodName()) {
							Method method = null;
							String m = names[names.length - 1];
							Class[] parameterTypes = node.getParameterTypes();

							method = getMethodOfClass(m, parameterTypes, type);
							node.setType(method);
						} else {
							String name = names[names.length - 1];
							type = getFieldTypeOfClass(name, type);
							node.setType(type);
						}
					}
					return null;
				}
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			} catch (NullPointerException e) {
				System.out.println(node.getBeginLine()+":"+node.getBeginColumn());
				throw e;
			}
		}
		if(decl instanceof ClassNameDeclaration){
			//同文件中定义的类名
			type = typeset.findClassWithoutEx(decl.getImage());
			node.setType(type);
			
			for(int i=1;i<names.length-1;i++){
				if(type!=null){
					String name=names[i];
					type=getFieldTypeOfClass(name, type);
					node.setType(type);
				}
			}
			if(names.length>1&&type!=null){
				if(node.isMethodName()){
					Method method=null;
					String m=names[names.length-1];
					Class[] parameterTypes=node.getParameterTypes();
					
					method=getMethodOfClass(m, parameterTypes, type);
					node.setType(method);
				}else{
					String name=names[names.length-1];
					type=getFieldTypeOfClass(name, type);
					node.setType(type);
				}
			}
		}else if(decl instanceof VariableNameDeclaration){
			//同类中定义的变量名
			VariableNameDeclaration v=(VariableNameDeclaration)(decl);
			type=v.getType();
			node.setType(type);
			
			for(int i=1;i<names.length-1;i++){
				if(type!=null){
					String name=names[i];
					type=getFieldTypeOfClass(name, type);
					node.setType(type);
				}
			}
			if(names.length>1&&type!=null){
				if(node.isMethodName()){
					Method method=null;
					String m=names[names.length-1];
					Class[] parameterTypes=node.getParameterTypes();
					
					method=getMethodOfClass(m, parameterTypes, type);
					node.setType(method);
				}else{
					String name=names[names.length-1];
					type=getFieldTypeOfClass(name, type);
					node.setType(type);
				}
			}
		}else if(decl instanceof MethodNameDeclaration){
			//同类中定义的方法名
			MethodNameDeclaration m=(MethodNameDeclaration)decl;
			Class[] parameterTypes=node.getParameterTypes();
				
			String classname=m.getScope().getEnclosingClassScope().getClassName();
			type = typeset.findClassWithoutEx(classname);

			Method method=null;
			method=getMethodOfClass(m.getImage(), parameterTypes, type);
			node.setType(method);
		}else{
			//可能是一个外部的包，或者类名,或者静态引入的变量或对象
			int i=0;
			String name=null;
			for (i = 0; i < names.length; i++) {
				if (i == 0) {
					name = names[i];
				} else {
					name = name + "." + names[i];
				}
				type = typeset.findClassWithoutEx(name);
				if(type!=null){
					node.setType(type);
					break;
				}else{
					//考虑如下情况：
					//import static softtest.fsmanalysis.java.TestAnalysis.CURRENT_FILE
					type = typeset.findStaticImportClassWithoutEx(name);
					if(type!=null){
						node.setType(type);
						i--;
						break;
					}
				}		
			}
			
			for(i=i+1;i<names.length-1;i++){
				if(type!=null){
					name=names[i];
					type=getFieldTypeOfClass(name, type);
					node.setType(type);
				}
			}
			
			if(type!=null&&i==names.length-1){
				if(node.isMethodName()){
					Method method=null;
					String m=names[names.length-1];
					Class[] parameterTypes=node.getParameterTypes();
					method=getMethodOfClass(m, parameterTypes, type);
					node.setType(method);
				}else{
					name=names[names.length-1];
					type=getFieldTypeOfClass(name, type);
					node.setType(type);
				}
			}
		}
		return null;
	}

	@Override
	public Object visit(ASTNullLiteral node, Object data) {
		//null的类型设为空
		//node.setType(Object.class);
		return null;
	}

	@Override
	public Object visit(ASTPostfixExpression node, Object data) {
		super.visit(node, data);
		ExpressionBase e=(ExpressionBase)node.jjtGetChild(0);
		node.setType(e.getType());
		return null;
	}

	@Override
	public Object visit(ASTPreDecrementExpression node, Object data) {
		super.visit(node, data);
		ExpressionBase e=(ExpressionBase)node.jjtGetChild(0);
		node.setType(e.getType());
		return null;
	}

	@Override
	public Object visit(ASTPreIncrementExpression node, Object data) {
		super.visit(node, data);
		ExpressionBase e=(ExpressionBase)node.jjtGetChild(0);
		node.setType(e.getType());
		return null;
	}

	@Override
	public Object visit(ASTPrimaryExpression node, Object data) {
		//先处理所有的包含Arguments的PrimarySuffix
		ArrayList<Node> list1=new ArrayList<Node>();
		for(int i=0;i<node.jjtGetNumChildren();i++){
			if(node.jjtGetChild(i) instanceof ASTPrimarySuffix){
				ASTPrimarySuffix suffix=(ASTPrimarySuffix)node.jjtGetChild(i);
				if(suffix.jjtGetNumChildren()>0&&suffix.jjtGetChild(0) instanceof ASTArguments){
					list1.add(suffix.jjtGetChild(0));
					continue;
				}
			}
		}
		for(Node n:list1){
			((JavaNode) n).jjtAccept(this, data);
		}

		for(int i=0;i<node.jjtGetNumChildren();i++){
			if(node.jjtGetChild(i) instanceof ASTPrimarySuffix){
				ASTPrimarySuffix suffix=(ASTPrimarySuffix)node.jjtGetChild(i);
				if(suffix.jjtGetNumChildren()>0&&suffix.jjtGetChild(0) instanceof ASTArguments){
					ASTPrimaryExpression primary=(ASTPrimaryExpression)suffix.jjtGetParent();
					int find=-1;
			    	for(int j=0;i<primary.jjtGetNumChildren();j++){
			    		if(primary.jjtGetChild(j)==suffix){
			    			find=j;
			    			break;
			    		}
			    	}
			    	ExpressionBase eb = (ExpressionBase)primary.jjtGetChild(find-1);
			    	if (eb.getType() instanceof Method) {
			    		Method m=(Method)eb.getType();
			    		((ASTArguments)suffix.jjtGetChild(0)).setType(m.getReturnType());
			    		suffix.setType(m.getReturnType());
			    	}
				}else{
					((JavaNode) node.jjtGetChild(i)).jjtAccept(this, data);
				}
			}else{
				((JavaNode) node.jjtGetChild(i)).jjtAccept(this, data);
			}
		}
		
		ExpressionBase e=(ExpressionBase)node.jjtGetChild(node.jjtGetNumChildren()-1);
		node.setType(e.getType());
		return null;
	}

	@Override
	public Object visit(ASTPrimaryPrefix node, Object data) {
		  /*Literal()
		  | "this" {jjtThis.setUsesThisModifier();}
		  | "super" {jjtThis.setUsesSuperModifier();} "." t=<IDENTIFIER> {jjtThis.setImage(t.image);}
		  | "(" Expression() ")"
		  | AllocationExpression()
		  | LOOKAHEAD( ResultType() "." "class" ) ResultType() "." "class"
		  | Name()
		  */		
		super.visit(node, data);
		if(node.usesThisModifier()){
			String typeimage=node.getScope().getEnclosingClassScope().getClassName();
			node.setType(findClassWithArray(typeimage, 0));
		}else if(node.usesSuperModifier()){
			String typeimage=node.getScope().getEnclosingClassScope().getClassName();
			Class type=findClassWithArray(typeimage, 0);
			if(type!=null){
				Class s=type.getSuperclass();
				if(s!=null){
					if(node.isMethodName()){
						Method method=null;
						String m=node.getImage();
						Class[] parameterTypes=node.getParameterTypes();
						method=getMethodOfClass(m, parameterTypes, s);
						node.setType(method);
					}else{
						String name=node.getImage();
						type=getFieldTypeOfClass(name, s);
						node.setType(type);
					}
				}
			}
		}else if(node.jjtGetChild(0) instanceof ASTResultType){
			node.setType(Class.class);
		}else if(node.jjtGetChild(0) instanceof ASTAllocationExpression) {
			ASTAllocationExpression ae = (ASTAllocationExpression) node.jjtGetChild(0);
			if (ae.getType() instanceof Constructor) {
				node.setType(((Constructor)ae.getType()).getDeclaringClass());
			} else {
				ExpressionBase e=(ExpressionBase)node.jjtGetChild(0);
				node.setType(e.getType());
			}
		}else{
			ExpressionBase e=(ExpressionBase)node.jjtGetChild(0);
			node.setType(e.getType());
		}
		return null;
	}

	@Override
	public Object visit(ASTPrimarySuffix node, Object data) {
		/*	{ LOOKAHEAD(2) "." "this"
			| LOOKAHEAD(2) "." "super"
			| LOOKAHEAD(2) "." AllocationExpression()
			| LOOKAHEAD(3) MemberSelector()
			| "[" Expression() "]" {jjtThis.setIsArrayDereference();}
			| "." t=<IDENTIFIER> {jjtThis.setImage(t.image);}
			| Arguments() {jjtThis.setIsArguments();}*/
		super.visit(node, data);
		if(node.usesThisModifier()){
			String typeimage=node.getScope().getEnclosingClassScope().getClassName();
			node.setType(findClassWithArray(typeimage, 0));
		}else if(node.usesSuperModifier()){
			String typeimage=node.getScope().getEnclosingClassScope().getClassName();
			Class type=findClassWithArray(typeimage, 0);
			if(type!=null){
				node.setType(type.getSuperclass());
			}
		}else if(node.jjtGetNumChildren() > 0 && node.jjtGetChild(0) instanceof ASTMemberSelector){
			ASTMemberSelector ms=(ASTMemberSelector)node.jjtGetChild(0);
			Class type=null;
			ExpressionBase e=node.getLastExpression();
			if(e!=null&&e.getType() instanceof Class){
				type = (Class) e.getType();
			}
			if (type != null) {
				if (node.isMethodName()) {
					Method method = null;
					String m = ms.getImage();
					Class[] parameterTypes = node.getParameterTypes();
					method=getMethodOfClass(m, parameterTypes, type);
					node.setType(method);
				} else {
					String name = ms.getImage();
					type=getFieldTypeOfClass(name, type);
					node.setType(type);
				}
			}
		}else if(node.getImage()!=null){
			Class type=null;
			ExpressionBase e=node.getLastExpression();
			if(e!=null&&e.getType() instanceof Class){
				type = (Class) e.getType();
			}
			if (type != null) {
				if (node.isMethodName()) {
					Method method = null;
					String m = node.getImage();
					Class[] parameterTypes = node.getParameterTypes();
					method=getMethodOfClass(m, parameterTypes, type);
					node.setType(method);
				} else {
					String name = node.getImage();
					type=getFieldTypeOfClass(name, type);
					node.setType(type);
				}
			}
		}else if(node.isArrayDereference()){
			ExpressionBase e=node.getLastExpression();
			Class type=null;
			if(e!=null&&e.getType() instanceof Class){
				type = (Class) e.getType();
			}
			if(type!=null){
				type=getArrayElementType(type);
			}
			node.setType(type);
		}
		else{
			ExpressionBase e=(ExpressionBase)node.jjtGetChild(0);
			node.setType(e.getType());
		}
		
		return null;
	}

	@Override
	public Object visit(ASTRelationalExpression node, Object data) {
		super.visit(node, data);
		node.setType(boolean.class);
		return null;
	}

	@Override
	public Object visit(ASTShiftExpression node, Object data) {
		super.visit(node, data);
		node.setType(int.class);
		return null;
	}

	@Override
	public Object visit(ASTUnaryExpression node, Object data) {
		super.visit(node, data);
		ExpressionBase e=(ExpressionBase)node.jjtGetChild(0);
		node.setType(e.getType());
		return null;
	}

	@Override
	public Object visit(ASTUnaryExpressionNotPlusMinus node, Object data) {
		super.visit(node, data);
		ExpressionBase e=(ExpressionBase)node.jjtGetChild(0);
		node.setType(e.getType());
		return null;
	}

	private int getPriorityLevelFromClass(Class c){
		int ret =7;
		
		if(c==char.class||c==Character.class){
			ret =0;
		}else if(c==byte.class||c==Byte.class){
			ret =1;
		}else if(c==short.class||c==Short.class){
			ret =2;
		}else if(c==int.class||c==Integer.class){
			ret =3;
		}else if(c==long.class||c==Long.class){
			ret =4;
		}else if(c==float.class||c==Float.class){
			ret =5;
		}else if(c==double.class||c==Double.class){
			ret =6;
		}else if(c==String.class){
			ret =8;
		}else{
			ret=7;
		}
		return ret;
	}
	
	private Class getClassFromPriorityLevel(int p){
		Class ret=Object.class;
		switch(p){
		case 0:
			ret=char.class;
			break;
		case 1:
			ret=byte.class;
			break;
		case 2:
			ret=short.class;
			break;
		case 3:
			ret=int.class;
			break;
		case 4:
			ret=long.class;
			break;
		case 5:
			ret=float.class;
			break;
		case 6:
			ret=double.class;
			break;
		case 7:	
			ret=Object.class;
			break;
		case 8:
			ret=String.class;
			break;
		default:
			ret=Object.class;
			break;		
		}
		return ret;
	}
	
	private Class checkPriority(Class c1,Class c2){
		Class ret=c1;
		int i1=getPriorityLevelFromClass(c1);
		int i2=getPriorityLevelFromClass(c2);
		ret=getClassFromPriorityLevel(Math.max(i1, i2));
		return ret;
	}
	
	private Class getArrayElementType(Class type){
		Class ret=null;
		if(!type.isArray()){
			return null;
		}
		String typeimage=type.getName();
		typeimage=typeimage.substring(1);
		if(typeimage.charAt(0)=='['){
		}else if(typeimage.charAt(typeimage.length()-1)==';'){
			typeimage=typeimage.substring(1,typeimage.length()-1);
		}else if(typeimage.charAt(0)=='Z'){
			typeimage="boolean";
		}else if(typeimage.charAt(0)=='B'){
			typeimage="byte";
		}else if(typeimage.charAt(0)=='C'){
			typeimage="char";
		}else if(typeimage.charAt(0)=='D'){
			typeimage="double";
		}else if(typeimage.charAt(0)=='F'){
			typeimage="float";
		}else if(typeimage.charAt(0)=='I'){
			typeimage="int";
		}else if(typeimage.charAt(0)=='J'){
			typeimage="long";
		}else if(typeimage.charAt(0)=='S'){
			typeimage = "short";
		}else{
			throw new RuntimeException("this is a type error!");
		}
		TypeSet typeset = TypeSet.getCurrentTypeSet();
		ret = typeset.findClassWithoutEx(typeimage);

		return ret;
	}
	
	private Class findClassWithArray(String typeimage,int arrayDepth){
		Class type=null;
		
		/*java 类型命名规则
		 * Element Type  Encoding  
		 * boolean  Z  
		 * byte  B  
		 * char  C  
		 * class or interface  Lclassname;  
		 * double  D  
		 * float  F  
		 * int  I  
		 * long  J  
		 * short  S  
		 * 
		 * The class or interface name classname is the binary name of the class specified above. 
		 * 
		 * Examples: 
		 * String.class.getName()
		 * 		returns "java.lang.String"
		 * byte.class.getName()
		 * 		returns "byte"
		 * (new Object[3]).getClass().getName()
		 * 		returns "[Ljava.lang.Object;"
		 * (new int[3][4][5][6][7][8][9]).getClass().getName()
		 * 		returns "[[[[[[[I"
		 */
		TypeSet typeset = TypeSet.getCurrentTypeSet();
		try {
			StringBuffer sb = new StringBuffer("");
			if (arrayDepth > 0) {
				for (int index = 0; index < arrayDepth; index++) {
					sb.append("[");
				}
				if (typeimage.equals("boolean")) {
					sb.append("Z");
				} else if (typeimage.equals("byte")) {
					sb.append("B");
				} else if (typeimage.equals("char")) {
					sb.append("C");
				} else if (typeimage.equals("double")) {
					sb.append("D");
				} else if (typeimage.equals("float")) {
					sb.append("F");
				} else if (typeimage.equals("int")) {
					sb.append("I");
				} else if (typeimage.equals("long")) {
					sb.append("J");
				} else if (typeimage.equals("short")) {
					sb.append("S");
				} else {
					Class t = typeset.findClass(typeimage);
					sb.append("L" + t.getName() + ";");
				}
				typeimage = sb.toString();
			}
			type = typeset.findClass(typeimage);
		} catch (ClassNotFoundException e) {
		}catch(NoClassDefFoundError e){
		}
		return type;
	}
	
	private Class getFieldTypeOfClass(String name,Class type){
		Class ret=null;
		//循环查找父类成员
		while(type!=null&&ret==null){
			try{
				Field field=type.getDeclaredField(name);
				ret=field.getType();
			}catch(NoSuchFieldException e){
			}catch(NoClassDefFoundError e){
			}
			type=type.getSuperclass();
		}
		return ret;
	}
	
	public static Constructor getConstructorOfClass(Class []parameterTypes,Class type){
		Constructor ret =null;
		try{
			ret = type.getDeclaredConstructor(parameterTypes);
		} catch (SecurityException e) {
			// e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// e.printStackTrace();
		}catch(NoClassDefFoundError e){
		}
		
		if(ret==null){
			//没找到，尝试参数模糊匹配
			
			ArrayList<Constructor> same_n=new ArrayList<Constructor>();
			ArrayList<Constructor> same_n_p=new ArrayList<Constructor>();
			int pramnum=parameterTypes==null?0:parameterTypes.length;
			
			try{
				Constructor[] ms= type.getDeclaredConstructors();
				for(Constructor m:ms){
					same_n.add(m);
					if(m.getParameterTypes().length==pramnum){
						same_n_p.add(m);
					}
				}
			}
			catch(NoClassDefFoundError e){
			}
			for(Constructor m:same_n_p){
				Class[] cs=m.getParameterTypes();
				boolean match=true;
				for(int i=0;i<pramnum;i++){
					Class c1=parameterTypes[i];
					Class c2=cs[i];
					if(c1!=null){
						if(!isSelfOrSuperClass(c1,c2)){
							match=false;
							break;
						}
					}else{
						if(c2.isPrimitive()){
							match=false;
							break;
						}
					}
				}
				if(match){
					ret=m;
					break;
				}
			}
			//还没找到，随便取一个名字一样的
			if(ret==null){
				if(same_n_p.size()>0){
					ret=same_n_p.get(0);
				}else if(same_n.size()>0){
					ret=same_n.get(0);
				}
			}
		}
		
		return ret;
	}
	
	public static Method getMethodOfClass(String name,Class []parameterTypes,Class type){
		Method ret = null;
		Class temp=type;
		while (type != null && ret == null) {
			try {
				ret = type.getDeclaredMethod(name,parameterTypes);
			} catch (NoSuchMethodException e) {
			}catch (NoClassDefFoundError e) {
			}
			type=type.getSuperclass();
		}
		if(ret==null){
			//没找到，尝试参数模糊匹配
			type=temp;
			ArrayList<Method> same_n=new ArrayList<Method>();
			ArrayList<Method> same_n_p=new ArrayList<Method>();
			int pramnum=parameterTypes==null?0:parameterTypes.length;
			while (type != null ) {
				try{
					Method[] ms= type.getDeclaredMethods();
					for(Method m:ms){
						if(m.getName().equals(name)){
							same_n.add(m);
							if(m.getParameterTypes().length==pramnum){
								same_n_p.add(m);
							}
						}
					}
				}catch (NoClassDefFoundError e) {
				}
				type=type.getSuperclass();
			}
			for(Method m:same_n_p){
				Class[] cs=m.getParameterTypes();
				boolean match=true;
				for(int i=0;i<pramnum;i++){
					Class c1=parameterTypes[i];
					Class c2=cs[i];
					if(c1!=null){
						if(!isSelfOrSuperClass(c1,c2)){
							match=false;
							break;
						}
					}else{
						if(c2.isPrimitive()){
							match=false;
							break;
						}
					}
				}
				if(match){
					ret=m;
					break;
				}
			}
			//还没找到，随便取一个名字一样的
			if(ret==null){
				if(same_n_p.size()>0){
					ret=same_n_p.get(0);
				}else if(same_n.size()>0){
					ret=same_n.get(0);
				}
			}
		}
		
		return ret;
	}
	
	public static boolean isSelfOrSuperClass(Class child,Class c){
		boolean ret=false;
		while(!ret&&child!=null){
			if(child==c){
				ret=true;
			}
			child=child.getSuperclass();
		}
		return ret;
	}
}
