package spaceexplorers.strategies;



import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;
import java.util.Set;
import spaceexplorers.publicapi.IEdge;
import spaceexplorers.publicapi.IEvent;
import spaceexplorers.publicapi.IPlanet;
import spaceexplorers.publicapi.IPlanetOperations;
import spaceexplorers.publicapi.IStrategy;
import spaceexplorers.publicapi.IVisiblePlanet;
import spaceexplorers.publicapi.Owner;


public class MyStrat implements IStrategy {

    /**
     * Method where students can observe the state of the system and schedule events to be executed.
     *
     * @param //planets          The current state of the system.
     * @param //planetOperations Helper methods students can use to interact with the system.
     * @param //eventsToExecute  Queue students will add to in order to schedule events.
     */
    public MyStrat() {
    }

    public void takeTurn(List<IPlanet> planets, IPlanetOperations planetOperations, Queue<IEvent> eventsToExecute) {
        Map<Integer, IVisiblePlanet> owned = new HashMap();
        Map<Integer, IVisiblePlanet> neturals = new HashMap();
        Map<Integer, IVisiblePlanet> enemies = new HashMap();
        Stack<IVisiblePlanet> LongestTrav= new Stack<>();
        LinkedList<IVisiblePlanet> ShortestTrav = new LinkedList<>();

        Iterator var7 = planets.iterator();

        IVisiblePlanet vPlanet;
        while(var7.hasNext()) {
            IPlanet planet = (IPlanet)var7.next();
            if (planet instanceof IVisiblePlanet) {
                vPlanet = (IVisiblePlanet)planet;
                if (vPlanet.getOwner() == Owner.SELF) {
                    owned.put(vPlanet.getId(), vPlanet);
                } else if (vPlanet.getOwner() == Owner.NEUTRAL) {
                    neturals.put(vPlanet.getId(), vPlanet);
                } else {
                    enemies.put(vPlanet.getId(), vPlanet);
                }
            }
        }

        Set edges;
        boolean attacked;
        Iterator var12;
        IEdge edge;
        int targetId;
        Integer id;
        if (neturals.size() > 0) {
            var7 = owned.keySet().iterator();


        }



    }


    public String getName() {
        return "Omar";
    }


    public boolean compete() {
        return true;
    }
    public boolean getShortest(){
        
        return true;
    }
}