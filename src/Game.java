import com.maze.GridSpace;
import com.maze.Maze;
import edu.usu.graphics.*;

import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;

public class Game {
    private final Graphics2D graphics;
    private Maze maze;

    private Texture backgroundImage;
    private Rectangle backgroundRect = new Rectangle(-.4f, -.4f, .8f, .8f);

    public Game(Graphics2D graphics) {
        this.graphics = graphics;
    }

    public void initialize() {
        backgroundImage = new Texture("resources/images/stick.png");
        maze = new Maze(.8f,18);
    }

    public void shutdown() {
        backgroundImage.cleanup();
    }

    public void run() {
        // Grab the first time
        double previousTime = glfwGetTime();

        while (!graphics.shouldClose()) {
            double currentTime = glfwGetTime();
            double elapsedTime = currentTime - previousTime;    // elapsed time is in seconds
            previousTime = currentTime;

            processInput(elapsedTime);
            update(elapsedTime);
            render(elapsedTime);
        }
    }

    private void processInput(double elapsedTime) {
        // Poll for window events: required in order for window, keyboard, etc events are captured.
        glfwPollEvents();

        // If user presses ESC, then exit the program
        if (glfwGetKey(graphics.getWindow(), GLFW_KEY_ESCAPE) == GLFW_PRESS) {
            glfwSetWindowShouldClose(graphics.getWindow(), true);
        }
    }

    private void update(double elapsedTime) {

    }

    private void render(double elapsedTime) {
        graphics.begin();
        graphics.draw(backgroundImage,backgroundRect,Color.WHITE);
        ArrayList<ArrayList<GridSpace>> gridsToRender = maze.getGridSpaces();

        for(var row : gridsToRender){
            for(var grid : row) {
                graphics.draw(grid.getBottomRect(), Color.PURPLE);
                graphics.draw(grid.getTopRect(), Color.PURPLE);
                graphics.draw(grid.getLeftRect(), Color.PURPLE);
                graphics.draw(grid.getRightRect(), Color.PURPLE);
            }
        }

        graphics.end();
    }
}
