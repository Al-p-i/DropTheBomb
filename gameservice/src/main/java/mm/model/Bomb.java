package mm.model;

import mm.geometry.Point;
import org.slf4j.LoggerFactory;

public class Bomb extends GameObject implements Tickable {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Bomb.class);
    private static final int DEFAULT_LIFETIME = 7000;
    private static final int BOMB_WIDTH = 28;
    private static final int BOMB_HEIGHT = 28;
    private int lifetime;
    private transient Player owner;
    private transient int elapsed = 0;

    public Bomb(GameSession session, Point position, Player owner) {
        super(session, new Point(position.getX(), position.getY()),
                "Bomb", BOMB_WIDTH, BOMB_HEIGHT);
        this.owner = owner;
        this.lifetime = DEFAULT_LIFETIME;
    }

    public Player getOwner() {
        return this.owner;
    }

    @Override
    public void tick(int elapsed) {
        this.elapsed += elapsed;
    }

    public boolean dead() {
        return this.elapsed >= lifetime;
    }

    public int getLifetime() {
        return lifetime;
    }

    public void setLifetime(int lifetime) {
        this.lifetime = lifetime;
    }

    public void reset(){
        this.elapsed = 0;
    }
}
