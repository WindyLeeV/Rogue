import java.util.*;
public class Rogue extends Player {
    boolean canwin = false;
    public Rogue(Game game) {
        super(game);
    }

    public Site move() {
        Site monster = game.getMonsterSite();
        Site rogue = game.getRogueSite();
        Site move;
        List<Site> suitableCorridor = new ArrayList<>();
        List<Site> cangoCorridor = new ArrayList<>();
        List<Site> deadcorridor = new ArrayList<>();

        System.out.println("It's the Rogue's turn");
        System.out.println(canwin);

        if (dungeon.isRoom(rogue)) {
            suitableCorridor = detectCorridor(rogue);
            Site temp = detectCanCorridor(rogue);
            System.out.println(temp);
            cangoCorridor.add(temp);

            if (!suitableCorridor.isEmpty()){
                canwin = false;
            }
        }

        if (!suitableCorridor.isEmpty() && suitableCorridor.get(0) != null) {
            System.out.println("O is "+suitableCorridor.get(0));
            canwin = true;
            System.out.println("In " + suitableCorridor);
            move = goToSuitableCorridor(rogue, suitableCorridor);
            /*List<Site> loop = detectSuitableCorridor(suitableCorridor);
            if (loop.contains(move)) {
                boolean isCorridorLoop = isCorridorLoop(loop);
                if (isCorridorLoop) {
                    move = moveInCorridorLoop(rogue, monster, loop);
                }
            }*/
        }



        // 2. If after DFS, the way along certain corridor is longer than certain value, recognize it as can go corridor.
        else if (!cangoCorridor.isEmpty() && cangoCorridor.get(0) != null){
            move = goToSuitableCorridor(rogue, cangoCorridor);
        }

        else {
            // 1. Avoid dead corner in the corridor
            if (dungeon.isCorridor(rogue)){
                deadcorridor = avoidDeadCorner(rogue);
                // Test successfully!!!
                System.out.println(deadcorridor);
            }
            move = moveAwayFromMonster(rogue, monster, deadcorridor);
        }
        return move;
    }
    protected List<Site> avoidDeadCorner(Site rogue) {
        List<Site> availableNeighbors = findAvailableNeighbors(rogue);
        boolean allCorridors = availableNeighbors.stream().allMatch(dungeon::isCorridor);

        if (allCorridors && canwin && availableNeighbors.size() == 3) {
            return detectDeadCorridor(availableNeighbors);
        }

        return new ArrayList<>(); // Return an empty list if no dead corridors are detected
    }

    private List<Site> findAvailableNeighbors(Site site) {
        List<Site> neighbors = new ArrayList<>();

        // Define directions for movement (8 directions)
        int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1};

        for (int k = 0; k < 8; k++) {
            int newX = site.i() + dx[k];
            int newY = site.j() + dy[k];

            if (isInBounds(newX, newY)) {
                Site neighbor = new Site(newX, newY);
                if (dungeon.isLegalMove(site, neighbor)) {
                    neighbors.add(neighbor);
                }
            }
        }

