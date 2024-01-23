# Build Your Own World Design Document

## Classes and Data Structures

### WeightedQuickUnionUF
**Purpose: Implements the weighted quick-union algorithm for managing a set of elements partitioned into disjoint sets.**
#### Instance Variables
* int[] parent: An array where parent[i] holds the parent of element i. If parent[i] == i, then i is the root of its set.
* int[] size: An array where size[i] represents the number of elements in the subtree rooted at i.
* int count: The number of distinct sets or components.
* Constructors
WeightedQuickUnionUF(int n): Initializes the union-find data structure with n elements. Each element is initially in its own set.
#### Methods
* int count(): Returns the number of distinct sets.
* int find(int p): Returns the root of the set containing element p. Implements path compression for efficiency.
* boolean connected(int p, int q): Determines whether elements p and q are in the same set.
* void union(int p, int q): Merges the sets containing p and q. Uses weighting to ensure smaller trees are merged into larger ones, optimizing the tree's height.
* void validate(int p): Checks if p is a valid element index.

### World
**Purpose: Manages the generation and structure of the game world, including rooms, hallways, and special features like the locked door and player position.**
#### Instance Variables
* TETile[][] tiles: 2D array representing the game world's tiles.
* int WIDTH, HEIGHT: Dimensions of the game world.
* Random RANDOM: Random number generator for various world generation features.
* int numberOfRooms: The number of rooms to generate in the world.
* List<Room> rooms: List of all the rooms in the world.
* int id: Identifier for each room.
* WeightedQuickUnionUF uf: Union-find data structure to ensure all rooms are connected.
* Pos playerPos: Position of the player in the world.
* List<Pos> wallPoints: List of positions where walls are located.
#### Inner Classes
* Pos: Represents a position in the world with x and y coordinates.
* Room: Represents a room in the world with properties like position, dimensions, and connected status.

### Game
**Purpose: This class is the main driver of the game, handling user inputs, rendering the game state, and maintaining the main game loop.**
#### Instance Variables
* int WIDTH, HEIGHT: Constants defining the dimensions of the game world.
* String NORTH, SOUTH, EAST, WEST: Constants representing movement directions.
* String SAVE_GAME: The filename for saving and loading game states.
* boolean mainMenuMode, newGameMode, quitMode, withPerson, lineOfSightEnabled, gameOver: Boolean flags for managing various game states.
* String seedString, movements: Strings for storing the seed for world generation and player movements.
* TERenderer ter: Object for rendering the game world.
* TETile[][] world: 2D array representing the game world.
* int playerX, playerY: Variables representing the player's position.
* int health: Variable representing the player's health.
* int lockedDoorX, lockedDoorY: Variables representing the position of the locked door.


## Algorithms

### WeightedQuickUnionUF
**The weighted quick-union algorithm is an improvement over the basic quick-union approach. It addresses the issue of tree imbalance, which can lead to inefficient operations in the basic version.**
* Find Operation: Traverses up the tree from a given element to find the root of its set. This root is used to check set membership and perform unions.
* Union Operation: Connects two sets by making the root of the smaller set point to the root of the larger set. This is determined by the size array. This approach keeps the trees more balanced, ensuring better performance for future operations.
* Path Compression: During the find operation, the class could be further optimized by implementing path compression, which flattens the structure of the tree whenever find is used, keeping trees almost completely flat.

#### World
* World(int width, int height, Long seed): Constructor that initializes the world, generates rooms and hallways, and places special features.
* void generateRoom(): Generates a single room with random position and size, and ensures it doesn't overlap with existing rooms.
* void createWalls(): Creates walls around rooms and hallways.
* void generatePlayer(): Randomly places the player in an open floor space.
* boolean isSpaceOccupied(int startX, int startY, int width, int height): Checks if a specified area is occupied by any room or hallway.
* void createLockDoor(): Places a locked door at a random wall location.
* void connectEdges(Room source): Connects a given room to other rooms using hallways.
* void connectEdge(Room source, Room target): Connects two rooms with a hallway.
* int manhattanDistance(Room source, Room target): Calculates the Manhattan distance between two rooms.
* boolean isValidPosition(int x, int y): Checks if a given position is valid (within bounds and not occupied).
* TETile[][] getTiles(): Returns the tile array representing the world.

### Game
* void processInput(String input): Recursively processes user input. Handles game state transitions and player movements.
* void processInputString(String first): Processes a single character of input. Manages movement, game mode toggles, and game state changes.
* void enterSeedAndCreateWorld(): Initializes a new game world based on the provided seed.
* void processMainMenu(): Handles the main menu loop, processing inputs and rendering the menu.
* void renderMainMenu(): Renders the main menu screen.
* void moveAvatar(String input): Moves the player in the specified direction, updating the player's position and health.
* boolean checkWinCondition(): Checks if the player has met the win condition.
* boolean checkLoseCondition(): Checks if the player has met the loss condition.
* void getLockedDoorPos(): Finds and sets the position of the locked door in the game world.
* void showLoseMessage(), showWinMessage(): Renders the loss or win message screen.
* void renderGame(): Main game rendering loop.
* void renderWorld(): Renders the game world based on the current state.
* void showHUD(): Displays the heads-up display (HUD), including health and other information.
* void saveAndQuit(): Saves the current game state and exits.
* void loadGame(): Loads a saved game state.
* boolean isInLineOfSight(int x, int y): Determines if a given tile is within the player's line of sight.

## Persistence
**The game state is persisted by saving key game data to a file and reloading it upon request. This includes player position, health, the seed for the world generation, and the sequence of movements made by the player. The file used for this purpose is defined by the SAVE_GAME constant.**
* void saveAndQuit(): This method is called to save the game. It writes the seedString and movements to a file. The file format is simple text, with data separated by a delimiter for easy parsing.
* void loadGame(): This method loads a saved game. It reads from the save file, extracting the seed and movements, then reconstructs the game state based on this data. This includes regenerating the world and reapplying movements to reach the saved state.
