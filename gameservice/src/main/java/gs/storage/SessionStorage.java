package gs.storage;

import gs.message.Message;
import gs.message.Topic;
import gs.model.GameObject;
import gs.model.GameSession;
import gs.model.Player;
import gs.ticker.Action;
import gs.ticker.Ticker;
import gs.util.JsonHelper;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static gs.message.Topic.GAME_OVER;

public class SessionStorage {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(SessionStorage.class);
    private static ConcurrentHashMap<GameSession, ArrayList<WebSocketSession>> connections
            = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<Player, WebSocketSession> playersToWebsocket
            = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<GameSession, Ticker> tickers = new ConcurrentHashMap<>();
    private static volatile long lastId = -1;

    public static GameSession getByWebsocket(WebSocketSession session) {
        for (Map.Entry<GameSession, ArrayList<WebSocketSession>> i : connections.entrySet()) {
            for (WebSocketSession j : i.getValue()) {
                if (session.equals(j)) {
                    return i.getKey();
                }
            }
        }
        return null;
    }

    public static ArrayList<WebSocketSession> getWebsocketsByGameSession(GameSession session) {
        return connections.get(session);
    }

    public static void addByGameId(long gameId, WebSocketSession session) {
        for (ConcurrentHashMap.Entry<GameSession, ArrayList<WebSocketSession>> entry : connections.entrySet()) {
            if (entry.getKey().getId() == gameId) {
                entry.getValue().add(session);
            }
        }
    }

    public static synchronized long addSession(int playerCount) {
        GameSession gameSession = new GameSession(playerCount, ++lastId);
        connections.put(gameSession, new ArrayList<>(playerCount));
        return lastId;
    }

    public static int getId(long gameId) {
        return connections.get(getSessionById(gameId)).size();
    }

    @Nullable
    public static GameSession getSessionById(long gameId) {
        Optional<GameSession> first = connections.keySet().stream().filter((gs) -> gameId == gs.getId()).findFirst();
        return first.orElse(null);
    }

    public static Player getPlayerBySocket(WebSocketSession session) {
        for (Map.Entry<Player, WebSocketSession> i : playersToWebsocket.entrySet()) {
            if (i.getValue().equals(session)) {
                return i.getKey();
            }
        }
        return null;
    }

    public static WebSocketSession getWebsocketByPlayer(Player player) {
        return playersToWebsocket.get(player);
    }

    public static void putGirlToSocket(WebSocketSession session, GameObject object) {
        playersToWebsocket.put((Player) object, session);
    }

    public static void putTicker(Ticker ticker, GameSession session) {
        tickers.put(session, ticker);
    }

    public static void putAction(GameSession session, Action action) {
        tickers.get(session).putAction(action);
    }

    public static void removeWebsocket(WebSocketSession session) {
        for (Map.Entry e : connections.entrySet()) {
            ArrayList<WebSocketSession> tmp = (ArrayList<WebSocketSession>) e.getValue();
            if (tmp.contains(session)) {
                GameSession gameSession = (GameSession) e.getKey();
                gameSession.removeGameObject(getPlayerBySocket(session));
                tmp.remove(session);
                if (tmp.size() == 1) {
                    SessionStorage.send(tmp.get(0), GAME_OVER, "YOU WIN!");
                    removeGameSession(gameSession);
                }
                if (tmp.isEmpty()) {
                    SessionStorage.send(tmp.get(0), GAME_OVER, "YOU WIN!");
                    removeGameSession(gameSession);
                }
            }
        }
        playersToWebsocket.remove(getPlayerBySocket(session));
        log.info("Websocket session: " + session + "removed");
    }

    public static void removeGameSession(GameSession session) {
        connections.remove(session);
        tickers.get(session).kill();
        tickers.remove(session);
        log.info("Session " + session.getId() + " removed");
    }

    public static void send(@NotNull WebSocketSession session, @NotNull Topic topic, @NotNull Object object) {
        String message = JsonHelper.toJson(new Message(topic, JsonHelper.toJson(object)));
        try {
            session.sendMessage(new TextMessage(message));
        } catch (IOException e) {
            log.error("Fail to send ws message " + message, e);
        }
    }

    public static void broadcast(@NotNull GameSession gameSession, @NotNull Topic topic, @NotNull Object object) {
        String message = JsonHelper.toJson(new Message(topic, JsonHelper.toJson(object)));
        ArrayList<WebSocketSession> webSocketSessions = connections.get(gameSession);
        if (webSocketSessions == null) {
            log.error("Fail to broadcast " + message + ". No connections to gameSession " + gameSession.getId());
            return;
        }
        for (WebSocketSession session : webSocketSessions) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                log.error("Fail to send ws message " + message, e);
            }
        }
    }
}
