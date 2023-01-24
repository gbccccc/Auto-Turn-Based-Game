package game.world;

import gui.DrawTask;
import gui.Drawable;
import gui.GameWindow;

import java.awt.*;

public enum Terrain implements Drawable {
    FLOOR(true, "floor.png"), // id: 0

    WALL(false, "wall.png"); // id: 1

    private final boolean movable;
    private final String assetName;

    private Terrain(boolean movable, String assetName) {
        this.movable = movable;
        this.assetName = assetName;
    }

    public boolean isMovable() {
        return movable;
    }

    // get a type of terrain by terrain id
    public static Terrain getTerrain(int id) {
        return Terrain.values()[id];
    }

    @Override
    public DrawTask generateDrawTask(int x, int y, DrawTask foreground) {
        return new DrawTask(assetName, GameWindow.convertCoordinate(x), GameWindow.convertCoordinate(y), foreground);
    }
}