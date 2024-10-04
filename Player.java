import java.util.*;

public abstract class Player {
    protected Game game;
    protected Dungeon dungeon;
    protected int N;

    // Define directions for movement (8 directions)
    protected static final int[] DX = {-1, -1, -1, 0, 0, 1, 1, 1};
    protected static final int[] DY = {-1, 0, 1, -1, 1, -1, 0, 1};

    public Player(Game game) {
        this.game = game;
        this.dungeon = game.getDungeon();
        this.N = dungeon.size();
    }

    // Method to check if the site is within the bounds of the dungeon
    protected boolean isInBounds(int x, int y) {
        return x >= 0 && x < N && y >= 0 && y < N;
    }

    /**
     * @param begin
     * @param end
     * @return The shortest available path's distance between begin and end.
     */
    // BFS to find the shortest path distance between two sites
    protected int distanceBFS(Site begin, Site end) {
        boolean[][] visited = new boolean[N][N];
        Queue<Site> queue = new LinkedList<>();
        queue.offer(begin);
        visited[begin.i()][begin.j()] = true;
        int currentDistance = 0;

        while (!queue.isEmpty()) {
            int levelSize = queue.size();

            for (int i = 0; i < levelSize; i++) {
                Site current = queue.poll();
                if (current.equals(end)) {
                    return currentDistance;
                }
                for (int k = 0; k < DX.length; k++) {
                    int newX = current.i() + DX[k];
                    int newY = current.j() + DY[k];
                    if (isInBounds(newX, newY) && !visited[newX][newY] && dungeon.isLegalMove(current, new Site(newX, newY))) {
                        visited[newX][newY] = true;
                        queue.offer(new Site(newX, newY));
                    }
                }
            }
            currentDistance++;
        }
        return currentDistance;
    }
    //对于环的定义：相邻的走廊和房间的连接。考虑以下三种情况：
    // 1.只有走廊的环；2.走廊和同一个房间相接而成的环；3.多个走廊与不同房间连通形成的环（连通性？）。
    /*protected boolean detectLoop(Site start) {
        Set<Site> visited = new HashSet<>();
        Stack<Site> stack = new Stack<>();
        Map<Site, Site> parent = new HashMap<>();

        stack.push(start);
        parent.put(start, null);
            while (!stack.isEmpty()) {
                Site current = stack.pop();
                visited.add(current);
                // 获取当前节点的合法邻接点列表
                List<Site> validNeighbors = exploreNeighbors(current, visited);
                for (Site neighbor : validNeighbors) {
                    if ((dungeon.isCorridor(neighbor) || dungeon.isRoom(neighbor))) {
                        if (!visited.contains(neighbor)) {
                            stack.push(neighbor);
                            parent.put(neighbor, current);
                        }
                        if (visited.contains(neighbor) && !neighbor.equals(parent.get(current))) {
                            return true;
                        }
                    }
                }
            }
        System.out.println("77777777777777777777777");
        return false;
    }

    private List<Site> exploreNeighbors(Site current, Set<Site> visited) {
        List<Site> validNeighbors = new ArrayList<>();

        for (int k = 0; k < DX.length; k++) {
            int newX = current.i() + DX[k];
            int newY = current.j() + DY[k];
            // Check if the new node is within the map range and has not been visited yet
            if (isInBounds(newX, newY) && !visited.contains(new Site(newX, newY))) {
                // If it is a legal move, add the adjacent point to the list
                if (dungeon.isLegalMove(current, new Site(newX, newY))) {
                    validNeighbors.add(new Site(newX, newY));
                }
            }
        }
        return validNeighbors;
    }*/

    protected boolean detectLoop(Site start, Site rogue) {
        Set<Site> visited = new HashSet<>(); // Set to track visited sites
        Stack<Site> stack = new Stack<>();   // Stack for DFS traversal
        Map<Site, Site> parent = new HashMap<>(); // Need this map to make the child does not visit its parent.

        stack.push(start);
        parent.put(start, null); // Set parent of start site as null

        while (!stack.isEmpty()) {
            Site current = stack.pop();   // Pop the top site from the stack
            // Mark the current site as visited
            visited.add(current);
            // In the first point, we can only go into the corridor, cannot be out.
            if (current.equals(start)){
                for (int k = 0; k < 8; k++) {
                    int newX = current.i() + DX[k];
                    int newY = current.j() + DY[k];

                    if (isInBounds(newX, newY)) {
                        Site neighbor = new Site(newX, newY);

                        // Check if the move is legal, and make sure they are corridors.
                        if (dungeon.isLegalMove(current, neighbor) && dungeon.isCorridor(neighbor) && !neighbor.equals(rogue)) {
                            stack.push(neighbor);
                            parent.put(neighbor, current); // Update parent information
                        }
                    }
                }
            }
            else{
                // Explore the neighbors of the current site
                for (int k = 0; k < 8; k++) {
                    int newX = current.i() + DX[k];
                    int newY = current.j() + DY[k];

                    if (isInBounds(newX, newY)) {
                        Site neighbor = new Site(newX, newY);
                        // Check if the move is legal
                        if (dungeon.isLegalMove(current, neighbor)) {
                            // If the neighbor has been visited and is not the parent, a loop is detected
                            Site parent1 = parent.get(current);
                            boolean ifNeighborBack = neighbor.equals(parent1);
                            // It is useless to detect a loop in the room, so the end point must be in corridor.
                            if (visited(visited, neighbor) && !ifNeighborBack && dungeon.isCorridor(neighbor)) {
                                return true; // Loop detected
                            }
                            // If the neighbor hasn't been visited yet, push it to the stack
                            if (!visited(visited, neighbor)) {
                                stack.push(neighbor);
                                parent.put(neighbor, current); // Update parent information
                            }
                        }
                    }
                }
            }
        }
        // No loop detected
        return false;
    }

    // Given a Site start, detect the farest distance it could go using BFS.
    // Return the distance.
    protected int detectDepth(Site start) {
        boolean[][] visited = new boolean[N][N];
        Queue<Site> queue = new LinkedList<>();
        queue.offer(start);
        visited[start.i()][start.j()] = true;
        int maxDepth = 0;

        while (!queue.isEmpty()) {
            int levelSize = queue.size();
            maxDepth++;

            for (int i = 0; i < levelSize; i++) {
                Site current = queue.poll();
                if (current == start){
                    System.out.println("Bug test1");
                    for (int k = 0; k < DX.length; k++) {
                        int newX = current.i() + DX[k];
                        int newY = current.j() + DY[k];
                        if (isInBounds(newX, newY) && !visited[newX][newY] && dungeon.isLegalMove(current, new Site(newX, newY)) && dungeon.isCorridor(new Site(newX, newY))) {
                            visited[newX][newY] = true;
                            queue.offer(new Site(newX, newY));
                            System.out.println("Bug test2");
                        }
                    }
                }
                else{
                    for (int k = 0; k < DX.length; k++) {
                        int newX = current.i() + DX[k];
                        int newY = current.j() + DY[k];
                        if (isInBounds(newX, newY) && !visited[newX][newY] && dungeon.isLegalMove(current, new Site(newX, newY))) {
                            visited[newX][newY] = true;
                            queue.offer(new Site(newX, newY));
                        }
                    }
                }
            }
        }
        return maxDepth;
    }
    private boolean visited(Set<Site> visit, Site test) {
        int m = test.i();
        int s = test.j();
        if (visit.isEmpty()) {
            return true;
        }
        for (Site i : visit) {
            int mi = i.i();
            int si = i.j();
            if (m == mi && s == si) return true;
        }
        return false;
    }
}