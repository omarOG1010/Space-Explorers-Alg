package spaceexplorers.core;

import spaceexplorers.publicapi.*;
import spaceexplorers.strategies.*;


import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.*;

public final class SpaceExplorers implements IPlanetLookup {
    private static final int MOVE_TIMEOUT = 1;
    private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;
    public static final int MAX_TURNS = 100;

    private Map<Integer, Planet> planetMap = new HashMap<>();
    private IStrategy player1;
    private IStrategy player2;
    private SpaceExplorersFrame observer;
    private PlanetOperations player1Operations;
    private PlanetOperations player2Operations;
    private boolean player1Turn;
    private boolean gameOver;
    private int turns;

    private boolean async;
    private ExecutorService moveExecutor; // Handles timeouts
    private InternalPlayer winner;

    public SpaceExplorers(IStrategy player1, IStrategy player2, String graph) throws FileNotFoundException {
        this(player1, player2, graph, true);
    }

    public SpaceExplorers(IStrategy player1, IStrategy player2, String graph, boolean async) throws FileNotFoundException {
        this.player1 = player1;
        this.player2 = player2;
        this.player1Operations = new PlanetOperations(this, InternalPlayer.PLAYER1);
        this.player2Operations = new PlanetOperations(this, InternalPlayer.PLAYER2);
        this.player1Turn = true;
        this.gameOver = false;

        this.async = async;
        if (async) {
            this.moveExecutor = Executors.newSingleThreadExecutor();
        }
        this.loadGraph(graph);
    }

    private void loadGraph(String graph) throws FileNotFoundException {
        this.planetMap = SystemLoader.load(graph, this);
    }

