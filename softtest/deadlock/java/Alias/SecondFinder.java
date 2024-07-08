package softtest.deadlock.java.Alias;

import java.util.List;

import com.sun.tools.javac.util.Name;

import softtest.ast.java.ASTAllocationExpression;
import softtest.ast.java.ASTArgumentList;
import softtest.ast.java.ASTArguments;
import softtest.ast.java.ASTAssignmentOperator;
import softtest.ast.java.ASTClassOrInterfaceDeclaration;
import softtest.ast.java.ASTClassOrInterfaceType;
import softtest.ast.java.ASTExpression;
import softtest.ast.java.ASTFormalParameter;
import softtest.ast.java.ASTFormalParameters;
import softtest.ast.java.ASTMethodDeclaration;
import softtest.ast.java.ASTMethodDeclarator;
import softtest.ast.java.ASTName;
import softtest.ast.java.ASTPrimaryExpression;
import softtest.ast.java.ASTPrimaryPrefix;
import softtest.ast.java.ASTPrimarySuffix;
import softtest.ast.java.ASTPrimitiveType;
import softtest.ast.java.ASTResultType;
import softtest.ast.java.ASTReturnStatement;
import softtest.ast.java.ASTStatement;
import softtest.ast.java.ASTStatementExpression;
import softtest.ast.java.ASTVariableDeclarator;
import softtest.ast.java.ASTVariableDeclaratorId;
import softtest.ast.java.ASTVariableInitializer;
import softtest.ast.java.JavaParserVisitorAdapter;
import softtest.ast.java.Node;
import softtest.symboltable.java.MethodNameDeclaration;
import softtest.symboltable.java.NameDeclaration;
import softtest.symboltable.java.VariableNameDeclaration;

