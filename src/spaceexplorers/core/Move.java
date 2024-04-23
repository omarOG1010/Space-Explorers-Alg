package spaceexplorers.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a move made by a shuttle from one planet to another.
 */
final class Move {

    private Shuttle shuttle; // The shuttle making the move
    private int time; // The time at which the move is made
    private InternalPlayer who; // The player making the move
    private List<Point2D> path; // The path taken by the shuttle

    private Planet src; // The source planet
    private Planet target; // The target planet

    /**
     * Constructs a move from a source planet to a target planet by a shuttle at a specified time.
     *
     * @param src        The source planet.
     * @param target     The target planet.
     * @param shuttle    The shuttle making the move.
     * @param time       The time at which the move is made.
     * @param pathFinder The path finder used to determine the path between source and target planets.
     */
    public Move(Planet src, Planet target, Shuttle shuttle, int time, PathFinder pathFinder) {
        this.shuttle = shuttle;
        this.time = time;
        this.who = shuttle.getOwningPlayer();

        this.src = src;
        this.target = target;

        // Find the path from source to target planet using the given path finder
        this.path = new ArrayList<>(pathFinder.findPath(src, target, time, false));
    }

    /**
     * Gets the target planet of the move.
     *
     * @return The target planet.
     */
    public Planet getTarget() {
        return target;
    }

    /**
     * Gets the player who made the move.
     *
     * @return The player who made the move.
     */
    public InternalPlayer getMoveMaker() {
        return who;
    }

    /**
     * Gets the number of people transported by the shuttle.
     *
     * @return The number of people transported.
     */
    public long getNumPeople() {
        return shuttle.getNumberPeople();
    }

    /**
     * Checks if the shuttle has reached its target planet.
     *
     * @return True if the shuttle has reached its target, false otherwise.
     */
    public boolean hasReached() {
        return path.isEmpty();
    }

    /**
     * Retrieves the next position of the shuttle along its path.
     *
     * @return The next position of the shuttle as a Point2D object, or null if the shuttle has reached its target.
     */
    public Point2D nextPos() {
        if (hasReached()) {
            return null;
        } else {
            return path.remove(0);
        }
    }
}
