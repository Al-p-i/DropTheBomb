package gs.map;

import gs.geometry.Point;
import gs.model.Brick;
import gs.model.GameSession;
import gs.model.Wall;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by a.pomosov on 27/01/2018.
 */
public class GGJ2018MapGenerator implements MapGenerator {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(GGJ2018MapGenerator.class);
    private static String MAP_DIR = "maps/";

    @Override
    public void generateMap(GameSession gameSession) {
        final File jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
        log.info("JAR file: {}", jarFile.getAbsolutePath());
        Deque<String> lines = new ArrayDeque<>();
        InputStream mapInsputStream = gameSession.getClass().getClassLoader().getResourceAsStream("maps/ggj-2018.map");

        try (Scanner scanner = new Scanner(mapInsputStream)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                lines.addFirst(line);
            }
            scanner.close();
        } catch (Exception e) {
            log.error("Fail to read map {}", e);
        }

        //InputStream resourceAsStream = gameSession.getClass().getResourceAsStream("maps/ggs-2018");
        /*try {
            log.info(Arrays.toString(getResourceListing(GameSession.class, "/")));
            if (jarFile.isFile()) {  // Run with JAR file
                List<String> maps = new ArrayList<>();
                final JarFile jar = new JarFile(jarFile);
                final Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
                while (entries.hasMoreElements()) {
                    final String name = entries.nextElement().getName();
                    if (name.startsWith(MAP_DIR) && name.endsWith(".map")) { //filter according to the path
                        maps.add(entries.nextElement().getName());
                    }
                }
                log.info("Maps: {}", maps);
                jar.close();
                String map = maps.get(new Random().nextInt(maps.size()));
                log.info("Playing {} map", map);
                InputStream in = getClass().getResourceAsStream(map);
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                reader.lines().forEach(lines::addFirst);
            } else { // Run with IDE
                URL dirURL = GameSession.class.getClassLoader().getResource(MAP_DIR);
                //File mapsDir = new File(gameSession.getClass().getResource(MAP_DIR + "/").getFile());
                String[] fileList = new File(dirURL.toURI()).list();
                log.info("IDE maps list: {}", Arrays.toString(fileList));
                List<String> list = Stream.of(fileList).filter(s -> s.endsWith(".map")).collect(Collectors.toList());
                if (list == null) {
                    log.error("Fail to read maps list");
                    return;
                }
                if (list.isEmpty()) {
                    log.error("Empty maps dir");
                    return;
                }
                String mapName = list.get(new Random().nextInt(list.size()));
                String mapFile = dirURL.getPath() + mapName;
                log.info("Playing {} map", mapName);
                BufferedReader br = new BufferedReader(new FileReader(mapFile));
                String line;
                while ((line = br.readLine()) != null) {
                    lines.addFirst(line);
                }
            }
        } catch (Exception e) {
            log.error("Fail to read map file", e);
        }*/
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

    String[] getResourceListing(Class clazz, String path) throws URISyntaxException, IOException {
        URL dirURL = clazz.getClassLoader().getResource(path);
        if (dirURL != null && dirURL.getProtocol().equals("file")) {
            log.info("run from files");
        /* A file path: easy enough */
            return new File(dirURL.toURI()).list();
        }

        if (dirURL == null) {
        /*
         * In case of a jar file, we can't actually find a directory.
         * Have to assume the same jar as clazz.
         */
            String me = clazz.getName().replace(".", "/") + ".class";
            dirURL = clazz.getClassLoader().getResource(me);
        }

        if (dirURL.getProtocol().equals("jar")) {
            log.info("run from jar");
        /* A JAR path */
            String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
            JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
            Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
            Set<String> result = new HashSet<String>(); //avoid duplicates in case it is a subdirectory
            while (entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (name.startsWith(path)) { //filter according to the path
                    String entry = name.substring(path.length());
                    int checkSubdir = entry.indexOf("/");
                    if (checkSubdir >= 0) {
                        // if it is a subdirectory, we just return the directory name
                        entry = entry.substring(0, checkSubdir);
                    }
                    result.add(entry);
                }
            }
            return result.toArray(new String[result.size()]);
        }

        throw new UnsupportedOperationException("Cannot list files for URL " + dirURL);
    }
}
