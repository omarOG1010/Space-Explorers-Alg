package spaceexplorers.strategies;


import java.lang.Math;
import java.util.*;

import spaceexplorers.publicapi.IEdge;
import spaceexplorers.publicapi.IEvent;
import spaceexplorers.publicapi.IPlanet;
import spaceexplorers.publicapi.IPlanetOperations;
import spaceexplorers.publicapi.IStrategy;
import spaceexplorers.publicapi.IVisiblePlanet;
import spaceexplorers.publicapi.Owner;



public class StudentStrategy implements IStrategy {

//    Strategy summary:
//    First, the current planet instantly sends 50% of its population to the closest planets then
//    it sends 30% of its population to the farther planets (based on edges length). The strategy does
//    this until there are no more neutral planets in the whole game. Once there are no neutral planets,
//    the owned planets send to the most habitable planet 20% of their population. Then basically until the end of the game
//    the owned planets send 10% of their population to the most habitable planet and another 10% to
//    the enemy planets. ( more detailed explanation in the comments of the actual java file)
    // three data types are dictionaries through hashmaps, linked lists, and stacks.

    public void takeTurn(List<IPlanet> planets, IPlanetOperations planetOperations, Queue<IEvent> eventsToExecute) {
        Map<Integer, IVisiblePlanet> owned = new HashMap();
        Map<Integer, IVisiblePlanet> neutrals = new HashMap(); // HashMaps to traverse the graph and identify the type of planet which will be needed in this implementation
        Map<Integer, IVisiblePlanet> enemies = new HashMap();

        Iterator PlanetIter = planets.iterator(); // iterator over the planets
        IVisiblePlanet CurrPlanet; // this section places the planet and its id in the Hashmaps previously declared
        while (PlanetIter.hasNext()) {
            IPlanet planet = (IPlanet) PlanetIter.next();
            if (planet instanceof IVisiblePlanet) {
                CurrPlanet = (IVisiblePlanet) planet;
                if (CurrPlanet.getOwner() == Owner.SELF) {
                    owned.put(CurrPlanet.getId(), CurrPlanet);
                } else if (CurrPlanet.getOwner() == Owner.NEUTRAL) {
                    neutrals.put(CurrPlanet.getId(), CurrPlanet);
                } else {
                    enemies.put(CurrPlanet.getId(), CurrPlanet);
                }
            }
        }

        Set edges;
        boolean done;
        Iterator edgesIter;
        IEdge edge;
        int targetId;
        Integer id;

        if (neutrals.size() > 0) { // when there are neutrals in the entire graph run this block of code
            PlanetIter = owned.keySet().iterator(); // iterator over planets
            while (true) {
                while (true) {
                    LinkedList<IEdge> edgesList; // linked list of the edges of the planet, I have a Set to Linked List helper function and another one that sorts the edges based on length

                    id = (Integer) PlanetIter.next();
                    CurrPlanet = owned.get(id);
                    edges = CurrPlanet.getEdges();
                    edgesList = SettoLinkedList(edges); // sets the set to a linked list
                    getShortest(edgesList);// sorts the linked list based on the linked for the edges
                    double firstHalf = Math.ceil(edgesList.size() / 2);// gets the middle ish number in order to split the for function in two as below

                    for (int i = 0; i < firstHalf; i++) { // for the closest edges aka planets connected
                        edge =  edgesList.get(i);
                        targetId = edge.getDestinationPlanetId();
                        if (neutrals.containsKey(targetId) && neutrals.get(targetId).getTotalPopulation() != neutrals.get(targetId).getSize()) { //send 50% of current population if the neutral planet isn't full
                            eventsToExecute.add(planetOperations.transferPeople(CurrPlanet, neutrals.get(targetId), (long) ((double) CurrPlanet.getTotalPopulation() * 0.5D )));
                        }
                    }
                    for (int i = (int) firstHalf; i < edgesList.size(); i++) { // for the farthest edges aka planets connected
                        edge = edgesList.get(i);
                        targetId = edge.getDestinationPlanetId();
                        if (neutrals.containsKey(targetId)  && neutrals.get(targetId).getTotalPopulation() != neutrals.get(targetId).getSize()) { //send 50% of current population if the neutral planet isn't full
                            eventsToExecute.add(planetOperations.transferPeople(CurrPlanet, neutrals.get(targetId), (long) ((double) CurrPlanet.getTotalPopulation() * 0.3D)));
                        }
                    }
                }
            }
        }else{ // run this block of code if there are no more neutral planets
            PlanetIter = owned.keySet().iterator(); // new iterator over owned planets
            while(true) {
                do {
                    if (!PlanetIter.hasNext()) {
                        return;
                    } // this do while is necessary in order to constantly loop through the second part of this block of code.
                    id = (Integer) PlanetIter.next();
                    CurrPlanet =  owned.get(id);
                    edges = CurrPlanet.getEdges();
                    done = false;
                    edgesIter = edges.iterator(); // iterates over edges in order to add them into the stack

                    while (edgesIter.hasNext()) { // while edgesIter has next and next planet is owned and current planet has less habitability than the next planet and while next planet total population is less than half and while current planet P1 population is less than current planet's P2 population by 30 percent send 20 percent of current planet's population
                        edge = (IEdge) edgesIter.next();
                        targetId = edge.getDestinationPlanetId();
                        if (owned.containsKey(targetId) && (CurrPlanet.getHabitability() < owned.get(targetId).getHabitability()) && owned.get(targetId).getTotalPopulation() < owned.get(targetId).getSize() - owned.get(targetId).getSize() * 0.5 && CurrPlanet.getP1Population()<CurrPlanet.getP2Population()-CurrPlanet.getP2Population()*0.7) {
                            eventsToExecute.add(planetOperations.transferPeople(CurrPlanet, owned.get(targetId), (long) ((double) CurrPlanet.getTotalPopulation() * 0.2D)));
                        }
                    }

                } while (done);
                edgesIter = edges.iterator(); // another edges iterator
                Stack<Long> Mypop = new Stack<>();// stack that keeps track of the current P2 planet's population
                Mypop.add(CurrPlanet.getP2Population());

                while (edgesIter.hasNext()) {
                    edge = (IEdge) edgesIter.next();
                    targetId = edge.getDestinationPlanetId(); // if next planet is an enemy and current planet P2 population more than 6 and enemies don't have the total size of the planet and if current planet's player 1 population is less than half of P2's send 10 percent of current population
                    if (enemies.containsKey(targetId) && CurrPlanet.getP2Population() > 6 && enemies.get(targetId).getTotalPopulation()!=enemies.get(targetId).getSize() && CurrPlanet.getP1Population()<Mypop.peek()-Mypop.peek()*0.5) {
                        eventsToExecute.add(planetOperations.transferPeople(CurrPlanet, enemies.get(targetId), (long) ((double) CurrPlanet.getTotalPopulation() * 0.1D)));
                    }
                }
                while (edgesIter.hasNext()) {
                    edge = (IEdge) edgesIter.next();
                    targetId = edge.getDestinationPlanetId(); // if next planet is owned and current planet has less habitability and next planet's total population is less than the half of the max and if current planet's P1 population is less than current planet's population by 60 % then send 10 percent of current population
                    if (owned.containsKey(targetId) && (CurrPlanet.getHabitability() < owned.get(targetId).getHabitability()) && owned.get(targetId).getTotalPopulation() < owned.get(targetId).getSize() - owned.get(targetId).getSize() * 0.5 && CurrPlanet.getP1Population()<Mypop.peek()-Mypop.peek()*0.4) {
                        eventsToExecute.add(planetOperations.transferPeople(CurrPlanet, owned.get(targetId), (long) ((double) CurrPlanet.getTotalPopulation() * 0.1D)));
                    }
                }
            }
        }
    }


    public String getName () {
        return "Omar";
    }


    public boolean compete () {
        return true;
    }

    public void getShortest (LinkedList<IEdge> other) { // sorts the linked list in terms of the edge's length
        for (int i = 0; i < other.size() ; i++) {
            int min = i;
            for (int j = i+1; j  < other.size(); j++){
                if(other.get(j).getLength()<other.get(min).getLength()) {
                    min = j;
                }
                IEdge former = other.get(min);
                other.set(min,other.get(i));
                other.set(i,former);
            }
        }
    }

    public LinkedList<IEdge> SettoLinkedList(Set other){ // converts sets into linked lists
        LinkedList<IEdge> list = new LinkedList();
        Iterator<IEdge> EdgesIter = other.iterator();
        while(EdgesIter.hasNext()){
            list.add(EdgesIter.next());
        }
        return list;
    }





}

