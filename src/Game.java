import com.maze.GridSpace;
import com.maze.Maze;
import edu.usu.graphics.*;
import org.joml.Vector3f;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import static org.lwjgl.glfw.GLFW.*;

public class Game {
    private final Graphics2D graphics;
    private Maze maze;
    private int mazeSize = 10; // Default maze size
    private boolean hintEnabled = false;
    private boolean breadcrumbsEnabled = false;
    private boolean pathEnabled = false;
    private List<GridSpace> shortestPath = null;
    private List<GridSpace> shortestPathFromPlayer = null; // Shortest path from player
    private GridSpace endSpace = null;
    private GridSpace startSpace = null;
    private List<GridSpace> playerPath = new ArrayList<>();
    private int score = 0;
    private double startTime;
    private Font font;
    private Texture backgroundImage;
    private Rectangle backgroundRect = new Rectangle(-1.0f, -1.0f, 2.0f, 2.0f); // Cover entire screen
    private Texture endMarkerImage;
    private Texture breadcrumbImage;
    private Texture hintImage;
    private Texture playerImage;
    private List<HighScore> highScores = new ArrayList<>();
    private boolean displayHighScores = false;
    private boolean displayCredits = false;
    private double lastMoveTime = 0;
    private static final double MOVE_COOLDOWN = 0.15;

    // Added for key debouncing for H and P
    private boolean hWasPressed = false;
    private boolean pWasPressed = false;

    // Flags to track if movement keys are currently pressed
    private boolean isLeftKeyPressed = false;
    private boolean isRightKeyPressed = false;
    private boolean isUpKeyPressed = false;
    private boolean isDownKeyPressed = false;

    private enum GameState {
        MENU, PLAYING, HIGH_SCORES, CREDITS
    }

    private GameState gameState = GameState.MENU;

    private static class HighScore {
        int score;
        int mazeSize;

        public HighScore(int score, int mazeSize) {
            this.score = score;
            this.mazeSize = mazeSize;
        }

        @Override
        public String toString() {
            return "Maze Size: " + mazeSize + "x" + mazeSize + ", Score: " + score;
        }
    }

    public Game(Graphics2D graphics) {
        this.graphics = graphics;
    }

    public void initialize() {
        backgroundImage = new Texture("resources/images/stick.png");
        endMarkerImage = new Texture("resources/images/flag.jpg");
        breadcrumbImage = new Texture("resources/images/dot.png");
        hintImage = new Texture("resources/images/hint.png");
        playerImage = new Texture("resources/images/player.png");
        font = new Font(Paths.get("resources", "fonts", "roboto.ttf").toString(), 32, false);
        startNewGame(mazeSize); // Initialize with default size
    }

    private void startNewGame(int size) {
        mazeSize = size;
        maze = new Maze(.8f, mazeSize);
        startSpace = maze.getGridSpaces().get(0).get(0);
        endSpace = maze.getGridSpaces().get(mazeSize - 1).get(mazeSize - 1);
        maze.setPlayerPosition(0, 0);
        playerPath.clear();
        playerPath.add(startSpace);
        score = 0;
        startTime = glfwGetTime();
        shortestPath = maze.findShortestPath(startSpace, endSpace);
        shortestPathFromPlayer = null;
        hintEnabled = false;
        breadcrumbsEnabled = false;
        pathEnabled = false;
        gameState = GameState.PLAYING;
    }

    public void shutdown() {
        backgroundImage.cleanup();
        endMarkerImage.cleanup();
        breadcrumbImage.cleanup();
        hintImage.cleanup();
        playerImage.cleanup();
    }

    public void run() {
        // Grab the first time
        double previousTime = glfwGetTime();

        while (!graphics.shouldClose()) {
            double currentTime = glfwGetTime();
            double elapsedTime = currentTime - previousTime;
            previousTime = currentTime;

            processInput(elapsedTime);
            update(elapsedTime);
            render(elapsedTime);
        }
    }

    private void processInput(double elapsedTime) {
        glfwPollEvents();

        if (glfwGetKey(graphics.getWindow(), GLFW_KEY_ESCAPE) == GLFW_PRESS) {
            glfwSetWindowShouldClose(graphics.getWindow(), true);
        }

        if (gameState == GameState.PLAYING) {
            processGameInput(elapsedTime);
        } else if (gameState == GameState.MENU) {
            processMenuInput();
        } else if (gameState == GameState.HIGH_SCORES || gameState == GameState.CREDITS) {
            if (glfwGetKey(graphics.getWindow(), GLFW_KEY_ENTER) == GLFW_PRESS) {
                gameState = GameState.MENU;
                displayHighScores = false;
                displayCredits = false;
            }
        }
    }

