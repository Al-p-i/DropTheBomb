package mm.mechanics;

import mm.model.GameSession;
import org.slf4j.LoggerFactory;

/**
 * Created by a.pomosov on 27/01/2018.
 */
public interface BombGenerator {
    org.slf4j.Logger log = LoggerFactory.getLogger(BombGenerator.class);
    void generateBombs(GameSession gameSession);
}
