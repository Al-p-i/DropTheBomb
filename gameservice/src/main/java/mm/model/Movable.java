package mm.model;

import mm.geometry.Point;

public interface Movable {
    /**
     * Tries to move entity towards specified direction for time
     *
     * @return final position after movement
     */
    Point move(int time);

    enum Direction {
        UP, DOWN, RIGHT, LEFT, IDLE;
    }
}
