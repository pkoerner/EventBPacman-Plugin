package de.heinzen.probplugin.pacman;


import de.prob.translator.types.BigInteger;
import de.prob.translator.types.Tuple;

/**
 * Created by Christoph Heinzen on 15.08.17.
 */
public class Position {

    private int x, y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Position(Tuple value) {
        this(((BigInteger) value.get(0)).intValue(), ((BigInteger) value.get(1)).intValue());
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setPosition(Position pos) {
        if (pos != null) {
            setX(pos.getX());
            setY(pos.getY());
        }
    }

    public double getDistance(Position pos) {
        int deltaX = getX()-pos.getX();
        int deltaY = getY()-pos.getY();
        return Math.sqrt(deltaX*deltaX + deltaY*deltaY);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Position position = (Position) o;

        if (Double.compare(position.x, x) != 0) return false;
        return Double.compare(position.y, y) == 0;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "x: " + getX() + ", y: " + getY();
    }
}
