package spaceexplorers.publicapi;

import spaceexplorers.core.Assets;
import spaceexplorers.core.SpaceExplorers;
import spaceexplorers.strategies.*;

import java.io.FileNotFoundException;
import java.util.*;

/**
 * The StrategyRanker class is responsible for ranking different strategies based on their performance in simulated games.
 */
public class StrategyRanker {

    /** The number of trials to run for each strategy matchup. */
    public static final int NUM_TRIALS = 10;

    /**
     * The main method runs the strategy ranking algorithm and prints out the results.
     *
     * @param args Command line arguments (not used).
     * @throws IllegalAccessException    If an illegal access exception occurs during strategy instantiation.
     * @throws InstantiationException    If an instantiation exception occurs during strategy instantiation.
     * @throws FileNotFoundException     If a file not found exception occurs during strategy loading.
     */
    public static void main(String[] args) throws IllegalAccessException, InstantiationException, FileNotFoundException {
        // Strategy providers for comparison
        List<IStrategyProvider> strategyProviders = new ArrayList<>();
        strategyProviders.add(new ClassStrategyProvider(NoOpStrategy.class));
        strategyProviders.add(new ClassStrategyProvider(RandomStrategy.class));
        strategyProviders.add(new JarStrategyProvider("AI1Strategy"));
        strategyProviders.add(new JarStrategyProvider("AI2Strategy"));
        strategyProviders.add(new JarStrategyProvider("AI3Strategy"));
        strategyProviders.add(new ClassStrategyProvider(StudentStrategy.class));

        // List of graphs to test strategies on
        List<String> graphs = new ArrayList<>();
        graphs.add("rings");
        graphs.add("k4");
        for (int i = 0; i < 100; i++) {
            graphs.add("graph_" + i);
        }

        // Initialize win counts
        Map<IStrategyProvider, Integer> wins = new HashMap<>();
        Map<IStrategyProvider, Map<String, Integer>> winsPerMap = new HashMap<>();
        for (IStrategyProvider strategyProvider : strategyProviders) {
            wins.put(strategyProvider, 0);

            winsPerMap.put(strategyProvider, new HashMap<>());
            for (String graph : graphs) {
                winsPerMap.get(strategyProvider).put(graph, 0);
            }
        }

        // Run strategy matchups
        for (int i = 0; i < strategyProviders.size(); i++) {
            for (int j = i + 1; j < strategyProviders.size(); j++) {
                if (i == j) {
                    continue;
                }

                for (String graph : graphs) {
                    for (int trial = 0; trial < NUM_TRIALS; trial++) {
                        IStrategyProvider strategyProvider1;
                        IStrategyProvider strategyProvider2;
                        if (trial < NUM_TRIALS / 2) {
                            strategyProvider1 = strategyProviders.get(i);
                            strategyProvider2 = strategyProviders.get(j);
                        } else {
                            strategyProvider1 = strategyProviders.get(j);
                            strategyProvider2 = strategyProviders.get(i);
                        }

                        IStrategy strategy1 = strategyProvider1.newInstance();
                        IStrategy strategy2 = strategyProvider2.newInstance();

                        SpaceExplorers spaceExplorers = new SpaceExplorers(strategy1, strategy2, graph, false);
                        IStrategy winner = spaceExplorers.runToCompletion(10000);
                        if (winner == strategy1) {
                            wins.put(strategyProvider1, wins.get(strategyProvider1) + 1);
                            winsPerMap.get(strategyProvider1).put(graph, winsPerMap.get(strategyProvider1).get(graph) + 1);
                        } else if (winner == strategy2) {
                            wins.put(strategyProvider2, wins.get(strategyProvider2) + 1);
                            winsPerMap.get(strategyProvider2).put(graph, winsPerMap.get(strategyProvider2).get(graph) + 1);
                        } else {
                            // No one won, neither player gets a point
                        }
                    }
                }
            }
        }

        // Print rankings
        System.out.println("Rankings:");
        List<Map.Entry<IStrategyProvider, Integer>> winsPairs = new ArrayList<>(wins.entrySet());
        winsPairs.sort(Comparator.comparingInt(e -> -e.getValue()));
        for (Map.Entry<IStrategyProvider, Integer> entry : winsPairs) {
            System.out.println(String.format("Strategy: %s, Wins: %d", entry.getKey().newInstance().getName(), entry.getValue()));
        }

        // Print wins per map
        System.out.println();
        System.out.println("Wins per Map");
        List<Map.Entry<IStrategyProvider, Map<String, Integer>>> winsPerMapPairs = new ArrayList<>(winsPerMap.entrySet());
        for (Map.Entry<IStrategyProvider, Map<String, Integer>> entry : winsPerMapPairs) {
            List<Map.Entry<String, Integer>> winsPerMapInnerPairs = new ArrayList<>(entry.getValue().entrySet());
            winsPerMapInnerPairs.sort(Comparator.comparingInt(e -> -e.getValue()));
            System.out.println(String.format("Strategy: %s, Wins: %s", entry.getKey().newInstance().getName(), winsPerMapInnerPairs));
        }
    }

    /**
     * Interface for getting an instance of a strategy, without knowing where it came from.
     */
    private interface IStrategyProvider {
        IStrategy newInstance() throws IllegalAccessException, InstantiationException;
    }

    /**
     * Class providing instances of a strategy which can be referred to directly, e.g. {@code MyStrategy.class}.
     */
    private static class ClassStrategyProvider implements IStrategyProvider {
        private Class<? extends IStrategy> strategyClass;

        /**
         * Constructs a ClassStrategyProvider with the provided strategy class.
         *
         * @param strategyClass The class of the strategy.
         */
        public ClassStrategyProvider(Class<? extends IStrategy> strategyClass) {
            this.strategyClass = strategyClass;
        }

        /**
         * Creates a new instance of the strategy class.
         *
         * @return An instance of the strategy.
         * @throws IllegalAccessException    If an illegal access exception occurs.
         * @throws InstantiationException    If an instantiation exception occurs.
         */
        @Override
        public IStrategy newInstance() throws IllegalAccessException, InstantiationException {
            return strategyClass.newInstance();
        }
    }

    /**
     * Class providing instances of a strategy which has been jarred up, e.g. {@code "AI1_obf"}.
     */
    private static class JarStrategyProvider implements IStrategyProvider {
        private String jar;

        /**
         * Constructs a JarStrategyProvider with the provided jar name.
         *
         * @param jar The name of the jar file containing the strategy.
         */
        public JarStrategyProvider(String jar) {
            this.jar = jar;
        }

        /**
         * Loads and creates a new instance of the strategy from the jar.
         *
         * @return An instance of the strategy.
         * @throws IllegalAccessException    If an illegal access exception occurs.
         * @throws InstantiationException    If an instantiation exception occurs.
         */
        @Override
        public IStrategy newInstance() throws IllegalAccessException, InstantiationException {
            IStrategy instance = Assets.loadPlayer(jar);
            if (instance == null) {
                throw new InstantiationException("Failed to instantiate " + jar);
            }
            return instance;
        }
    }
}
