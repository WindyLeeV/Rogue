public class Monster extends Player {
    public Monster(Game game) {
        super(game);
    }

    public Site move() {
        Site monster = game.getMonsterSite();
        Site rogue = game.getRogueSite();
        Site shortestMove = null;
        int minDistance = Integer.MAX_VALUE;
        int minManhattanDis = Integer.MAX_VALUE;

        System.out.println("It's the Monster's turn");
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                Site site = new Site(i, j);
                if (dungeon.isLegalMove(monster, site)) {
                    int distance = distanceBFS(site, rogue);
                    int manhattanDis = site.manhattanTo(rogue);
                    /*If there are multiple moving points with the same distance,
                    the one with the smaller Manhattan distance is preferred.*/
                    if (distance < minDistance || (distance == minDistance && manhattanDis < minManhattanDis)) {
                        shortestMove = site;
                        minDistance = distance;
                        minManhattanDis = manhattanDis;
                    }
                }
            }
        }
        return shortestMove;
    }
}

