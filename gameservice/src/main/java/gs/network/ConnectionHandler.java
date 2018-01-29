package gs.network;

import gs.message.Message;
import gs.message.Topic;
import gs.model.GameSession;
import gs.model.Player;
import gs.storage.SessionStorage;
import gs.ticker.Action;
import gs.ticker.Ticker;
import gs.util.JsonHelper;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.Collections;

@Component
public class ConnectionHandler extends TextWebSocketHandler implements WebSocketHandler {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(ConnectionHandler.class);

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        MultiValueMap<String, String> parameters =
                UriComponentsBuilder.fromUri(session.getUri()).build().getQueryParams();
        String idParam = parameters.get("gameId").toString();
        long gameId = Long.parseLong(idParam.substring(1, idParam.length() - 1));
        GameSession gameSession = SessionStorage.getSessionById(gameId);
        if (gameSession == null) {
            log.error("No gameSession with id " + gameId);
            session.close();
            return;
        }
        if (!gameSession.isReady()) {
            SessionStorage.addByGameId(gameId, session);
            int data = SessionStorage.getId(gameId);
            SessionStorage.send(session, Topic.POSSESS, data);
            Player player = gameSession.addPlayer(data);
            SessionStorage.putGirlToSocket(session, gameSession.getById(gameSession.getLastId()));
            SessionStorage.send(session, Topic.REPLICA, gameSession.getGameObjects());
            for (Player p : gameSession.getPlayers()) {
                if(!player.equals(p)){
                    WebSocketSession otherSession = SessionStorage.getWebsocketByPlayer(p);
                    SessionStorage.send(otherSession, Topic.REPLICA, Collections.singletonList(player));
                }
            }

            if (gameSession.getPlayerCount()
                    == SessionStorage.getWebsocketsByGameSession(gameSession).size()) {
                Ticker ticker = new Ticker(gameSession);
                SessionStorage.putTicker(ticker, gameSession);
                ticker.setName("gameId : " + gameId);
                ticker.begin();
                ticker.start();
            }
        } else {
            session.close();
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        if (SessionStorage.getByWebsocket(session).isReady()) {
            Message msg = JsonHelper.fromJson(message.getPayload(), Message.class);
            Action action = new Action(msg.getTopic(),
                    SessionStorage.getPlayerBySocket(session), msg.getData());
            SessionStorage.putAction(SessionStorage.getByWebsocket(session), action);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log.info("Socket Closed: [" +
                closeStatus.getCode() + "] " + closeStatus.getReason());
        SessionStorage.removeWebsocket(session);
        super.afterConnectionClosed(session, closeStatus);
    }

}
