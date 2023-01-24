package game.world;

import game.creature.Player;
import game.creature.enemy.Skeleton;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PositionTest {
    Position position = new Position(12, 8);
    Player[] players;

    @Test
    void isEmptyCreature() {
        assert position.isEmptyCreature();
        position.creatureGetIn(new Skeleton(players));
        assert !position.isEmptyCreature();
        position.creatureLeave();
        assert position.isEmptyCreature();
    }

    @Test
    void setTerrain() {
        position.setTerrain(Terrain.FLOOR);
        assert position.getTerrain() == Terrain.FLOOR;
        position.setTerrain(Terrain.WALL);
        assert position.getTerrain() == Terrain.WALL;
    }


    @Test
    void getX() {
        assert position.getX() == 12;
    }

    @Test
    void getY() {
        assert position.getY() == 8;
    }

    @Test
    void creatureGetIn() {
        boolean success;
        success = position.creatureGetIn(new Skeleton(players));
        assert success;
        success = position.creatureGetIn(new Skeleton(players));
        assert !success;
    }
}