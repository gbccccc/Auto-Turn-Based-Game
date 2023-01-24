package networking;

import gui.DrawTask;
import gui.GameWindow;
import mylib.ByteBufferBackedInputStream;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static networking.GameServer.BUFFER_SIZE;
import static networking.GameServer.INT_SIZE;


public class GameClient extends Thread {
    private GameWindow gameWindow;
    private SocketChannel server;

    private boolean isClosing;

    public GameClient(String serverName, int port) throws IOException {
        SocketAddress address = new InetSocketAddress(serverName, port);
        this.server = SocketChannel.open(new InetSocketAddress(serverName, port));
        this.server.configureBlocking(false);

        isClosing = false;
    }

    @Override
    public void run() {
        ByteBuffer bufferInt = ByteBuffer.allocate(INT_SIZE);
        while (!isClosing && server.isOpen()) {
            try {
                server.read(bufferInt);
                bufferInt.flip();
                if (!bufferInt.hasRemaining()) {
                    bufferInt.clear();
                    continue;
                }

                int contentSize = bufferInt.getInt();
                bufferInt.clear();

                if (contentSize == -1) {
                    server.close();
                    continue;
                }

                ByteBuffer buffer = ByteBuffer.allocate(contentSize);
                buffer.flip();
                while (!buffer.hasRemaining()) {
                    buffer.clear();
                    server.read(buffer);
                    buffer.flip();
                }

                Charset charset = StandardCharsets.UTF_8;
                String content = charset.decode(buffer).toString();
                handleContent(content);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        gameWindow.dispose();
        System.exit(0);
    }

    public void close() {
        isClosing = true;
    }

    private void handleContent(String content) {
        String[] words = content.split(" ");
        switch (words[0]) {
            case "DRAWTASKS" -> {
                int bufferSize = Integer.parseInt(words[1]);
                handleDrawTasks(bufferSize);
            }
            case "LINKED" -> {
                int id = Integer.parseInt(words[1]);
                int width = Integer.parseInt(words[2]), height = Integer.parseInt(words[3]);
                gameWindow = new GameWindow(this, id, width, height);
                gameWindow.setVisible(true);
            }
            case "GAMEOVER" ->{
                try {
                    System.out.println("Game Over!");
                    server.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            case "MESSAGE" -> {
                System.out.println("From Server: " + words[1]);
            }
        }
    }

    private void handleDrawTasks(int bufferSize) {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
            int readByteCount = 0;
            while (readByteCount < bufferSize) {
                readByteCount += server.read(buffer);
            }
            buffer.flip();
            ByteBufferBackedInputStream is = new ByteBufferBackedInputStream(buffer);
            ObjectInputStream ois = new ObjectInputStream(is);

            Object object = ois.readObject();
            if (object instanceof List<?> objects) {
                List<DrawTask> drawTasks = new ArrayList<>();
                objects.forEach(
                        o -> {
                            if (o instanceof DrawTask drawTask)
                                drawTasks.add(drawTask);
                        }
                );
                gameWindow.setDrawTasks(drawTasks);
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(String content) {
        if (gameWindow == null) {
            return;
        }
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        buffer.putInt(content.getBytes().length);
        buffer.put(content.getBytes());
        buffer.flip();

        try {
            server.write(buffer);
        } catch (IOException e) {
//            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        String serverName = args[0];
        int port = Integer.parseInt(args[1]);
        try {
            GameClient client = new GameClient(serverName, port);
            client.start();
        } catch (IOException e) {
            System.out.println("Failed linking server!");
        }
    }
}