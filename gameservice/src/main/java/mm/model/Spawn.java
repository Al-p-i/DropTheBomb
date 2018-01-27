package mm.model;

import mm.geometry.Point;
import org.slf4j.LoggerFactory;

public class Spawn extends GameObject {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Spawn.class);
    private static final int WALL_WIDTH = 32;
    private static final int WALL_HEIGHT = 32;

    public Spawn(GameSession session, Point position) {
        super(session, new Point(position.getX() * GameObject.getWidthBox(),
                        position.getY() * GameObject.getWidthBox()),
                "Spawn", WALL_WIDTH, WALL_HEIGHT);
    }
}