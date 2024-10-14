package pd.util;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.EqualsAndHashCode;
import lombok.ToString;

public class Dag {

    private final Set<String> nodes = new LinkedHashSet<>();

    // possible multiple edges between two nodes
    private final List<Edge> edges = new LinkedList<>();

    public void addNode(String name) {
        if (name == null) {
            throw new RuntimeException("Dag: node name is null");
        }
        if (nodes.contains(name)) {
            throw new RuntimeException("Dag: node name already existed: " + name);
        }
        nodes.add(name);
    }

    public void addEdge(String from, String to) {
        if (from == null || to == null) {
            throw new RuntimeException("Dag: node name is null");
        }
        if (!nodes.contains(from)) {
            throw new RuntimeException("Dag: unknown node: " + from);
        } else if (!nodes.contains(to)) {
            throw new RuntimeException("Dag: unknown node: " + to);
        }

        Edge newEdge = new Edge(from, to);
        if (checkIfCycledWithEdge(newEdge)) {
            throw new RuntimeException(String.format("Dag: cycled with (%s, %s)", from, to));
        }
        edges.add(newEdge);
    }

    /**
     * assume no cycle without this edge
     */
    private boolean checkIfCycledWithEdge(Edge edge) {
        if (edge.from.equals(edge.to)) {
            return true;
        }

//        List<DagEdge> edges = new LinkedList<>(this.edges);
//        edges.add(edge);
//        boolean removed;
//        do {
//            Set<String> toNodes = edges.stream()
//                    .map(a -> a.to)
//                    .collect(Collectors.toSet());
//            Set<String> headNodes = edges.stream()
//                    .map(a -> a.from)
//                    .filter(a -> !toNodes.contains(a))
//                    .collect(Collectors.toSet());
//            removed = edges.removeIf(a -> headNodes.contains(a.from));
//        } while (removed);
//        return !edges.isEmpty();

        // it is cycled if path(`to`, `from`) exists
        // often, `to` is a leaf node
        Set<String> settledNodes = new HashSet<>();
        LinkedList<String> q = new LinkedList<>();
        q.add(edge.to);
        while (!q.isEmpty()) {
            String node = q.removeFirst();
            if (settledNodes.contains(node)) {
                continue;
            }
            for (String nextNode : getNextNodes(node)) {
                if (nextNode.equals(edge.from)) {
                    return true;
                }
                q.add(nextNode);
            }
            settledNodes.add(node);
        }
        return false;
    }

    public List<String> getHeadNodes() {
        Set<String> toNodes = edges.stream()
                .map(a -> a.to)
                .collect(Collectors.toSet());
        return nodes.stream()
                .filter(a -> !toNodes.contains(a))
                .collect(Collectors.toList());
    }

    public List<String> getTailNodes() {
        Set<String> fromNodes = edges.stream()
                .map(a -> a.from)
                .collect(Collectors.toSet());
        return nodes.stream()
                .filter(a -> !fromNodes.contains(a))
                .collect(Collectors.toList());
    }

    public List<String> getNextNodes(String node) {
        return edges.stream()
                .filter(a -> a.from.equals(node))
                .map(a -> a.to)
                .collect(Collectors.toList());
    }

    public List<String> getPrevNodes(String node) {
        return edges.stream()
                .filter(a -> a.to.equals(node))
                .map(a -> a.from)
                .collect(Collectors.toList());
    }

    @EqualsAndHashCode
    @ToString
    static class Edge {

        public final String from;

        public final String to;

        public Edge(String from, String to) {
            this.from = from;
            this.to = to;
        }
    }
}
