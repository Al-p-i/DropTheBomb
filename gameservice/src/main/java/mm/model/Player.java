package mm.model;

import mm.geometry.Point;
import org.slf4j.LoggerFactory;

public class Player extends GameObject implements Movable, Tickable {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(Player.class);
    private static final int GIRL_WIDTH = 26;
    private static final int GIRL_HEIGHT = 26;
    private Direction direction = Direction.IDLE;
    private transient double speed = 0.5;
    private transient int bombCapacity = 1;
    private transient int bombRange = 1;
    private Bomb bomb;
    private int immunityTimer = 0;
    public static final int BOMB_IMMINUTY = 3000;

    public Player(GameSession session, Point position) {
        super(session, new Point(position.getX() * GameObject.getWidthBox(),
                        position.getY() * GameObject.getWidthBox()),
                "Pawn", GIRL_WIDTH, GIRL_HEIGHT);
    }

    public void plantBomb() {
        Point bitmapPosition = this.position.convertToBitmapPosition();
        new Bomb(this.session, bitmapPosition, this);
        this.bombCapacity--;
    }

    @Override
    public Point move(int time) {
        int delta = (int) (speed * (double) time);
        switch (direction) {
            case UP:
                moveLog(direction, position.getX(), position.getY(),
                        position.getX(), position.getY() + delta);
                setPosition(new Point(position.getX(), position.getY() + delta));
                break;
            case DOWN:
                moveLog(direction, position.getX(), position.getY(),
                        position.getX(), position.getY() - delta);
                setPosition(new Point(position.getX(), position.getY() - delta));
                break;
            case RIGHT:
                moveLog(direction, position.getX(), position.getY(),
                        position.getX() + delta, position.getY());
                setPosition(new Point(position.getX() + delta, position.getY()));
                break;
            case LEFT:
                moveLog(direction, position.getX(), position.getY(),
                        position.getX() - delta, position.getY());
                setPosition(new Point(position.getX() - delta, position.getY()));
                break;
            default:
                return position;
        }
        if (bomb != null) {
            bomb.setPosition(position);
        }
        return position;
    }

    public Point moveBack(int time) {
        int delta = (int) (speed * (double) time);
        switch (direction) {
            case DOWN:
                moveLog(direction, position.getX(), position.getY(),
                        position.getX(), position.getY() + delta);
                setPosition(new Point(position.getX(), position.getY() + delta));
                setDirection(Direction.IDLE);
                break;
            case UP:
                moveLog(direction, position.getX(), position.getY(),
                        position.getX(), position.getY() - delta);
                setPosition(new Point(position.getX(), position.getY() - delta));
                setDirection(Direction.IDLE);
                break;
            case LEFT:
                moveLog(direction, position.getX(), position.getY(),
                        position.getX() + delta, position.getY());
                setPosition(new Point(position.getX() + delta, position.getY()));
                setDirection(Direction.IDLE);
                break;
            case RIGHT:
                moveLog(direction, position.getX(), position.getY(),
                        position.getX() - delta, position.getY());
                setPosition(new Point(position.getX() - delta, position.getY()));
                setDirection(Direction.IDLE);
                break;
            case IDLE:
                return position;
            default:
                return position;
        }
        if(bomb != null){
            bomb.setPosition(position);
        }
        return position;
    }

    public void moveLog(Direction direction, int oldX, int oldY, int x, int y) {
        //logger.info("Player id = {} moved {} ({}, {}) to ({}, {})",
        //      getId(), direction.name(), oldX, oldY, x, y);
    }

    public void takeBonus(Bonus bonus) {
        if (bonus.getBonusType().equals(Bonus.BonusType.BOMBS))
            this.bombCapacity++;

        else if (bonus.getBonusType().equals(Bonus.BonusType.SPEED) && this.speed < 0.26)
            this.speed += 0.04;

        else this.bombRange++;
    }

    public void setBomb(Bomb bomb) {
        log.info("Bomb transmitted");
        this.bomb = bomb;
    }

    public Bomb getBomb() {
        return bomb;
    }

    @Override
    public void tick(int elapsed) {
        move(elapsed);
        tickImmunityTimer(elapsed);
    }

    private void tickImmunityTimer(int elapsed) {
        if (elapsed > immunityTimer) {
            immunityTimer = 0;
        } else {
            immunityTimer -= elapsed;
        }
    }

    public Direction getDirection() {
        return this.direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public int getBombCapacity() {
        return bombCapacity;
    }

    public void decBombCapacity() {
        --this.bombCapacity;
    }

    public void incBombCapacity() {
        ++this.bombCapacity;
    }

    public int getBombRange() {
        return bombRange;
    }

    public static int getGirlWidth() {
        return GIRL_WIDTH;
    }

    public static int getGirlHeight() {
        return GIRL_HEIGHT;
    }

    public boolean isBombImmune() {
        return immunityTimer > 0;
    }

    public void setBombImmune(int bombImmune) {
        immunityTimer = bombImmune;
    }

    @Override
    public String toString() {
        return "Player{" +
                "id=" + id +
                '}';
    }
}
