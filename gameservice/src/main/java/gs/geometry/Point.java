package gs.geometry;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gs.model.GameObject;

import java.util.ArrayList;

public class Point implements Collider {
    private final int x;
    private final int y;

    @JsonCreator
    public Point(@JsonProperty("x") int x, @JsonProperty("y") int y) {
        this.x = x;
        this.y = y;
    }

    public Bar getOuterBar() {
        return new Bar(this.getX() + 3, this.getY() + 3, this.getX() + 29, this.getY() + 29);
    }

    public Bar getBar() {
        int x = this.x - this.x % 32;
        int y = this.y - this.y % 32;
        return new Bar(new Point(x, y));
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isNode() {
        return (this.x % 32 == 0 && this.y % 32 == 0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Point point = (Point) o;

        if (x == point.x && y == point.y) {
            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        return result;
    }

    @Override
    public boolean isColliding(Collider other) {
        if (other instanceof Point) {
            return this.equals(other);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return "{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
