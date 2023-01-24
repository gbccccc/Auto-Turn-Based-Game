package game.creature.enemy;

import game.creature.Creature;
import game.creature.Player;
import game.effect.Effect;

import java.util.Scanner;

public class Skeleton extends Enemy {
    private int state;

    public Skeleton(Player[] players) {
        super(15, "skeleton-0.png", players);
        state = 0;
    }

    @Override
    public String generateSave() {
        return "Skeleton " +
                hp + " " +
                state;
    }

    public static Skeleton generateFromRecord(Scanner record, Player[] players) {
        Skeleton skeleton = new Skeleton(players);
        skeleton.hp = record.nextInt();
        skeleton.changeState(record.nextInt());
        return skeleton;
    }

    @Override
    public void update() {
        switch (state) {
            case 0 -> {
            }

            // move and attack
            case 1 -> {
                Creature adverseCreature = moveStep(tracePlayer(findClosestPlayer()));
                if (adverseCreature != null) {
                    attack(adverseCreature);
                }
            }
        }

        changeState((state + 1) % 2);
    }

    @Override
    public void changeState(int state) {
        this.state = state;
        setAssetName("skeleton-" + state + ".png");
    }

    @Override
    protected void attack(Creature creature) {
        creature.changeHp(-5);
        creature.getPosition().addEffect(new Effect("scratch.png"));
    }
}
