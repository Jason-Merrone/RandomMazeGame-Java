package com.maze;

import edu.usu.graphics.Graphics2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.HashMap;
import java.util.Collections;

public class Maze {
    private final int gridSpacesPerRow;
    private ArrayList<ArrayList<GridSpace>> gridSpaces = new ArrayList<>();
    private final Random random = new Random();
    private int playerX = 0; // Player's row index
    private int playerY = 0; // Player's column index
    private float CELL_WIDTH;
    private float WALL_THICKNESS;
    private float mazeCenterX;
    private float mazeCenterY;

    public Maze(float size, int gridSpacesPerRow){
        this.gridSpacesPerRow = gridSpacesPerRow;
        this.CELL_WIDTH = size / gridSpacesPerRow;
        this.WALL_THICKNESS = .002f;
        this.mazeCenterX = CELL_WIDTH * (gridSpacesPerRow / 2f);
        this.mazeCenterY = CELL_WIDTH * (gridSpacesPerRow / 2f);

        // Build the grid
        for (int i = 0; i < gridSpacesPerRow; i++) {
            ArrayList<GridSpace> row = new ArrayList<>();
            for (int j = 0; j < gridSpacesPerRow; j++) {
                row.add(new GridSpace(i * CELL_WIDTH - mazeCenterX, j * CELL_WIDTH - mazeCenterY, CELL_WIDTH, WALL_THICKNESS));
            }
            gridSpaces.add(row);
        }

        generateMaze();
        setPlayerPosition(0, 0); // Initialize player at start
    }

    public ArrayList<ArrayList<GridSpace>> getGridSpaces(){
        return new ArrayList<>(gridSpaces);
    }

    public float getCellSize() {
        return CELL_WIDTH;
    }

    public float getWallThickness() {
        return WALL_THICKNESS;
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
        int[][] directions = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
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

    public int getRowIndex(GridSpace cell) {
        for (int i = 0; i < gridSpacesPerRow; i++) {
            if (gridSpaces.get(i).contains(cell)) {
                return i;
            }
        }
        return -1;
    }

    public int getColIndex(GridSpace cell) {
        for (int i = 0; i < gridSpacesPerRow; i++) {
            for (int j = 0; j < gridSpacesPerRow; j++) {
                if (gridSpaces.get(i).get(j) == cell) {
                    return j;
                }
            }
        }
        return -1;
    }

    public List<GridSpace> findShortestPath(GridSpace start, GridSpace end) {
        if (start == null || end == null) {
            return null; // Handle invalid input
        }

        Queue<GridSpace> queue = new LinkedList<>();
        Map<GridSpace, GridSpace> predecessorMap = new HashMap<>(); // To reconstruct path
        Set<GridSpace> visitedSpaces = new HashSet<>();

        queue.offer(start);
        visitedSpaces.add(start);
        predecessorMap.put(start, null); // Start has no predecessor

        while (!queue.isEmpty()) {
            GridSpace currentSpace = queue.poll();

            if (currentSpace == end) {
                return reconstructPath(predecessorMap, end); // Path found!!!!!!!!!!!
            }

            int currentRow = getRowIndex(currentSpace);
            int currentCol = getColIndex(currentSpace);

            int[][] directions = { { 0, -1 }, { 0, 1 }, { 1, 0 }, { -1, 0 } };
            String[] wallNames = { "top", "bottom", "right", "left" };

            for (int i = 0; i < directions.length; i++) {
                int newRow = currentRow + directions[i][0];
                int newCol = currentCol + directions[i][1];

                if (isValidCell(newRow, newCol)) {
                    GridSpace neighbor = gridSpaces.get(newRow).get(newCol);

                    if (!visitedSpaces.contains(neighbor) && !hasWallBetween(currentSpace, neighbor, wallNames[i])) {
                        visitedSpaces.add(neighbor);
                        predecessorMap.put(neighbor, currentSpace);
                        queue.offer(neighbor);
                    }
                }
            }
        }

        return null;
    }

    // method to find shortest path from player position
    public List<GridSpace> findShortestPathFromPlayer() {
        GridSpace startSpace = getPlayerGridSpace();
        GridSpace endSpace = gridSpaces.get(gridSpacesPerRow - 1).get(gridSpacesPerRow - 1); // Assuming end is bottom-right
        return findShortestPath(startSpace, endSpace);
    }

    private boolean hasWallBetween(GridSpace cell1, GridSpace cell2, String direction) {
        return cell1.isWall(direction);
    }

    private List<GridSpace> reconstructPath(Map<GridSpace, GridSpace> predecessorMap, GridSpace endSpace) {
        List<GridSpace> path = new ArrayList<>();
        GridSpace current = endSpace;

        while (current != null) {
            path.add(current);
            current = predecessorMap.get(current);
        }

        Collections.reverse(path);
        return path;
    }

    // Player-related methods

    public void setPlayerPosition(int row, int col) {
        if (isValidCell(row, col)) {
            playerX = row;
            playerY = col;
        } else {
            System.out.println("Invalid player position: (" + row + ", " + col + ")");
        }
    }


    public GridSpace getPlayerGridSpace() {
        if (isValidCell(playerX, playerY)) {
            return gridSpaces.get(playerX).get(playerY);
        }
        return null;
    }

    public boolean movePlayer(int dx, int dy) {

        int newRow = playerX + dy;
        int newCol = playerY + dx;

        if (isValidCell(newRow, newCol)) {
            GridSpace currentSpace = getPlayerGridSpace();

            if (dx == 1 && !currentSpace.isWall("bottom")) { // Moving right
                playerY = newCol;
                return true;
            } else if (dx == -1 && !currentSpace.isWall("top")) { // Moving left
                playerY = newCol;
                return true;
            } else if (dy == 1 && !currentSpace.isWall("right")) { // Moving down
                playerX = newRow;
                return true;
            } else if (dy == -1 && !currentSpace.isWall("left")) { // Moving up
                playerX = newRow;
                return true;
            }
        }
        return false;
    }

    public int calculateMoveScore(GridSpace currentSpace, List<GridSpace> shortestPath) {
        if (shortestPath != null && shortestPath.contains(currentSpace)) {
            return 1; // Positive score for being on the shortest path
        } else {
            return -1; // Negative score for being off the shortest path
        }
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
            return (cell1.equals(wall.cell1) && cell2.equals(wall.cell2)) ||
                    (cell1.equals(wall.cell2) && cell2.equals(wall.cell1));
        }

        @Override
        public int hashCode() {
            return cell1.hashCode() + cell2.hashCode();
        }
    }
}
