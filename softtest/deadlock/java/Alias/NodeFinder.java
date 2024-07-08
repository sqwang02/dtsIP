package softtest.deadlock.java.Alias;

import java.util.List;

import softtest.ast.java.ASTAllocationExpression;
import softtest.ast.java.ASTAssignmentOperator;
import softtest.ast.java.ASTClassOrInterfaceDeclaration;
import softtest.ast.java.ASTClassOrInterfaceType;
import softtest.ast.java.ASTExpression;
import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.ASTMethodDeclarator;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTPrimaryExpression;
import softtest.ast.java.ASTPrimaryPrefix;
import softtest.ast.java.ASTPrimarySuffix;
import softtest.ast.java.ASTPrimitiveType;
import softtest.ast.java.ASTResultType;
import softtest.ast.java.ASTReturnStatement;
import softtest.ast.java.ASTStatementExpression;
import softtest.ast.java.ASTVariableDeclarator;
import softtest.ast.java.ASTVariableDeclaratorId;
import softtest.ast.java.ASTVariableInitializer;
import softtest.ast.java.JavaParserVisitorAdapter;
import softtest.ast.java.Node;
import softtest.symboltable.java.MethodNameDeclaration;
import softtest.symboltable.java.NameDeclaration;
import softtest.symboltable.java.VariableNameDeclaration;

public class NodeFinder extends JavaParserVisitorAdapter{
	
	/** 对象id，用于区别对象 */
	private int id=0;
	
	@Override
	public Object visit(ASTVariableDeclarator treenode, Object data) {
		if(treenode instanceof ASTVariableDeclarator){
			if(treenode.jjtGetNumChildren()>=2){
				ASTVariableDeclaratorId aid=(ASTVariableDeclaratorId)treenode.jjtGetChild(0);
				VariableNameDeclaration vnd=aid.getNameDeclaration();
				ASTVariableInitializer avi=(ASTVariableInitializer)treenode.jjtGetChild(1);
				if(avi.getFirstChildOfType(ASTPrimaryExpression.class)!=null){
					ASTPrimaryExpression ape=(ASTPrimaryExpression)avi.getFirstChildOfType(ASTPrimaryExpression.class);
//					this
//					PrimaryExpression的子节点：prefix和suffix
					if(ape.jjtGetNumChildren()!=1){
//						暂时不考虑this.f.f.f.m();
						if(ape.jjtGetNumChildren()==2){
//							= method();
							ASTPrimaryPrefix app=(ASTPrimaryPrefix)ape.jjtGetChild(0);
							if(app.jjtGetNumChildren()!=0&&app.jjtGetChild(0) instanceof ASTName){
								ASTName name=(ASTName)app.jjtGetChild(0);
								if(name.isMethodName()){
									List<NameDeclaration> list=name.getNameDeclarationList();
									if(!list.isEmpty()){
										NameDeclaration nameDec=list.get(list.size()-1);
										if(nameDec instanceof MethodNameDeclaration){
											MethodNameDeclaration mnd=(MethodNameDeclaration)nameDec;
											ASTMethodDeclarator amr=(ASTMethodDeclarator)mnd.getNode();
											ASTMethodDeclaration amd=(ASTMethodDeclaration)amr.jjtGetParent();
											AliasObject ao=getAliasObject(amd);
											if(ao!=null)
												vnd.setAliasObject(ao);
										}
									}
								}
							}
						}
						else{
							if(ape.jjtGetNumChildren()>2){
//								=m().m().m();
								ASTPrimarySuffix aps=(ASTPrimarySuffix)ape.jjtGetChild(ape.jjtGetNumChildren()-2);
								if(aps.isMethodName()){
									NameDeclaration nd=(NameDeclaration)aps.getNameDeclaration();
									if(nd!=null){
										ASTMethodDeclarator amr=(ASTMethodDeclarator)nd.getNode();
										ASTMethodDeclaration amd=(ASTMethodDeclaration)amr.jjtGetParent();
										AliasObject ao=getAliasObject(amd);
										if(ao!=null){
											vnd.setAliasObject(ao);
										}
									}
								}
							}
						}
					}
					else{
//						只有prefix的时候，分为原数据类型和自定义数据类型
						ASTPrimaryPrefix app=(ASTPrimaryPrefix)ape.jjtGetChild(0);
						if(app.usesThisModifier()||app.usesSuperModifier()){
							Node parent=app.jjtGetParent();
							while(!(parent instanceof ASTClassOrInterfaceDeclaration) ){
								parent=parent.jjtGetParent();
							}
							ASTClassOrInterfaceDeclaration ac=(ASTClassOrInterfaceDeclaration)parent;
							AliasObject ao=new AliasObject(ac.getImage(),getId());
							setId(getId() + 1);
							vnd.setAliasObject(ao);
						}else
						if(app.jjtGetChild(0) instanceof ASTAllocationExpression){
//							new的情况
							ASTAllocationExpression aae=(ASTAllocationExpression)app.jjtGetChild(0);
							if(aae.jjtGetChild(0) instanceof ASTClassOrInterfaceType){
//								类对象
								ASTClassOrInterfaceType aci=(ASTClassOrInterfaceType)aae.jjtGetChild(0);
//								创建对象
								AliasObject ao=new AliasObject(aci.getImage(), getId());
								setId(getId() + 1);
								vnd.setAliasObject(ao);
							}
							else
							if(aid.isArray()&& aae.jjtGetChild(0) instanceof ASTPrimitiveType){
//								原数据类型
								ASTPrimitiveType apt=(ASTPrimitiveType)aae.jjtGetChild(0);
								AliasObject ao=new AliasObject(apt.getImage(),getId());
								setId(getId() + 1);
								vnd.setAliasObject(ao);
							}
							
						}
						else{
							if(app.jjtGetChild(0) instanceof ASTName){
								ASTName name=(ASTName)app.jjtGetChild(0);
								if(!name.isMethodName()){
									VariableNameDeclaration vd=(VariableNameDeclaration)name.getNameDeclaration();
									if(vd!=null&&vd.getAliasObject()!=null)
										vnd.setAliasObject(vd.getAliasObject());
								}
							}
						}
					}
				}
			}
		}
		return null;
	}
	