        return neighbors;
    }

    private List<Site> detectDeadCorridor(List<Site> availableNeighbors) {
        List<Site> deadCorridors = new ArrayList<>();

        for (Site neighbor : availableNeighbors) {
            if (!detectLoop(neighbor, game.getRogueSite()) && !detectRoom(neighbor)) {
                deadCorridors.add(neighbor);
                System.out.println(neighbor);
            }
        }
        return deadCorridors;
    }

    private boolean detectRoom(Site start) {
        // Define directions for the four adjacent sites (up, down, left, right)
        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};

        for (int k = 0; k < 4; k++) {
            int newX = start.i() + dx[k];
            int newY = start.j() + dy[k];

            if (isInBounds(newX, newY)) {
                Site neighbor = new Site(newX, newY);
                if (dungeon.isRoom(neighbor)) {
                    return true;
                }
            }
        }
        return false;
    }
    private Site goToSuitableCorridor(Site rogue, List<Site> corridors) {
        Site move = null;
        int minCorridorDistance = Integer.MAX_VALUE;
        int minManhattanDistance = Integer.MAX_VALUE;
        System.out.println(corridors);
        Site entrance = corridors.get(0);

        for (Site site : corridors) {
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    Site possibleMove = new Site(i, j);
                    if (dungeon.isLegalMove(rogue, possibleMove)) {
                        int currentDis = distanceBFS(possibleMove, entrance);
                        int currentManhattan = possibleMove.manhattanTo(entrance);
                        if (currentDis < minCorridorDistance || (currentDis == minCorridorDistance && currentManhattan < minManhattanDistance)) {
                            move = possibleMove;
                            minCorridorDistance = currentDis;
                            minManhattanDistance = currentManhattan;
                        }
                    }
                }
            }
        }
        return move;
    }

    private Site moveAwayFromMonster(Site rogue, Site monster, List<Site> deadcorridor) {
        Site move = rogue;
        int maxDistance = 0;
        int maxManhattanDis = 0;

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                Site possibleMove = new Site(i, j);

                // Check if the possible move is in the deadcorridor list
                boolean isDeadCorridor = false;
                for (Site sit : deadcorridor) {
                    if (sit.equals(possibleMove)) {
                        isDeadCorridor = true;
                        break; // Found a match, no need to check further
                    }
                }

                if (isDeadCorridor) {
                    System.out.println("Skipping dead corridor: " + possibleMove);
                    continue; // Skip the current iteration
                }

                // Check if the move is legal
                if (dungeon.isLegalMove(rogue, possibleMove)) {
                    int curMonDis = distanceBFS(possibleMove, monster);
                    int curManDis = possibleMove.manhattanTo(monster);

                    // Update the best move based on distances
                    if (curMonDis > maxDistance || (curMonDis == maxDistance && curManDis > maxManhattanDis)) {
                        move = possibleMove;
                        maxDistance = curMonDis;
                        maxManhattanDis = curManDis;
                    }
                }
            }
        }
        return move;
    }

    private Site moveInCorridorLoop(Site rogue, Site monster, List<Site> loop) {
        Site move = rogue;
        int maxDistanceFromMonster = 0;
        int maxManhattanDistanceFromMonster = 0;

        for (Site site : loop) {
            int distanceFromMonster = distanceBFS(site, monster);
            int manhattanDistanceFromMonster = site.manhattanTo(monster);
            if (distanceFromMonster > maxDistanceFromMonster || (distanceFromMonster == maxDistanceFromMonster && manhattanDistanceFromMonster > maxManhattanDistanceFromMonster)) {
                move = site;
                maxDistanceFromMonster = distanceFromMonster;
                maxManhattanDistanceFromMonster = manhattanDistanceFromMonster;
            }
        }
        return move;
    }

    protected Site detectCanCorridor(Site start){
        List<Site> corridors = new ArrayList<>();
        corridors = adjacentCorridor(start);
        corridors = detectMonster(corridors);
        Site longerCorridor = detectLongCorridor(corridors);
        return longerCorridor;
    }
    protected List<Site> detectCorridor(Site start) {
        List<Site> corridors = new ArrayList<>();
        corridors = adjacentCorridor(start);
        corridors = detectMonster(corridors);
        corridors = detectSuitableCorridor(corridors); // 选择是Cycle且离Monster远的Corridor
        if (!corridors.isEmpty())   System.out.println("Suitable:"+corridors);
        return corridors;
    }

    // This function is for find adjacent corridor of a room.
    protected List<Site> adjacentCorridor(Site start){
        List<Site> corridors = new ArrayList<>();
        boolean[][] visited = new boolean[N][N];
        Queue<Site> queue = new LinkedList<>();
        queue.add(start);
        visited[start.i()][start.j()] = true;
        while (!queue.isEmpty()) {
            Site current = queue.poll();
            for (int k = 0; k < 8; k++) {
                int newX = current.i() + DX[k];
                int newY = current.j() + DY[k];
                if (isInBounds(newX, newY) && !visited[newX][newY]) {
                    Site neighbor = new Site(newX, newY);
                    if (dungeon.isLegalMove(current, neighbor) && dungeon.isCorridor(neighbor)) {
                        corridors.add(neighbor);
                    }
                    if (dungeon.isLegalMove(current, neighbor) && dungeon.isRoom(neighbor)) {
                        visited[newX][newY] = true;
                        queue.add(neighbor);
                    }
                }
            }

        }
        return corridors;
    }

    //Detect if can go faster than monster to this loop.
    protected List<Site> detectMonster(List<Site> list) {
        List<Site> goodCorridor = new ArrayList<>();
        for (Site site : list) {
            // The distance between Monster and Corridor
            int monsterDistance = distanceBFS(game.getMonsterSite(), site);
            // The distance between Rogue and Corridor
            int rogueDistance = distanceBFS(game.getRogueSite(), site);
            if (monsterDistance > rogueDistance) {
                goodCorridor.add(site);
            }
        }
        return goodCorridor;
    }

    // For detecting a loop corridor, if a loop is detected, add the loop site to the list
    protected List<Site> detectSuitableCorridor(List<Site> list) {
        List<Site> suitableCorridors = new ArrayList<>();
        Site monsterSite = game.getMonsterSite();
        Site rogueSite = game.getRogueSite();

        for (Site site : list) {
            // Check if the site is in loop
            if (detectLoop(site, rogueSite)) {
                System.out.println("5555555555555555555");
                // Calculate the distance of monster and rogue from the corridor respectively
                int monsterDistance = distanceBFS(monsterSite, site);
                int rogueDistance = distanceBFS(rogueSite, site);
                // Check if the site is safer (farther from the monster than the rogue)
                if (monsterDistance > rogueDistance) {
                    suitableCorridors.add(site);
                }
            }
        }
        return suitableCorridors;
    }

    // For detecting a long corridor, return the longest distance from this corridor could reach.
    // Using BFS to see what is the longest distance it could go.
    protected Site detectLongCorridor(List<Site> list){
        int leastLength = 0;
        Site longerCorridor = null;
        System.out.println(list);
        for (Site site : list) {
            // Check if the site is long enough for traversal;

            if (detectDepth(site) > leastLength) {
                leastLength = detectDepth(site);
                longerCorridor = site;
            }
        }
        System.out.println(leastLength);
        System.out.println(longerCorridor);
        return longerCorridor;
    }

    private boolean isCorridorLoop(List<Site> loop) {
        for (Site site : loop) {
            if (!dungeon.isCorridor(site)) {
                return false;
            }
        }
        return true;
    }

    /*private boolean visited(Set<Site> visit, Site test) {
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
    }*/

}