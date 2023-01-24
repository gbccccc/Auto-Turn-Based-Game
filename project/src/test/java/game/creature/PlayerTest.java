package game.creature;

import game.world.Direction;
import game.world.World;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {
    World world = new World("empty");

    @Test
    void getID() {
        Player player = new Player(1, Direction.RIGHT);
        assert player.getID() == 1;
    }

    @Test
    void isEnemy() {
        Player player = new Player(0, Direction.RIGHT);
        assert !player.isEnemy();
    }

    @Test
    void update() {
        Player player = new Player(1, Direction.RIGHT);
        world.addCreature(player, 10, 10);

        player.update();
        assert player.getX() == 11 && player.getY() == 10;
        player.setDirection(Direction.DOWN);
        player.update();
        assert player.getX() == 11 && player.getY() == 11;
        player.setDirection(Direction.RIGHT);
        player.update();
        assert player.getX() == 12 && player.getY() == 11;
    }
}