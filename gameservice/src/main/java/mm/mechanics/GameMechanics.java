package mm.mechanics;

import mm.geometry.Bar;
import mm.geometry.Point;
import mm.message.Topic;
import mm.model.*;
import mm.network.Broker;
import mm.storage.SessionStorage;
import mm.ticker.Action;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import static mm.message.Topic.GAME_OVER;

/**
 * Created by a.pomosov on 27/01/2018.
 */
public class GameMechanics {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(GameMechanics.class);

    private final Queue<Action> inputQueue = new LinkedBlockingQueue<Action>();
    private final GameSession gameSession;
    private ArrayList<Player> movedPlayers = new ArrayList<>(4);
    private ArrayList<GameObject> changedObjects = new ArrayList<>();
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
        detonationBomb();
        sendReplica();
    }


    public void putAction(Action action) {
        inputQueue.offer(action);
    }

    private void sendReplica() {
        for (WebSocketSession session : SessionStorage.getWebsocketsByGameSession(gameSession)) {
            Broker.getInstance().send(session, Topic.REPLICA, changedObjects);
            SessionStorage.getPlayerBySocket(session).setDirection(Movable.Direction.IDLE);
        }

        ArrayList<WebSocketSession> sessions = SessionStorage.getWebsocketsByGameSession(gameSession);
        ArrayList<WebSocketSession> closed = new ArrayList<>();
        for (WebSocketSession session : sessions) {
            for (Player player : deadPlayers) {
                if (SessionStorage.getPlayerBySocket(session) == player) {
                    closed.add(session);
                    System.out.println("SESSION " + SessionStorage.getWebsocketByPlayer(player));
                    Broker.getInstance().send(session, GAME_OVER, "YOU LOSE");
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
                        closestPoint(action.getActor().getPosition()), action.getActor());
                action.getActor().decBombCapacity();
                gameSession.addGameObject(bomb);
                changedObjects.add(bomb);
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
            checkWallCollisions(player, playerBar, frameTime);
            checkBrickCollisions(player, playerBar, frameTime);
            checkBonusCollisions(player, playerBar, frameTime);
            //checkBombCollisions(player, playerBar, frameTime);
            tryBombTransmit(player, playerBar);
            changedObjects.add(player);
            if (player.getBomb() != null) {
                changedObjects.add(player.getBomb());
            }
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
        player.setBomb(null);
        other.getBomb().reset();
        player.setBombImmune(Player.BOMB_IMMINUTY);
    }

//    private void checkBombCollisions(Player player, Bar playerBar, int frameTime) {
//        for (Bomb bomb : gameSession.getBombs()) {
//            Bar barBomb = bomb.getBar();
//            if (!playerBar.isColliding(barBomb) && !bomb.getOwner().equals(player))
//                player.moveBack(frameTime);
//        }
//    }

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
                player.moveBack(frameTime);
        }
    }

    private void checkWallCollisions(Player player, Bar playerBar, int frameTime) {
        for (Wall wall : gameSession.getWalls()) {
            Bar barWall = wall.getBar();
            if (!playerBar.isColliding(barWall))
                player.moveBack(frameTime);
        }
    }

    public Point closestPoint(Point point) {
        double modX = point.getX() % GameObject.getWidthBox();
        double modY = point.getY() % GameObject.getHeightBox();
        int divX = (int) point.getX() / (int) GameObject.getWidthBox();
        int divY = (int) point.getY() / (int) GameObject.getHeightBox();
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


    public void detonationBomb() {
        ArrayList<GameObject> objectList = new ArrayList<>();
        for (Fire fire : gameSession.getFire()) {
            if (fire.dead()) {
                objectList.add(fire);
            }
        }

        for (Bomb bomb : gameSession.getBombs()) {
            if (bomb.dead()) {
                bomb.getOwner().incBombCapacity();
                objectList.add(bomb);
                changedObjects.add(bomb);

                Fire currentFire = new Fire(gameSession, bomb.getPosition());
                changedObjects.add(currentFire);
                gameSession.addGameObject(currentFire);

                ArrayList<ArrayList<Bar>> explosions = Bar.getExplosions(
                        Point.getExplosions(bomb.getPosition(), bomb.getOwner().getBombRange()));

                for (int i = 0; i < explosions.size(); i++) {
                    for (int j = 0; j < explosions.get(i).size(); j++) {
                        for (Player player : gameSession.getPlayers()) {
                            if (!player.getPlayerBar().isColliding(explosions.get(i).get(j))) {
                                deadPlayers.add(player);
                                changedObjects.remove(player);
                            }
                        }
                        if (gameSession.getGameObjectByPosition(explosions
                                .get(i).get(j).getLeftPoint()) == null) {
                            Fire fire = new Fire(gameSession, explosions.get(i).get(j).getLeftPoint());
                            changedObjects.add(fire);
                            gameSession.addGameObject(fire);
                            continue;
                        }
                        if (gameSession.getGameObjectByPosition(explosions
                                .get(i).get(j).getLeftPoint()).getType().equals("Bonus")
                                || gameSession.getGameObjectByPosition(explosions
                                .get(i).get(j).getLeftPoint()).getType().equals("Bomb")
                                || gameSession.getGameObjectByPosition(explosions
                                .get(i).get(j).getLeftPoint()).getType().equals("Pawn")) {
                            continue;
                        }
                        if (gameSession.getGameObjectByPosition(explosions.get(i).get(j)
                                .getLeftPoint()).getType().equals("Wall")) {
                            break;
                        }
                        if (gameSession.getGameObjectByPosition(explosions.get(i).get(j)
                                .getLeftPoint()).getType().equals("Wood")) {
                            objectList.add(gameSession.getGameObjectByPosition(explosions
                                    .get(i).get(j).getLeftPoint()));
                            changedObjects.add(gameSession.getGameObjectByPosition(explosions
                                    .get(i).get(j).getLeftPoint()));
                            Fire fire = new Fire(gameSession, explosions.get(i).get(j).getLeftPoint());
                            gameSession.addGameObject(fire);
                            changedObjects.add(fire);
                            if (isBonus()) {
                                Bonus bonus = new Bonus(gameSession, explosions.get(i).get(j).getLeftPoint(),
                                        bonusType());
                                changedObjects.add(bonus);
                                gameSession.addGameObject(bonus);
                            }
                            break;
                        }
                        objectList.add(gameSession.getGameObjectByPosition(explosions
                                .get(i).get(j).getLeftPoint()));
                        Fire fire = new Fire(gameSession, explosions
                                .get(i).get(j).getLeftPoint());
                        changedObjects.add(fire);
                        gameSession.addGameObject(fire);
                    }
                }
            }
        }

        for (GameObject gameObject : objectList) {
            gameSession.removeGameObject(gameObject);
        }
    }

    public boolean isBonus() {
        double random = Math.random();
        return (random < 0.3);
    }

    public Bonus.BonusType bonusType() {
        double random = Math.random();
        if (random < 0.4) return Bonus.BonusType.SPEED;
        else if (random < 0.8) return Bonus.BonusType.BOMBS;
        return Bonus.BonusType.RANGE;
    }
}
