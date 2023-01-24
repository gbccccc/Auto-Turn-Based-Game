package game.creature;

import game.effect.Effect;
import game.world.Direction;

import java.util.Scanner;

public class Player extends Creature {
    int id;
    Direction direction;

    public Player(int id, Direction direction) {
        super(30, "player-" + id + ".png");
        this.id = id;
        this.direction = direction;
    }

    public Player(Scanner record) {
        super(0, "");
        record.next(); // absorb "Player"
        id = record.nextInt();
        hp = record.nextInt();
        direction = Direction.valueOf(record.next().toUpperCase());
        setAssetName("player-" + id + ".png");
    }

    public int getID() {
        return id;
    }

    @Override
    public String generateSave() {
        return "Player " +
                id + " " +
                hp + " " +
                direction.name();
    }

    @Override
    public boolean isEnemy() {
        return false;
    }

    @Override
    public void update() {
        Creature adverseCreature = moveStep(direction);
        if (adverseCreature != null) {
            attack(adverseCreature);
        }
    }

    @Override
    protected void attack(Creature creature) {
        creature.changeHp(-5);
        creature.getPosition().addEffect(new Effect("hit.png"));
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }
}