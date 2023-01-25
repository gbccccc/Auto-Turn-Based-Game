package gui;

import game.Game;
import game.GameUpdateListener;
import game.world.Direction;
import networking.GameClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GameWindow extends JFrame {
    private GameClient client;
    private List<DrawTask> drawTasks;
    private static final int imgSize = 32;
    private int playerId;
    private String savePath;

    private boolean isClosing;

    public GameWindow(GameClient gameClient, int playerId, int width, int height) {
        this.client = gameClient;

        this.setSize(960, 720);
//        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        this.setLayout(new GridBagLayout());
        this.add(new GamePanel(width, height), new GridBagConstraints());

        this.playerId = playerId;

        isClosing = false;
    }

    public int getPlayerId() {
        return playerId;
    }

    // convert a game coordinate number into screen coordinate number
    public static int convertCoordinate(int gameLoc) {
        return gameLoc * imgSize;
    }

    private static final int updateGap = 400;

    @Override
    protected void processKeyEvent(KeyEvent e) {
        if (!(e.getID() == KeyEvent.KEY_PRESSED))
            return;

        try {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_UP, KeyEvent.VK_W -> {
                    client.sendMessage("DIRECTION " + playerId + " up");
                }
                case KeyEvent.VK_DOWN, KeyEvent.VK_S -> {
                    client.sendMessage("DIRECTION " + playerId + " down");
                }
                case KeyEvent.VK_LEFT, KeyEvent.VK_A -> {
                    client.sendMessage("DIRECTION " + playerId + " left");
                }
                case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> {
                    client.sendMessage("DIRECTION " + playerId + " right");
                }

                case KeyEvent.VK_I -> {
                    client.sendMessage("SAVE");
                }
                case KeyEvent.VK_P -> {
                    client.sendMessage("PAUSE");
                }
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    protected void processWindowEvent(WindowEvent pEvent) {
        if (pEvent.getID() == WindowEvent.WINDOW_CLOSING) {
            if (!isClosing) {
                isClosing = true;
            } else {
                return;
            }

            client.close();
        } else {
            super.processWindowEvent(pEvent);
        }
    }

    private class GamePanel extends JPanel {
        private static final Executor executor = Executors.newCachedThreadPool();

        private GamePanel(int worldWidth, int worldHeight) {
            setPreferredSize(new Dimension(worldWidth * imgSize, worldHeight * imgSize));
            drawTasks = Collections.synchronizedList(new ArrayList<>());
        }

        @Override
        public void paintComponent(Graphics graphics) {
            CountDownLatch doneSignal = new CountDownLatch(drawTasks.size());
            for (DrawTask task : drawTasks) {
                executor.execute(task.getDraw(graphics, doneSignal));
            }
            try {
                doneSignal.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void setDrawTasks(List<DrawTask> drawTasks) {
        this.drawTasks = drawTasks;
        repaint();
    }
}
