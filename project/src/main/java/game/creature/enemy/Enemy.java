package game.creature.enemy;

import game.creature.Creature;
import game.creature.Player;
import game.world.Direction;

import java.security.SecureRandom;
import java.util.Random;
import java.util.Scanner;

public abstract class Enemy extends Creature {
    Player[] players;

    public Enemy(int hp, String assetName, Player[] players) {
        super(hp, assetName);
        this.players = players;
    }

    @Override
    public boolean isEnemy() {
        return true;
    }

    // find the closest player
    protected Player findClosestPlayer() {
        int maxDistance = -1;
        Player closetPlayer = null;
        for (Player player : players) {
            if (player != null) {
                int distance = calculateDistance(player);
                if (distance < maxDistance || maxDistance == -1) {
                    maxDistance = distance;
                    closetPlayer = player;
                }
            }
        }

        return closetPlayer;
    }

    // get a Direction to trace a player
    protected Direction tracePlayer(Player player) {
        if (player == null) {
            return Direction.STAY;
        } else {
            return Direction.getDirectionByDisplacement(
                    player.getX() - this.getX(),
                    player.getY() - this.getY()
            );
        }
    }

    public static Enemy generateRandomEnemy(Player[] players) {
        Random random = new SecureRandom();
        switch (random.nextInt(4)) {
            case 0 -> {
                return new Slime(players);
            }
            case 1 -> {
                return new Skeleton(players);
            }
            case 2 -> {
                return new Fungus(players);
            }
            case 3 -> {
                return new Boomy(players);
            }
            default -> {
                throw new RuntimeException();
            }
        }
    }

    public static Enemy generateEnemyByRecord(Scanner record, Player[] players) {
        Enemy enemy;
        switch (record.next()) {
            case "Slime" -> {
                enemy = Slime.generateFromRecord(record, players);
            }
            case "Skeleton" -> {
                enemy = Skeleton.generateFromRecord(record, players);
            }
            case "Fungus" -> {
                enemy = Fungus.generateFromRecord(record, players);
            }
            case "Boomy" -> {
                enemy = Boomy.generateFromRecord(record, players);
            }
            default -> {
                enemy = null;
            }
        }
        return enemy;
    }
}