package mm.storage;

import mm.model.GameObject;
import mm.model.GameSession;
import mm.model.Player;
import mm.network.Broker;
import mm.ticker.Action;
import mm.ticker.Ticker;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static mm.message.Topic.GAME_OVER;

@Component
public class SessionStorage {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(SessionStorage.class);
    private static ConcurrentHashMap<GameSession, ArrayList<WebSocketSession>> storage
            = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<Long, GameSession> sessions = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<Player, WebSocketSession> girlToWebsocket
            = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<GameSession, Ticker> tickers = new ConcurrentHashMap<>();
    private static volatile long lastId = -1;

    public static GameSession getByWebsocket(WebSocketSession session) {
        for (Map.Entry<GameSession, ArrayList<WebSocketSession>> i : storage.entrySet()) {
            for (WebSocketSession j : i.getValue()) {
                if (session.equals(j)) {
                    return i.getKey();
                }
            }
        }
        return null;
    }

    public static ArrayList<WebSocketSession> getWebsocketsByGameSession(GameSession session) {
        return storage.get(session);
    }

    public static void addByGameId(long gameId, WebSocketSession session) {
        for (ConcurrentHashMap.Entry<GameSession, ArrayList<WebSocketSession>> entry : storage.entrySet()) {
            if (entry.getKey().getId() == gameId) {
                entry.getValue().add(session);
            }
        }
    }

    public static synchronized long addSession(int playerCount) {
        GameSession gameSession = new GameSession(playerCount, ++lastId);
        sessions.put(lastId, gameSession);
        storage.put(gameSession, new ArrayList<>(playerCount));
        return lastId;
    }

    public static int getId(long gameId) {
        return storage.get(getSessionById(gameId)).size();
    }

    public static GameSession getSessionById(long gameId) {
        Optional<GameSession> first = storage.keySet().stream().filter((gs) -> gameId == gs.getId()).findFirst();
        return first.get();
    }

    public static Player getGirlBySocket(WebSocketSession session) {
        for (Map.Entry<Player, WebSocketSession> i : girlToWebsocket.entrySet()) {
            if (i.getValue().equals(session)) {
                return i.getKey();
            }
        }
        return null;
    }

    public static WebSocketSession getWebsocketByGirl(Player player) {
        return girlToWebsocket.get(player);
    }

    public static void putGirlToSocket(WebSocketSession session, GameObject object) {
        girlToWebsocket.put((Player) object, session);
    }

    public static void putTicker(Ticker ticker, GameSession session) {
        tickers.put(session, ticker);
    }

    public static void putAction(GameSession session, Action action) {
        tickers.get(session).putAction(action);
    }

    public static void removeWebsocket(WebSocketSession session) {
        for (Map.Entry e : storage.entrySet()) {
            ArrayList<WebSocketSession> tmp = (ArrayList<WebSocketSession>) e.getValue();
            if (tmp.contains(session)) {
                GameSession gameSession = (GameSession) e.getKey();
                gameSession.removeGameObject(getGirlBySocket(session));
                tmp.remove(session);
                if (tmp.size() == 1) {
                    Broker.getInstance().send(tmp.get(0), GAME_OVER, "YOU WIN!");
                    removeGameSession(gameSession);
                }
                if (tmp.isEmpty()) {
                    Broker.getInstance().send(tmp.get(0), GAME_OVER, "YOU WIN!");
                    removeGameSession(gameSession);
                }
            }
        }
        girlToWebsocket.remove(getGirlBySocket(session));
        log.info("Websocket session: " + session + "removed");
    }

    public static void removeGameSession(GameSession session) {
        storage.remove(session);
        for (Map.Entry e : sessions.entrySet()) {
            if (e.getValue().equals(session)) sessions.remove(e.getKey());
        }
        tickers.get(session).kill();
        tickers.remove(session);
        log.info("Session " + session.getId() + " removed");
    }
}
