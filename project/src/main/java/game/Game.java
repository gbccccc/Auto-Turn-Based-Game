package game;

import game.creature.Player;
import game.creature.enemy.Enemy;
import game.world.Direction;
import game.world.World;

import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Game implements ActionListener {
    private World world;
    private final Player[] players;
    private final boolean[] playerBound;
    private int playerNum;
    private final List<Enemy> enemies;

    Timer timer;
    boolean hasBegun;

    private int summonCountDown;

    private boolean isPlayerTurn;
    GameUpdateListener updateListener;

    public Game(Reader saveReader) {
        players = new Player[4];
        playerBound = new boolean[]{false, false, false, false};
        playerNum = 0;
        enemies = Collections.synchronizedList(new LinkedList<>());

        renewSummonCountDown();
        isPlayerTurn = true;
        Scanner input = new Scanner(saveReader);

        while (input.hasNext()) {
            switch (input.next()) {
                case "GAME" -> {
                    handleRecordGame(input);
                }
                case "WORLD" -> {
                    handleRecordWorld(input);
                }
                case "ENEMY" -> {
                    handleRecordEnemy(input);
                }
                case "PLAYER" -> {
                    handleRecordPlayer(input);
                }
                // comment
                case "//" -> {
                    input.nextLine();
                }
            }
        }

        hasBegun = false;
        timer = new Timer(500, this);
    }

    public Game() {
        this(new InputStreamReader(
                Game.class.getClassLoader().getResourceAsStream("defaultSaves/defaultSave.txt")
        ));
    }

    private void handleRecordGame(Scanner input) {
        summonCountDown = input.nextInt();
        isPlayerTurn = input.nextBoolean();
    }

    private void handleRecordWorld(Scanner input) {
        String worldName = input.next();
        world = new World(worldName);
    }

    private void handleRecordPlayer(Scanner input) {
        Player player = new Player(input);
        int x = input.nextInt();
        int y = input.nextInt();
        if (world.addCreature(player, x, y)) {
            players[player.getID()] = player;
            playerNum++;
        }
    }

    private void handleRecordEnemy(Scanner input) {
        Enemy enemy = Enemy.generateEnemyByRecord(input, players);
        int x = input.nextInt();
        int y = input.nextInt();
        if (enemy != null) {
            addEnemy(enemy, x, y);
        }
    }

    public void setUpdateListener(GameUpdateListener updateListener) {
        this.updateListener = updateListener;
    }

    public void begin() {
        hasBegun = true;
        resume();
    }

    public boolean gameOver() {
        return hasBegun && playerNum == 0;
    }

    // players and enemies update in turn
    private void update() {
        if (isPlayerTurn) {
            playerUpdate();
        } else {
            enemyUpdate();

            if (summonCountDown > 0) {
                summonCountDown--;
            }
            if (summonCountDown == 0) {
                // mind: if summon enemy unsuccessfully, then will try summoning another enemy during next update
                if (summonEnemy()) {
                    renewSummonCountDown();
                }
            }
        }
        isPlayerTurn = !isPlayerTurn;

        if (gameOver()) {
            timer.stop();
        }

        if (updateListener != null) {
            updateListener.gameUpdatePerform();
        }
    }

    private void playerUpdate() {
        CountDownLatch doneSignal = new CountDownLatch(playerNum);
        for (Player player : players) {
            if (player != null) {
                if (player.isAlive()) {
                    executor.execute(player.getUpdate(doneSignal));
                } else {
                    doneSignal.countDown();
                }
            }
        }

        try {
            doneSignal.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        for (int i = 0; i < players.length; i++) {
            if (players[i] != null) {
                if (!players[i].isAlive()) {
                    players[i].clean();
                    players[i] = null;
                    playerNum--;
                }
            }
        }
    }

    private void enemyUpdate() {
        CountDownLatch doneSignal = new CountDownLatch(enemies.size());
        for (Enemy enemy : enemies) {
            if (enemy.isAlive()) {
                executor.execute(enemy.getUpdate(doneSignal));
            } else {
                doneSignal.countDown();
            }
        }

        try {
            doneSignal.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Iterator<Enemy> itr = enemies.listIterator();
        while (itr.hasNext()) {
            Enemy enemy = itr.next();
            if (!enemy.isAlive()) {
                enemy.clean();
                itr.remove();
            }
        }
    }

    // return player id
    // at most 4 players so may throw exception if room is full
    public int playerLink() throws Exception {
        int id = -1;
        for (int i = 0; i < players.length; i++) {
            if (!playerBound[i]) {
                id = i;
                break;
            }
        }
        if (id == -1) {
            throw new Exception("this room is full");
        }

        if (players[id] == null) {
            players[id] = new Player(id, Direction.RIGHT);

            // find an available position
            int x = 0, y = 0;
            while (!world.getPosition(x, y).isEmptyCreature()) {
                x++;
                if (x >= getWidth()) {
                    x = 0;
                    y++;
                    if (y >= getHeight()) {
                        throw new Exception("world is full");
                    }
                }
            }

            world.addCreature(players[id], x, y);
            playerNum++;
        }
        playerBound[id] = true;

        return id;
    }

    // summon a random enemy
    public boolean summonEnemy() {
        Random random = new SecureRandom();

        // mind: if summon enemy unsuccessfully, then will try summoning another enemy during next update
        return addEnemy(Enemy.generateRandomEnemy(players),
                random.nextInt(getWidth()), random.nextInt(getHeight()));
    }

    private void renewSummonCountDown() {
        Random random = new SecureRandom();
        summonCountDown = 10 - 2 * playerNum + random.nextInt(5);
    }

    // return true if add successfully
    public boolean addEnemy(Enemy enemy, int x, int y) {
        if (world.addCreature(enemy, x, y)) {
            enemies.add(enemy);
            return true;
        } else {
            return false;
        }
    }

    public void setPlayerDirection(int id, Direction direction) throws Exception {
        if (players[id] != null) {
            players[id].setDirection(direction);
        } else if (!playerBound[id]) {
            throw new Exception("illegal player id");
        }
    }

    public int getWidth() {
        return world.getWidth();
    }

    public int getHeight() {
        return world.getHeight();
    }

    public void repaint() {
        world.repaint();
    }

    private static final ExecutorService executor = Executors.newCachedThreadPool();

    public static ExecutorService getExecutor() {
        return executor;
    }

    public void pause() {
        timer.stop();
    }

    public boolean isActive() {
        return timer.isRunning();
    }

    public void resume() {
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.update();
    }

    public String generateSave() {
        StringBuilder save = new StringBuilder();

        save.append("GAME ").append(summonCountDown).append(" ").append(isPlayerTurn).append("\n");

        save.append("WORLD ").append(world.getName()).append("\n");

        for (Player player : players) {
            if (player != null) {
                save.append("PLAYER ").append(player.generateSave()).append(" ");
                save.append(player.getX()).append(" ").append(player.getY());
                save.append("\n");
            }
        }

        for (Enemy enemy : enemies) {
            save.append("ENEMY ").append(enemy.generateSave()).append(" ");
            save.append(enemy.getX()).append(" ").append(enemy.getY());
            save.append("\n");
        }

        return save.toString();
    }
}
