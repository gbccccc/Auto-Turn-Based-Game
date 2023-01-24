package game.creature;

import gui.DrawTask;
import gui.Drawable;
import game.world.Direction;
import game.world.Position;
import game.world.World;
import gui.GameWindow;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public abstract class Creature implements Drawable {
    protected int hp;
    private Position position;
    protected World world;
    String assetName;

    public Creature(int hp, String assetName) {
        this.hp = hp;
        this.assetName = assetName;
    }

    // generate a string representing this creature status
    public abstract String generateSave();

    public abstract boolean isEnemy();

    // check if a creature is adverse
    public boolean isAdverse(Creature creature) {
        return this.isEnemy() != creature.isEnemy();
    }

    public abstract void update();

    public void setWorld(World world) {
        this.world = world;
    }

    // enter a position, return true if enter successfully
    public boolean enterPosition(Position destination) {
        if (!destination.isMovable()) {
            return false;
        }

        if (destination.creatureGetIn(this)) {
            if (position != null) {
                position.creatureLeave();
            }
            this.position = destination;
            return true;
        } else {
            return false;
        }
    }

    public Position getPosition() {
        return position;
    }

    public int getX() {
        return position.getX();
    }

    public int getY() {
        return position.getY();
    }

    public synchronized void changeHp(int change) {
        hp += change;
    }

    public int getHp() {
        return hp;
    }

    public boolean isAlive() {
        return hp > 0;
    }

    // kill this creature
    public synchronized void die() {
        hp = 0;
    }

    // clean relevant things of this creature, usually called when this creature is dead.
    public void clean() {
        position.creatureLeave();
    }

    // move one step, return an adverse creature blocking this creature moving, may be null
    protected Creature moveStep(Direction direction) {
        int x, y;
        x = position.getX() + direction.getX();
        y = position.getY() + direction.getY();

        Position destination;
        try {
            destination = world.getPosition(x, y);
        } catch (Exception e) {
            return null;
        }

        if (destination.getCreature() != null && isAdverse(destination.getCreature())) {
            return destination.getCreature();
        } else {
            enterPosition(destination);
            return null;
        }
    }

    // calculate the hamilton distance to a creature
    protected int calculateDistance(Creature creature) {
        return Math.abs(this.position.getX() - creature.position.getX()) +
                Math.abs(this.position.getY() - creature.position.getY());
    }

    // receive a list of positions, get all adverse creatures in these positions
    protected List<Creature> getAllAdverseCreature(List<Position> positions) {
        List<Creature> adverseCreatures = new ArrayList<>();
        for (Position pos : positions) {
            if (isAdverse(pos.getCreature()))
                adverseCreatures.add(pos.getCreature());
        }
        return adverseCreatures;
    }

    protected abstract void attack(Creature creature);

    protected void changeState(int state) {
    }

    protected void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    @Override
    public DrawTask generateDrawTask(int x, int y, DrawTask foreground) {
        return new DrawTask(assetName, GameWindow.convertCoordinate(x), GameWindow.convertCoordinate(y), foreground);
    }

    public CreatureUpdate getUpdate() {
        return new CreatureUpdate();
    }

    public CreatureUpdate getUpdate(CountDownLatch doneSignal) {
        return new CreatureUpdate(doneSignal);
    }

    private class CreatureUpdate implements Runnable {
        CountDownLatch doneSignal;

        private CreatureUpdate() {
            this(null);
        }

        private CreatureUpdate(CountDownLatch doneSignal) {
            this.doneSignal = doneSignal;
        }

        @Override
        public void run() {
            Creature.this.update();

            if (doneSignal != null) {
                doneSignal.countDown();
            }
        }
    }
}