package gs.mechanics;

import gs.geometry.Bar;
import gs.geometry.Point;
import gs.message.Topic;
import gs.model.*;
import gs.storage.SessionStorage;
import gs.ticker.Action;
import gs.util.GameConstants;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import static gs.message.Topic.GAME_OVER;

/**
 * Created by a.pomosov on 27/01/2018.
 */
public class GameMechanics {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(GameMechanics.class);

    private final Queue<Action> inputQueue = new LinkedBlockingQueue<Action>();
    private final GameSession gameSession;
    private ArrayList<Player> movedPlayers = new ArrayList<>(4);
    private Set<GameObject> changedObjects = new HashSet<>();
    private ArrayList<Player> deadPlayers = new ArrayList<>();

    public GameMechanics(GameSession gameSession) {
        this.gameSession = gameSession;
    }

    public void tick(int frameTime) {
        changedObjects.clear();
        deadPlayers.clear();
        handleQueue();
        tickTickables(frameTime);
        checkCollisions(frameTime);
        bombDetonation();
        randomTransmit();
        sendReplica();
    }


    public void putAction(Action action) {
        inputQueue.offer(action);
    }

    private void sendReplica() {
        for (WebSocketSession session : SessionStorage.getWebsocketsByGameSession(gameSession)) {
            changedObjects.addAll(gameSession.getAllBombs());
            SessionStorage.send(session, Topic.REPLICA, changedObjects);
            SessionStorage.getPlayerBySocket(session).setDirection(Movable.Direction.IDLE);
        }

        ArrayList<WebSocketSession> sessions = SessionStorage.getWebsocketsByGameSession(gameSession);
        ArrayList<WebSocketSession> closed = new ArrayList<>();
        for (WebSocketSession session : sessions) {
            for (Player player : deadPlayers) {
                if (SessionStorage.getPlayerBySocket(session) == player) {
                    closed.add(session);
                    SessionStorage.send(session, GAME_OVER, "YOU LOSE");
                }
            }
        }
        for (WebSocketSession session : closed) {
            try {
                session.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleQueue() {
        movedPlayers.clear();
        for (Action action : inputQueue) {
            if (action.getAction().equals(Topic.PLANT_BOMB)
                    && action.getActor().getBombCapacity() != 0) {
                Bomb bomb = new Bomb(gameSession,
                        closestPoint(action.getActor().getPosition()), action.getActor(), Bomb.DEFAULT_PLANTED_BOMB_LIFETIME);
                action.getActor().decBombCapacity();
                gameSession.addGameObject(bomb);
                changedObjects.add(bomb);
            }
            if (action.getAction().equals(Topic.JUMP) && action.getActor().canJump()) {
                action.getActor().jump();
                action.getActor().restartJumpTimer();
                movedPlayers.add(action.getActor());
            }
            if (action.getAction().equals(Topic.MOVE)
                    && !movedPlayers.contains(action.getActor())) {
                action.getActor().setDirection(action.getData());
                movedPlayers.add(action.getActor());
            }
        }
        inputQueue.clear();
    }

    private void tickTickables(int elapsed) {
        for (GameObject object : gameSession.getGameObjects()) {
            if (object instanceof Tickable)
                ((Tickable) object).tick(elapsed);
        }
    }

    public void checkCollisions(int frameTime) {
        for (Player player : gameSession.getPlayers()) {
            Bar playerBar = player.getPlayerBar();
            checkFieldBordersCollisions(player);
            checkWallCollisions(player, playerBar, frameTime);
            checkBrickCollisions(player, playerBar, frameTime);
            checkBonusCollisions(player, playerBar, frameTime);
            tryBombTransmit(player, playerBar);
            changedObjects.add(player);
            if (player.getBomb() != null) {
                changedObjects.add(player.getBomb());
            }
        }
    }

    private void randomTransmit() {
        List<Player> players = gameSession.getPlayers().stream()
                .filter(e -> !deadPlayers.contains(e))
                .collect(Collectors.toList());
        Random random = new Random();
        boolean hasBomb = false;
        for(Player player : players) {
            if(player.hasBomb()) hasBomb = true;
        }
        if(!hasBomb) {
            Player player = players.get(random.nextInt(players.size()));
            Bomb bomb = new Bomb(gameSession, player.getPosition(), player, Bomb.DEFAULT_CARRIED_BOMB_LIFETIME);
            player.setBomb(bomb);
            gameSession.addGameObject(bomb);
        }
    }

    private void tryBombTransmit(Player player, Bar playerBar) {
        if (player.getBomb() == null) {
            return;
        }
        for (Player other : gameSession.getPlayers()) {
            if (other.equals(player)) {
                continue;
            }
            if (player.getBomb() == null || other.isBombImmune()) {
                continue;
            }
            if (!playerBar.isColliding(other.getPlayerBar())) {
                transmitBomb(player, other);
                log.info("Bomb transmits from {} to {}", player, other);
            }
        }
    }

    private void transmitBomb(Player player, Player other) {
        other.setBomb(player.getBomb());
        other.setSpeed(GameConstants.PLAYER_CARRYING_BOMB_SPEED);
        player.setSpeed(GameConstants.DEFAULT_PLAYER_SPEED);
        player.setBomb(null);
        other.getBomb().reset();
        player.setBombImmune(Player.BOMB_IMMUNITY);
    }

    private void checkBonusCollisions(Player player, Bar playerBar, int frameTime) {
        for (Bonus bonus : gameSession.getBonuses()) {
            Bar barBonus = bonus.getBar();
            if (!playerBar.isColliding(barBonus)) {
                player.takeBonus(bonus);
                changedObjects.add(bonus);
                gameSession.removeGameObject(bonus);
            }
        }
    }

    private void checkBrickCollisions(Player player, Bar playerBar, int frameTime) {
        for (Brick brick : gameSession.getBricks()) {
            Bar barBrick = brick.getBar();
            if (!playerBar.isColliding(barBrick))
                player.moveBack();
        }
    }

    private void checkWallCollisions(Player player, Bar playerBar, int frameTime) {
        for (Wall wall : gameSession.getWalls()) {
            Bar barWall = wall.getBar();
            if (!playerBar.isColliding(barWall))
                player.moveBack();
        }
    }

    private void checkFieldBordersCollisions(Player player) {
        if (player.getPosition().getX() < 0
                || player.getPosition().getY() < 0
                || player.getPosition().getX() > GameConstants.GAME_WIDTH_CELLS * GameConstants.PIXELS_IN_CELL
                || player.getPosition().getY() > GameConstants.GAME_HEIGHT_CELLS * GameConstants.PIXELS_IN_CELL) {
            log.info("out of field");
            player.moveBack();
        }
    }

    private Point closestPoint(Point point) {
        double modX = point.getX() % GameObject.getWidthBox();
        double modY = point.getY() % GameObject.getHeightBox();
        int divX = point.getX() / GameObject.getWidthBox();
        int divY = point.getY() / GameObject.getHeightBox();
        if (modX < GameObject.getWidthBox() / 2) {
            if (modY < GameObject.getHeightBox() / 2)
                return new Point(divX * GameObject.getWidthBox(),
                        divY * GameObject.getHeightBox());
            else
                return new Point(divX * GameObject.getWidthBox(),
                        (divY + 1) * GameObject.getHeightBox());
        } else if (modY < 16)
            return new Point((divX + 1) * GameObject.getWidthBox(),
                    divY * GameObject.getWidthBox());
        return new Point((divX + 1) * GameObject.getWidthBox(),
                (divY + 1) * GameObject.getHeightBox());
    }


    private void bombDetonation() {
        ArrayList<GameObject> objectsToRemove = new ArrayList<>();
        for (Fire fire : gameSession.getFire()) {
            if (fire.dead()) {
                objectsToRemove.add(fire);
            }
        }

        for (Bomb bomb : gameSession.getBombs()) {
            if (bomb.dead()) {
                bomb.getOwner().incBombCapacity();
                objectsToRemove.add(bomb);
                changedObjects.add(bomb);

                List<Point> blasts = bomb.getBlast();
                for(Point point : blasts) {
                    Fire fire = new Fire(gameSession, point);
                    List<Bar> exploded = point.getOuterBar().getCollidingBars();
                    for(Bar bar : exploded) {
                        for (Player player : gameSession.getPlayers()) {
                            if (!player.getPlayerBar().isColliding(bar)) {
                                deadPlayers.add(player);
                                changedObjects.remove(player);
                            }
                        }
                        GameObject obj = gameSession.getGameObjectByPosition(bar.getLeftPoint());
                        if(obj != null) {
                            if(obj instanceof Brick) {
                                changedObjects.add(obj);
                                objectsToRemove.add(obj);
                            }
                        }
                    }
                    changedObjects.add(fire);
                    gameSession.addGameObject(fire);
                }
                gameSession.removeBomb(bomb);
            }
        }

        for (GameObject gameObject : objectsToRemove) {
            gameSession.removeGameObject(gameObject);
        }
    }
}
