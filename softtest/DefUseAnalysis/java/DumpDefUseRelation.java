package softtest.DefUseAnalysis.java;
import softtest.cfg.java.*;
import softtest.symboltable.java.NameOccurrence;
import softtest.symboltable.java.VariableNameDeclaration;

import java.io.*;
import softtest.symboltable.java.*;
import java.util.*;

public class DumpDefUseRelation implements GraphVisitor { // 对图遍历的访问者接口
		static String[] colors={"coral","crimson","hotpink","lightpink","orangered","pink","red","violetred",
								"brown","chocolate","rosybrown","saddlebrown","sandybrown","tan",
								"darkorange","orange","orangered","darkgoldenrod","gold","greenyellow","lightyellow",
								"yellow","yellowgreen","chartreuse","darkgreen","forestgreen","limegreen","mintcream",
								"palegreen","springgreen","aquamarine","cyan","lightcyan","turquoise",
								"blue","blueviolet","darkslateblue","lightblue","navy","powderblue","skyblue",
								"steelblue","darkviolet","orchid","purple","violet"};
		/** 访问控制流图的节点 */
		public void visit(VexNode n, Object data) {
			FileWriter out = (FileWriter) data;
			try {
				String s = "";
				//s = s + ",color=red";
				out.write(n.getName() + "[label=\"" + n.getName() + "\""  + "];\n");
				for(NameOccurrence occ: n.getOccurrences()){
					if(!(occ.getDeclaration() instanceof VariableNameDeclaration)){
						continue;
					}
					VariableNameDeclaration v=(VariableNameDeclaration)occ.getDeclaration();
					if(occ.getOccurrenceType()==NameOccurrence.OccurrenceType.DEF){
						for(NameOccurrence use:occ.getDefUseList()){
							VexNode vex=use.getLocation().getCurrentVexNode();
							if(vex==null){
								continue;
							}
							s=n.getName() + " -> " + vex.getName() + "[label=\"" + v.getImage() + "\"";
							s=s+",color="+colors[occ.getLocation().hashCode()%colors.length]+",style=dashed,arrowsize=0.4";
							s = s + "];\n";
							out.write(s);
						}
					}
				}
			} catch (IOException ex) {
			}
		}

		/** 访问控制流图的边，打印名字 */
		public void visit(Edge e, Object data) {
			FileWriter out = (FileWriter) data;
			try {
				String s = "";
				s = e.getTailNode().getName() + " -> " + e.getHeadNode().getName() + "[label=\"" + e.getName() + "\"";
				// s= s + ",color=red";		
				s = s + "];\n";
				out.write(s);
			} catch (IOException ex) {
			}
		}

		/** 访问控制流图，遍历访问其节点集合和边集合 */
		public void visit(Graph g, Object data) {
			try {
				FileWriter out = new FileWriter((String) data);
				out.write("digraph G {\n");

				for (Enumeration<VexNode> e = g.nodes.elements(); e.hasMoreElements();) {
					VexNode n = e.nextElement();
					visit(n, out);
				}

				for (Enumeration<Edge> e = g.edges.elements(); e.hasMoreElements();) {
					Edge edge = e.nextElement();
					visit(edge, out);
				}
				out.write(" }");
				out.close();
			} catch (IOException ex) {
			}
		}
	}