    private void processMenuInput() {
        if (glfwGetKey(graphics.getWindow(), GLFW_KEY_F1) == GLFW_PRESS) {
            startNewGame(5);
        }
        if (glfwGetKey(graphics.getWindow(), GLFW_KEY_F2) == GLFW_PRESS) {
            startNewGame(10);
        }
        if (glfwGetKey(graphics.getWindow(), GLFW_KEY_F3) == GLFW_PRESS) {
            startNewGame(15);
        }
        if (glfwGetKey(graphics.getWindow(), GLFW_KEY_F4) == GLFW_PRESS) {
            startNewGame(20);
        }
        if (glfwGetKey(graphics.getWindow(), GLFW_KEY_F5) == GLFW_PRESS) {
            displayHighScores = true;
            gameState = GameState.HIGH_SCORES;
        }
        if (glfwGetKey(graphics.getWindow(), GLFW_KEY_F6) == GLFW_PRESS) {
            displayCredits = true;
            gameState = GameState.CREDITS;
        }
    }

    private void processGameInput(double elapsedTime) {
        double currentTime = glfwGetTime();
        if (currentTime - lastMoveTime < MOVE_COOLDOWN) {
            return;
        }

        int dx = 0, dy = 0;

        // LEFT
        if ((glfwGetKey(graphics.getWindow(), GLFW_KEY_LEFT) == GLFW_PRESS ||
                glfwGetKey(graphics.getWindow(), GLFW_KEY_A) == GLFW_PRESS ||
                glfwGetKey(graphics.getWindow(), GLFW_KEY_J) == GLFW_PRESS)) {
            if (!isLeftKeyPressed) {
                dy = -1;
                isLeftKeyPressed = true; // Mark as pressed to prevent continuous movement
            }
        } else {
            isLeftKeyPressed = false; // Reset when key is released
        }

        // RIGHT
        if ((glfwGetKey(graphics.getWindow(), GLFW_KEY_RIGHT) == GLFW_PRESS ||
                glfwGetKey(graphics.getWindow(), GLFW_KEY_D) == GLFW_PRESS ||
                glfwGetKey(graphics.getWindow(), GLFW_KEY_L) == GLFW_PRESS)) {
            if (!isRightKeyPressed) {
                dy = 1;
                isRightKeyPressed = true;
            }
        } else {
            isRightKeyPressed = false;
        }

        // UP
        if ((glfwGetKey(graphics.getWindow(), GLFW_KEY_UP) == GLFW_PRESS ||
                glfwGetKey(graphics.getWindow(), GLFW_KEY_W) == GLFW_PRESS ||
                glfwGetKey(graphics.getWindow(), GLFW_KEY_I) == GLFW_PRESS)) {
            if (!isUpKeyPressed) {
                dx = -1;
                isUpKeyPressed = true;
            }
        } else {
            isUpKeyPressed = false;
        }

        // DOWN
        if ((glfwGetKey(graphics.getWindow(), GLFW_KEY_DOWN) == GLFW_PRESS ||
                glfwGetKey(graphics.getWindow(), GLFW_KEY_S) == GLFW_PRESS ||
                glfwGetKey(graphics.getWindow(), GLFW_KEY_K) == GLFW_PRESS)) {
            if (!isDownKeyPressed) {
                dx = 1;
                isDownKeyPressed = true;
            }
        } else {
            isDownKeyPressed = false;
        }


        if (dx != 0 || dy != 0) {
            if (maze.movePlayer(dx, dy)) {
                GridSpace currentSpace = maze.getPlayerGridSpace();
                if (!playerPath.contains(currentSpace)) {
                    playerPath.add(currentSpace);
                    score += maze.calculateMoveScore(currentSpace, shortestPath);
                }
                if (currentSpace == endSpace) {
                    highScores.add(new HighScore(score, mazeSize));
                    highScores.sort((h1, h2) -> h2.score - h1.score);
                    gameState = GameState.MENU;
                }
                if (hintEnabled || pathEnabled) {
                    shortestPathFromPlayer = maze.findShortestPathFromPlayer();
                }
            }
            lastMoveTime = currentTime;
        }

        // Handle Hint Toggle (H key) with debouncing
        if (glfwGetKey(graphics.getWindow(), GLFW_KEY_H) == GLFW_PRESS) {
            if (!hWasPressed) {
                hintEnabled = !hintEnabled;
                if (hintEnabled) {
                    pathEnabled = false;
                    shortestPathFromPlayer = maze.findShortestPathFromPlayer();
                } else {
                    shortestPathFromPlayer = null;
                }
                hWasPressed = true;
            }
        } else {
            hWasPressed = false;
        }

        // Handle Path Toggle (P key) with debouncing
        if (glfwGetKey(graphics.getWindow(), GLFW_KEY_P) == GLFW_PRESS) {
            if (!pWasPressed) {
                pathEnabled = !pathEnabled;
                if (pathEnabled) {
                    hintEnabled = false;
                    shortestPathFromPlayer = maze.findShortestPathFromPlayer();
                } else {
                    shortestPathFromPlayer = null;
                }
                pWasPressed = true;
            }
        } else {
            pWasPressed = false;
        }

        // Process other keys (e.g., new game, high scores, credits)
        if (glfwGetKey(graphics.getWindow(), GLFW_KEY_F1) == GLFW_PRESS) {
            startNewGame(5);
        }
        if (glfwGetKey(graphics.getWindow(), GLFW_KEY_F2) == GLFW_PRESS) {
            startNewGame(10);
        }
        if (glfwGetKey(graphics.getWindow(), GLFW_KEY_F3) == GLFW_PRESS) {
            startNewGame(15);
        }
        if (glfwGetKey(graphics.getWindow(), GLFW_KEY_F4) == GLFW_PRESS) {
            startNewGame(20);
        }
        if (glfwGetKey(graphics.getWindow(), GLFW_KEY_F5) == GLFW_PRESS) {
            displayHighScores = true;
            gameState = GameState.HIGH_SCORES;
        }
        if (glfwGetKey(graphics.getWindow(), GLFW_KEY_F6) == GLFW_PRESS) {
            displayCredits = true;
            gameState = GameState.CREDITS;
        }
    }