public class SecondFinder extends JavaParserVisitorAdapter{
	
	
	@Override
	public Object visit(ASTStatementExpression treenode, Object data) {
		if(treenode instanceof ASTStatementExpression){
			if(treenode.jjtGetNumChildren()==3){
				ASTAssignmentOperator aao=(ASTAssignmentOperator)treenode.jjtGetChild(1);
				if(aao.getImage().equals("=")){
					ASTPrimaryExpression ape=(ASTPrimaryExpression)treenode.jjtGetChild(0);
//					a=b、a=this.f
					
					if(ape.getFirstChildOfType(ASTName.class)!=null){
						ASTName name=(ASTName)ape.getFirstChildOfType(ASTName.class);
						ASTExpression ae=(ASTExpression)treenode.jjtGetChild(2);
						if(!name.isMethodName()){
							VariableNameDeclaration vnd=(VariableNameDeclaration)name.getNameDeclaration();
							if(vnd==null)return null;
							if(ae.jjtGetChild(0) instanceof ASTPrimaryExpression){
								ASTPrimaryExpression ap=(ASTPrimaryExpression)ae.jjtGetChild(0);
								if(ap.getFirstChildOfType(ASTAllocationExpression.class)!=null){
									return null;
								}
								else
								if(ap.jjtGetNumChildren()==1){
//									没有考虑a=new C的情况
								
									
//									最简单的a=b
									if(ap.getFirstChildOfType(ASTName.class)!=null){
										ASTName nm=(ASTName)ap.getFirstChildOfType(ASTName.class);
										VariableNameDeclaration  vn=(VariableNameDeclaration)nm.getNameDeclaration();
										if(vn!=null&&vn.getAliasObject()!=null)	
											vnd.setAliasObject(vn.getAliasObject());
									}
								}
								else{
									if(ap.jjtGetNumChildren()==2){
										if(ap.jjtGetChild(0)instanceof ASTPrimaryPrefix){
											ASTPrimaryPrefix app=(ASTPrimaryPrefix)ap.jjtGetChild(0);
											if(app.usesThisModifier()||app.usesSuperModifier()){
//											a=this.f
												ASTPrimarySuffix aps=(ASTPrimarySuffix)ap.jjtGetChild(1);
												if(!aps.isMethodName()){
													VariableNameDeclaration vn=(VariableNameDeclaration)aps.getNameDeclaration();
													if(vn!=null&&vn.getAliasObject()!=null)	
														vnd.setAliasObject(vn.getAliasObject());
												}
											}
											else{
//												a=method();	
												if(app.isMethodName()){
													ASTName nm=(ASTName)app.jjtGetChild(0);
													List<NameDeclaration> list=nm.getNameDeclarationList();
													if(list.isEmpty())return null;
													NameDeclaration nd=list.get(list.size()-1);
													if(nd instanceof MethodNameDeclaration){
														ASTMethodDeclarator amr=(ASTMethodDeclarator)nd.getNode();
														ASTMethodDeclaration amd=(ASTMethodDeclaration)amr.jjtGetParent();
														AliasObject  ao=getAliasObject(amd);
														vnd.setAliasObject(ao);
													}
												}
											}
										}
									}
									else{
//									a=method().method().method().f
//									a=method().method().method()	
										if(ap.jjtGetNumChildren()>2){
											ASTPrimarySuffix aps=(ASTPrimarySuffix)ap.jjtGetChild(ap.jjtGetNumChildren()-2);
											if(aps.isMethodName()){
												NameDeclaration nd=aps.getNameDeclaration();
												if(nd instanceof MethodNameDeclaration){
													ASTMethodDeclarator amr=(ASTMethodDeclarator)nd.getNode();
													ASTMethodDeclaration amd=(ASTMethodDeclaration)amr.jjtGetParent();
													AliasObject  ao=getAliasObject(amd);
													vnd.setAliasObject(ao);
												}
											}
										}
									}
								}
							}
						}
					}
//					this.f=b;
					else{
						ASTPrimaryPrefix aspp=(ASTPrimaryPrefix)ape.jjtGetChild(0);
						if(aspp.usesThisModifier()||aspp.usesSuperModifier()){
							ASTExpression ae=(ASTExpression)treenode.jjtGetChild(2);
							ASTPrimarySuffix apss=(ASTPrimarySuffix)ape.jjtGetChild(1);
							if(!apss.isMethodName()){
								VariableNameDeclaration vnd=(VariableNameDeclaration)apss.getNameDeclaration();
								if(vnd==null)return null;
								if(ae.jjtGetChild(0) instanceof ASTPrimaryExpression){
									ASTPrimaryExpression ap=(ASTPrimaryExpression)ae.jjtGetChild(0);
									if(ap.jjtGetNumChildren()==1){
//										最简单的a=b
										if(ap.getFirstChildOfType(ASTName.class)!=null){
											ASTName nm=(ASTName)ap.getFirstChildOfType(ASTName.class);
											VariableNameDeclaration  vn=(VariableNameDeclaration)nm.getNameDeclaration();
											if(vn!=null&&vn.getAliasObject()!=null)	
												vnd.setAliasObject(vn.getAliasObject());
										}
									}
									else{
										if(ap.jjtGetNumChildren()==2){
											if(ap.jjtGetChild(0)instanceof ASTPrimaryPrefix){
												ASTPrimaryPrefix app=(ASTPrimaryPrefix)ap.jjtGetChild(0);
												if(app.usesThisModifier()||app.usesSuperModifier()){
//												a=this.f
													ASTPrimarySuffix aps=(ASTPrimarySuffix)ap.jjtGetChild(1);
													if(!aps.isMethodName()){
														VariableNameDeclaration vn=(VariableNameDeclaration)aps.getNameDeclaration();
														if(vn!=null&&vn.getAliasObject()!=null)	
															vnd.setAliasObject(vn.getAliasObject());
													}
												}
												else{
//													a=method();	
													if(app.isMethodName()){
														ASTName nm=(ASTName)app.jjtGetChild(0);
														List<NameDeclaration> list=nm.getNameDeclarationList();
														if(list.isEmpty())return null;
														NameDeclaration nd=list.get(list.size()-1);
														if(nd instanceof MethodNameDeclaration){
															ASTMethodDeclarator amr=(ASTMethodDeclarator)nd.getNode();
															ASTMethodDeclaration amd=(ASTMethodDeclaration)amr.jjtGetParent();
															AliasObject  ao=getAliasObject(amd);
															vnd.setAliasObject(ao);
														}
													}
												}
											}
										}
										else{
//										a=method().method().method().f
//										a=method().method().method()	
											if(ap.jjtGetNumChildren()>2){
												ASTPrimarySuffix aps=(ASTPrimarySuffix)ap.jjtGetChild(ap.jjtGetNumChildren()-2);
												if(aps.isMethodName()){
													NameDeclaration nd=aps.getNameDeclaration();
													if(nd instanceof MethodNameDeclaration){
														ASTMethodDeclarator amr=(ASTMethodDeclarator)nd.getNode();
														ASTMethodDeclaration amd=(ASTMethodDeclaration)amr.jjtGetParent();
														AliasObject  ao=getAliasObject(amd);
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
	
	@Override
	/** 函数参数的问题 */
	public Object visit(ASTPrimaryPrefix treenode, Object data) {
		if(treenode instanceof ASTPrimaryPrefix){
			if(treenode.isMethodName()){
//				System.out.println(treenode.getIndexOfParent());
				int index=treenode.getIndexOfParent();
				ASTPrimaryExpression ape=(ASTPrimaryExpression)treenode.jjtGetParent();
				ASTPrimarySuffix aps=(ASTPrimarySuffix)ape.jjtGetChild(index+1);
				if(aps.usesThisModifier())return null;
				ASTName name=(ASTName)treenode.getFirstChildOfType(ASTName.class);
				if(name==null)return null;
				List<NameDeclaration>list=name.getNameDeclarationList();
				if(list.size()==0)return null;
				if(!(list.get(list.size()-1)instanceof MethodNameDeclaration))return null;
				MethodNameDeclaration mnd=(MethodNameDeclaration)list.get(list.size()-1);
				if(mnd.getNode()instanceof ASTMethodDeclarator){
					ASTMethodDeclarator amr=(ASTMethodDeclarator)mnd.getNode();
					ASTFormalParameters afps=(ASTFormalParameters)amr.jjtGetChild(0);
					if(afps.jjtGetNumChildren()!=0){
						ASTArgumentList aal=(ASTArgumentList)aps.getFirstChildOfType(ASTArgumentList.class);
						for(int i=0;i<afps.jjtGetNumChildren();i++){
							ASTFormalParameter afp=(ASTFormalParameter)afps.jjtGetChild(i);
							ASTExpression ase=(ASTExpression)aal.jjtGetChild(i);
							ASTVariableDeclaratorId avi=(ASTVariableDeclaratorId)afp.jjtGetChild(1);
							VariableNameDeclaration vnd=avi.getNameDeclaration();
							ASTPrimaryExpression ap=(ASTPrimaryExpression)ase.getFirstChildOfType(ASTPrimaryExpression.class);
							if(ap.jjtGetNumChildren()==1){
								if(ap.getFirstChildOfType(ASTName.class)!=null){
									ASTName nm=(ASTName)ap.getFirstChildOfType(ASTName.class);
									VariableNameDeclaration  vn=(VariableNameDeclaration)nm.getNameDeclaration();
									if(vn!=null&&vn.getAliasObject()!=null)	
										vnd.setAliasObject(vn.getAliasObject());
								}
							}else{
								if(ap.jjtGetNumChildren()==2){
									if(ap.jjtGetChild(0)instanceof ASTPrimaryPrefix){
										ASTPrimaryPrefix app=(ASTPrimaryPrefix)ap.jjtGetChild(0);
										if(app.usesThisModifier()||app.usesSuperModifier()){
//										a=this.f
											ASTPrimarySuffix asps=(ASTPrimarySuffix)ap.jjtGetChild(1);
											if(!asps.isMethodName()){
												VariableNameDeclaration vn=(VariableNameDeclaration)asps.getNameDeclaration();
												if(vn!=null&&vn.getAliasObject()!=null)	
													vnd.setAliasObject(vn.getAliasObject());
											}
										}
										else{
//											a=method();	
											if(app.isMethodName()){
												ASTName nm=(ASTName)app.jjtGetChild(0);
												List<NameDeclaration> l=nm.getNameDeclarationList();
												if(l.isEmpty())return null;
												NameDeclaration nd=l.get(l.size()-1);
												if(nd instanceof MethodNameDeclaration){
													ASTMethodDeclarator asmr=(ASTMethodDeclarator)nd.getNode();
													ASTMethodDeclaration amd=(ASTMethodDeclaration)asmr.jjtGetParent();
													AliasObject  ao=getAliasObject(amd);
													vnd.setAliasObject(ao);
												}
											}
										}
									}
								}
								else{
//								a=method().method().method().f
//								a=method().method().method()	
									if(ap.jjtGetNumChildren()>2){
										ASTPrimarySuffix asps=(ASTPrimarySuffix)ap.jjtGetChild(ap.jjtGetNumChildren()-2);
										if(aps.isMethodName()){
											NameDeclaration nd=asps.getNameDeclaration();
											if(nd instanceof MethodNameDeclaration){
												ASTMethodDeclarator asmr=(ASTMethodDeclarator)nd.getNode();
												ASTMethodDeclaration amd=(ASTMethodDeclaration)asmr.jjtGetParent();
												AliasObject  ao=getAliasObject(amd);
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
		return null;
	}
	
	@Override
	/** 函数参数的问题 */
	public Object visit(ASTPrimarySuffix treenode, Object data) {
		if(treenode instanceof ASTPrimarySuffix){
			if(treenode.isMethodName()){
				int index=treenode.getIndexOfParent();
				ASTPrimaryExpression ape=(ASTPrimaryExpression)treenode.jjtGetParent();
				ASTPrimarySuffix aps=(ASTPrimarySuffix)ape.jjtGetChild(index+1);
				MethodNameDeclaration mnd=(MethodNameDeclaration)treenode.getNameDeclaration();
				if(mnd==null)return null;
				if(mnd.getNode() instanceof ASTMethodDeclarator){
					ASTMethodDeclarator amr=(ASTMethodDeclarator)mnd.getNode();
					ASTFormalParameters afps=(ASTFormalParameters)amr.jjtGetChild(0);
					if(afps.jjtGetNumChildren()!=0){
						ASTArgumentList aal=(ASTArgumentList)aps.getFirstChildOfType(ASTArgumentList.class);
						for(int i=0;i<afps.jjtGetNumChildren();i++){
							ASTFormalParameter afp=(ASTFormalParameter)afps.jjtGetChild(i);
							ASTExpression ase=(ASTExpression)aal.jjtGetChild(i);
							ASTVariableDeclaratorId avi=(ASTVariableDeclaratorId)afp.jjtGetChild(1);
							VariableNameDeclaration vnd=avi.getNameDeclaration();
							ASTPrimaryExpression ap=(ASTPrimaryExpression)ase.getFirstChildOfType(ASTPrimaryExpression.class);
							if(ap.jjtGetNumChildren()==1){
								if(ap.getFirstChildOfType(ASTName.class)!=null){
									ASTName nm=(ASTName)ap.getFirstChildOfType(ASTName.class);
									VariableNameDeclaration  vn=(VariableNameDeclaration)nm.getNameDeclaration();
									if(vn!=null&&vn.getAliasObject()!=null)	
										vnd.setAliasObject(vn.getAliasObject());
								}
							}else{
								if(ap.jjtGetNumChildren()==2){
									if(ap.jjtGetChild(0)instanceof ASTPrimaryPrefix){
										ASTPrimaryPrefix app=(ASTPrimaryPrefix)ap.jjtGetChild(0);
										if(app.usesThisModifier()||app.usesSuperModifier()){
//										a=this.f
											ASTPrimarySuffix asps=(ASTPrimarySuffix)ap.jjtGetChild(1);
											if(!asps.isMethodName()){
												VariableNameDeclaration vn=(VariableNameDeclaration)asps.getNameDeclaration();
												if(vn!=null&&vn.getAliasObject()!=null)	
													vnd.setAliasObject(vn.getAliasObject());
											}
										}
										else{
//											a=method();	
											if(app.isMethodName()){
												ASTName nm=(ASTName)app.jjtGetChild(0);
												List<NameDeclaration> l=nm.getNameDeclarationList();
												if(l.isEmpty())return null;
												NameDeclaration nd=l.get(l.size()-1);
												if(nd instanceof MethodNameDeclaration){
													ASTMethodDeclarator asmr=(ASTMethodDeclarator)nd.getNode();
													ASTMethodDeclaration amd=(ASTMethodDeclaration)asmr.jjtGetParent();
													AliasObject  ao=getAliasObject(amd);
													vnd.setAliasObject(ao);
												}
											}
										}
									}
								}
								else{
//								a=method().method().method().f
//								a=method().method().method()	
									if(ap.jjtGetNumChildren()>2){
										ASTPrimarySuffix asps=(ASTPrimarySuffix)ap.jjtGetChild(ap.jjtGetNumChildren()-2);
										if(aps.isMethodName()){
											NameDeclaration nd=asps.getNameDeclaration();
											if(nd instanceof MethodNameDeclaration){
												ASTMethodDeclarator asmr=(ASTMethodDeclarator)nd.getNode();
												ASTMethodDeclaration amd=(ASTMethodDeclaration)asmr.jjtGetParent();
												AliasObject  ao=getAliasObject(amd);
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
		return null;
	}
	
	@Override
	public Object visit(ASTVariableDeclarator treenode, Object data){
		if(treenode instanceof ASTVariableDeclarator){
			if(treenode.jjtGetNumChildren()>=2){
				ASTVariableDeclaratorId aid=(ASTVariableDeclaratorId)treenode.jjtGetChild(0);
				VariableNameDeclaration vnd=aid.getNameDeclaration();
				if(vnd.getAliasObject()!=null)return null;
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
										AliasObject ao=new AliasObject(aci.getImage(), 11);
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
}
