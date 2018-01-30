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
    private transient double speed = GameConstants.DEFAULT_PLAYER_SPEED;
    private transient int bombCapacity = 1;
    private transient int bombRange = 1;
    private transient Bomb bomb;
    private int immunityTimer = 0;
    private int jumpTimer = 0;
    public static final int BOMB_IMMUNITY = 2000;
    public static final int JUMP_TIMEOUT = 2000;
    private transient Point previousPosition;

    public Player(GameSession session, Point position) {
        super(session, new Point(position.getX() * GameObject.getWidthBox(),
                        position.getY() * GameObject.getWidthBox()),
                "Pawn", PLAYER_WIDTH, PLAYER_HEIGHT);
        this.previousPosition = new Point(position.getX(), position.getY());
    }

    @Override
    public Point move(int time) {
        if (direction != Direction.IDLE) {
            jumpDirection = direction;
        }
        int delta = (int) (speed * (double) time);
        if (!previousPosition.equals(position)) {
            previousPosition = new Point(position.getX(), position.getY());
        }
        return moveDelta(delta, direction);
    }

    public Point jump() {
        int delta = GameConstants.JUMP_PIXELS;
        previousPosition = new Point(position.getX(), position.getY());
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
        //log.info("{} move back from {} to {}", this, position, previousPosition);
        this.position = new Point(previousPosition.getX(), previousPosition.getY());
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
        if (!alreadyJumpedThisTick()) {
            move(elapsed);
        }
        tickImmunityTimer(elapsed);
        tickJumpTimer(elapsed);
    }

    private boolean alreadyJumpedThisTick() {
        return jumpTimer == JUMP_TIMEOUT;
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
        return bomb == null && jumpTimer == 0;
    }

    public void restartJumpTimer() {
        jumpTimer = JUMP_TIMEOUT;
    }

    public void setBombImmune(int bombImmune) {
        immunityTimer = bombImmune;
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
}
