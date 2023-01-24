package networking;

import game.Game;
import game.GameUpdateListener;
import game.world.Direction;
import gui.DrawTask;
import mylib.Inet4Address;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class GameServer extends Thread implements GameUpdateListener {
    Game game;
    String savePath;

    private ServerSocketChannel serverChannel;
    private Selector selector;
    private int clientNum;
    private List<DrawTask> drawTasks;
    private boolean drawTasksReady;

    public static int BUFFER_SIZE = 1024;
    public static int INT_SIZE = 4;


    private GameServer(String saveFile, String savePath, int port) {
        try (Reader saveReader = new FileReader(saveFile)) {
            this.savePath = savePath;

            game = new Game(saveReader);
            game.setUpdateListener(this);

            drawTasks = Collections.synchronizedList(new ArrayList<>());
            drawTasksReady = false;

            selector = Selector.open();
            clientNum = 0;

            serverChannel = ServerSocketChannel.open();

            serverChannel.configureBlocking(false);
            serverChannel.bind(new InetSocketAddress(Inet4Address.getInet4Address(), port));
            System.out.println("Server waiting at " + serverChannel.getLocalAddress());
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static GameServer gameServer;

    public static GameServer getGameServer(String saveFile, String savePath, int port) {
        if (gameServer == null) {
            gameServer = new GameServer(saveFile, savePath, port);
        }
        return gameServer;
    }

    @Override
    public void run() {
        try {
            while (!game.gameOver()) {
                int readyCount = selector.select();
                if (readyCount == 0) {
                    continue;
                }
                processKeyInterests();
            }
        } catch (IOException ignored) {
        }
        System.out.println("Game Over!");
        onGameOver();
        System.exit(0);
    }

    private synchronized void processKeyInterests() throws IOException {
        Set<SelectionKey> readyKeys = selector.selectedKeys();
        Iterator<SelectionKey> iterator = readyKeys.iterator();
        while (iterator.hasNext()) {
            SelectionKey key = iterator.next();
            // Remove key from set so we don't process it twice
            iterator.remove();

            if (!key.isValid()) {
                continue;
            }

            if (key.isAcceptable()) {
                handleAcceptableKey(key);
                continue;
            }

            if (key.isWritable() && drawTasksReady) {
                handleWritableKey(key);
            }

            if (key.isReadable()) {
                handleReadableKey(key);
            }
        }
        drawTasks.clear();
        drawTasksReady = false;
    }

    private void onGameOver() {
        int notifiedNum = 0;
        try {
            while (notifiedNum < clientNum) {
                selector.select();
                Set<SelectionKey> readyKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = readyKeys.iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    // Remove key from set so we don't process it twice
                    iterator.remove();
                    if (!key.isValid()) {
                        continue;
                    }

                    if (key.isWritable()) {
                        sendMessage("GAMEOVER", (SocketChannel) key.channel());
                        key.cancel();
                        notifiedNum++;
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleAcceptableKey(SelectionKey key) {
        try {
            SocketChannel client;
            ServerSocketChannel server = (ServerSocketChannel) key.channel();
            client = server.accept();
            client.configureBlocking(false);

            selector.wakeup();
            client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            int id = game.playerLink();
            sendMessage("LINKED " + id + " " + game.getWidth() + " " + game.getHeight(), client);
            sendMessage("MESSAGE welcome! ", client);
            clientNum++;

            if (!game.isActive()) {
                game.begin();
            }
        } catch (IOException e) {
            throw new RuntimeException();
        } catch (Exception e) {
            System.out.println(e.getCause().getMessage());
        }
    }

    private void handleReadableKey(SelectionKey key) {
        try {
            SocketChannel client = (SocketChannel) key.channel();
            // Read byte coming from the client
            ByteBuffer bufferInt = ByteBuffer.allocate(INT_SIZE);
            client.read(bufferInt);
            bufferInt.flip();
            if (!bufferInt.hasRemaining()) {
                return;
            }
            int contentSize = bufferInt.getInt();

            if (contentSize == -1) {
                client.close();
                key.cancel();
                return;
            }

            ByteBuffer buffer = ByteBuffer.allocate(contentSize);
            client.read(buffer);
            buffer.flip();

            if (buffer.hasRemaining()) {
                Charset charset = StandardCharsets.UTF_8;
                String content = charset.decode(buffer).toString();
                handleContent(content);
            }
        } catch (IOException e) {
//            throw new RuntimeException();
        }
    }

    private void handleContent(String content) {
        String[] words = content.split(" ");
        int i = 0;
        switch (words[i]) {
            case "DIRECTION" -> {
                i++;
                int id = Integer.parseInt(words[i]);
                i++;
                Direction direction = Direction.valueOf(words[i].toUpperCase());
                try {
                    game.setPlayerDirection(id, direction);
                } catch (Exception ignored) {
                }
            }
            case "SAVE" -> {
                try (Writer output = new FileWriter(savePath)) {
                    output.write(game.generateSave());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            case "PAUSE" -> {
                if (game.isActive()) {
                    game.pause();
                } else {
                    game.resume();
                }
            }
            case "MESSAGE" -> {
                i++;
                int id = Integer.parseInt(words[i]);
                i++;
                String message = words[i];
                System.out.println("player " + id + ": " + message);
            }
        }
    }

    private void handleWritableKey(SelectionKey key) {
        if (drawTasks.isEmpty()) {
            return;
        }

        try {
            SocketChannel client = (SocketChannel) key.channel();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);

            oos.writeObject(drawTasks);
            byte[] bytes = baos.toByteArray();

            sendMessage("DRAWTASKS " + bytes.length, client);

            ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
            buffer.put(bytes);
            buffer.flip();
            client.write(buffer);
        } catch (IOException e) {
//            throw new RuntimeException();
        }
    }

    @Override
    public synchronized void gameUpdatePerform() {
        game.repaint();
        drawTasksReady = true;
    }

    public void sendMessage(String content, SocketChannel client) {
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        buffer.putInt(content.getBytes().length);
        buffer.put(content.getBytes());
        buffer.flip();

        try {
            client.write(buffer);
        } catch (IOException e) {
//            throw new RuntimeException(e);
        }
    }

    public static void addDrawTask(DrawTask drawTask) {
        gameServer.drawTasks.add(drawTask);
    }

    public static void main(String[] args) {
        String saveFile = "saves/" + args[0] + ".txt", savePath = "saves/" + args[1] + ".txt";
        int port = Integer.parseInt(args[2]);
        GameServer server = GameServer.getGameServer(saveFile, savePath, port);
        server.start();
    }
}