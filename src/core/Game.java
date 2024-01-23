package core;

import edu.princeton.cs.algs4.StdDraw;
import tileengine.TERenderer;
import tileengine.TETile;
import tileengine.Tileset;
import java.io.File;

import java.io.IOException;


import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

public class Game {
    // static variables
    private static final int WIDTH = 100;
    private static final int HEIGHT = 50;
    private static final String NORTH = "w";
    private static final String WEST = "a";
    private static final String SOUTH = "s";
    private static final String EAST = "d";
    private static final String SAVE_GAME = "save_game.txt";

    // Instance variables
    private boolean mainMenuMode = true;
    private boolean newGameMode = false;
    private boolean quitMode = false;
    private String seedString = "";
    private transient TERenderer ter = new TERenderer();
    private TETile[][] world;
    private int playerX;
    private int playerY;
    private String movements = "K";
    private boolean withPerson = true;
    private int health = 100; // initial health = 100
    private boolean lineOfSightEnabled = false;
    private int lockedDoorX;
    private int lockedDoorY;
    private boolean gameOver = false;


    private void switchMainMenu() {
        mainMenuMode = !mainMenuMode;
    }
    private void switchNewGameMode() {
        newGameMode = !newGameMode;
    }
    private void switchQuitMode() {
        quitMode = !quitMode;
    }
    private void switchLineOfSight() {
        lineOfSightEnabled = !lineOfSightEnabled;
    }

    // process game recursively according to a given input Strings
    private void processInput(String input) {
        if (input == null) {
            System.out.println("No inputs given");
            System.exit(0);
        }
        // get the first input char and normalize to lowercase
        String first = Character.toString(input.charAt(0)).toLowerCase();
        processInputString(first);
        if (input.length() > 1) {
            String rest = input.substring(1);
            processInput(rest); // recursive call until input ends
        }

    }

    // process input according to a single input
    private void processInputString(String first) {
        if (!gameOver) {
            if (mainMenuMode) {
                switch (first) {
                    case "n" -> switchNewGameMode();
                    case "s" -> enterSeedAndCreateWorld();
                    case "l" -> loadGame();
                    case "q" -> System.exit(0);
                    default -> {        // append next seed integer to seedString
                        try {
                            Long.parseLong(first);
                            seedString += first;
                        } catch (NumberFormatException e) { // exit program if input is invalid
                            System.out.println("Invalid input given: " + first);
                            System.exit(0);
                        }
                    }
                }
            } else {
                switch (first) {
                    case NORTH, SOUTH, EAST, WEST -> {
                        moveAvatar(first);
                        movements += first;
                    }
                    case ":" -> switchQuitMode();
                    case "q" -> saveAndQuit();
                    case "t" -> switchLineOfSight();
                    default -> System.out.println("Invalid inputs");
                }
            }
        } else {
            if (first.equals("q")) {
                System.exit(0);
            }

        }
    }

    private void enterSeedAndCreateWorld() {
        // check validity of input
        if (!newGameMode) {
            String error = "Input string " + "\"S\" given, but no game has been initialized.\n"
                    + "Please initialize game first by input string \"N\" and following random seed"
                    + "numbers";
            System.out.println(error);
            System.exit(0);
        }
        switchNewGameMode();
        World tiles;

        // set up a random seed and generate a world according to the seed
        if (seedString.isEmpty()) {
            tiles = new World(WIDTH, HEIGHT, Long.parseLong("0"));
        } else {
            long seed = Long.parseLong(seedString);
            tiles = new World(WIDTH, HEIGHT, seed);
        }
        // get players coordinates
        playerX = tiles.playerPos.x;
        playerY = tiles.playerPos.y;
        world = tiles.getTiles();
        // set main menu mode to false
        switchMainMenu();
    }


    public void processMainMenu() {
        StdDraw.setCanvasSize(512, 512);
        StdDraw.clear(StdDraw.BLACK);

        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                String typed = Character.toString(StdDraw.nextKeyTyped());
                processInput(typed);
            }

            renderMainMenu();