    private void update(double elapsedTime) {
        // Only game time updates here if needed
    }

    private void render(double elapsedTime) {
        graphics.begin();

        if (gameState == GameState.PLAYING) {
            graphics.draw(backgroundImage,backgroundRect,Color.WHITE);
            renderGame();
        } else if (gameState == GameState.MENU) {
            renderMenu();
        } else if (gameState == GameState.HIGH_SCORES) {
            renderHighScores();
        } else if (gameState == GameState.CREDITS) {
            renderCredits();
        }

        graphics.end();
    }

    private void renderMenu() {
        float startY = 0.3f;
        float lineHeight = 0.1f;
        float centerX = 0.0f;

        graphics.drawTextByWidth(font, "Maze Game", centerX - 0.2f, startY, 0.4f, Color.YELLOW);
        startY -= 2 * lineHeight;
        graphics.drawTextByWidth(font, "F1 - New Game 5x5", centerX - 0.2f, startY, 0.4f, Color.WHITE);
        startY -= lineHeight;
        graphics.drawTextByWidth(font, "F2 - New Game 10x10", centerX - 0.2f, startY, 0.4f, Color.WHITE);
        startY -= lineHeight;
        graphics.drawTextByWidth(font, "F3 - New Game 15x15", centerX - 0.2f, startY, 0.4f, Color.WHITE);
        startY -= lineHeight;
        graphics.drawTextByWidth(font, "F4 - New Game 20x20", centerX - 0.2f, startY, 0.4f, Color.WHITE);
        startY -= lineHeight;
        graphics.drawTextByWidth(font, "F5 - High Scores", centerX - 0.2f, startY, 0.4f, Color.WHITE);
        startY -= lineHeight;
        graphics.drawTextByWidth(font, "F6 - Credits", centerX - 0.2f, startY, 0.4f, Color.WHITE);
    }

    private void renderHighScores() {
        float startY = 0.4f;
        float lineHeight = 0.08f;
        float centerX = -0.4f;

        graphics.drawTextByWidth(font, "High Scores", -0.2f, startY, 0.4f, Color.YELLOW);
        startY -= 2 * lineHeight;

        if (highScores.isEmpty()) {
            graphics.drawTextByWidth(font, "No scores yet!", centerX, startY, 0.8f, Color.WHITE);
        } else {
            for (HighScore score : highScores) {
                graphics.drawTextByWidth(font, score.toString(), centerX, startY, 0.8f, Color.WHITE);
                startY -= lineHeight;
                if (startY < -0.9f) break;
            }
        }
        startY -= 2 * lineHeight;
        graphics.drawTextByWidth(font, "Press ENTER to return to Menu", -0.4f, startY, 0.8f, Color.WHITE);
    }

