package softtest.deadlock.java.Alias;

import java.util.*;

import softtest.ast.java.ASTBlock;
import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.ASTMethodDeclarator;
import softtest.ast.java.ASTVariableDeclaratorId;
import softtest.ast.java.JavaParserVisitorAdapter;
import softtest.symboltable.java.VariableNameDeclaration;

public class TestFinder extends JavaParserVisitorAdapter{
	@Override
	public Object visit(ASTMethodDeclaration treenode, Object data) {
		ASTMethodDeclarator amr=(ASTMethodDeclarator)treenode.jjtGetChild(1);
		if(amr.getImage().equals("main")){
			List<ASTVariableDeclaratorId>list=treenode.findChildrenOfType(ASTVariableDeclaratorId.class);
			for(int i=0;i<list.size();i++){
				ASTVariableDeclaratorId aid=list.get(i);
				VariableNameDeclaration vnd=aid.getNameDeclaration();
				if(vnd.getAliasObject()!=null)
					System.out.println(vnd.getImage()+" "+vnd.getAliasObject().getName()+vnd.getAliasObject().getId());
			}
		}
		return null;
	}
}
