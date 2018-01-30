package gs.geometry;

import gs.model.Bomb;
import gs.model.GameObject;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Bar implements Collider {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Bomb.class);

    private final Point leftPoint; //lower left corner
    private final Point rightPoint; //upper right corner


    public Bar(int x1, int y1, int x2, int y2) {
        leftPoint = new Point(Math.min(x1, x2), Math.min(y1, y2));
        rightPoint = new Point(Math.max(x1, x2), Math.max(y1, y2));
    }

    @Override
    public String toString() {
        return "Bar{" +
                "leftPoint=" + leftPoint +
                ", rightPoint=" + rightPoint +
                '}';
    }

    public Bar(Point point) {
        this.leftPoint = point;
        this.rightPoint = new Point(point.getX() + GameObject.getWidthBox(), point.getY() + GameObject.getHeightBox());
    }

    public List<Bar> getCollidingBars() {
        List<Bar> bars = new ArrayList<>();
        if(this.rightPoint.getX() / 32 - this.leftPoint.getX() / 32 == 1
                && this.rightPoint.getX() % 32 != 0) {
            if(this.rightPoint.getY() / 32 - this.leftPoint.getY() / 32 == 1) {
                bars.add(new Bar(new Point(this.leftPoint.getX() / 32 * 32, this.leftPoint.getY() / 32 * 32)));
                bars.add(new Bar(new Point(this.getUpLeftPoint().getX() / 32 * 32, this.getUpLeftPoint().getY() / 32 * 32)));
                bars.add(new Bar(new Point(this.getBotRightPoint().getX() / 32 * 32, this.getBotRightPoint().getY() / 32 * 32)));
                bars.add(new Bar(new Point(this.rightPoint.getX() / 32 * 32, this.rightPoint.getY() / 32 * 32)));
                return bars;
            } else {
                bars.add(new Bar(new Point(this.leftPoint.getX() / 32 * 32, this.leftPoint.getY() / 32 * 32)));
                bars.add(new Bar(new Point(this.getBotRightPoint().getX() / 32 * 32, this.getBotRightPoint().getY() / 32 * 32)));
                return bars;
            }
        } else {
            if(this.rightPoint.getY() / 32 - this.leftPoint.getY() / 32 == 1
                    && this.rightPoint.getY() % 32 != 0) {
                bars.add(new Bar(new Point(this.leftPoint.getX() / 32 * 32, this.leftPoint.getY() / 32 * 32)));
                bars.add(new Bar(new Point(this.getUpLeftPoint().getX() / 32 * 32, this.getUpLeftPoint().getY() / 32 * 32)));
                return bars;
            }
        }
        bars.add(new Bar(new Point(this.getLeftPoint().getX() / 32 * 32, this.getLeftPoint().getY() / 32 * 32)));
        return bars;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Bar bar = (Bar) o;

        if (leftPoint.equals(bar.leftPoint)
                && rightPoint.equals(bar.rightPoint)) {
            return true;
        }

        return false;
    }

    @Override
    public boolean isColliding(Collider other) {
        if (other.getClass() == this.getClass()) {
            Bar bar = (Bar) other;
            return leftPoint.getX() > bar.rightPoint.getX()
                    || rightPoint.getX() < bar.leftPoint.getX()
                    || leftPoint.getY() > bar.rightPoint.getY()
                    || rightPoint.getY() < bar.leftPoint.getY();
        } else if (other.getClass() == Point.class) {
            Point point = (Point) other;
            return leftPoint.getX() > point.getX() || leftPoint.getY() > point.getY()
                    || rightPoint.getX() < point.getX() || rightPoint.getY() < point.getY();
        } else throw new IllegalArgumentException();
    }

    public Point getLeftPoint() {
        return leftPoint;
    }

    private Point getBotRightPoint() {
        return new Point(this.leftPoint.getX() + 32, this.leftPoint.getY());
    }

    private Point getUpLeftPoint() {
        return new Point(this.leftPoint.getX(), this.leftPoint.getY() + 32);
    }
}