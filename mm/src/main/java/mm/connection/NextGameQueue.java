package mm.connection;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by a.pomosov on 30/01/2018.
 */
@Component
public class NextGameQueue {
    private final ConcurrentLinkedQueue<String> nextGamePlayers = new ConcurrentLinkedQueue<>();

    public void addToQueue(@NotNull String player){
        nextGamePlayers.add(player);
    }

    public void removeFromQueue(){
        nextGamePlayers.poll();
    }

    public int size() {
        return nextGamePlayers.size();
    }

    public void clear(){
        nextGamePlayers.clear();
    }
}
