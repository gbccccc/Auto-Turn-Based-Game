package game.creature.enemy;

import game.creature.Creature;
import game.creature.Player;
import game.effect.Effect;
import game.world.Direction;

import java.util.Scanner;

public class Slime extends Enemy {
    public Slime(Player[] players) {
        super(5, "slime.png", players);
        direction = Direction.RIGHT;
    }

    private Direction direction;

    @Override
    public String generateSave() {
        return "Slime " +
                hp + " " +
                direction.name();
    }

    public static Slime generateFromRecord(Scanner record, Player[] players) {
        Slime slime = new Slime(players);
        slime.hp = record.nextInt();
        slime.direction = Direction.valueOf(record.next().toUpperCase());
        return slime;
    }

    @Override
    public void update() {
        Creature adverseCreature = moveStep(direction);
        if (adverseCreature != null) {
            attack(adverseCreature);
        }
        direction = direction.turnRight();
    }

    @Override
    protected void attack(Creature creature) {
        creature.changeHp(-5);
        creature.getPosition().addEffect(new Effect("hit.png"));
    }
}