	@Override
	public Object visit(ASTStatementExpression treenode, Object data) {
		if(treenode instanceof ASTStatementExpression){
			if(treenode.jjtGetNumChildren()==3){
				ASTAssignmentOperator aao=(ASTAssignmentOperator)treenode.jjtGetChild(1);
				if(aao.getImage().equals("=")){
					ASTPrimaryExpression ape=(ASTPrimaryExpression)treenode.jjtGetChild(0);
					ASTExpression ae=(ASTExpression)treenode.jjtGetChild(2);
//					分析左边是name的情况
					if(ape.jjtGetNumChildren()==1){
						ASTPrimaryPrefix app=(ASTPrimaryPrefix)ape.jjtGetChild(0);
						ASTName name=(ASTName)app.getFirstChildOfType(ASTName.class);
						if(name.isMethodName())return null;
						List<NameDeclaration>list =name.getNameDeclarationList();
						if(list.isEmpty())return null;
						NameDeclaration nd=list.get(list.size()-1);
						VariableNameDeclaration vnd=(VariableNameDeclaration)nd;
						if(vnd!=null&&vnd.getAliasObject()==null){
//							判断右侧的情况：
//							=new C(class 和 primitive)，=getY()
							if(ae.getFirstChildOfType(ASTAllocationExpression.class)!=null){
								ASTAllocationExpression aae=(ASTAllocationExpression)ae.getFirstChildOfType(ASTAllocationExpression.class);
								if(vnd.isArray()&&aae.jjtGetChild(0)instanceof ASTPrimitiveType){
									ASTPrimitiveType apt=(ASTPrimitiveType)aae.jjtGetChild(0);
									AliasObject ao=new AliasObject(apt.getImage(),getId());
									setId(getId() + 1);
									vnd.setAliasObject(ao);
								}
								else{
									if(aae.jjtGetChild(0)instanceof ASTClassOrInterfaceType){
										ASTClassOrInterfaceType aci=(ASTClassOrInterfaceType)aae.jjtGetChild(0);
//										创建对象
										AliasObject ao=new AliasObject(aci.getImage(), getId());
										setId(getId() + 1);
										vnd.setAliasObject(ao);
									}
								}
							}
							else{
//								判断是否是函数
								ASTPrimaryExpression aspe=(ASTPrimaryExpression)ae.getFirstChildOfType(ASTPrimaryExpression.class);
								if(aspe.jjtGetNumChildren()==2){
									if(aspe.getFirstChildOfType(ASTName.class)!=null){
										ASTName na=(ASTName)aspe.getFirstChildOfType(ASTName.class);
										if(na.isMethodName()){
											List<NameDeclaration>l=na.getNameDeclarationList();
											if(l.isEmpty())return null;
											NameDeclaration nad=l.get(l.size()-1);
											if(!(nad instanceof MethodNameDeclaration))return null;
											ASTMethodDeclarator amr=(ASTMethodDeclarator)nad.getNode();
											ASTMethodDeclaration amd=(ASTMethodDeclaration)amr.jjtGetParent();
											AliasObject ao=getAliasObject(amd);
											vnd.setAliasObject(ao);
										}
									}
								}
								else{
									if(aspe.jjtGetNumChildren()>2){
										ASTPrimarySuffix aps=(ASTPrimarySuffix)aspe.jjtGetChild(aspe.jjtGetNumChildren()-2);
										if(aps.isMethodName()){
											NameDeclaration nad=aps.getNameDeclaration();
											if(nad==null)return null;
											ASTMethodDeclarator amr=(ASTMethodDeclarator)nad.getNode();
											ASTMethodDeclaration amd=(ASTMethodDeclaration)amr.jjtGetParent();
											AliasObject ao=getAliasObject(amd);
											vnd.setAliasObject(ao);
										}
									}
								}
							}
						}
					}
					else{
//						分析左边是this.f或者C.f的情况
						if(ape.jjtGetNumChildren()==2){
							ASTPrimaryPrefix app=(ASTPrimaryPrefix)ape.jjtGetChild(0);
							if(app.usesThisModifier()||app.usesSuperModifier()){
								ASTPrimarySuffix apss=(ASTPrimarySuffix)ape.jjtGetChild(1);
								if(!apss.isMethodName()){
									VariableNameDeclaration vnd=(VariableNameDeclaration)apss.getNameDeclaration();
									if(vnd!=null&&vnd.getAliasObject()==null){
//										判断右侧的情况：
//										=new C(class 和 primitive)，=getY()
										if(ae.getFirstChildOfType(ASTAllocationExpression.class)!=null){
											ASTAllocationExpression aae=(ASTAllocationExpression)ae.getFirstChildOfType(ASTAllocationExpression.class);
											if(vnd.isArray()&&aae.jjtGetChild(0)instanceof ASTPrimitiveType){
												ASTPrimitiveType apt=(ASTPrimitiveType)aae.jjtGetChild(0);
												AliasObject ao=new AliasObject(apt.getImage(),getId());
												setId(getId() + 1);
												vnd.setAliasObject(ao);
											}
											else{
												if(aae.jjtGetChild(0)instanceof ASTClassOrInterfaceType){
													ASTClassOrInterfaceType aci=(ASTClassOrInterfaceType)aae.jjtGetChild(0);
//													创建对象
													AliasObject ao=new AliasObject(aci.getImage(), getId());
													setId(getId() + 1);
													vnd.setAliasObject(ao);
												}
											}
										}
										else{
//											判断是否是函数
											ASTPrimaryExpression aspe=(ASTPrimaryExpression)ae.getFirstChildOfType(ASTPrimaryExpression.class);
											if(aspe.jjtGetNumChildren()==2){
												if(aspe.getFirstChildOfType(ASTName.class)!=null){
													ASTName na=(ASTName)aspe.getFirstChildOfType(ASTName.class);
													if(na.isMethodName()){
														List<NameDeclaration>l=na.getNameDeclarationList();
														if(l.isEmpty())return null;
														NameDeclaration nad=l.get(l.size()-1);
														if(!(nad instanceof MethodNameDeclaration))return null;
														ASTMethodDeclarator amr=(ASTMethodDeclarator)nad.getNode();
														ASTMethodDeclaration amd=(ASTMethodDeclaration)amr.jjtGetParent();
														AliasObject ao=getAliasObject(amd);
														vnd.setAliasObject(ao);
													}
												}
											}
											else{
												if(aspe.jjtGetNumChildren()>2){
													ASTPrimarySuffix aps=(ASTPrimarySuffix)aspe.jjtGetChild(aspe.jjtGetNumChildren()-2);
													if(aps.isMethodName()){
														NameDeclaration nad=aps.getNameDeclaration();
														if(nad==null)return null;
														ASTMethodDeclarator amr=(ASTMethodDeclarator)nad.getNode();
														ASTMethodDeclaration amd=(ASTMethodDeclaration)amr.jjtGetParent();
														AliasObject ao=getAliasObject(amd);
														vnd.setAliasObject(ao);
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return null;
	}
	
	/** 函数初始化分析 */
	private AliasObject getAliasObject(ASTMethodDeclaration amd){
		if(amd.jjtGetChild(0) instanceof ASTResultType){
			ASTResultType art=(ASTResultType)amd.jjtGetChild(0);
			if(art.getFirstChildOfType(ASTClassOrInterfaceType.class)!=null){
				ASTClassOrInterfaceType act=(ASTClassOrInterfaceType)art.getFirstChildOfType(ASTClassOrInterfaceType.class);
				if(amd.getFirstChildOfType(ASTReturnStatement.class)!=null){
					List<ASTReturnStatement>list=amd.findChildrenOfType(ASTReturnStatement.class);
					for(int i=0;i<list.size();i++){
						ASTReturnStatement ars=list.get(i);
						if(ars.getFirstChildOfType(ASTPrimaryExpression.class )!=null){
							ASTPrimaryExpression ape=(ASTPrimaryExpression)ars.getFirstChildOfType(ASTPrimaryExpression.class);
							if(ape.jjtGetNumChildren()==1){
								ASTPrimaryPrefix app=(ASTPrimaryPrefix)ape.jjtGetChild(0);
								if(app.jjtGetNumChildren()==0)continue;
								if(app.jjtGetChild(0)instanceof ASTAllocationExpression){
									ASTAllocationExpression astAe=(ASTAllocationExpression)app.jjtGetChild(0);
									if(astAe.jjtGetChild(0) instanceof ASTClassOrInterfaceType){
//										类对象
										ASTClassOrInterfaceType aci=(ASTClassOrInterfaceType)astAe.jjtGetChild(0);
//										创建对象
										AliasObject ao=new AliasObject(aci.getImage(), getId());
										setId(getId() + 1);
										return ao;
									}
								}
								else
								if(app.getFirstChildOfType(ASTName.class)!=null){
									ASTName name=(ASTName)app.getFirstChildOfType(ASTName.class);
									VariableNameDeclaration vnd=(VariableNameDeclaration)name.getNameDeclaration();
									if(vnd!=null&&vnd.getTypeImage().equals(act.getImage()))
										return vnd.getAliasObject();
								}
							}
							else{
								if(ape.jjtGetNumChildren()==2){
									ASTPrimaryPrefix app=(ASTPrimaryPrefix)ape.jjtGetChild(0);
									if(app.isMethodName()){
										if(app.getFirstChildOfType(ASTName.class)!=null){
											ASTName name=(ASTName)app.getFirstChildOfType(ASTName.class);
											List<NameDeclaration>l=name.getNameDeclarationList();
											if(!l.isEmpty()){
												NameDeclaration md=(NameDeclaration)l.get(l.size()-1);
												if(md!=null&&md.getNode() instanceof ASTMethodDeclarator){
													ASTMethodDeclarator adr=(ASTMethodDeclarator)md.getNode();
													ASTMethodDeclaration method=(ASTMethodDeclaration)adr.jjtGetParent();
													return getAliasObject(method);
												}
											}
										}
									}
								}
								else
									if(ape.jjtGetNumChildren()>2){
										ASTPrimarySuffix aps=(ASTPrimarySuffix)ape.jjtGetChild(ape.jjtGetNumChildren()-2);
										if(aps.isMethodName()){
											NameDeclaration md=(NameDeclaration)aps.getNameDeclaration();
											if(md!=null&&md.getNode() instanceof ASTMethodDeclarator){
												ASTMethodDeclarator adr=(ASTMethodDeclarator)md.getNode();
												ASTMethodDeclaration method=(ASTMethodDeclaration)adr.jjtGetParent();
												return getAliasObject(method);
											}
										}
									}
							}
						}
					}
				}
			}
			else
				if(art.getFirstChildOfType(ASTPrimitiveType.class)!=null){
					ASTPrimitiveType act=(ASTPrimitiveType)art.getFirstChildOfType(ASTPrimitiveType.class);
					if(!act.isArray())return null;
					if(amd.getFirstChildOfType(ASTReturnStatement.class)!=null){
						List<ASTReturnStatement>list=amd.findChildrenOfType(ASTReturnStatement.class);
						for(int i=0;i<list.size();i++){
							ASTReturnStatement ars=list.get(i);
							if(ars.getFirstChildOfType(ASTPrimaryExpression.class )!=null){
								ASTPrimaryExpression ape=(ASTPrimaryExpression)ars.getFirstChildOfType(ASTPrimaryExpression.class);
								if(ape.jjtGetNumChildren()==1){
									if(ape.getFirstChildOfType(ASTName.class)!=null){
										ASTName name=(ASTName)ape.getFirstChildOfType(ASTName.class);
										VariableNameDeclaration vnd=(VariableNameDeclaration)name.getNameDeclaration();
										if(vnd.getTypeImage().equals(act.getImage()))
											return vnd.getAliasObject();
									}
								}
								else{
									if(ape.jjtGetNumChildren()==2){
										ASTPrimaryPrefix app=(ASTPrimaryPrefix)ape.jjtGetChild(0);
										if(app.isMethodName()){
											if(app.getFirstChildOfType(ASTName.class)!=null){
												ASTName name=(ASTName)app.getFirstChildOfType(ASTName.class);
												List<NameDeclaration>l=name.getNameDeclarationList();
												NameDeclaration md=(NameDeclaration)l.get(l.size()-1);
												if(md!=null&&md.getNode() instanceof ASTMethodDeclarator){
													ASTMethodDeclarator adr=(ASTMethodDeclarator)md.getNode();
													ASTMethodDeclaration method=(ASTMethodDeclaration)adr.jjtGetParent();
													return getAliasObject(method);
												}
											}
										}
									}
									else
										if(ape.jjtGetNumChildren()>2){
											ASTPrimarySuffix aps=(ASTPrimarySuffix)ape.jjtGetChild(ape.jjtGetNumChildren()-2);
											if(aps.isMethodName()){
												NameDeclaration md=(NameDeclaration)aps.getNameDeclaration();
												if(md!=null&&md.getNode() instanceof ASTMethodDeclarator){
													ASTMethodDeclarator adr=(ASTMethodDeclarator)md.getNode();
													ASTMethodDeclaration method=(ASTMethodDeclaration)adr.jjtGetParent();
													return getAliasObject(method);
												}
											}
										}
								}
							}
						}
					}
				}
		}
		return null;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
	
}
