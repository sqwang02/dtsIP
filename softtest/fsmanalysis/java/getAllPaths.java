package softtest.fsmanalysis.java;

import java.util.ArrayList;
import java.util.List;
 /* ���ʾ���У�`ControlFlowGraph`���ʾ������ͼ��`Node`���ʾͼ�еĽڵ㡣
  ʹ��`getAllPaths`�������Ի�ȡ�ӿ�ʼ�ڵ㵽�����ڵ������·����
  Ȼ��ʹ��`main`��������һ��������ͼ������ӡ����·����������Լ���ʵ������޸Ĵ�ʾ�����ʺ���Ŀ�����ͼ��*/
public class getAllPaths {

    //������һ��ʹ��Java����ʵ�ֿ�����ͼ���ɳ��������·������ʾ����

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
            // ����������ͼ
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

            // ��ȡ����·��
            List<List<Integer>> paths = graph.getAllPaths(1, 6);

            // ��ӡ����·��
            for (List<Integer> path : paths) {
                System.out.println(path);
            }
        }
    }
}
