import edu.usu.graphics.Color;
import edu.usu.graphics.Graphics2D;

public class StarterProject {
    public static void main(String[] args) {
        try (Graphics2D graphics = new Graphics2D(1920, 1080, "Random Maze Game")) {
            graphics.initialize(Color.CORNFLOWER_BLUE);
            Game game = new Game(graphics);
            game.initialize();
            game.run();
            game.shutdown();
        }
    }
}