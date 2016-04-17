package org.layfield.graph.barnes;

import java.awt.geom.Point2D;

/**
 * Created by Ryan Layfield on 4/14/2016.
 */
public class Vector2D {

    private double x;
    private double y;

    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2D(Vector2D vector) {
        this.x = vector.x;
        this.y = vector.y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void addX(double xAdd) {
        x += xAdd;
    }

    public void addY(double yAdd) {
        y += yAdd;
    }

    public String toString() {
        return "Vector<" + x + "," + y + ">";
    }

    public Vector2D scaleBy(double amnt) {
        return new Vector2D(x * amnt, y * amnt);
    }

    public Vector2D averageWith(Vector2D other) {
        return new Vector2D(
                (this.x + other.x) / 2.0,
                (this.y + other.y) / 2.0
        );
    }

    public double distTo(Vector2D other) {
        return Point2D.distance(this.x, this.y, other.x, other.y);
    }
}
