package game.creature.enemy;

import game.creature.Player;
import game.world.Direction;
import game.world.World;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SlimeTest {
    World world = new World("empty");
    Player[] players;

    @Test
    void update() {
        Slime slime = new Slime(players);
        world.addCreature(slime, 10, 10);

        assert slime.getX() == 10 && slime.getY() == 10;
        slime.update();
        assert slime.getX() == 11 && slime.getY() == 10;
        slime.update();
        assert slime.getX() == 11 && slime.getY() == 11;
        slime.update();
        assert slime.getX() == 10 && slime.getY() == 11;
        slime.update();
        assert slime.getX() == 10 && slime.getY() == 10;
    }

    @Test
    void attack() {
        Slime slime = new Slime(players);
        world.addCreature(slime, 10, 10);
        Player player = new Player(0, Direction.RIGHT);
        world.addCreature(player, 11, 10);
        int originPlayerHp = player.getHp();

        slime.update();
        assert originPlayerHp - player.getHp() == 5;
        assert slime.getX() == 10 && slime.getY() == 10;

        slime.update();
        assert slime.getX() == 10 && slime.getY() == 11;
    }
}