    void gameTick() {
        if(turns > MAX_TURNS){
            winner = majorityPopulation();
            this.gameOver = true;
        }
        if (this.gameOver) {
            return;
        }
        turns++;
        // Time passes on each planet
        for (Planet planet : this.planetMap.values()) {
            if(planet.getP1Population() < 0 || planet.getP2Population() < 0){
                System.out.println(turns + ": Before Turn Planet: " + planet.getId() + ", P1: " + planet.getP1Population() + " P2: " + planet.getP2Population());
            }
            planet.grow();
            if(planet.getP1Population() < 0 || planet.getP2Population() < 0){
                System.out.println(turns + "After grow Planet: " + planet.getId() + ", P1: " + planet.getP1Population() + " P2: " + planet.getP2Population());
            }
            planet.processShuttles();
            if(planet.getP1Population() < 0 || planet.getP2Population() < 0){
                System.out.println(turns + "After shuttles Planet: " + planet.getId() + ", P1: " + planet.getP1Population() + " P2: " + planet.getP2Population());
            }
            planet.shrink();
            if(planet.getP1Population() < 0 || planet.getP2Population() < 0){
                System.out.println(turns + "After shrink Planet: " + planet.getId()+ ", P1: " + planet.getP1Population() + " P2: " + planet.getP2Population());
            }
        }
        // Check for win conditions
        if (hasWon(InternalPlayer.PLAYER1) || hasWon(InternalPlayer.PLAYER2)) {
            this.gameOver = true;
            return;
        }
        IStrategy player;
        InternalPlayer playerToken;
        IPlanetOperations operations;
        if (this.player1Turn) {
            player = this.player1;
            playerToken = InternalPlayer.PLAYER1;
            operations = this.player1Operations;
        } else {
            player = this.player2;
            playerToken = InternalPlayer.PLAYER2;
            operations = this.player2Operations;
        }
        Queue<IEvent> eventsToProcess = new ArrayDeque<>();
        List<IPlanet> snapshot = getPlanetsSnapshot(playerToken);
        if (async) {
            // Let the player make their moves
            Future<?> turn = moveExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    player.takeTurn(snapshot, operations, eventsToProcess);
                }
            });
            try {
                turn.get(MOVE_TIMEOUT, TIME_UNIT);
            } catch (InterruptedException | TimeoutException e) {
                // Turn skips, multiple catches in case we want to do something else
                // due to timeouts
            } catch (ExecutionException e) {
                // welp, ok then
            }
        } else {
            try {
                player.takeTurn(snapshot, operations, eventsToProcess);
            } catch (Exception e) {
                // welp, ok then
            }
        }
        // Process the player's moves
        for (IEvent event : eventsToProcess) {
            if (event instanceof Shuttle) {
                Shuttle shuttle = (Shuttle) event;
                if (this.lookupPlanet(shuttle.getSourcePlanetId()).checkAndLaunchShuttle(shuttle)) {
                    this.lookupPlanet(shuttle.getDestinationPlanetId()).addIncomingShuttle(shuttle);
                    if (this.observer != null) {
                        this.observer.notifyNewShuttle(shuttle);
                    }
                }
            }
        }
        this.player1Turn = !this.player1Turn;
    }

    InternalPlayer majorityPopulation(){
        long p1 = 0;
        long p2 = 0;
        for(Planet p : this.planetMap.values()){
            p1+=p.getP1Population();
            p2+=p.getP2Population();
        }
        if(p1 > p2){
            return InternalPlayer.PLAYER1;
        } else if (p1 < p2){
            return InternalPlayer.PLAYER2;
        } else {
            return InternalPlayer.NEUTRAL;
        }
    }

    boolean hasWon(InternalPlayer player) {
        for (Planet planet : this.planetMap.values()) {
            // If the other player owns a planet, the game is still going
            if (planet.getOwnerFromViewer(player) == Owner.OPPONENT) {
                return false;
            }
            // It isn't over until the last shuttle says it's over
            for (IShuttle shuttle : planet.getIncomingIShuttles(player)) {
                if (shuttle.getOwner() == Owner.OPPONENT) {
                    return false;
                }
            }
        }
        if (async) {
            this.moveExecutor.shutdownNow();
        }
        winner = player;
        return true;
    }

    boolean isOver() {
        return gameOver;
    }

    InternalPlayer getWinner() {
        return gameOver ? winner : InternalPlayer.NEUTRAL;
    }

    @Override
    public Planet lookupPlanet(int id) {
        return planetMap.getOrDefault(id, null);
    }

    @Override
    public Collection<Planet> getPlanets() {
        return this.planetMap.values();
    }

    public int getTurns(){
        return turns;
    }

    List<IPlanet> getPlanetsSnapshot(InternalPlayer viewer) {
        // Record which planets are visible
        HashMap<Integer, Planet> visiblePlanets = new HashMap<>();
        for (Planet planet : this.planetMap.values()) {
            Owner owner = planet.getOwnerFromViewer(viewer);
            if (owner == Owner.SELF) {
                visiblePlanets.put(planet.getId(), planet);
                for (IEdge edge : planet.getIEdges()) {
                    visiblePlanets.put(edge.getDestinationPlanetId(), this.lookupPlanet(edge.getDestinationPlanetId()));
                }
            }
        }

        // The remaining planets are not visible
        HashMap<Integer, Planet> nonvisiblePlanets = new HashMap<>();
        for (Planet planet : this.planetMap.values()) {
            if (!visiblePlanets.containsKey(planet.getId())) {
                nonvisiblePlanets.put(planet.getId(), planet);
            }
        }

        // Add VisiblePlanetSnapshots for visible planets
        List<IPlanet> snapshots = new ArrayList<>();
        for (Planet planet : visiblePlanets.values()) {
            snapshots.add(planet.getVisiblePlanetSnapshot(viewer));
        }

        // Add PlanetSnapshots for other planets
        for (Planet planet : nonvisiblePlanets.values()) {
            snapshots.add(planet.getPlanetSnapshot(viewer));
        }

        return snapshots;
    }

    @Override
    public String toString() {
        return planetMap.toString();
    }

    void setPlayer1(IStrategy player) {
        this.player1 = player;
    }

    void setPlayer2(IStrategy player) {
        this.player2 = player;
    }

    void setObserver(SpaceExplorersFrame observer) {
        this.observer = observer;
    }

    /**
     * Runs the simulation to completion.
     *
     * @param maxTicks The maximum number of time-steps to run the simulation for. 0 indicates indefinitely.
     * @return The winning strategy
     */
    public IStrategy runToCompletion(int maxTicks) {
        if (maxTicks == 0) {
            while (!hasWon(InternalPlayer.PLAYER1) && !hasWon(InternalPlayer.PLAYER2) && !isOver()) {
                gameTick();
            }
        } else {
            for (int tick = 0; tick < maxTicks && !hasWon(InternalPlayer.PLAYER1) && !hasWon(InternalPlayer.PLAYER2) && !isOver(); tick++) {
                gameTick();
            }
        }

        if(getTurns() > MAX_TURNS) {
            if (getWinner() == InternalPlayer.PLAYER1) {
                return player1;
            } else if (getWinner() == InternalPlayer.PLAYER2) {
                return player2;
            } else {
                return null;
            }
        }  else {
            if (hasWon(InternalPlayer.PLAYER1)) {
                return player1;
            } else if (hasWon(InternalPlayer.PLAYER2)) {
                return player2;
            } else {
                return null;
            }
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        IStrategy strategy1 = new StudentStrategy();
        IStrategy strategy2 = new StudentStrategy();
        SpaceExplorers spaceExplorers = new SpaceExplorers(strategy1, strategy2, "graph_11");

        int rounds = 0;
        while (!spaceExplorers.hasWon(InternalPlayer.PLAYER1) && !spaceExplorers.hasWon(InternalPlayer.PLAYER2) && !spaceExplorers.isOver()) {
            spaceExplorers.gameTick();
            rounds++;
        }

        String winner;

        if(spaceExplorers.getTurns() > spaceExplorers.MAX_TURNS) {
            winner = (spaceExplorers.getWinner() == InternalPlayer.PLAYER1) ? "Player One" : "Player Two";
        } else {
            winner = spaceExplorers.hasWon(InternalPlayer.PLAYER1) ? "Player One" : "Player Two";
        }

        System.out.print(String.format("%s has won in %d rounds!", winner, rounds));
    }
}
