package mm.service;

import mm.storage.SessionStorage;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GameService {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(GameService.class);

    public long create(int playerCount) {
        return SessionStorage.addSession(playerCount);
    }
}
