package game.creature.enemy;

import game.creature.Creature;
import game.creature.Player;
import game.effect.Effect;
import game.world.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Fungus extends Enemy {
    private int state;

    public Fungus(Player[] players) {
        super(10, "fungus-0.png", players);
        state = 0;
    }

    @Override
    public void update() {
        if (state == 3) {
            attack();
        }

        changeState((state + 1) % 4);
    }

    @Override
    public void changeState(int state) {
        this.state = state;
        setAssetName("fungus-" + state + ".png");
    }

    private void attack() {
        List<Position> range = generateRange();

        for (Position position : range) {
            Creature creature = position.getCreature();
            if (creature != null && isAdverse(creature)) {
                attack(creature);
            }
            position.addEffect(new Effect("spore.png"));
        }
    }

    private List<Position> generateRange() {
        ArrayList<Position> range = new ArrayList<>();
        int x = getX(), y = getY();

        for (int targetX = x - 2; targetX <= x + 2; targetX++) {
            if (targetX == x) {
                continue;
            }
            try {
                range.add(world.getPosition(targetX, y));
            } catch (Exception ignored) {
            }
        }
        for (int targetY = y - 2; targetY <= y + 2; targetY++) {
            if (targetY == y) {
                continue;
            }
            try {
                range.add(world.getPosition(x, targetY));
            } catch (Exception ignored) {
            }
        }

        return range;
    }

    @Override
    protected void attack(Creature creature) {
        creature.changeHp(-5);
    }

    @Override
    public String generateSave() {
        return "Fungus " +
                hp + " " +
                state;
    }

    public static Fungus generateFromRecord(Scanner record, Player[] players) {
        Fungus fungus = new Fungus(players);
        fungus.hp = record.nextInt();
        fungus.changeState(record.nextInt());
        return fungus;
    }
}
