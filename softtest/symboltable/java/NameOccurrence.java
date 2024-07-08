/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */
package softtest.symboltable.java;

import softtest.ast.java.*;
// added by xqing
import java.util.*;

import softtest.ast.java.ASTAssignmentOperator;
import softtest.ast.java.ASTExpression;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTPostfixExpression;
import softtest.ast.java.ASTPreDecrementExpression;
import softtest.ast.java.ASTPreIncrementExpression;
import softtest.ast.java.ASTPrimaryExpression;
import softtest.ast.java.ASTPrimaryPrefix;
import softtest.ast.java.ASTStatementExpression;
import softtest.ast.java.Node;
import softtest.ast.java.SimpleNode;

public class NameOccurrence {

    private SimpleNode location;
    private String image;
    private NameOccurrence qualifiedName;

    private boolean isMethodOrConstructorInvocation;
    private int argumentCount;   

    public NameOccurrence(SimpleNode location, String image) {
        this.location = location;
        this.image = image;
        // added by xqing
        this.occurrenceType=OccurrenceType.USE;
    }

    public void setIsMethodOrConstructorInvocation() {
        isMethodOrConstructorInvocation = true;
    }

    public void setArgumentCount(int count) {
        argumentCount = count;
    }

    public int getArgumentCount() {
        return argumentCount;
    }

    public boolean isMethodOrConstructorInvocation() {
        return isMethodOrConstructorInvocation;
    }

