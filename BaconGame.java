import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * BaconGame implements the methods in GraphLibrary to create a working version of the game
 * @author Anand McCoole (TA: Jason Pak)
 * @author Ryan Kim (TA: Caroline Hall)
 */

public class BaconGame {
    Map<String, String> actors; // ID --> Actor Name
    Map<String, String> movies; // ID --> Movie Name
    Map<String, HashSet<String>> mta; // MovieID --> Set of Actor IDs
    Graph<String, HashSet<String>> g; // Graph holding all actors as vertices and Set of co-star movies as edges

    Map<String, Double> actorsBySep; // Actor --> Average Separation
    ArrayList<String> biggerList; // Holds all the actors sorted by avg separation

    String actorsFile;
    String moviesFile;
    String movieToActorsFile;

    String centerOfUniverse;

    public BaconGame(String aFile, String mFile, String mtaFile) {
        actorsFile = aFile;
        moviesFile = mFile;
        movieToActorsFile = mtaFile;

        actors = new TreeMap<>();
        movies = new TreeMap<>();
        mta = new HashMap<>();
        g = new AdjacencyMapGraph<>();

        actorsBySep = new TreeMap<>();
        biggerList = new ArrayList<>();
    }

    /**
     * Creates the Graph g which holds all the actors as vertices and Set of co-star movies as edges
     * @throws IOException
     */
    public void createGraph() throws IOException {
        BufferedReader a = new BufferedReader(new FileReader(actorsFile));
        BufferedReader m = new BufferedReader(new FileReader(moviesFile));
        BufferedReader ma = new BufferedReader(new FileReader(movieToActorsFile));

        String line;
        String[] spline;

        // Reading actor file and building actors Map
        while ((line = a.readLine()) != null) {
            spline = line.split("\\|");
            actors.put(spline[0], spline[1]);
        }
        a.close();

        // Reading movie file and building movies Map
        while ((line = m.readLine()) != null) {
            spline = line.split("\\|");
            movies.put(spline[0], spline[1]);
        }
        m.close();

        // Reading movieToActor file and building mta Map
        while ((line = ma.readLine()) != null) {
            spline = line.split("\\|");
            String mname = movies.get(spline[0]);
            if (!mta.containsKey(mname)) {
                mta.put(mname, new HashSet<>());
            }
            mta.get(mname).add(this.actors.get(spline[1]));
        }
        ma.close();

        // Adding to Graph g (vertices)
        for (String s: actors.keySet()) {
            g.insertVertex(actors.get(s));
        }

        // Adding to Graph g (edges)
        for (String s: mta.keySet()) { // for each movie (s holds the keys to mta which are movie names)
            for (String x: mta.get(s)) { // for each actor set in each movie
                for (String y: mta.get(s)) {
                    if (!Objects.equals(x, y) && g.hasEdge(x, y)) {
                        g.getLabel(x, y).add(s);
                    } else if (!Objects.equals(x, y) && !g.hasEdge(x, y)) {
                        g.insertUndirected(x, y, new HashSet<>());
                        g.getLabel(x, y).add(s);
                    }
                }
            }
        }
    }

    /**
     * Sort the actors by average number of separation
     * @return List holding the actors sorted by average degree of separation
     * @throws Exception
     */
    public ArrayList<String> sortByAvg(boolean positive, int listSize) throws Exception {
        if (biggerList.size() == 0) {
            Graph<String,HashSet<String>> tempG = GraphLibrary.bfs(g, centerOfUniverse);

            // Puts into Map all the actors and their average degree of separation
            for (String s: tempG.vertices()) {
                actorsBySep.put(s, GraphLibrary.averageSeparation(GraphLibrary.bfs(g, s), s));
            }

            // Adds all actors to a list and sorts it based on their average degree of separation
            biggerList = new ArrayList<>(actorsBySep.keySet());
        }

        // Sorts list based on their average degree of separation and boolean positive
        if (positive) {
            biggerList.sort((String s1, String s2) -> (int) Math.signum(actorsBySep.get(s1) - actorsBySep.get(s2)));
        } else {
            biggerList.sort((String s1, String s2) -> (int) Math.signum(actorsBySep.get(s2) - actorsBySep.get(s1)));
        }

        ArrayList<String> shorterList = new ArrayList<>();
        for (int i = 0; i < listSize; i++) {
            shorterList.add(biggerList.get(i));
        }

        return shorterList;
    }

    /**
     * Sorts the actors by their degrees
     * @param low, high; integers representing the lower and upper bounds of degrees
     * @return List of sorted actors
     */
    public ArrayList<String> sortByDegree(int low, int high) {
        ArrayList<String> list = new ArrayList<>();

        for (String s: g.vertices()) {
            if (g.outDegree(s) >= low && g.outDegree(s) <= high) {
                list.add(s);
            }
        }
        list.sort((String s1, String s2) -> g.outDegree(s2) - g.outDegree(s1));

        return list;
    }

    /**
     * List actors sorted by non-infinite separation from the current center
     * @param low, high; integers representing the lower and upper bounds of degrees
     * @return List of sorted actors
     */
    public List<String> sortByFiniteSeparation(Graph<String, HashSet<String>> graph, int low, int high)
            throws Exception {
        Map<String, Integer> actorsToSep = new TreeMap<>(); // Actors --> Separation from center

        // Puts actors:separation into Map if separation within high/low boundaries
        for (String s: graph.vertices()) {
            List<String> tempList = GraphLibrary.getPath(graph, s);
            if (tempList.size()-1 >= low && tempList.size()-1 <= high) {
                actorsToSep.put(s, tempList.size()-1);
            }
        }

        // Uses the Map to create list of actors and sort by separation
        ArrayList<String> list = new ArrayList<>(actorsToSep.keySet());
        list.sort((String s1, String s2) -> (actorsToSep.get(s1) - actorsToSep.get(s2)));

        return list;
    }

