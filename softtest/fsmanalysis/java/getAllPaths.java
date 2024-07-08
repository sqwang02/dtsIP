package softtest.fsmanalysis.java;

import java.util.ArrayList;
import java.util.List;
 /* 这个示例中，`ControlFlowGraph`类表示控制流图，`Node`类表示图中的节点。
  使用`getAllPaths`方法可以获取从开始节点到结束节点的所有路径。
  然后，使用`main`方法创建一个控制流图，并打印所有路径。请根据自己的实际情况修改此示例以适合你的控制流图。*/
public class getAllPaths {

    //以下是一个使用Java代码实现控制流图生成程序的所有路径集的示例：

    class Node {
        int id;
        List<Integer> nextNodes;

        Node(int id) {
            this.id = id;
            nextNodes = new ArrayList<>();
        }
    }

    public class ControlFlowGraph {
        List<Node> nodes;

        ControlFlowGraph() {
            nodes = new ArrayList<>();
        }

        public List<List<Integer>> getAllPaths(int startNode, int endNode) {
            List<List<Integer>> paths = new ArrayList<>();
            List<Integer> currentPath = new ArrayList<>();

            getAllPathsUtil(startNode, endNode, currentPath, paths);

            return paths;
        }

        private void getAllPathsUtil(int currentNode, int endNode, List<Integer> currentPath, List<List<Integer>> paths) {
            currentPath.add(currentNode);
            if (currentNode == endNode) {
                paths.add(new ArrayList<>(currentPath));
            } else {
                Node node = nodes.get(currentNode);
                for (int nextNode : node.nextNodes) {
                    getAllPathsUtil(nextNode, endNode, currentPath, paths);
                }
            }
            currentPath.remove(currentPath.size() - 1);
        }

        public void main(String[] args) {
            // 创建控制流图
            ControlFlowGraph graph = new ControlFlowGraph();
            Node node1 = new Node(1);
            Node node2 = new Node(2);
            Node node3 = new Node(3);
            Node node4 = new Node(4);
            Node node5 = new Node(5);
            Node node6 = new Node(6);

            node1.nextNodes.add(2);
            node1.nextNodes.add(3);
            node2.nextNodes.add(4);
            node2.nextNodes.add(5);
            node3.nextNodes.add(6);
            node4.nextNodes.add(6);
            node5.nextNodes.add(6);

            graph.nodes.add(node1);
            graph.nodes.add(node2);
            graph.nodes.add(node3);
            graph.nodes.add(node4);
            graph.nodes.add(node5);
            graph.nodes.add(node6);

            // 获取所有路径
            List<List<Integer>> paths = graph.getAllPaths(1, 6);

            // 打印所有路径
            for (List<Integer> path : paths) {
                System.out.println(path);
            }
        }
    }
}
