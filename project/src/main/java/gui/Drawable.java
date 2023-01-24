package gui;

public interface Drawable {
    public DrawTask generateDrawTask(int x, int y, DrawTask foreground);

    public default DrawTask generateDrawTask(int x, int y) {
        return generateDrawTask(x, y, null);
    }
}
