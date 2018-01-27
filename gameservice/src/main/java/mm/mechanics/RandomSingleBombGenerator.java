package mm.mechanics;

import mm.model.Bomb;
import mm.model.GameSession;
import mm.model.Player;

import java.util.Random;

/**
 * Created by a.pomosov on 27/01/2018.
 */
public class RandomSingleBombGenerator implements BombGenerator {
    @Override
    public void generateBombs(GameSession gameSession) {
        Random random = new Random();
        Player player = gameSession.getPlayers().get(random.nextInt(gameSession.getPlayers().size()));
        Bomb bomb = new Bomb(gameSession, player.getPosition(), player);
        player.setBomb(bomb);
        gameSession.addGameObject(bomb);
        log.info(player + " get bomb");
    }
}
