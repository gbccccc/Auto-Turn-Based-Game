package gui;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.concurrent.CountDownLatch;

public class DrawTask implements Serializable {
    private final DrawTask foreground;
    private final String imgName;
    private final int x, y;

    public DrawTask(String imgName, int x, int y, DrawTask foreground) {
        this.imgName = imgName;
        this.foreground = foreground;
        this.x = x;
        this.y = y;
    }

    // draw images recursively
    public void draw(Graphics graphics) {
        drawImage(graphics, imgName, x, y);
        if (foreground != null)
            foreground.draw(graphics);
    }

    // draw an single image
    private static void drawImage(Graphics graphics, String imgName, int x, int y) {
        if (imgName == null) {
            return;
        }

        try (InputStream imgInput = DrawTask.class.getClassLoader().getResourceAsStream("assets/" + imgName)) {
            if (imgInput != null) {
                Image img = ImageIO.read(imgInput);
                graphics.drawImage(img, x, y, null);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public DrawTaskRunnable getDraw(Graphics graphics, CountDownLatch doneSignal) {
        return new DrawTaskRunnable(graphics, doneSignal);
    }

    public DrawTaskRunnable getDraw(Graphics graphics) {
        return new DrawTaskRunnable(graphics);
    }

    private class DrawTaskRunnable implements Runnable {
        Graphics graphics;
        CountDownLatch doneSignal;

        private DrawTaskRunnable(Graphics graphics) {
            this(graphics, null);
        }

        private DrawTaskRunnable(Graphics graphics, CountDownLatch doneSignal) {
            this.graphics = graphics;
            this.doneSignal = doneSignal;
        }

        @Override
        public void run() {
            DrawTask.this.draw(graphics);
            if (doneSignal != null) {
                doneSignal.countDown();
            }
        }
    }
}