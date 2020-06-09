package pl.mini.projectgame.integration.gameMasterMessages;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import pl.mini.projectgame.GameMaster;
import pl.mini.projectgame.GameMasterConfiguration;
import pl.mini.projectgame.models.*;

@SpringBootTest
@ComponentScan
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GameMasterDiscoverTests {

    @Autowired
    private GameMaster gameMaster;

    private Message testMessage;
    private MasterBoard testBoard;

    private Position testPosition;
    private Player testPlayer = new Player();

    @BeforeAll
    void beforeAll() {
        gameMaster.setMode(GameMaster.gmMode.GAME);
    }

    @AfterAll
    void cleanup() {
        gameMaster.setMode(GameMaster.gmMode.NONE);
    }

    @BeforeEach
    public void setup() {
        testMessage = new Message();
        testMessage.setAction("discover");
        GameMasterConfiguration config = new GameMasterConfiguration();

        testBoard = new MasterBoard(config);
        gameMaster.setMasterBoard(testBoard);

        testPosition = new Position(
                gameMaster.getMasterBoard().getBoardWidth() / 2,
                gameMaster.getMasterBoard().getGoalAreaHeight() + 4);
        testPlayer.setPosition(testPosition);

        gameMaster.getPlayerMap().put(testPlayer.getPlayerUuid(), testPlayer);
        testMessage.setPlayerUuid(testPlayer.getPlayerUuid());
    }

    @Test
    public void serverShouldReturnEightFields() {
        Message response = gameMaster.processAndReturn(testMessage);
        Assert.assertEquals(8, response.getFields().size());
    }

    @Test
    public void serverShouldReturnThreeFields() {
        Position newPos = new Position(0, gameMaster.getMasterBoard().getGoalAreaHeight());
        testPlayer.setPosition(newPos);
        Message response = gameMaster.processAndReturn(testMessage);
        Assert.assertEquals(3, response.getFields().size());
    }

    @Test
    public void serverShouldReturnFiveFields() {
        testPosition.setY(gameMaster.getMasterBoard().getGoalAreaHeight());
        testPlayer.setPosition(testPosition);
        Message response = gameMaster.processAndReturn(testMessage);
        Assert.assertEquals(5, response.getFields().size());
    }

    @Test
    public void serverShouldReturnErrorMessage() {
        testPlayer.setPosition(null);
        Message response = gameMaster.processAndReturn(testMessage);
        Assert.assertEquals(Message.Status.DENIED, response.getStatus());
    }
}
