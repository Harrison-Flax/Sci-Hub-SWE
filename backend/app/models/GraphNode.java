package models;



import java.util.HashSet;
import java.util.Set;

public  class GraphNode {

    public static long val;
    public static int minPath;
    public static Set<GraphNode> edges;

    public GraphNode(long val){
        this.val = val;
        this.minPath = 0;
        this.edges = new HashSet<>();
    }
}