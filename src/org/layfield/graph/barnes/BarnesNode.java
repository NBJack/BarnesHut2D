package org.layfield.graph.barnes;

import java.util.function.*;

/**
 * Created by Ryan Layfield on 4/14/2016.
 */
public class BarnesNode<T extends Locationable> {


    public static final int MAX_CHILD = 4;
    BarnesNode<T> [] children;

    private Vector2D upLeft;
    private Vector2D lowRight;
    private Vector2D middle;

    private int countChild = 0;

    private T data;
    private boolean wasFlagged = false;

    private BarnesNode() {
        lowRight = upLeft = middle = new Vector2D(0,0);
        data = null;
    }

    public BarnesNode(Vector2D upLeft, Vector2D lowRight) {
        this.upLeft = new Vector2D(upLeft);
        this.lowRight = new Vector2D(lowRight);
        this.middle = upLeft.averageWith(lowRight);

        children = null;

        data = null;
    }

    public Vector2D getUpLeft() {
        return upLeft;
    }

    public Vector2D getLowRight() {
        return lowRight;
    }

    public int getChildCount() {
        return countChild;
    }

    public void visitAll(Consumer<BarnesNode<T>> visitor) {
        visitor.accept(this);
        this.flag();

        if (!isLeaf()) {
            for (BarnesNode<T> child : children) {
                child.visitAll(visitor);
            }
        }
    }

    /**
     * Visit all nodes with a note of the level we're at.
     * @param visitor The visitor agent to use.
     */
    public void visitAll(BiConsumer<BarnesNode<T>, Integer> visitor) {
        visitAll(visitor, 0);
    }

    private void visitAll(BiConsumer<BarnesNode<T>, Integer> visitor, int level) {
        visitor.accept(this, level);

        if (!isLeaf()) {
            for (BarnesNode<T> child : children) {
                child.visitAll(visitor, level + 1);
            }
        }
    }

    public T getData() {
        return data;
    }

    public void addData(T data) {
        // Initial check
        if (isWithin(data.getLocation())) {
            countChild++;
            addDataInternal(data);
            return;
        }
        throw new IllegalArgumentException("You must provide a valid point within the confines of this node..");
    }

    /**
     * Add data to this node, which may result actually add it to an underlying hirearchy.
     * @param data
     */
    private void addDataInternal(T data) {
        if (isLeaf()) {
            if (!hasData()) {
                // Case 1: Has no children, no data. Add it and return
                this.data = data;
                return;
            } else {
                // Case 2: Has no children, but has data. Need to create children and divide. Don't forget to include
                // any child that may already be here.
                createChildren();
                for (int i = 0; i < MAX_CHILD; i++) {
                    if (children[i].isWithin(data.getLocation())) {
                        // Add the new child and stop the loop
                        children[i].addDataInternal(data);
                        break;
                    }
                }
                for (int i = 0; i < MAX_CHILD; i++) {
                    if (children[i].isWithin(this.data.getLocation())) {
                        // Add it
                        children[i].addDataInternal(this.data);
                        // 'Forget' that data for this node.
                        this.data = null;
                        return;
                    }
                }
            }
        } else {
            // Case 3: Has children, need to add this to one of them.
            for (int i = 0; i < MAX_CHILD; i++) {
                if (children[i].isWithin(data.getLocation())) {
                    children[i].addDataInternal(data);
                    return;
                }
            }

        }
        throw new IllegalStateException("I coudln't add the data to a valid node: " + data);
    }

    public double getWidth() {
        return lowRight.getX() - upLeft.getX();
    }

    public double getHeight() {
        return lowRight.getY() - upLeft.getY();
    }

    public Vector2D getMiddle() {
        return middle;
    }

    private void createChildren() {
        // Divide and conquer
        children = new BarnesNode[MAX_CHILD];


        // Upper left
        children[0] = new BarnesNode<T>(
                upLeft,
                middle
        );

        // Upper right
        children[1] = new BarnesNode<T>(
                new Vector2D(middle.getX(), upLeft.getY()),
                new Vector2D(lowRight.getX(), middle.getY())
        );

        // Lower left
        children[2] = new BarnesNode<T>(
                new Vector2D(upLeft.getX(), middle.getY()),
                new Vector2D(middle.getX(), lowRight.getY())
        );

        // Lower right
        children[3] = new BarnesNode<T>(
                middle,
                lowRight
        );
    }

    /**
     * Is this a leaf node (no children)?
     * @return
     */
    public boolean isLeaf() {
        return children == null;
    }

    /**
     * Is this an empty leaf (no children, no data)?
     * @return
     */
    public boolean isEmptyLeaf() {
        return isLeaf() && !hasData();
    }



    /**
     * Is a vector within this location?
     * @param vec The vector to check. Cannot be null.
     * @return true if it is, false otherwise.
     */
    public boolean isWithin(Vector2D vec) {
        return (vec.getX() >= upLeft.getX() && vec.getX() <= lowRight.getX()
              &&vec.getY() >= upLeft.getY() && vec.getY() <= lowRight.getY());
    }

    /** Clears out data. */
    private void clearData() {
        data = null;
    }

    /**
     * Does this node contain data it immediately points to?
     * @return true
     */
    public boolean hasData() {
        return data != null;
    }



    protected boolean isTerminus() {
        return false;
    }

    /**
     * Get all children. Do not modify!
     * @return
     */
    public BarnesNode<T>[] getChildren() {
        return children;
    }

    public void flag() {
        wasFlagged = true;
    }

    public boolean getFlag() {
        return wasFlagged;
    }

}
