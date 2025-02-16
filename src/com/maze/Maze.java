package com.maze;

import edu.usu.graphics.Graphics2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Maze {
    private final int gridSpacesPerRow;
    private ArrayList<ArrayList<GridSpace>> gridSpaces = new ArrayList<>();
    private final Random random = new Random();
    private int playerX = 0;
    private int playerY = 0;

    public Maze(float size, int gridSpacesPerRow){
        this.gridSpacesPerRow = gridSpacesPerRow;
        float CELL_WIDTH = size/gridSpacesPerRow;
        float WALL_THICKNESS = .002f;
        float mazeCenterX = CELL_WIDTH * (gridSpacesPerRow / 2f);
        float mazeCenterY = CELL_WIDTH * (gridSpacesPerRow / 2f);

        // Build the grid
        for(int i = 0; i < gridSpacesPerRow; i++){
            ArrayList<GridSpace> row = new ArrayList<>();
            for(int j = 0; j < gridSpacesPerRow; j++){
                row.add(new GridSpace(i * CELL_WIDTH - mazeCenterX, j * CELL_WIDTH - mazeCenterY, CELL_WIDTH, WALL_THICKNESS));
            }
            gridSpaces.add(row);
        }

        generateMaze();
    }

    public ArrayList<ArrayList<GridSpace>> getGridSpaces(){
        return new ArrayList<>(gridSpaces);
    }

    private void generateMaze() {
        for (var row : gridSpaces) {
            for (var gridSpace : row) {
                gridSpace.resetWalls();
                gridSpace.setVisited(false);
            }
        }

        int startRow = 0;
        int startCol = 0;
        GridSpace startSpace = gridSpaces.get(startRow).get(startCol);
        startSpace.setVisited(true);

        Set<Wall> frontier = new HashSet<>();
        addFrontierWalls(startSpace, frontier);

        while (!frontier.isEmpty()) {
            Wall wall = getRandomWall(frontier);
            frontier.remove(wall);

            GridSpace cell1 = wall.cell1;
            GridSpace cell2 = wall.cell2;

            GridSpace nextCell;
            if (cell1.isVisited() && !cell2.isVisited()) {
                nextCell = cell2;
            } else if (!cell1.isVisited() && cell2.isVisited()) {
                nextCell = cell1;
            } else {
                continue;
            }

            removeWallBetween(cell1, cell2);
            nextCell.setVisited(true);
            addFrontierWalls(nextCell, frontier);
        }
    }

    private void addFrontierWalls(GridSpace cell, Set<Wall> frontier) {
        int row = getRowIndex(cell);
        int col = getColIndex(cell);

        // Check neighbors
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        String[] wallNames = {"top", "bottom", "left", "right"};

        for (int i = 0; i < directions.length; i++) {
            int newRow = row + directions[i][0];
            int newCol = col + directions[i][1];

            if (isValidCell(newRow, newCol)) {
                GridSpace neighbor = gridSpaces.get(newRow).get(newCol);
                if (!neighbor.isVisited()) {
                    frontier.add(new Wall(cell, neighbor));
                }
            }
        }
    }


    private Wall getRandomWall(Set<Wall> frontier) {
        int randomIndex = random.nextInt(frontier.size());
        int i = 0;
        for (Wall wall : frontier) {
            if (i == randomIndex) {
                return wall;
            }
            i++;
        }
        return null;
    }


    private void removeWallBetween(GridSpace cell1, GridSpace cell2) {
        int row1 = getRowIndex(cell1);
        int col1 = getColIndex(cell1);
        int row2 = getRowIndex(cell2);
        int col2 = getColIndex(cell2);

        if (row1 == row2) {
            if (col1 < col2) {
                cell1.removeWall("bottom");
                cell2.removeWall("top");
            } else { // cell1 is to the right of cell2
                cell1.removeWall("top");
                cell2.removeWall("bottom");
            }
        } else { // Same column, vertical wall
            if (row1 < row2) { // cell2 is below cell1
                cell1.removeWall("right");
                cell2.removeWall("left");
            } else { // cell1 is below cell2
                cell1.removeWall("left");
                cell2.removeWall("right");
            }
        }
    }


    private boolean isValidCell(int row, int col) {
        return row >= 0 && row < gridSpacesPerRow && col >= 0 && col < gridSpacesPerRow;
    }

    private int getRowIndex(GridSpace cell) {
        for (int i = 0; i < gridSpacesPerRow; i++) {
            if (gridSpaces.get(i).contains(cell)) {
                return i;
            }
        }
        return -1;
    }

    private int getColIndex(GridSpace cell) {
        for (int i = 0; i < gridSpacesPerRow; i++) {
            for (int j = 0; j < gridSpacesPerRow; j++) {
                if (gridSpaces.get(i).get(j) == cell) {
                    return j;
                }
            }
        }
        return -1;
    }



    private static class Wall {
        GridSpace cell1;
        GridSpace cell2;

        public Wall(GridSpace cell1, GridSpace cell2) {
            this.cell1 = cell1;
            this.cell2 = cell2;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Wall wall = (Wall) o;
            return (cell1.equals(wall.cell1) && cell2.equals(wall.cell2)) || (cell1.equals(wall.cell2) && cell2.equals(wall.cell1));
        }

        @Override
        public int hashCode() {
            return cell1.hashCode() + cell2.hashCode();
        }
    }
}