package game.creature.enemy;

import game.creature.Player;
import game.world.Direction;
import game.world.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SkeletonTest {
    World world = new World("empty");
    Player[] players;

    @BeforeEach
    void start() {
        players = new Player[4];
        players[0] = new Player(0, Direction.RIGHT);
        world.addCreature(players[0], 3, 15);
        players[1] = new Player(0, Direction.RIGHT);
        world.addCreature(players[1], 8, 12);
    }

    @Test
    void update() {
        Skeleton skeleton = new Skeleton(players);
        world.addCreature(skeleton, 3, 8);

        skeleton.update();
        assert skeleton.getX() == 3 && skeleton.getY() == 8;
        skeleton.update();
        assert skeleton.getX() == 3 && skeleton.getY() == 9;
    }

    @Test
    void attack() {
        Skeleton skeleton = new Skeleton(players);
        world.addCreature(skeleton, 7, 12);
        int originPlayer1Hp = players[1].getHp();

        skeleton.update();
        assert skeleton.getX() == 7 && skeleton.getY() == 12;
        assert originPlayer1Hp == players[1].getHp();
        skeleton.update();
        assert skeleton.getX() == 7 && skeleton.getY() == 12;
        assert originPlayer1Hp - players[1].getHp() == 5;
    }
}