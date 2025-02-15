package com.maze;

import edu.usu.graphics.Color;
import edu.usu.graphics.Graphics2D;
import edu.usu.graphics.Rectangle;
import edu.usu.utils.Tuple2;
import java.util.HashMap;

public class GridSpace {
    private final  Tuple2<Float,Float> position;
    private final HashMap<String,Boolean> walls = new HashMap<>();
    private final Rectangle topWall;
    private final Rectangle bottomWall;
    private final Rectangle leftWall;
    private final Rectangle rightWall;

    public GridSpace(float x, float y, float cellWidth, float wallThickness) {
        this.position = new Tuple2<>(x,y);

        this.walls.put("top",true);
        this.walls.put("bottom",true);
        this.walls.put("left",true);
        this.walls.put("right",true);

        topWall = new Rectangle(x-((float) cellWidth /2), y-((float) cellWidth /2), cellWidth, wallThickness);
        bottomWall = new Rectangle(x-((float) cellWidth /2), y+((float) cellWidth /2), cellWidth, wallThickness);
        leftWall = new Rectangle(x-((float) cellWidth /2), y-((float) cellWidth /2), wallThickness, cellWidth);
        rightWall = new Rectangle(x+((float) cellWidth /2), y-((float) cellWidth /2), wallThickness, cellWidth);
    }

    public Tuple2<Float, Float> getPosition(){
        return position;
    }

    public boolean isWall(String direction) {
        return walls.get(direction);
    }

    public void removeWall(String direction) {
        walls.replace(direction, false);
    }

    public void render(Graphics2D graphics){
        if(walls.get("top"))
            graphics.draw(topWall, Color.PURPLE);
        if(walls.get("bottom"))
            graphics.draw(bottomWall, Color.PURPLE);
        if(walls.get("left"))
            graphics.draw(leftWall, Color.PURPLE);
        if(walls.get("right"))
            graphics.draw(rightWall, Color.PURPLE);
    }
}
