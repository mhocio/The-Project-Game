package pl.mini.projectgame.unit;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import pl.mini.projectgame.GameMasterConfiguration;
import pl.mini.projectgame.models.Board;
import pl.mini.projectgame.models.Position;

import java.util.Random;

@SpringBootTest
@ComponentScan
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BoardTests {

    @Autowired
    private GameMasterConfiguration configuration;
    private Board board;

    @BeforeEach
    public void setup() {
        board = new Board(configuration);
    }

    @Test
    public void allCellsShouldBeCreated() {
        int expected = board.getBoardWidth() * board.getHeight();
        int actual = board.getCells().size();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void checkIfConfigurationWasApplied() {
        int expected = configuration.getBoardWidth();
        int actual = board.getBoardWidth();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void checkIfRandomCellCreatedCorrectly() {
        Random random = new Random();
        var expected = new Position(
                random.nextInt(board.getBoardWidth()),
                random.nextInt(board.getHeight()));

        var actual = board.getCells().get(expected).getPosition();

        Assert.assertEquals(expected, actual);
    }

}
