package pl.mini.projectgame.models;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;
import pl.mini.projectgame.GameMasterConfiguration;

import java.util.Random;

@RunWith(SpringRunner.class)
@SpringBootTest
@ComponentScan
public class BoardTests {

    @Autowired
    private GameMasterConfiguration configuration;
    private Board board;

    @Before
    public void setup() {
        board = new Board(configuration);
    }

    @Test
    public void allCellsShouldBeCreated() {
        int expected = board.getWidth() * board.getHeight();
        int actual = board.getCells().size();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void checkIfConfigurationWasApplied() {
        int expected = configuration.getBoardWidth();
        int actual = board.getWidth();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void checkIfRandomCellCreatedCorrectly() {
        Random random = new Random();
        var expected = new Position(
                random.nextInt(board.getWidth()),
                random.nextInt(board.getHeight()));

        var actual = board.getCells().get(expected).getPosition();

        Assert.assertEquals(expected, actual);
    }

}
