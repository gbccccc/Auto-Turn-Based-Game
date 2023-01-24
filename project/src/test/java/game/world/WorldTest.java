package game.world;

import static org.junit.jupiter.api.Assertions.*;

class WorldTest {
    World testWorld = new World("world");

    @org.junit.jupiter.api.Test
    void getName() {
        assert testWorld.getName().equals("world");
    }

    @org.junit.jupiter.api.Test
    void getWidth() {
        assert testWorld.getWidth() == 24;
    }

    @org.junit.jupiter.api.Test
    void getHeight() {
        assert testWorld.getHeight() == 18;
    }

    @org.junit.jupiter.api.Test
    void getPosition(){
        try {
            testWorld.getPosition(19, 19);
            fail("did not check illegal coordinate");
        } catch (Exception ignored) {
        }
        try {
            testWorld.getPosition(-1, 14);
            fail("did not check illegal coordinate");
        } catch (Exception ignored) {
        }
    }
}