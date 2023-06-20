package spaceexplorers.core;

import spaceexplorers.publicapi.IEdge;
import spaceexplorers.publicapi.IShuttle;
import spaceexplorers.publicapi.IVisiblePlanet;
import spaceexplorers.publicapi.Owner;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Class representing the state of a visible planet at a single moment; the data members will not be updated as the state of the planet changes.
 * <p>
 * This is a distinct class from {@link PlanetSnapshot} so that students cannot cast between them to get more information.
 */
final class VisiblePlanetSnapshot implements IVisiblePlanet {
    private int id;
    private Set<IEdge> edges;
    private int habitability;
    private long size;
    private long totalPopulation;
    private long p1Population;
    private long p2Population;
    private Owner owner;
    private boolean isHomeworld;
    private List<IShuttle> incomingShuttles;

    public VisiblePlanetSnapshot(int id, Set<IEdge> edges, int habitability, long size, long totalPopulation, long p1Population, long p2Population, Owner owner, boolean isHomeworld, List<IShuttle> incomingShuttles) {
        this.id = id;
        this.edges = edges;
        this.habitability = habitability;
        this.size = size;
        this.totalPopulation = totalPopulation;
        this.p1Population = p1Population;
        this.p2Population = p2Population;
        this.owner = owner;
        this.isHomeworld = isHomeworld;
        this.incomingShuttles = incomingShuttles;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public Set<IEdge> getEdges() {
        // TODO: Return a copy here so they can't modify the set
        return edges;
    }

    @Override
    public int getHabitability() {
        return habitability;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public long getTotalPopulation() {
        return totalPopulation;
    }

    @Override
    public long getP1Population() {
        return p1Population;
    }

    @Override
    public long getP2Population() {
        return p2Population;
    }

    @Override
    public Owner getOwner() {
        return owner;
    }

    @Override
    public boolean isHomeworld() {
        return isHomeworld;
    }

    @Override
    public List<IShuttle> getIncomingShuttles() {
        return new ArrayList<>(this.incomingShuttles);
    }
}
