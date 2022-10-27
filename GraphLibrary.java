import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * GraphLibrary holds assigned methods: bfs, getPath, missingVertices, and averageSeparation
 * @author Anand McCoole (TA: Jason Pak)
 * @author Ryan Kim (TA: Caroline Hall)
 */

public class GraphLibrary {

    public GraphLibrary() {}

    /**
     * Runs a Breadth First Search and graphs the shortest path to all reachable vertices
     * @param g, Graph with actors as vertices and Set of co-star movies as edges
     * @param source, the Start vertex that acts as our center of the universe
     * @param <V>
     * @param <E>
     * @return path, Graph that holds the shortest paths to all other vertices
     * @throws Exception
     */
    public static <V,E> Graph<V,E> bfs(Graph<V,E> g, V source) throws Exception {
        // Error Checking
        if (!g.hasVertex(source)) { throw new Exception("graph doesn't contain source"); }

        //SLLQueue<V> queue = new SLLQueue<>();
        Queue<V> queue = new LinkedList<V>();
        Graph<V,E> path = new AdjacencyMapGraph<>();

        queue.add(source);
        path.insertVertex(source);

        while (!queue.isEmpty()) {
            V u = queue.remove();

            for (V v: g.outNeighbors(u)) {
                if (!path.hasVertex(v)) {
                    queue.add(v);
                    path.insertVertex(v);
                    path.insertDirected(v, u, null);
                }
            }
        }
        return path;
    }

    /**
     * Finds and provides the shortest path from source to another vertex
     * @param tree, Graph that provides shortest paths from source to all other reachable vertices (received from bfs)
     * @param v, vertex that acts as the end vertex
     * @param <V>
     * @param <E>
     * @return shortestPath, List holding the vertex path from source to end vertex
     * @throws Exception
     */
    public static <V,E> List<V> getPath(Graph<V,E> tree, V v) throws Exception {
        // Error Checking
        if (tree == null) { return null; }
        if (!tree.hasVertex(v)) { throw new Exception("source not in tree"); }

        ArrayList<V> shortestPath = new ArrayList<V>(); //this will hold the path from source to end vertex
        V current = v; //start at end vertex

        // Loop from end vertex back to source vertex
        while (current != null) {
            shortestPath.add(0, current); //add this vertex to front of arraylist path

            if (tree.outDegree(current) < 1) {
                return shortestPath;
            }

            // Get next vertex
            for (V n: tree.outNeighbors(current)) {
                current = n;
            }
        }
        return shortestPath;
    }

    /**
     * Given two graphs, one a subgraph of the other, finds all the vertices missing from the smaller graph
     * @param graph, larger Graph
     * @param subgraph, smaller Graph
     * @param <V>
     * @param <E>
     * @return missing, Set of all the missing vertices
     * @throws Exception
     */
    public static <V,E> Set<V> missingVertices(Graph<V,E> graph, Graph<V,E> subgraph) throws Exception {
        // Error Checking
        if (graph == null || subgraph == null) {
            throw new Exception ("invalid graph");
        }

        Set<V> missing = new HashSet<>();

        for (V vert: graph.vertices()) {
            if (!subgraph.hasVertex(vert)) {
                missing.add(vert);
            }
        }
        return missing;
    }

    /**
     * Given a Graph and root/source, finds the average degree of separation between itself and all other vertices
     * @param tree, Graph holding shortest paths to all reachable vertices
     * @param root, source of the Graph
     * @param <V>
     * @param <E>
     * @return sum, Double holding the average degree of separation
     * @throws Exception
     */
    public static <V,E> double averageSeparation(Graph<V,E> tree, V root) throws Exception {
        // Exception shit
        if (!tree.hasVertex(root)) {
            throw new Exception("root not in tree");
        }

        double avg = ((double) recursiveHelper(tree, root, 0, 0)) / (tree.numVertices());
        return avg;
    }

    /**
     * Recursive helper function that traverses the Graph to find average degree of separation
     * @param tree
     * @param root
     * @param sum, total sum of degrees of separation
     * @param distance, individual vertices' distance to root (increases every call)
     * @param <V>
     * @param <E>
     * @return
     */
    public static <V,E> int recursiveHelper(Graph<V,E> tree, V root, int sum, int distance) {
        sum += distance;
        if (tree.inDegree(root) != 0) {
            for (V v: tree.inNeighbors(root)) {
                sum = recursiveHelper(tree, v, sum, distance+1);
            }
        }
        return sum;
    }
}