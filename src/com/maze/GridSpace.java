package com.maze;

import edu.usu.graphics.Color;
import edu.usu.graphics.Graphics2D;
import edu.usu.graphics.Rectangle;
import edu.usu.utils.Tuple2;
import java.util.HashMap;

public class GridSpace {
    private final Tuple2<Float, Float> position;
    private final HashMap<String, Boolean> walls = new HashMap<>();
    private final Rectangle topWall;
    private final Rectangle bottomWall;
    private final Rectangle leftWall;
    private final Rectangle rightWall;
    private float wallThickness;
    private float cellWidth;
    private float x;
    private float y;
    private boolean visited = false;

    public GridSpace(float x, float y, float cellWidth, float wallThickness) {
        this.position = new Tuple2<>(x, y);

        this.walls.put("top", true);
        this.walls.put("bottom", true);
        this.walls.put("left", true);
        this.walls.put("right", true);

        topWall = new Rectangle(x - cellWidth / 2, y - cellWidth / 2, cellWidth, wallThickness);
        bottomWall = new Rectangle(x - cellWidth / 2, y + cellWidth / 2, cellWidth, wallThickness);
        leftWall = new Rectangle(x - cellWidth / 2, y - cellWidth / 2, wallThickness, cellWidth);
        rightWall = new Rectangle(x + cellWidth / 2, y - cellWidth / 2, wallThickness, cellWidth);
    }

    public float getWallThickness() {
        return wallThickness;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getCellSize() {
        return cellWidth;
    }

    public Tuple2<Float, Float> getPosition() {
        return position;
    }

    public boolean isWall(String direction) {
        return walls.get(direction);
    }

    public void removeWall(String direction) {
        walls.replace(direction, false);
    }

    public void resetWalls() {
        walls.replace("top", true);
        walls.replace("bottom", true);
        walls.replace("left", true);
        walls.replace("right", true);
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public Rectangle getTopRect() {
        if (walls.get("top"))
            return topWall;
        return new Rectangle(0, 0, 0, 0);
    }

    public Rectangle getBottomRect() {
        if (walls.get("bottom"))
            return bottomWall;
        return new Rectangle(0, 0, 0, 0);
    }

    public Rectangle getLeftRect() {
        if (walls.get("left"))
            return leftWall;
        return new Rectangle(0, 0, 0, 0);
    }

    public Rectangle getRightRect() {
        if (walls.get("right"))
            return rightWall;
        return new Rectangle(0, 0, 0, 0);
    }
}
