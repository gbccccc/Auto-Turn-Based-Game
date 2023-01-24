package game.world;

import game.Game;
import game.creature.Creature;

import java.io.*;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

public class World {
    private Position[][] positions;
    private int width, height;
    private String worldName;

    /*
     * map file structure
     * first line: two integer, representing width and height
     * a (width * height) matrix, 0 for floor, 1 for wall
     */
    public World(String worldName) {
        this.worldName = worldName;
        Scanner input = new Scanner(new BufferedReader(new InputStreamReader(
                World.class.getClassLoader().getResourceAsStream("world/" + worldName + ".txt"))
        ));

        this.width = input.nextInt();
        this.height = input.nextInt();

        this.positions = new Position[this.height][this.width];
        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                int terrainId = input.nextInt();
                this.positions[y][x] = new Position(x, y);
                this.positions[y][x].setTerrain(Terrain.getTerrain(terrainId));
            }
        }
    }


//    public static World generateWorld(String worldName) {
//        World world = new World(worldName);
//        return world;
//    }

    public String getName() {
        return worldName;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Position getPosition(int x, int y) throws Exception {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            throw new Exception("coordinate out of bound");
        }

        return positions[y][x];
    }

    public void repaint() {
        CountDownLatch doneSignal = new CountDownLatch(width * height);
        for (Position[] row : positions) {
            for (Position position : row) {
                Game.getExecutor().execute(position.getRepaint(doneSignal));
            }
        }

        try {
            doneSignal.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // return true if add creature successfully
    public boolean addCreature(Creature creature, int x, int y) {
        creature.setWorld(this);
        return creature.enterPosition(positions[y][x]);
    }
}