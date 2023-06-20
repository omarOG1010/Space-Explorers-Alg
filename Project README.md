<a name="br1"></a> 

Space Explorers

The Game

Premise

Space Explorers is a game where two players are pitted against each other in a system of

interconnected planets. The players each start out on a home planet and the object of the game

is to have the largest population by the end of the game.

Game Board

The game board is set up as a graph of planets interconnected by weighted edges.

●

●

Population growth occurs at a set rate on each planet.

If two planets are connected by an edge, then shuttles can be sent between these

planets. The distance between the planets affects how many turns it takes for shuttles to

move from one planet to the other.

●

The planets vary by two factors:

○

Size correlates to the total population the planet can support. Once a planet hits

its maximum population, population growth will cease and any population that

exceeds the maximum will decrease by the rate described below.

Habitability correlates to the population growth rate on a planet. Population

change after one turn for a given planet is defined below.

○

■

Let *c* = current population, *m* = max population, *g* = growth rate, and *p* =

overpopulation penalty.

●

●

If *c < m*, pop next turn = *c\*g*

If *c >= m*, pop next turn = *c - (c-m)\*p*

■

Currently, *p =* 0.1 and *g =* 1 + (habitability / 100)

●

Each player starts out with one planet with a given population. A player can send

shuttles with explorers to neighboring planets on their turn.



<a name="br2"></a> 

●

Each player receives information about the entire game board, including the planet IDs

of all planets and which planets are interconnected. However, a player can only see

detailed information (population percentages, size, habitability, incoming shuttles) about

the planets that their people have the majority on and their neighboring planets.

Game Flow

In one turn, a player

●

●

Receives information about the game state

Adds moves to the event queue

○

A ‘move’ is sending a shuttle from one planet to a neighboring planet. The player

can set how many explorers are sent in the shuttle.

○

A player can make as many moves as they want in a turn.

●

Returns the event queue to the game engine

The game engine will then make all legal moves in the event queue, and allow one unit of time

(one full turn cycle) to pass, in which

●

Population growth (or decay, if overpopulation cap) occurs on all planets

All shuttles move one step

●

The game play then passes to the other player.

Shuttles and Landings

A shuttle carries some amount of population from Planet A to Planet B. Let’s say that Player 1

sent a shuttle with 100 explorers, and the distance between the planets is 2. The shuttle takes

two full turn cycles to arrive at Planet B, and during that time, no population growth occurs on

the shuttle. There are several possibilities when the shuttle arrives at Planet B.

Player 1 Has Majority on Planet B

Since Player 1’s population is the majority on the planet, the explorers in the shuttle will always

be added to the population as their people are able to accommodate some overcrowding with

their own people if necessary.

Player 2 Has Majority on Planet B

If the population on Planet B has not hit the population cap, the explorers in the shuttle will

simply be added to Player 1’s population on the planet. If the population cap has been reached,

the explorers on the shuttle will be lost since the player’s population on the planet does not have

resources necessary to accommodate overpopulation.

Planet B is Neutral

Player 1’s explorers are added to the population if the population cap has not been reached.



<a name="br3"></a> 

Majority populations will give precedence to shuttles that they recognize (“friendly shuttles”), so

all friendly shuttles will land before any others are able to land.

End of the Game

The game ends when one player has a majority population on all planets or a maximum number

of turns is reached. If the maximum number of turns is reached, the player with the larger total

population at that point wins the game.

For more information about the game flow, see the “SpaceExplorers\_ExampleRound.pdf”

Running the Game

This example assumes that you’re using IntelliJ as your Java IDE.

1\. Download the assignment from the class Canvas and unzip the folder

2\. Import the folder “project4” as a project into IntelliJ

3\. On the libraries screen, uncheck the “strategies” folder

○

This can be done later if necessary via File -> Project Structure -> Libraries

4\. To add a strategy of your own, create a new java file in the folder

project4/src/spaceexplorers/strategies

○

Use the provided example strategies as a reference

Note that you only need to edit the takeTurn method

○

5\. To run the game WITH graphics...

○

Open the file project4/src/spaceexplorers/publicapi/Driver.Java



<a name="br4"></a> 

○

Edit the file to call your strategy instead of the provided strategies:

E.g. GameWindow window = new GameWindow(Strategy.class, false);

To create a .jar file for your strategy:

o

.

.

For a strategy at project4/src/spaceexplorers/strategies/Strategy.java…

Create a .jar file for your strategy by going to File -> Project Structure ->

Project Settings -> Artifacts -> Plus sign -> JAR -> from modules with

dependencies and click ‘OK’

.

Name your strategy so that it matches your class name (i.e.

RandomStrategy -> RandomStrategy.jar), and set its output folder to

project4/strategies

.

Press ok, go to the menu Build -> Build Artifacts -> Build

o

Then, run the game by running the Driver.Java class, which has a main method.

Be sure to set your Working directory to the parent folder for the project (this

contains the subfolders src, strategies, graphs, etc). Set your Working directory

by going to Run -> Edit Configurations and then updating the “Working directory”

field.

6\. To run the game WITHOUT graphics (this can be useful for testing your strategy)...

○

○

Open the file project4/src/spaceexplorers/core/SpaceExplorers.Java

Edit the file to call your strategy instead of the provided strategies

■

For a strategy at

project4/src/spaceexplorers/strategies/MyStrategy.java…

Import your strategy with import spaceexplorers.strategies.MyStrategy

In the main method, change strategy1 to be an instance of MyStrategy

■

■

●

E.g. with IStrategy strategy1 = new MyStrategy();

■

Then, run the game by running the SpaceExplorers.Java class, which has

a main method. Be sure to set your Working directory to the parent folder

for the project (this contains the subfolders src, strategies, graphs, etc).

Set your Working directory by going to Run -> Edit Configurations and

then updating the “Working directory” field.

Public API

The API (interfaces you will be working with are listed below) - -

1\. IEdge - Interface denoting an edge between two planets.

2\. IEvent - An event is a wrapper around a population transfer from a source and

destination planet.

3\. IPlanet - A planet which has not been discovered yet.

4\. IPlanetOperations - An interface denoting the events scheduling the movement of

people.

5\. IShuttle - An interface to represent the movement of people.

6\. IStrategy - Your strategy class should implement this interface. Essentially, your strategy

class should move a fixed number of people from planets where you have people to a

destination planet such that you increase the number of planets you have population on

as the game progresses.



<a name="br5"></a> 

7\. IVisiblePlanet - A planet which you have conquered or is adjacent to you. You are able

to see a complete set of characteristics for these planets.

Have a look at the RandomStrategy.java file for an example on how to use these interfaces.


