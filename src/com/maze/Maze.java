package com.maze;

import edu.usu.graphics.Graphics2D;
import edu.usu.utils.Tuple4;

import java.util.ArrayList;

public class Maze {
    private int gridSpacesPerRow;
    private ArrayList<GridSpace> gridSpaces = new ArrayList<>();

    // Maze of size x size
    public Maze(float size, int gridSpacesPerRow){
        this.gridSpacesPerRow = gridSpacesPerRow;
        float CELL_WIDTH = size/gridSpacesPerRow;
        float WALL_THICKNESS = .002f;
        float mazeCenterX = CELL_WIDTH*((float) gridSpacesPerRow /2);
        float mazeCenterY = CELL_WIDTH*((float) gridSpacesPerRow /2);

        for(int i = 0; i < gridSpacesPerRow; i++){
            for(int j = 0; j < gridSpacesPerRow; j++){
                gridSpaces.add(new GridSpace((i)*CELL_WIDTH - mazeCenterX,(j)*CELL_WIDTH - mazeCenterY, CELL_WIDTH, WALL_THICKNESS));
            }
        }
    }

    private void generateMaze(){
        // Generate the maze using Wilson's algorithm
    }

    public void render(Graphics2D graphics){
        for(var gridSpace : gridSpaces){
            gridSpace.render(graphics);
        }
    }
}