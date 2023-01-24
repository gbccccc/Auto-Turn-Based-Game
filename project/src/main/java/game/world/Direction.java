package game.world;

import java.security.SecureRandom;
import java.util.Random;

public enum Direction {
    UP(0, -1),
    DOWN(0, 1),
    LEFT(-1, 0),
    RIGHT(1, 0),
    STAY(0, 0);

    private int x, y;

    Direction(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Direction turnRight() {
        switch (this) {
            case UP -> {
                return RIGHT;
            }
            case RIGHT -> {
                return DOWN;
            }
            case DOWN -> {
                return LEFT;
            }
            case LEFT -> {
                return UP;
            }
            default -> {
                return STAY;
            }
        }
    }

    public Direction turnLeft() {
        switch (this) {
            case UP -> {
                return LEFT;
            }
            case RIGHT -> {
                return UP;
            }
            case DOWN -> {
                return RIGHT;
            }
            case LEFT -> {
                return DOWN;
            }
            default -> {
                return STAY;
            }
        }
    }

    // get a Direction by displacement x and y, return a proper Direction randomly if having multiple choices
    public static Direction getDirectionByDisplacement(int disX, int disY) {
        Direction first = disX > 0 ? RIGHT : LEFT;
        Direction second = disY > 0 ? DOWN : UP;

        if (disX == 0) {
            return second;
        } else if (disY == 0) {
            return first;
        } else {
            // return a choice randomly
            Random random = new SecureRandom();
            return random.nextInt(2) == 1 ? first : second;
        }
    }
}