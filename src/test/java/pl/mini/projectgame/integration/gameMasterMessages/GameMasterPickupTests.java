package pl.mini.projectgame.integration.gameMasterMessages;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import pl.mini.projectgame.GameMaster;
import pl.mini.projectgame.GameMasterConfiguration;
import pl.mini.projectgame.models.*;

import java.io.IOException;
import java.util.Map;

@SpringBootTest
@ComponentScan
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GameMasterPickupTests {

    @Autowired
    private GameMaster gameMaster;

    private Message testMessage;
    private MasterBoard testBoard;

    private Map<Position, Cell> cells;

    @BeforeAll
    void saveConfig() {
        cells = gameMaster.getMasterBoard().getCells();
        gameMaster.setMode(GameMaster.gmMode.GAME);
    }

    @AfterAll
    void cleanUp() {
        gameMaster.getMasterBoard().setCells(cells);
        gameMaster.setMode(GameMaster.gmMode.NONE);
    }

    @BeforeEach
    public void setup() throws IOException {
        GameMasterConfiguration config = new GameMasterConfiguration();
        Position testPosition = new Position(1, 1);
        Player testPlayer = new Player();

        testBoard = new MasterBoard(config);
        testBoard.getCellByPosition(testPosition).addContent(Piece.class, new Piece(0));
        gameMaster.setMasterBoard(testBoard);
        testMessage = new Message();

        testMessage.setPosition(testPosition);
        gameMaster.getPlayerMap().put(testPlayer.getPlayerUuid(), testPlayer);
        testMessage.setAction("pickUp");
        testMessage.setPlayerUuid(testPlayer.getPlayerUuid());
    }

    @AfterEach
    void after() {
        gameMaster.getPlayerMap().clear();
    }

    @Test
    public void serverShouldReturnOKMessage() {
        Message response = gameMaster.processAndReturn(testMessage);
        Assert.assertEquals(Message.Status.OK, response.getStatus());
    }

    @Test
    public void serverShouldReturnErrorMessage() {
        testMessage.setPosition(new Position(3, 3));
        Message response = gameMaster.processAndReturn(testMessage);
        Assert.assertEquals("error", response.getAction());
    }
}
