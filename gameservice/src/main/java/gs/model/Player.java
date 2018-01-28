package gs.model;

import gs.geometry.Point;
import gs.util.GameConstants;
import org.slf4j.LoggerFactory;

public class Player extends GameObject implements Movable, Tickable {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(Player.class);
    private static final int PLAYER_WIDTH = 26;
    private static final int PLAYER_HEIGHT = 26;
    private Direction direction = Direction.IDLE;
    private Direction jumpDirection = Direction.IDLE;
    private transient double speed = 0.3;
    private transient int bombCapacity = 1;
    private transient int bombRange = 1;
    private transient Bomb bomb;
    private int immunityTimer = 0;
    private int jumpTimer = 0;
    public static final int BOMB_IMMUNITY = 2000;
    public static final int JUMP_TIMEOUT = 1000;
    public static final double BOMB_CARRIER_SPEEDUP_ABS = 0.2;
    private transient Point previousPosition;

    public Player(GameSession session, Point position) {
        super(session, new Point(position.getX() * GameObject.getWidthBox(),
                        position.getY() * GameObject.getWidthBox()),
                "Pawn", PLAYER_WIDTH, PLAYER_HEIGHT);
        this.previousPosition = position;
    }

    
    public void plantBomb() {
        Point bitmapPosition = this.position.convertToBitmapPosition();
        new Bomb(this.session, bitmapPosition, this, Bomb.DEFAULT_PLANTED_BOMB_LIFETIME);
        this.bombCapacity--;
    }

    @Override
    public Point move(int time) {
        if (direction != Direction.IDLE) {
            jumpDirection = direction;
        }
        int delta = (int) (speed * (double) time);
        previousPosition = position;
        return moveDelta(delta, direction);
    }

    public Point jump() {
        int delta = GameConstants.JUMP_PIXELS;
        previousPosition = position;
        return moveDelta(delta, jumpDirection);
    }

    private Point moveDelta(int delta, Direction dir) {
        switch (dir) {
            case UP:
                moveLog(dir, position.getX(), position.getY(),
                        position.getX(), position.getY() + delta);
                setPosition(new Point(position.getX(), position.getY() + delta));
                break;
            case DOWN:
                moveLog(dir, position.getX(), position.getY(),
                        position.getX(), position.getY() - delta);
                setPosition(new Point(position.getX(), position.getY() - delta));
                break;
            case RIGHT:
                moveLog(dir, position.getX(), position.getY(),
                        position.getX() + delta, position.getY());
                setPosition(new Point(position.getX() + delta, position.getY()));
                break;
            case LEFT:
                moveLog(dir, position.getX(), position.getY(),
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

    public Point moveBack() {
        this.position = previousPosition;
        /*int delta = (int) (speed * (double) time);
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
        }*/
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
        this.bomb = bomb;
    }

    public Bomb getBomb() {
        return bomb;
    }

    @Override
    public void tick(int elapsed) {
        move(elapsed);
        tickImmunityTimer(elapsed);
        tickJumpTimer(elapsed);
    }

    private void tickImmunityTimer(int elapsed) {
        if (elapsed > immunityTimer) {
            immunityTimer = 0;
        } else {
            immunityTimer -= elapsed;
        }
    }

    private void tickJumpTimer(int elapsed) {
        if (elapsed > jumpTimer) {
            jumpTimer = 0;
        } else {
            jumpTimer -= elapsed;
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

    public static int getPlayerWidth() {
        return PLAYER_WIDTH;
    }

    public static int getPlayerHeight() {
        return PLAYER_HEIGHT;
    }

    public boolean isBombImmune() {
        return immunityTimer > 0;
    }

    public boolean canJump() {
        return jumpTimer == 0;
    }

    public void restartJumpTimer() {
        jumpTimer = JUMP_TIMEOUT;
    }

    public void setBombImmune(int bombImmune) {
        immunityTimer = bombImmune;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    @Override
    public String toString() {
        return "Player{" +
                "id=" + id +
                '}';
    }

    public boolean hasBomb() {
        return !(bomb == null);
    }

    public Point getPreviousPosition() {
        return previousPosition;
    }

    public void setPreviousPosition(Point previousPosition) {
        this.previousPosition = previousPosition;
    }
}
