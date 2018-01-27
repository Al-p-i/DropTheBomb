package mm.map;

import mm.geometry.Point;
import mm.model.Brick;
import mm.model.GameSession;
import mm.model.Spawn;
import mm.model.Wall;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Random;

/**
 * Created by a.pomosov on 27/01/2018.
 */
public class FromFileMapGenerator implements MapGenerator {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(FromFileMapGenerator.class);
    private static String MAP_DIR = "/maps/";

    @Override
    public void generateMap(GameSession gameSession) {
        try {
            File mapsDir = new File(gameSession.getClass().getResource(MAP_DIR).getFile());
            String[] list = mapsDir.list();
            if (list == null) {
                log.error("Fail to read maps list");
                return;
            }
            String mapName = list[new Random().nextInt(list.length)];
            String mapFile = mapsDir.toString() + "/" + mapName;
            log.info("Playing {} map", mapName);
            try (BufferedReader br = new BufferedReader(new FileReader(mapFile))) {
                String line;
                int y = 0;
                while ((line = br.readLine()) != null) {
                    // process the line.
                    int x = 0;
                    for (char c : line.toCharArray()) {
                        Point point = new Point(x, y);
                        if (c == 'b') {
                            gameSession.addGameObject(new Brick(gameSession, point));
                        }
                        if (c == 'w') {
                            gameSession.addGameObject(new Wall(gameSession, point));
                        }
                        if (c == 's') {
                            gameSession.addGameObject(new Spawn(gameSession, point));
                        }
                        x++;
                    }
                    y++;
                }
            }
        } catch (Exception e) {
            log.error("Fail to read map file", e);
        }
    }
}
