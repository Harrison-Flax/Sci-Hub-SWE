package services;

import java.util.*;

import models.Author;
import models.Paper;

public class GraphService {
    static List<List<Long>> criticalConnections = new ArrayList<>();

    public static List<List<Long>> criticalConnections(List<Paper> paperList) {
        criticalConnections = new ArrayList<>();

        // Graph
        Map<Long, List<Long>> map = new HashMap<>();

        // Keep track of visited vertices for dfs
        boolean[] visited = new boolean[20];

        // Keep track of Strongly Conected Compoments low-link value
        Long[] lowLinkValues = new Long[20];

        // Keep track of cycles, in dfs we increment and then set time to current lowLinkValues[] & discoverd[] values
        Long time = new Long(0);

        // Load graph with vertices and connections
        for(Paper paper:paperList) {
            if (paper.getAuthors() != null && paper.getAuthors().size() > 1) {
                List<Author> connectedAuthors = paper.getAuthors();
                System.out.println("********" + connectedAuthors.toString());

                if (connectedAuthors.size() > 1)
                    for (int i = 0; i < connectedAuthors.size(); i++) {
                        for (int j = i + 1; j < connectedAuthors.size(); j++) {
                            long a = connectedAuthors.get(i).getId();
                            long b = connectedAuthors.get(j).getId();

                            // Intialize [a ,b] in graph if not already
                            map.putIfAbsent(a, new ArrayList<>());
                            map.putIfAbsent(b, new ArrayList<>());

                            // Add vertices to each respective List
                            map.get(a).add(b);
                            map.get(b).add(a);
                        }
                    }
            }
        }


        // Iterate through vertices in map and call dfs on unvisited vertices
        for (Long k = new Long(1); k <= map.size(); k++) {
            if (!visited[k.intValue()]) {
                dfs(k, k, visited, lowLinkValues, map, time);
            }
        }

        return criticalConnections;
    }

    private static void dfs(Long currentVertice, Long parentVertice, boolean[] visited, Long[] lowLinkValues,
                            Map<Long, List<Long>> map, Long time) {
        // Increment time per cycle
        time++;

        // set current vertice's low link value to cycle time
        lowLinkValues[currentVertice.intValue()] = time;

        // Set discoveredValue to cycle time to avoid comparing
        Long discoverdValue = time;

        // Avoid infinite loop
        visited[currentVertice.intValue()] = true;

        // Iterate through edges
        for (Long vertice : map.get(currentVertice)) {

            // Skip look to avoid comparing in reverse. Ex: if A -> B don't compare B -> A
            if (vertice == parentVertice) continue;

            // Process only unique vertices to avoid leaving a strongly connected component
            if (!visited[vertice.intValue()]) {

                dfs(vertice, currentVertice, visited, lowLinkValues, map, time);

                // This means there is an edge that connects strongly connected components
                if (discoverdValue < lowLinkValues[vertice.intValue()]) {
                    criticalConnections.add(Arrays.asList(currentVertice, vertice));
                }
            }

            // Update lowlinkValues
            lowLinkValues[currentVertice.intValue()] = Math.min(lowLinkValues[currentVertice.intValue()],
                    lowLinkValues[vertice.intValue()]);
        }
    }
}
        // Init Graph

