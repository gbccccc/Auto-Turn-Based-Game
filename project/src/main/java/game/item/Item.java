package game.item;

import gui.DrawTask;
import gui.Drawable;
import gui.GameWindow;

public class Item implements Drawable {
    String assetName;

    public Item(String assetName) {
        this.assetName = assetName;
    }

    @Override
    public DrawTask generateDrawTask(int x, int y, DrawTask foreground) {
        return new DrawTask(assetName, GameWindow.convertCoordinate(x), GameWindow.convertCoordinate(y), foreground);
    }
}