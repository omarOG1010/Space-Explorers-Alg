package spaceexplorers.core;

import com.paypal.digraph.parser.GraphParser;
import spaceexplorers.publicapi.IStrategy;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

/**
 * The Assets class provides utility methods for loading assets such as images, player strategies, and graphs.
 */
public final class Assets {

    // Directory paths for strategies, graphs, and images
    private static final String STRATEGIES_DIR = "strategies/";
    private static final String GRAPHS_DIR = "graphs/";
    private static final String IMG_DIR = "img/";
    private static final String[] PLANET_IMGS = {IMG_DIR + "alpha.png", IMG_DIR + "beta.png", IMG_DIR +"neutral.png"};

    /**
     * Load the planet image based on the specified player.
     *
     * @param who The player for which the planet image is being loaded.
     * @return The image of the planet corresponding to the specified player.
     * @throws IOException If an error occurs while reading the image file.
     */
    public static Image loadPlanet(InternalPlayer who) throws IOException {
        switch (who) {
            case PLAYER1:
                return ImageIO.read(new File(PLANET_IMGS[0]));
            case PLAYER2:
                return ImageIO.read(new File(PLANET_IMGS[1]));
            case NEUTRAL:
                return ImageIO.read(new File(PLANET_IMGS[2]));
            default:
                return null;
        }
    }

    /**
     * Load a player strategy from a JAR file.
     *
     * @param jar The name of the JAR file containing the player strategy.
     * @return The loaded player strategy.
     */
    public static IStrategy loadPlayer(String jar) {
        final String playerJar = STRATEGIES_DIR + jar + ".jar";
        final String STRATEGY = "spaceexplorers.strategies."+jar;
        try {
            // Create a URLClassLoader to load classes from the JAR file
            ClassLoader loader = new URLClassLoader(new URL[]{new File(playerJar).toURI().toURL()}, Thread.currentThread().getContextClassLoader());
            // Load the strategy class
            Class<?> c = loader.loadClass(STRATEGY);
            // Instantiate and return the strategy object
            return (IStrategy) c.newInstance();
        } catch (IllegalAccessException | InstantiationException | ClassNotFoundException | MalformedURLException e) {
            return null;
        }
    }

    /**
     * Load a graph from a DOT file.
     *
     * @param graph The name of the DOT file containing the graph.
     * @return The loaded GraphParser object representing the graph.
     * @throws FileNotFoundException If the specified DOT file is not found.
     */
    public static GraphParser loadGraph(String graph) throws FileNotFoundException {
        String filename = GRAPHS_DIR + graph + ".dot";
        return new GraphParser(new FileInputStream(filename));
    }

    /**
     * Load a player strategy from a provided class.
     *
     * @param strategyClass The class object representing the player strategy.
     * @return The loaded player strategy.
     */
    public static IStrategy loadPlayer(Class<? extends IStrategy> strategyClass) {
        try {
            // Instantiate and return the strategy object
            return strategyClass.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            return null;
        }
    }

    /**
     * Get the list of available strategy JAR files.
     *
     * @return An array of strings containing the names of available strategy JAR files.
     */
    public static String[] getStrategies() {
        String[] strategies = new File(STRATEGIES_DIR).list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });

        assert strategies != null;
        Arrays.sort(strategies);
        return strategies;
    }

    /**
     * Get the list of available graph DOT files.
     *
     * @return An array of strings containing the names of available graph DOT files.
     */
    public static String[] getGraphs() {
        String[] graphs = new File(GRAPHS_DIR).list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".dot");
            }
        });
        assert graphs != null;
        Arrays.sort(graphs);
        return graphs;
    }
}
