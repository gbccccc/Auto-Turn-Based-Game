package game.creature.enemy;

import game.creature.Player;
import game.world.Direction;
import game.world.World;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnemyTest {
    World world = new World("empty");
    Player[] players;

    @BeforeEach
    void start() {
        players = new Player[4];
        players[0] = new Player(0, Direction.RIGHT);
        world.addCreature(players[0], 10, 17);
        players[1] = new Player(1, Direction.RIGHT);
        world.addCreature(players[1], 1, 0);
        players[2] = new Player(2, Direction.RIGHT);
        world.addCreature(players[2], 16, 8);
        players[3] = new Player(3, Direction.RIGHT);
        world.addCreature(players[3], 23, 3);
    }

    @Test
    void isEnemy() {
        Skeleton skeleton = new Skeleton(players);
        assert skeleton.isEnemy();
    }

    @Test
    void findClosestPlayer() {
        Skeleton skeleton1 = new Skeleton(players);
        world.addCreature(skeleton1, 14, 10);
        assert skeleton1.findClosestPlayer().equals(players[2]);

        Skeleton skeleton2 = new Skeleton(players);
        world.addCreature(skeleton1, 2, 14);
        assert skeleton1.findClosestPlayer().equals(players[0]);
    }

    @Test
    void tracePlayer() {
        Skeleton skeleton = new Skeleton(players);
        world.addCreature(skeleton, 10, 10);
        Direction direction;

        direction = skeleton.tracePlayer(players[0]);
        assert direction == Direction.DOWN;
        direction = skeleton.tracePlayer(players[1]);
        assert direction == Direction.LEFT || direction == Direction.UP;
        direction = skeleton.tracePlayer(players[2]);
        assert direction == Direction.RIGHT || direction == Direction.UP;
        direction = skeleton.tracePlayer(players[3]);
        assert direction == Direction.RIGHT || direction == Direction.UP;
    }

    @Test
    void changeHp() {
        Skeleton skeleton = new Skeleton(players);
        int originHp = skeleton.getHp();
        skeleton.changeHp(-5);
        assert originHp - skeleton.getHp() == 5;
    }
}