    private void renderCredits() {
        float startY = 0.4f;
        float lineHeight = 0.08f;
        float centerX = -0.4f;

        graphics.drawTextByWidth(font, "Credits", -0.2f, startY, 0.4f, Color.YELLOW);
        startY -= 2 * lineHeight;

        graphics.drawTextByWidth(font, "Developed by Jason Merrone", centerX, startY, 0.8f, Color.WHITE);
        startY -= lineHeight;
        startY -= lineHeight;
        graphics.drawTextByWidth(font, "Utah State University", centerX, startY, 0.8f, Color.WHITE);
        startY -= 2 * lineHeight;
        graphics.drawTextByWidth(font, "Press ENTER to return to Menu", -0.4f, startY, 0.8f, Color.WHITE);
    }

    private void renderGame() {
        ArrayList<ArrayList<GridSpace>> gridsToRender = maze.getGridSpaces();
        float cellSize = maze.getCellSize();
        float wallThickness = maze.getWallThickness();

        for (var row : gridsToRender) {
            for (var grid : row) {
                Color wallColor = Color.PURPLE;
                graphics.draw(grid.getBottomRect(), wallColor);
                graphics.draw(grid.getTopRect(), wallColor);
                graphics.draw(grid.getLeftRect(), wallColor);
                graphics.draw(grid.getRightRect(), wallColor);

                if (breadcrumbsEnabled && playerPath.contains(grid)) {
                    Rectangle breadcrumbRect = new Rectangle(
                            grid.getPosition().item1() - cellSize / 4f,
                            grid.getPosition().item2() - cellSize / 4f,
                            cellSize / 2f, cellSize / 2f);
                    graphics.draw(breadcrumbImage, breadcrumbRect, Color.WHITE);
                }
            }
        }

        // Render End Marker
        if (endSpace != null) {
            Rectangle endMarkerRect = new Rectangle(
                    endSpace.getPosition().item1() - cellSize / 2f + wallThickness,
                    endSpace.getPosition().item2() - cellSize / 2f + wallThickness,
                    cellSize - 2 * wallThickness, cellSize - 2 * wallThickness);
            graphics.draw(endMarkerImage, endMarkerRect, Color.WHITE);
        }

        // Render Player
        GridSpace playerGridSpace = maze.getPlayerGridSpace();
        if (playerGridSpace != null) {
            Rectangle playerRect = new Rectangle(
                    playerGridSpace.getPosition().item1() - cellSize / 2f + wallThickness,
                    playerGridSpace.getPosition().item2() - cellSize / 2f + wallThickness,
                    cellSize - 2 * wallThickness, cellSize - 2 * wallThickness);
            graphics.draw(playerImage, playerRect, Color.WHITE);
        }

        // Render Path
        if (pathEnabled && shortestPathFromPlayer != null) {
            Color pathColor = new Color(1f, 1f, 0f, 0.5f); // Semi-transparent yellow
            for (int i = 0; i < shortestPathFromPlayer.size() - 1; i++) {
                GridSpace current = shortestPathFromPlayer.get(i);
                GridSpace next = shortestPathFromPlayer.get(i + 1);
                Vector3f startPoint = new Vector3f(
                        current.getPosition().item1(),
                        current.getPosition().item2(), 0);
                Vector3f endPoint = new Vector3f(
                        next.getPosition().item1(),
                        next.getPosition().item2(), 0);
                graphics.draw(startPoint, endPoint, pathColor); // Draw the connecting line
            }
        }

        // Render Hint
        if (hintEnabled && !pathEnabled && shortestPathFromPlayer != null && shortestPathFromPlayer.size() > 1) {
            GridSpace nextHintSpace = shortestPathFromPlayer.get(1); // Next step on shortest path
            Rectangle hintRect = new Rectangle(
                    nextHintSpace.getPosition().item1() - cellSize / 2.5f + wallThickness,
                    nextHintSpace.getPosition().item2() - cellSize / 2.5f + wallThickness,
                    cellSize / 1.25f - 2 * wallThickness,
                    cellSize / 1.25f - 2 * wallThickness);
            graphics.draw(hintImage, hintRect, Color.WHITE);
        }

        // Display Score and Time
        double elapsedTimeInSeconds = glfwGetTime() - startTime;
        String timeString = String.format("%.0f", elapsedTimeInSeconds);
        graphics.drawTextByWidth(font, "Score: " + score, -0.95f, 0.9f, 0.4f, Color.WHITE);
        graphics.drawTextByWidth(font, "Time: " + timeString, 0.65f, 0.9f, 0.4f, Color.WHITE);
    }
}