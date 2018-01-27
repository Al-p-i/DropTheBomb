package gs.ticker;

import gs.mechanics.BombGenerator;
import gs.mechanics.GameMechanics;
import gs.mechanics.RandomSingleBombGenerator;
import gs.model.*;

import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class Ticker extends Thread {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(Ticker.class);
    private static final int FPS = 60;
    private static final int FRAME_TIME = 1000 / FPS;
    private boolean isRunning;
    private BombGenerator bombGiver = new RandomSingleBombGenerator();
    private GameSession gameSession;
    private final GameMechanics gameMechanics;

    public Ticker(GameSession session) {
        this.gameSession = session;
        gameMechanics = new GameMechanics(gameSession);
    }

    @Override
    public void run() {
        startGameActions();
        while (!Thread.currentThread().isInterrupted()) {
            if (isRunning) {
                long started = System.currentTimeMillis();
                gameMechanics.tick(FRAME_TIME);
                long elapsed = System.currentTimeMillis() - started;
                if (elapsed < FRAME_TIME) {
                    LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(FRAME_TIME - elapsed));
                } else {
                    log.warn("tick lag {} ms", elapsed - FRAME_TIME);
                }
            } else {
                log.info("THE END!");
                return;
            }
        }
        postGameActions();
    }

    private void postGameActions() {
        log.info("post-game actions");
        log.info("post-game actions finished");
    }

    private void startGameActions() {
        log.info("before-game actions");
        bombGiver.generateBombs(gameSession); //TODO Unsafe construction
        log.info("before-game actions finished");
    }

    public void begin() {
        isRunning = true;
    }

    public void kill() {
        isRunning = false;
    }

    public void putAction(Action action) {
        gameMechanics.putAction(action);
    }

}
