package gs.map;

import gs.geometry.Point;
import gs.model.Brick;
import gs.model.GameSession;
import gs.model.Wall;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by a.pomosov on 27/01/2018.
 */
public class FromFileMapGenerator implements MapGenerator {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(FromFileMapGenerator.class);
    private static String MAP_DIR = "/maps/";

    @Override
    public void generateMap(GameSession gameSession) {
        File mapsDir = new File(gameSession.getClass().getResource(MAP_DIR).getFile());
        List<String> list = Stream.of(mapsDir.list()).filter(s -> s.endsWith(".map")).collect(Collectors.toList());
        if (list == null) {
            log.error("Fail to read maps list");
            return;
        }
        String mapName = list.get(new Random().nextInt(list.size()));
        String mapFile = mapsDir.toString() + "/" + mapName;
        log.info("Playing {} map", mapName);
        Deque<String> lines = new ArrayDeque<>();
        try (BufferedReader br = new BufferedReader(new FileReader(mapFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.addFirst(line);
            }
        } catch (Exception e) {
            log.error("Fail to read map file", e);
        }
        int y = 0;
        for (String line : lines) {
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
                //if (c == 's') {
                //    gameSession.addGameObject(new Spawn(gameSession, point));
                //}
                x++;
            }
            y++;
        }
    }
}