            if (!mainMenuMode) {   // break after setup has been done and enter game mode
                break;
            }
            StdDraw.pause(100);
        }
        processGame();
    }

    public void renderMainMenu() {
        StdDraw.clear(StdDraw.BLACK);
        StdDraw.setPenColor(StdDraw.WHITE);

        // title
        StdDraw.setFont(new Font("Arial", Font.BOLD, 40));
        StdDraw.text(0.5, 0.8, "CS61B: THE GAME");

        // menu
        StdDraw.setFont(new Font("Arial", Font.PLAIN, 20));
        StdDraw.text(0.5, 0.5, "New Game: N");
        StdDraw.text(0.5, 0.4, "Load Game: L");
        StdDraw.text(0.5, 0.3, "Quit: Q");

        // seed
        if (newGameMode) {
            StdDraw.text(0.5, 0.2, "Seed: " + seedString);
            StdDraw.text(0.5, 0.1, "Press S to Start");
        }
        StdDraw.show();
    }
    private void moveAvatar(String input) {
        switch (input) {
            case NORTH -> {
                if (!world[playerX][playerY + 1].equals(Tileset.WALL)) {
                    world[playerX][playerY + 1] = Tileset.AVATAR;
                    world[playerX][playerY] = Tileset.FLOOR;
                    playerY = playerY + 1;
                    health -= 1;
                }
            }
            case WEST -> {
                if (!world[playerX - 1][playerY].equals(Tileset.WALL)) {
                    world[playerX - 1][playerY] = Tileset.AVATAR;
                    world[playerX][playerY] = Tileset.FLOOR;
                    playerX = playerX - 1;
                    health -= 1;
                }
            }
            case SOUTH -> {
                if (!world[playerX][playerY - 1].equals(Tileset.WALL)) {
                    world[playerX][playerY - 1] = Tileset.AVATAR;
                    world[playerX][playerY] = Tileset.FLOOR;
                    playerY = playerY - 1;
                    health -= 1;
                }
            }
            case EAST -> {
                if (!world[playerX + 1][playerY].equals(Tileset.WALL)) {
                    world[playerX + 1][playerY] = Tileset.AVATAR;
                    world[playerX][playerY] = Tileset.FLOOR;
                    playerX = playerX + 1;
                    health -= 1;
                }
            }
            default -> System.out.println("Invalid inputs");
        }

    }
    private boolean checkWinCondition() {
        if (playerX == lockedDoorX && playerY == lockedDoorY && health > 0) {
            gameOver = true;
            return true;
        }
        return false;
    }

    private boolean checkLoseCondition() {
        if (health <= 0) {
            gameOver = true;
            return true;
        }
        return false;
    }


    private void getLockedDoorPos() {
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                if (world[i][j].equals(Tileset.LOCKED_DOOR)) {
                    lockedDoorX = i;
                    lockedDoorY = j;
                }
            }
        }
    }
    private void showLoseMessage() {
        StdDraw.setPenColor(Color.RED);
        StdDraw.setFont(new Font("Arial", Font.BOLD, 40));
        StdDraw.text(WIDTH / 2.0, HEIGHT / 2.0, "You lose!");
        StdDraw.text(WIDTH / 2.0, (HEIGHT / 2.0) - 10, "Press Q to quit");
        StdDraw.show();
    }

    private void showWinMessage() {
        StdDraw.setPenColor(Color.GREEN);
        StdDraw.setFont(new Font("Arial", Font.BOLD, 40));
        StdDraw.text(WIDTH / 2.0, HEIGHT / 2.0, "Congratulations ! You Win!");
        StdDraw.text(WIDTH / 2.0, (HEIGHT / 2.0) - 10, "Press Q to quit");
        StdDraw.show();
    }


    private void processGame() {
        ter.initialize(WIDTH, HEIGHT);
        getLockedDoorPos();
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                String typed = Character.toString(StdDraw.nextKeyTyped());
                processInput(typed);
            }
            renderGame();
        }
    }


    private void renderGame() {
        renderWorld();
        showHUD();
        if (checkWinCondition()) {
            showWinMessage();
            StdDraw.pause(10);
            switchMainMenu();
        }
        if (checkLoseCondition()) {
            showLoseMessage();
            StdDraw.pause(10);
            switchMainMenu();

        }
        StdDraw.pause(100);
    }

    private void renderWorld() {
        // modify world for line of sight
        // ter.renderFrame(world);
        TERenderer tempRenderer = new TERenderer();
        TETile[][] tempWorld = new TETile[WIDTH][HEIGHT];
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                if (!lineOfSightEnabled && !isInLineOfSight(x, y)) {
                    tempWorld[x][y] = Tileset.NOTHING;
                } else {
                    tempWorld[x][y] = world[x][y];
                }
            }
        }
        tempRenderer.renderFrame(tempWorld);
    }

    private void showHUD() {
        int mouseX = (int) StdDraw.mouseX();
        int mouseY = (int) StdDraw.mouseY();
        // get current mouse positions tile
        TETile mouseTile = world[mouseX][mouseY];
        // get current time
        Date date = new Date();
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.textLeft(1, HEIGHT - 1, mouseTile.description()); // add to HUD
        StdDraw.text(50, HEIGHT - 1, "health :" + health);
        StdDraw.textRight(99, HEIGHT - 1, date.toString()); // add to HUD
        StdDraw.show();
    }
    private void saveAndQuit() {
        if (!quitMode) {
            return;
        }
        switchQuitMode();
        File f = new File(SAVE_GAME);
        try {
            if (!f.exists()) {
                f.createNewFile();
            }
            String strInstanceVars =  seedString + "," + movements;
            Files.writeString(Path.of(SAVE_GAME), strInstanceVars, StandardCharsets.UTF_8);
            if (withPerson)  {
                System.exit(0);
            }
        } catch (IOException e) {
            System.out.println(e);
            System.exit(1);
        }
    }
    private void loadGame() {
        try {
            Path path = Path.of(SAVE_GAME);
            seedString = Files.readString(path).split(",")[0];
            String oldMovements = Files.readString(path).split(",")[1];
            switchNewGameMode();
            enterSeedAndCreateWorld();
            processInput(oldMovements);
        } catch (IOException e) {
            System.out.println(e);
            System.exit(1);
        }
    }

    // Checking line of Sight
    private boolean isInLineOfSight(int tileX, int tileY) {
        int deltaX = Math.abs(tileX - playerX);
        int deltaY = Math.abs(tileY - playerY);
        int lineOfSightRadius = 3;
        return deltaX <= lineOfSightRadius && deltaY <= lineOfSightRadius;
    }


    
    public void playWithKeyboard() {
        processMainMenu();
    }

    public TETile[][] playWithInputString(String input) {
        withPerson = false;
        processInput(input);
        return world;
    }


}
