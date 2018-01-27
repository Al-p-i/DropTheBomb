package gs.map;

import gs.geometry.Point;
import gs.model.Brick;
import gs.model.GameSession;
import gs.model.Wall;
import org.slf4j.LoggerFactory;

/**
 * Created by a.pomosov on 26/01/2018.
 */
public class EmptyMapGenerator implements MapGenerator {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(EmptyMapGenerator.class);

    @Override
    public void generateMap(GameSession session) {
        log.info("Generate map...");
        for (int x = 0; x < 27; x++) {
            addWall(session, new Point(x, 0));
            addWall(session, new Point(x, 16));
        }
        for (int y = 1; y < 16; y++) {
            addWall(session, new Point(0, y));
            addWall(session, new Point(26, y));
        }
        for (int i = 3; i < 23; i++) {
            addBrick(session, new Point(i, 1));
            addBrick(session, new Point(i, 15));
        }
        for (int i = 3; i < 13; i++) {
            addBrick(session, new Point(1, i));
            addBrick(session, new Point(25, i));
        }
        log.info("Map generated");
    }

    private static void addBrick(GameSession session, Point position) {
        session.addGameObject(new Brick(session, position));
    }

    private static void addWall(GameSession session, Point position) {
        session.addGameObject(new Wall(session, position));
    }
}
