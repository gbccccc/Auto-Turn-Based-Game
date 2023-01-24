package game.world;

import game.creature.*;
import game.effect.Effect;
import game.item.*;
import gui.DrawTask;
import gui.GameWindow;
import networking.GameServer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class Position {
    private Terrain terrain;
    private Creature creature;
    private Item item;
    private final List<Effect> effects;
    private final int x, y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
        effects = Collections.synchronizedList(new ArrayList<>());
    }

    public boolean isMovable() {
        return terrain.isMovable();
    }

    public boolean isEmptyCreature() {
        return creature == null;
    }

    public boolean isEmptyItem() {
        return item == null;
    }

    public void setTerrain(Terrain terrain) {
        this.terrain = terrain;
    }

    public Terrain getTerrain() {
        return terrain;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void repaint() {
        DrawTask task = null;
        if (effects != null) {
            for (Effect effect : effects) {
                task = effect.generateDrawTask(x, y, task);
            }
            effects.clear();
        }
        if (creature != null) {
            task = creature.generateDrawTask(x, y, task);
        }
        if (item != null) {
            task = item.generateDrawTask(x, y, task);
        }
        task = terrain.generateDrawTask(x, y, task);
        GameServer.addDrawTask(task);
    }

//    public synchronized void setCreature(Creature creature) {
//        this.creature = creature;
//    }

    public synchronized void creatureLeave() {
        this.creature = null;
    }

    /*
     * let a creature get in this position, may be rejected if another creature have been in already
     * return true if creature get in successfully
     * should be called only by enterPosition() in case of inconsistency
     */
    public synchronized boolean creatureGetIn(Creature creature) {
        if (!isEmptyCreature()) {
            return false;
        }

        this.creature = creature;
        return true;
    }

    public Creature getCreature() {
        return creature;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Item getItem() {
        return item;
    }

    public void addEffect(Effect effect) {
        effects.add(effect);
    }

    public PositionRepaint getRepaint(CountDownLatch doneSignal) {
        return new PositionRepaint(doneSignal);
    }

    public PositionRepaint getRepaint() {
        return new PositionRepaint();
    }

    private class PositionRepaint implements Runnable {
        CountDownLatch doneSignal;

        private PositionRepaint() {
            this(null);
        }

        private PositionRepaint(CountDownLatch doneSignal) {
            this.doneSignal = doneSignal;
        }

        @Override
        public void run() {
            Position.this.repaint();

            if (doneSignal != null) {
                doneSignal.countDown();
            }
        }
    }
}