package core;

import tileengine.TETile;
import tileengine.Tileset;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class World {

    // 1. pseudo-randomly generated
    // 2. must include distinct rooms and hallways (could contain outdoor spaces)
    // 3. At least some rooms should be rectangular
    // 4. capable of generating hallways (or equivalently, straight hallways that intersect)
    // 5. contain random number of rooms and hallways
    // 6. location of the rooms and hallways should be random
    // 7. room - width = random
    //           height = random
    //           walls
    // 8. hallways - width = 1
    //           length = random
    //           dead-end not allowed
    //           walls
    // 9. walls should be visually distinct from floors and unused space
    // 10. floors should be visually distinct from unused space
    // 11. rooms and hallways should be connected
    // 12. All rooms should be reachable - no rooms with no way to enter
    // 13. The world should be substantially different each time



    // method to write
    // World()
    // generateRooms()
    // connectRooms()
    // getTiles()
    // isValidPosition()
    // Class room




    private final TETile[][] tiles;
    private final int WIDTH;
    private final int HEIGHT;
    private final Random RANDOM;
    private final List<Room> rooms = new ArrayList<>();
    private int id = 0;
    WeightedQuickUnionUF uf;
    Pos playerPos;
    private final List<Pos> wallPoints  = new ArrayList<>();
    public static class Pos {
        int x;
        int y;
        public Pos(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
    // Inner class to represent a room
    private static class Room {
        int positionX;
        int positionY;
        int width;
        int height;
        int edges = 2;
        int connectedEdge = 0;
        int id;
        List<Room> connected = new ArrayList<>();

        private Room(int posX, int posY, int width, int height, int id) {
            this.positionX = posX;
            this.positionY = posY;
            this.width = width;
            this.height = height;
            this.id = id;
        }
        private boolean isConnected() {
            return edges == connectedEdge;
        }
        public int getId() {
            return id;
        }
    }
    public World(int width, int height, Long seed) {
        this.WIDTH = width;
        this.HEIGHT = height;
        this.RANDOM = new Random(seed);
        int numberOfRooms = RANDOM.nextInt(10) + 10;
        uf = new WeightedQuickUnionUF(numberOfRooms);
        tiles = new TETile[WIDTH][HEIGHT];
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                tiles[x][y] = Tileset.NOTHING;
            }
        }
        for (int i = 0; i < numberOfRooms; i++) {
            generateRoom();
        }
        for (Room room: rooms) {
            connectEdges(room);
        }
        for (int i = 0; i < rooms.size(); i++) {
            if (!uf.connected(0, i)) {  // Assuming 0 is the starting room
                Room source = rooms.get(i);
                Room target = rooms.get(i - 1);
                connectEdge(source, target);
                uf.union(source.getId(), target.getId());
                System.out.println(source);

            }
        }



        createWalls();
        createLockDoor();
        generatePlayer();

    }
    private boolean isSpaceOccupied(int startX, int startY, int width, int height) {
        for (int x = startX - 1; x <= startX + width; x++) {
            for (int y = startY - 1; y <= startY + height; y++) {
                if (!isValidPosition(x, y)) {
                    return true;
                }
            }
        }
        return false;
    }
    private void generatePlayer() {
        while (true) {
            int x = RANDOM.nextInt(WIDTH);
            int y = RANDOM.nextInt(HEIGHT);
            if (tiles[x][y].equals(Tileset.FLOOR)) {
                playerPos = new Pos(x, y);
                tiles[x][y] = Tileset.AVATAR;
                return;
            }
        }
    }

    private void generateRoom() {
        boolean spaceOccupied;
        int width, height, startX, startY;
        do {
            width = RANDOM.nextInt(10) + 2;  // Ensure a minimum size of 2 for the room
            height = RANDOM.nextInt(10) + 2;
            startX = RANDOM.nextInt(WIDTH - width + 1);  // Adjust the starting X coordinate
            startY = RANDOM.nextInt(HEIGHT - height + 1); // Adjust the starting Y coordinate
            spaceOccupied = isSpaceOccupied(startX, startY, width, height);
        } while (spaceOccupied);

        // Once an unoccupied space is found, place the room
        for (int x = startX; x < startX + width; x++) {
            for (int y = startY; y < startY + height; y++) {
                tiles[x][y] = Tileset.FLOOR;
            }
        }
        // Create walls around the room


        // Add the center of the room to the list of rooms
        int centerX = startX + width / 2;
        int centerY = startY + height / 2;
        Room room = new Room(centerX, centerY, width, height, id);
        id += 1;
        rooms.add(room);
    }

    private void createWalls() {
        for (int x = 1; x < WIDTH - 1; x++) {
            for (int y = 1; y < HEIGHT - 1; y++) {
                // Place walls only around the edges of rooms or hallways
                if (tiles[x][y] == Tileset.FLOOR) {
                    // Check the adjacent tiles for Tileset.NOTHING
                    if (tiles[x + 1][y] == Tileset.NOTHING) {
                        tiles[x + 1][y] = Tileset.WALL;
                        Pos pos = new Pos(x + 1, y);
                        wallPoints.add(pos);
                    }
                    if (tiles[x - 1][y] == Tileset.NOTHING) {
                        tiles[x - 1][y] = Tileset.WALL;
                        Pos pos = new Pos(x - 1, y);
                        wallPoints.add(pos);
                    }
                    if (tiles[x][y + 1] == Tileset.NOTHING) {
                        tiles[x][y + 1] = Tileset.WALL;
                        Pos pos = new Pos(x, y + 1);
                        wallPoints.add(pos);
                    }
                    if (tiles[x][y - 1] == Tileset.NOTHING) {
                        tiles[x][y - 1] = Tileset.WALL;
                        Pos pos = new Pos(x, y - 1);
                        wallPoints.add(pos);
                    }
                }
            }
        }
    }




    private Room findClosestRoom(Room source, List<Room> roomList) {
        Room closestRoom = null;
        int minDistance = Integer.MAX_VALUE;

        for (Room room : roomList) {
            if (room != source) {
                int distance = manhattanDistance(source, room);
                if (distance < minDistance) {
                    minDistance = distance;
                    closestRoom = room;
                }
            }
        }
        return closestRoom;
    }
    private List<Room> findOneOrMultiClosestRooms(Room source) {
        List<Room> roomCopy = new ArrayList<>(rooms);
        List<Room> closestRooms = new ArrayList<>();
        int i = 0;
        while (i < source.edges) {
            Room temp = findClosestRoom(source, roomCopy);
            if (!temp.connected.contains(source)) {
                closestRooms.add(temp);
                i += 1;
            }
            roomCopy.remove(temp);
        }
        return closestRooms;
    }
    private void connectEdges(Room source) {
        List<Room> closestRooms = findOneOrMultiClosestRooms(source);
        for (Room target : closestRooms) {
            if (!source.isConnected() && uf.find(target.getId()) != uf.find(source.getId())) {
                connectEdge(source, target);
                source.connectedEdge += 1;
                target.connectedEdge += 1;
                uf.union(source.getId(), target.getId());
            }
        }
    }

    private void connectEdge(Room source, Room target) {
        // Start from the center of the source room
        int x = source.positionX;
        int y = source.positionY;

        // Connect horizontally until aligned with target room center
        int horizontalDirection = Integer.compare(target.positionX, x);
        while (x != target.positionX) {
            if (tiles[x][y] == Tileset.NOTHING) {
                tiles[x][y] = Tileset.FLOOR;
            }
            x += horizontalDirection;
        }

        // Connect vertically until in target room center
        int verticalDirection = Integer.compare(target.positionY, y);
        while (y != target.positionY) {
            if (tiles[x][y] == Tileset.NOTHING) {
                tiles[x][y] = Tileset.FLOOR;
            }
            y += verticalDirection;
        }
    }

    private void createLockDoor() {
        int randomPos = RANDOM.nextInt(wallPoints.size());
        Pos lockDoorPos = wallPoints.get(randomPos);
        tiles[lockDoorPos.x][lockDoorPos.y] = Tileset.LOCKED_DOOR;
    }

    private int manhattanDistance(Room source, Room target) {
        return Math.abs(source.positionX - target.positionX) + Math.abs(source.positionY - target.positionY);
    }
    private boolean isValidPosition(int x, int y) {
        return x >= 1 && x < WIDTH - 1 && y >= 1 && y < HEIGHT - 1 && tiles[x][y] == Tileset.NOTHING;
    }

    public TETile[][] getTiles() {
        return tiles;
    }
}