    /**
     * Provides set of missing vertices
     * @return Set of missing vertices
     * @throws Exception
     */
    public Set<String> infiniteSeparation() throws Exception {
        return GraphLibrary.missingVertices(g, GraphLibrary.bfs(g, centerOfUniverse));
    }

    /**
     * Resets the center of the universe
     * @param name, actor name of new center
     * @throws Exception
     */
    public void setCenterOfUniverse(String name) throws Exception {
        Graph<String, HashSet<String>> bfsGraph;

        if (!g.hasVertex(name)) {
            throw new Exception("invalid input");
        }
        centerOfUniverse = name;
        bfsGraph = GraphLibrary.bfs(g, centerOfUniverse);

        System.out.println(name
                + " is now the centre of the acting universe, connected to ["
                + (bfsGraph.numVertices()-1)
                + "]/" + g.numVertices() + " with average separation ["
                + GraphLibrary.averageSeparation(bfsGraph, centerOfUniverse) + "]");
    }

    public static void main(String[] args) throws Exception {
        BaconGame bg = new BaconGame("PS4/actors.txt", "PS4/movies.txt", "PS4/movie-actors.txt");
        bg.createGraph();

        System.out.println("Welcome to the Kevin Bacon game!\n");
        System.out.println("Commands:");
        System.out.println("c <#>: list top (positive number) or bottom (negative) <#> centers of the universe, " +
                "sorted by average separation");
        System.out.println("d <low> <high>: list actors sorted by degree, with degree between low and high");
        System.out.println("i: list actors with infinite separation from the current center");
        System.out.println("p <name>: find path from <name> to current center of the universe");
        System.out.println("s <low> <high>: list actors sorted by non-infinite separation from the current center, " +
                "with separation between low and high");
        System.out.println("u <name>: make <name> the center of the universe");
        System.out.println("q: quit game\n");

        bg.setCenterOfUniverse("Kevin Bacon");

        while (true) {
            Scanner in = new Scanner(System.in);
            String[] next = in.nextLine().split(" ");

            // Mode i
            if (next[0].equals("i")) {
                if (next.length != 1) { throw new Exception("invalid use of parameter"); }

                System.out.println("Actors with infinite separation from " + bg.centerOfUniverse + ":");
                System.out.println(bg.infiniteSeparation());

            // Mode p
            } else if (next[0].equals("p")) {
                String name = "";
                for (int i = 1; i < next.length; i++) {
                    name += (next[i] + " ") ;
                }
                name = name.strip();

                Graph<String, HashSet<String>> bfsGraph = GraphLibrary.bfs(bg.g, bg.centerOfUniverse);
                ArrayList<String> list = (ArrayList<String>) GraphLibrary.getPath(bfsGraph, name);

                System.out.println(name + "'s number is " + (list.size() - 1));
                for (int i = list.size()-1; i >= 1; i--) {
                    String act1 = list.get(i);
                    String act2 = list.get(i-1);
                    System.out.println(act1 + " appeared in " + bg.g.getLabel(act1, act2) + " with " + act2);
                }

            // Mode q
            } else if (next[0].equals("q")) {
                break;

            // Mode u
            } else if (next[0].equals("u")) {
                String name = "";
                for (int i = 1; i < next.length; i++) {
                    name += (next[i] + " ") ;
                }
                name = name.strip();
                bg.setCenterOfUniverse(name);

            // Mode d
            } else if (next[0].equals("d")) {
                if (next.length != 3) { throw new Exception("invalid low and high parameters"); }

                int low;
                int high;
                try {
                    low = Integer.parseInt(next[1]);
                    high = Integer.parseInt(next[2]);

                    if (low > high) { throw new Exception("please input valid parameters"); }

                    System.out.println("Actors sorted by degree, between " + low + " and " + high + ":");
                    System.out.println(bg.sortByDegree(low, high));
                } catch (Exception e) {
                    System.err.println("please input valid parameters");
                }

            // Mode s
            } else if (next[0].equals("s")) {
                if (next.length != 3) { throw new Exception("invalid low and high parameters"); }

                int low;
                int high;
                try {
                    low = Integer.parseInt(next[1]);
                    high = Integer.parseInt(next[2]);

                    if (low > high) { throw new Exception("please input valid parameters"); }

                    System.out.println("Actors sorted by non-infinite separation, between "
                            + low + " and " + high + ":");
                    System.out.println(bg.sortByFiniteSeparation
                            (GraphLibrary.bfs(bg.g, bg.centerOfUniverse), low, high));
                } catch (Exception e) {
                    System.err.println("please input valid parameters");
                }

            // Mode c
            } else if (next[0].equals("c")) {
                // Error checking - "c4"
                if (next.length != 2) { throw new Exception("invalid parameters"); }

                int num;
                try {
                    num = Integer.parseInt(next[1]);
                    if (num == 0) {
                        System.out.println("[]");
                    } else if (num > 0) {
                        System.out.println(bg.sortByAvg(true, num));
                    } else {
                        System.out.println(bg.sortByAvg(false, num * -1));
                    }
                } catch (Exception e) {
                    System.err.println("please input valid parameter");
                }

            } else {
                System.out.println("Invalid input; please try again");
            }
        }
    }
}
