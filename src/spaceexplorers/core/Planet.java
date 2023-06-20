// Updated 4/9/2022 by Maranda Donaldson, donal163

package spaceexplorers.core;

import spaceexplorers.publicapi.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class Planet {
    private int id;
    private Set<Edge> edges;
    private int habitability;
    private long size;
    private long totalPopulation;
    private long p1Population;
    private long p2Population;
    private InternalPlayer owningPlayer;
    private boolean isHomeworld;
    private List<Shuttle> incomingShuttles;
    private Point2D location;

    private IPlanetLookup planetLookup;

    public Planet(int id, int habitability, long size, InternalPlayer owningPlayer, boolean isHomeworld, IPlanetLookup planetLookup) {
        assert owningPlayer != null;

        this.id = id;
        this.habitability = habitability;
        this.size = size;
        this.owningPlayer = owningPlayer;
        this.isHomeworld = isHomeworld;

        this.totalPopulation = 0;
        this.p1Population = 0;
        this.p2Population = 0;

        this.incomingShuttles = new ArrayList<>();
        this.edges = new HashSet<>();
        this.planetLookup = planetLookup;
    }

    public IPlanet getPlanetSnapshot(InternalPlayer viewer) {
        return new PlanetSnapshot(this.id, this.getIEdges());
    }

    public IVisiblePlanet getVisiblePlanetSnapshot(InternalPlayer viewer) {
        return new VisiblePlanetSnapshot(
                this.id,
                this.getIEdges(),
                this.habitability,
                this.size,
                this.totalPopulation,
                this.p1Population,
                this.p2Population,
                this.getOwnerFromViewer(viewer),
                this.isHomeworld,
                this.getIncomingIShuttles(viewer)
        );
    }

    public Owner getOwnerFromViewer(InternalPlayer viewer) {
        assert viewer != InternalPlayer.NEUTRAL;
        assert viewer != null;
        if (this.owningPlayer == InternalPlayer.NEUTRAL) {
            return Owner.NEUTRAL;
        } else if (viewer == this.owningPlayer) {
            return Owner.SELF;
        } else {
            return Owner.OPPONENT;
        }
    }

    public InternalPlayer getOwningPlayer() {
        return owningPlayer;
    }

    public Set<IEdge> getIEdges() {
        Set<IEdge> iedges = new HashSet<>(this.edges.size());
        iedges.addAll(this.edges);
        return iedges;
    }

    public Set<Planet> getNeighboringPlanets() {
        Set<Planet> neighbors = new HashSet<>();

        for (Edge edge : this.edges) {
            int neighborId = edge.getDestinationPlanetId();
            Planet neighbor = this.planetLookup.lookupPlanet(neighborId);
            neighbors.add(neighbor);
        }

        return neighbors;
    }

    /**
     * Checks that a shuttle represents a valid move. If it does, depart the shuttle from this planet and return true.
     *
     * @return True if the shuttle has successfully left the planet
     */
    public boolean checkAndLaunchShuttle(Shuttle shuttle) {
        // Check that the player owning the planet is making the transaction
        // NO LONGER NECESSARY
//        if (shuttle.getOwningPlayer() != this.owningPlayer) {
//            return false;
//        }

        // Check that enough player population exists to support the transaction, and that the shuttle totalPopulation is positive
        if ((shuttle.getOwningPlayer() == InternalPlayer.PLAYER1 && shuttle.getNumberPeople() > this.p1Population)
                || (shuttle.getOwningPlayer() == InternalPlayer.PLAYER2 && shuttle.getNumberPeople() > this.p2Population)
                || shuttle.getNumberPeople() <= 0) {
            return false;
        }

        // Check that the destination planet isn't the same as the current one
        if (shuttle.getDestinationPlanetId() == shuttle.getSourcePlanetId()) {
            return false;
        }

        // Check that an edge exists
        Set<Edge> sourceEdges = planetLookup.lookupPlanet(shuttle.getSourcePlanetId()).edges;
        boolean found = false;
        for (Edge edge : sourceEdges) {
            if (edge.getDestinationPlanetId() == shuttle.getDestinationPlanetId()) {
                found = true;
            }
        }
        if (!found) {
            return false;
        }

        // update total and correct population
        if(shuttle.getOwningPlayer() == InternalPlayer.PLAYER1){
            if(p1Population < shuttle.getNumberPeople()){
                return false;
            }
            this.p1Population -= shuttle.getNumberPeople();
            this.totalPopulation -= shuttle.getNumberPeople();
        } else {
            if(p2Population < shuttle.getNumberPeople()){
                return false;
            }
            this.p2Population -= shuttle.getNumberPeople();
            this.totalPopulation -= shuttle.getNumberPeople();
        }

        // update planet owner
        if (totalPopulation == 0 || p1Population == p2Population) {
            this.owningPlayer = InternalPlayer.NEUTRAL;
        } else if (p1Population > p2Population) {
            this.owningPlayer = InternalPlayer.PLAYER1;
        } else {
            this.owningPlayer = InternalPlayer.PLAYER2;
        }

        return true;
    }

    public void addIncomingShuttle(Shuttle shuttle) {
        this.incomingShuttles.add(shuttle);
    }

    public List<IShuttle> getIncomingIShuttles(InternalPlayer viewer) {
        List<IShuttle> incomingIShuttles = new ArrayList<>(this.incomingShuttles.size());
        for (Shuttle shuttle : this.incomingShuttles) {
            incomingIShuttles.add(shuttle.getShuttleSnapshot(viewer));
        }
        return incomingIShuttles;
    }

    public void addEdge(Planet neighbor, int distance) {
        this.edges.add(new Edge(this.id, neighbor.id, distance));
        neighbor.edges.add(new Edge(neighbor.id, this.id, distance));
    }

    public int getId() {
        return id;
    }

    public long getTotalPopulation() {
        return totalPopulation;
    }

    public void setTotalPopulation(long totalPopulation) {
        this.totalPopulation = totalPopulation;
    }

    public long getP1Population() {
        return p1Population;
    }

    public void setP1Population(long p1Population) {
        this.p1Population = p1Population;
    }

    public long getP2Population() {
        return p2Population;
    }

    public void setP2Population(long p2Population) {
        this.p2Population = p2Population;
    }

    public Point2D getLocation() {
        return location;
    }

    public void setLocation(Point2D location) {
        this.location = location;
    }

    public void grow() {
        if (this.totalPopulation > size) {
            return;
        }

        double populationScaleFactor = 1. + (this.habitability / 100.);
        long popIncrease = (long) Math.min(this.size, Math.ceil(this.totalPopulation * populationScaleFactor));
        popIncrease -= this.totalPopulation; // determines how much total population will increase

        // split population increase proportionally between pop1 and pop2
        long p1fraction = (long) Math.floor(popIncrease * (p1Population / (double) totalPopulation));
        long p2fraction = (long) Math.floor(popIncrease * (p2Population / (double) totalPopulation));

        this.totalPopulation += popIncrease;
        this.p1Population += p1fraction;
        this.p2Population += p2fraction;

        if (p1fraction+p2fraction < popIncrease && p1Population >= p2Population){ // cause rounding
            p1Population += popIncrease - (p1fraction+p2fraction);
        } else if (p1fraction+p2fraction < popIncrease && p1Population < p2Population) {
            p2Population += popIncrease - (p1fraction+p2fraction);
        }

        // reassign ownership
        if(p1Population > p2Population){
            this.owningPlayer = InternalPlayer.PLAYER1;
        } else if (p1Population < p2Population) {
            this.owningPlayer = InternalPlayer.PLAYER2;
        } else {
            this.owningPlayer = InternalPlayer.NEUTRAL;
        }
    }

    public void processShuttles() {
        if (this.owningPlayer == InternalPlayer.NEUTRAL) {
            // If the planet is currently neutral, the person who is landing more troops gets it,
            // with the totalPopulation being the difference in arriving people. Neither player gets a
            // defending bonus.
            List <Shuttle> player1ShuttlesArrived = new ArrayList<>();
            List <Shuttle> player2ShuttlesArrived = new ArrayList<>();

            // Split shuttles based on owning player
            for (Shuttle shuttle : this.incomingShuttles) {
                shuttle.moveCloser();
                if (shuttle.getTurnsToArrival() == 0) {
                    if (shuttle.getOwningPlayer() == InternalPlayer.PLAYER1) {
                        player1ShuttlesArrived.add(shuttle);
                    } else {
                        player2ShuttlesArrived.add(shuttle);
                    }
                }
            }

            // Total up totalPopulation for each player
            int player1Pop = 0;
            int player2Pop = 0;
            for (Shuttle shuttle : player1ShuttlesArrived) {
                player1Pop += shuttle.getNumberPeople();
            }
            for (Shuttle shuttle : player2ShuttlesArrived) {
                player2Pop += shuttle.getNumberPeople();
            }

            //System.out.println(player1Pop + " " + player2Pop);
            // if planet is at capacity and neutral, no one can land so all populations stay the same
            if(this.totalPopulation < this.size){
                this.totalPopulation = Math.min(this.size, this.totalPopulation + (player1Pop + player2Pop));
                if (player1Pop > player2Pop) {
                    this.p1Population += player1Pop;
                    long temp = this.size - player1Pop;
                    if(temp < 0) temp = 0;
                    this.p2Population += Math.min(player2Pop, temp);
                    this.owningPlayer = InternalPlayer.PLAYER1;
                } else if (player1Pop < player2Pop) {
                    this.p2Population += player2Pop;
                    long temp = this.size - player2Pop;
                    if(temp < 0) temp = 0;
                    this.p1Population += Math.min(player1Pop, temp);
                    this.owningPlayer = InternalPlayer.PLAYER2;
                } else {
                    if(this.totalPopulation == this.size) {
                        this.p1Population = this.size / 2;
                        this.p2Population = this.size / 2;
                        this.totalPopulation = this.p1Population + this.p2Population;
                    } else {
                        this.p1Population += player1Pop;
                        this.p2Population += player2Pop;
                    }
                    assert this.owningPlayer == InternalPlayer.NEUTRAL;
                }
            }

            // Assign ownership to the person with more people landing

            this.incomingShuttles.removeAll(player1ShuttlesArrived);
            this.incomingShuttles.removeAll(player2ShuttlesArrived);
        } else {
            // Someone owns the planet; we'll land all friendly shuttles first, and then all
            // hostile shuttles. The owning player gets a defensive bonus.
            List<Shuttle> friendlyShuttlesArrived = new ArrayList<>();
            List<Shuttle> hostileShuttlesArrived = new ArrayList<>();

            // Pull out the friendly and hostile shuttles
            for (Shuttle shuttle : this.incomingShuttles) {
                shuttle.moveCloser();
                if (shuttle.getTurnsToArrival() == 0) {
                    if (shuttle.getOwningPlayer() == this.owningPlayer) {
                        friendlyShuttlesArrived.add(shuttle);
                    } else {
                        hostileShuttlesArrived.add(shuttle);
                    }
                }
            }

            // Land all friendly shuttles
            for (Shuttle shuttle : friendlyShuttlesArrived) {
                if (shuttle.getOwningPlayer() == this.owningPlayer) {
                    this.totalPopulation += shuttle.getNumberPeople();
                    if (shuttle.getOwningPlayer() == InternalPlayer.PLAYER1) {
                        this.p1Population += shuttle.getNumberPeople();
                    } else {
                        this.p2Population += shuttle.getNumberPeople();
                    }
                }
            }

            if (hostileShuttlesArrived.size() > 0) {
                long otherPopulation = 0;
                InternalPlayer otherPlayer = hostileShuttlesArrived.get(0).getOwningPlayer();

                // Calculate total number from minority
                for (Shuttle shuttle : hostileShuttlesArrived) {
                    otherPopulation += shuttle.getNumberPeople();
                }

                // add max possible population for other population
                if(this.owningPlayer == InternalPlayer.PLAYER1){
                    long maxPossible = this.size - this.p1Population;
                    if(maxPossible < 0) maxPossible = 0;
                    this.p2Population += Math.min(maxPossible, otherPopulation);
                    this.totalPopulation += Math.min(maxPossible, otherPopulation);
                } else {
                    long maxPossible = this.size - this.p2Population;
                    if(maxPossible < 0) maxPossible = 0;
                    this.p1Population += Math.min(maxPossible, otherPopulation);
                    this.totalPopulation += Math.min(maxPossible, otherPopulation);
                }
                // OLD CODE
                // The defending player gets a bonus
//                long effectivePopulation = (long) (1.1 * this.totalPopulation);
//                if (effectivePopulation < otherPopulation) {
//                    this.owningPlayer = otherPlayer;
//                    this.totalPopulation = otherPopulation - effectivePopulation;
//                } else if (effectivePopulation == otherPopulation) {
//                    this.owningPlayer = InternalPlayer.NEUTRAL;
//                    this.totalPopulation = 0;
//                } else {
//                    this.totalPopulation = Math.min(this.totalPopulation, effectivePopulation - otherPopulation);
//                }
            }

            this.incomingShuttles.removeAll(friendlyShuttlesArrived);
            this.incomingShuttles.removeAll(hostileShuttlesArrived);
        }
        assert this.totalPopulation >= 0;
    }

    public void shrink() {
        if (this.totalPopulation <= this.size) {
            return;
        }

        long difference = this.totalPopulation - this.size;
        long killedOff = (long) Math.ceil(difference * 0.1);

        if (this.owningPlayer == InternalPlayer.PLAYER1) {
            if(killedOff < p1Population){
                this.p1Population -= killedOff;
                this.totalPopulation -= killedOff;
            } else {
                killedOff = p1Population;
                p1Population -= killedOff;
                this.totalPopulation -= killedOff;
            }
        } else {
            if(killedOff < p2Population){
                this.p2Population -= killedOff;
                this.totalPopulation -= killedOff;
            } else {
                killedOff = p2Population;
                this.p2Population -= killedOff;
                this.totalPopulation -= killedOff;
            }
        }

        // reassign owner
        if(p1Population > p2Population){
            this.owningPlayer = InternalPlayer.PLAYER1;
        } else if (p2Population > p1Population) {
            this.owningPlayer = InternalPlayer.PLAYER2;
        } else {
            this.owningPlayer = InternalPlayer.NEUTRAL;
        }

        assert this.totalPopulation >= this.size;
    }

    @Override
    public String toString() {
        return String.format("{Planet: %d, Owner: %s, Pop: %d}", this.id, this.owningPlayer, this.totalPopulation);
    }
}
