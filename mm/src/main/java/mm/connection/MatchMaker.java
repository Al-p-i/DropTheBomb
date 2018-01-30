package mm.connection;

import mm.network.GSApiClient;
import mm.service.MatchmakerService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class MatchMaker extends Thread {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(MatchMaker.class);
    private static final int PLAYERS_IN_GAME = 2;
    private long gameId = -1;
    @Autowired
    NextGameQueue nextGameQueue;

    @Autowired
    GSApiClient client;

    @Autowired
    MatchmakerService service;

    @Override
    public void run() {
        gameId = Long.parseLong(client.createPost(PLAYERS_IN_GAME));
        logger.info("Game created id={}", gameId);
        while (!Thread.currentThread().isInterrupted()) {
            try {
                String player = ConnectionQueue.getInstance().poll(10_000, TimeUnit.SECONDS);
                nextGameQueue.addToQueue(player);
                putJoin(player, gameId);
            } catch (InterruptedException e) {
                logger.warn("Timeout reached");
            }
            if (nextGameQueue.size() == PLAYERS_IN_GAME) {
                nextGameQueue.clear();
                gameId = Long.parseLong(client.createPost(PLAYERS_IN_GAME));
            }
        }
    }

    private void putJoin(String player, long gameId) {
        Joins.getInstance().put(player, gameId);
    }
}
