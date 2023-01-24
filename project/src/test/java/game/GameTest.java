package game;

import game.creature.Player;
import game.creature.enemy.Enemy;
import game.creature.enemy.Slime;
import game.world.Direction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameTest {
    Game game = new Game();
    Player[] players;

    @Test
    void playerLink() {
        int i = 0;
        try {
            for (i = 0; i < 5; i++) {
                game.playerLink();
            }
            fail("room is full");
        } catch (Exception e) {
            if (i < 4) {
                fail("room is not full");
            }
        }
    }

    @Test
    void setPlayerDirection() {
        Game game = new Game();
        int playerA = 0, playerB = 0;
        try {
            playerA = game.playerLink();
            playerB = game.playerLink();
        } catch (Exception e) {
            fail("room is not full");
        }
        try {
            game.setPlayerDirection(playerA, Direction.DOWN);
            game.setPlayerDirection(playerB, Direction.LEFT);
        } catch (Exception e) {
            fail("legal id");
        }
        try {
            game.setPlayerDirection(3, Direction.LEFT);
            fail("illegal id");
        } catch (Exception ignored) {
        }

    }

    @Test
    void getWidth() {
        assert game.getWidth() == 24;
    }

    @Test
    void getHeight() {
        assert game.getHeight() == 18;
    }

    @Test
    void addEnemy() {
        Enemy enemy1 = new Slime(players);
        Enemy enemy2 = new Slime(players);
        boolean success;
        success = game.addEnemy(enemy1, 10, 10);
        assert success;
        success = game.addEnemy(enemy2, 10, 10);
        assert !success;
        success = game.addEnemy(enemy2, 12, 10);
        assert success;
    }
}