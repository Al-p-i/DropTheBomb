package mm.mm;

import mm.geometry.Point;
import mm.model.Brick;
import mm.model.GameSession;
import mm.model.Wall;
import org.slf4j.LoggerFactory;

/**
 * Created by a.pomosov on 26/01/2018.
 */
public class EmptyMapGenerator implements MapGenerator {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(EmptyMapGenerator.class);

    @Override
    public void generateMap(GameSession session) {
        log.info("Generate map...");
        for (int x = 0; x < 17; x++) {
            addWall(session, new Point(x, 0));
            addWall(session, new Point(x, 12));
        }
        for (int y = 1; y < 12; y++) {
            addWall(session, new Point(0, y));
            addWall(session, new Point(16, y));
        }
        for (int i = 3; i < 10; i++) {
            addBrick(session, new Point(15, i));
            addBrick(session, new Point(1, i));
        }
        for (int i = 3; i < 14; i++) {
            addBrick(session, new Point(i, 11));
            addBrick(session, new Point(i, 1));
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