    public void setNameWhichThisQualifies(NameOccurrence qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    public NameOccurrence getNameForWhichThisIsAQualifier() {
        return qualifiedName;
    }

    public boolean isPartOfQualifiedName() {
        return qualifiedName != null;
    }

    public SimpleNode getLocation() {
        return location;
    }

    public boolean isOnRightHandSide() {
        SimpleNode node = (SimpleNode) location.jjtGetParent().jjtGetParent().jjtGetParent();
        return node instanceof ASTExpression && node.jjtGetNumChildren() == 3;
    }


    public boolean isOnLeftHandSide() {
        // I detest this method with every atom of my being
        SimpleNode primaryExpression;
        if (location.jjtGetParent() instanceof ASTPrimaryExpression) {
            primaryExpression = (SimpleNode) location.jjtGetParent().jjtGetParent();
        } else if (location.jjtGetParent().jjtGetParent() instanceof ASTPrimaryExpression) {
            primaryExpression = (SimpleNode) location.jjtGetParent().jjtGetParent().jjtGetParent();
        } else {
            throw new RuntimeException("Found a NameOccurrence that didn't have an ASTPrimary Expression as parent or grandparent.  Parent = " + location.jjtGetParent() + " and grandparent = " + location.jjtGetParent().jjtGetParent());
        }

        if (isStandAlonePostfix(primaryExpression)) {
            return true;
        }

        if (primaryExpression.jjtGetNumChildren() <= 1) {
            return false;
        }

        if (!(primaryExpression.jjtGetChild(1) instanceof ASTAssignmentOperator)) {
            return false;
        }

        if (isPartOfQualifiedName() /* or is an array type */) {
            return false;
        }

        if (isCompoundAssignment(primaryExpression)) {
            return false;
        }

        return true;
    }

    private boolean isCompoundAssignment(SimpleNode primaryExpression) {
        return ((ASTAssignmentOperator) (primaryExpression.jjtGetChild(1))).isCompound();
    }

    private boolean isStandAlonePostfix(SimpleNode primaryExpression) {
        if (!(primaryExpression instanceof ASTPostfixExpression) || !(primaryExpression.jjtGetParent() instanceof ASTStatementExpression)) {
            return false;
        }

        ASTPrimaryPrefix pf = (ASTPrimaryPrefix) ((ASTPrimaryExpression) primaryExpression.jjtGetChild(0)).jjtGetChild(0);
        if (pf.usesThisModifier()) {
            return true;
        }

        return thirdChildHasDottedName(primaryExpression);
    }

    private boolean thirdChildHasDottedName(SimpleNode primaryExpression) {
        Node thirdChild = primaryExpression.jjtGetChild(0).jjtGetChild(0).jjtGetChild(0);
        return thirdChild instanceof ASTName && ((ASTName) thirdChild).getImage().indexOf('.') == -1;
    }

    public boolean isSelfAssignment() {
        Node l = location;
        while (true) {
            Node p = l.jjtGetParent();
            Node gp = p.jjtGetParent();
            Node node = gp.jjtGetParent();
            if (node instanceof ASTPreDecrementExpression || node instanceof ASTPreIncrementExpression || node instanceof ASTPostfixExpression) {
                return true;
            }
    
            if (node instanceof ASTStatementExpression) {
                ASTStatementExpression exp = (ASTStatementExpression) node;
                if (exp.jjtGetNumChildren() >= 2 && exp.jjtGetChild(1) instanceof ASTAssignmentOperator) {
                    ASTAssignmentOperator op = (ASTAssignmentOperator) exp.jjtGetChild(1);
                    if (op.isCompound()) {
                        return true;
                    }
                }
            }
    
            // deal with extra parenthesis: "(i)++"
            if (p instanceof ASTPrimaryPrefix && p.jjtGetNumChildren() == 1 &&
                    gp instanceof ASTPrimaryExpression && gp.jjtGetNumChildren() == 1&&
                    node instanceof ASTExpression && node.jjtGetNumChildren() == 1 &&
                    node.jjtGetParent() instanceof ASTPrimaryPrefix && node.jjtGetParent().jjtGetNumChildren() == 1) {
                l = node;
                continue;
            }
    
            return false;
        }
    }

    public boolean isThisOrSuper() {
        return image.equals("this") || image.equals("super");
    }

    @Override
	public boolean equals(Object o) {
        NameOccurrence n = (NameOccurrence) o;
        return n.getImage().equals(getImage());
    }

    @Override
	public int hashCode() {
        return getImage().hashCode();
    }

    public String getImage() {
        return image;
    }

    @Override
	public String toString() {
        return getImage() + ":" + location.getBeginLine() + ":" + location.getClass() + (this.isMethodOrConstructorInvocation() ? "(method call)" : "");
    }
    
    // added by xqing
    /** 是否为自增或自减出现 */
    public boolean isSelfIncOrDec(){
    	boolean b=false;
    	if(location.getSingleParentofType(ASTPreDecrementExpression.class)!=null
    	||location.getSingleParentofType(ASTPreIncrementExpression.class)!=null){
    		b=true;
    	}else{
    		ASTPostfixExpression post=(ASTPostfixExpression)location.getSingleParentofType(ASTPostfixExpression.class);
    		if(post!=null&&post.getImage()!=null){
    			String image=post.getImage();
    			if(image.equals("++")||image.equals("--")){
    				b=true;
    			}
    		}
    	}
    	return b;
    }
    
    /** 出现类型 */
    public enum OccurrenceType{
    	/** 定义 */
    	DEF,
    	/** 使用 */
    	USE
    }
    
    /** 出现类型,只针对变量的出现 */
    private OccurrenceType occurrenceType;
    
    /** 声明 */
    private NameDeclaration decl;
    
    /** 设置声明 */
    public void setDeclaration(NameDeclaration decl){
    	this.decl=decl;
    }
    
    /** 获得声明 */
    public NameDeclaration getDeclaration(){
    	return decl;
    }
    
    /** 设置出现类型 */ 
    public void setOccurrenceType(OccurrenceType occurrenceType){
    	this.occurrenceType=occurrenceType;
    }
    
    /** 获得出现类型 */
    public OccurrenceType getOccurrenceType(){
    	return this.occurrenceType;
    } 
    
    /** 使用-定义链，保存了可以到达本使用出现的所有定义出现，对于定义出现，该链表为null,链表中的元素都为定义出现 */
    private List<NameOccurrence> use_def = null;
    
    /** 设置使用-定义链 */
    public void setUseDefList(List<NameOccurrence> use_def){
    	this.use_def=use_def;
    }
    
    /** 获得使用-定义链 */
    public List<NameOccurrence> getUseDefList(){
    	return use_def;
    }
    
    /** 向使用-定义链添加定义，保证不重复添加 */
    public boolean addUseDef(NameOccurrence occ){
    	if(occ.getOccurrenceType()==OccurrenceType.DEF){
    		for(NameOccurrence o:use_def){
    			if(o.getLocation()==occ.getLocation()){
    				return false;
    			}
    		}
    		use_def.add(occ);
    		return true;
    	}
    	return false;
    }
    
    /** 定义-使用链，保存了本定义出现所有可以到达的使用出现，对于使用出现，该链表为null,链表中的元素都为使用出现 */
    private List<NameOccurrence> def_use = null;
    
    /** 设置定义-使用链 */
    public void setDefUseList(List<NameOccurrence> def_use){
    	this.def_use=def_use;
    }
    
    /** 获得定义-使用链 */
    public List<NameOccurrence> getDefUseList(){
    	return def_use;
    }
    
    /** 向定义-使用链添加定义，保证不重复添加 */
    public boolean addDefUse(NameOccurrence occ){
    	if(occ.getOccurrenceType()==OccurrenceType.USE){
    		for(NameOccurrence o:def_use){
    			if(o.getLocation()==occ.getLocation()){
    				return false;
    			}
    		}
    		def_use.add(occ);
    		return true;
    	}
    	return false;
    }
    
    /** 定义-取消定义链，保存了本定义出现所有可以到达的定义出现，对于使用出现，该链表为null,链表中的元素都为定义出现 */
    private List<NameOccurrence> def_undef = null;
    
    /** 设置定义-取消定义链 */
    public void setDefUndefList(List<NameOccurrence> def_undef){
    	this.def_undef=def_undef;
    }
    
    /** 获得定义-取消定义链 */
    public List<NameOccurrence> getDefUndefList(){
    	return def_undef;
    }
    
    /** 向定义-取消链添加定义，保证不重复添加 */
    public boolean addDefUndef(NameOccurrence occ){
    	if(occ.getOccurrenceType()==OccurrenceType.DEF){
    		for(NameOccurrence o:def_undef){
    			if(o.getLocation()==occ.getLocation()){
    				return false;
    			}
    		}
    		def_undef.add(occ);
    		return true;
    	}
    	return false;
    }
    
    /** 取消定义-定义链，保存了可以到达本定义出现的所有定义出现，对于使用出现，该链表为null,链表中的元素都为定义出现 */
    private List<NameOccurrence> undef_def = null;
    
    /** 设置取消定义-定义链 */
    public void setUndefDefList(List<NameOccurrence> undef_def){
    	this.undef_def=undef_def;
    }
    
    /** 获得取消定义-定义链 */
    public List<NameOccurrence> getUndefDefList(){
    	return undef_def;
    }
    
    /** 向定义-取消链添加定义，保证不重复添加 */
    public boolean addUndefDef(NameOccurrence occ){
    	if(occ.getOccurrenceType()==OccurrenceType.DEF){
    		for(NameOccurrence o:undef_def){
    			if(o.getLocation()==occ.getLocation()){
    				return false;
    			}
    		}
    		undef_def.add(occ);
    		return true;
    	}
    	return false;
    }
    
    /** 检查出现类型 */
    public OccurrenceType checkOccurrenceType(){
    	OccurrenceType type=OccurrenceType.USE;
    	if(isOnLeftHandSide()||isSelfAssignment()){
    		type=OccurrenceType.DEF;
    	}
    	if(type==OccurrenceType.USE){
    		if(use_def==null){
    			use_def=new LinkedList<NameOccurrence>();
    		}
    		def_use=null;
    		def_undef=null;
    		undef_def=null;
    	}else{
    		use_def=null;
    		if(def_use==null){
    			def_use=new LinkedList<NameOccurrence>();
    		}
    		if(def_undef==null){
    			def_undef=new LinkedList<NameOccurrence>();
    		}
    		if(undef_def==null){
    			undef_def=new LinkedList<NameOccurrence>();
    		}
    	}
    	return type;
    }
}
