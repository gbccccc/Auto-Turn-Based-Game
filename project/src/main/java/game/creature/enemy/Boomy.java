package game.creature.enemy;

import game.creature.Creature;
import game.creature.Player;
import game.effect.Effect;
import game.world.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Boomy extends Enemy {
    private int state;

    public Boomy(Player[] players) {
        super(20, "boomy-0.png", players);
        state = 0;
    }

    @Override
    public String generateSave() {
        return "Boomy " +
                hp + " " +
                state;
    }

    public static Boomy generateFromRecord(Scanner record, Player[] players) {
        Boomy boomy = new Boomy(players);
        boomy.hp = record.nextInt();
        boomy.changeState(record.nextInt());
        return boomy;
    }

    @Override
    public void update() {
        switch (state) {
            case 0 -> {
                Player player = findClosestPlayer();
                if (player != null) {

                    // move two steps
                    moveStep(tracePlayer(player));
                    moveStep(tracePlayer(player));

                    if (calculateDistance(player) <= 1) {
                        changeState(1);
                    }
                }
            }
            case 1 -> {
                changeState(2);
            }
            case 2 -> {
                attack();
            }
        }
    }

    @Override
    public void changeState(int state) {
        this.state = state;
        setAssetName("boomy-" + state + ".png");
    }

    protected void attack() {
        List<Position> range = generateRange();

        for (Position position : range) {
            Creature creature = position.getCreature();
            if (creature != null) {
                attack(creature);
            }
            position.addEffect(new Effect("explosion.png"));
        }

        // die after attacking
        die();
    }

    private List<Position> generateRange() {
        ArrayList<Position> range = new ArrayList<>();

        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2 + Math.abs(dx); dy <= 2 - Math.abs(dx); dy++) {
                try {
                    range.add(world.getPosition(getX() + dx, getY() + dy));
                } catch (Exception ignored) {
                }
            }
        }

        return range;
    }

    @Override
    protected void attack(Creature creature) {
        creature.changeHp(-10);
    }
}
