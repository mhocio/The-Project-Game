package pl.mini.projectgame.integration.gameMasterMessages;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;
import pl.mini.projectgame.GameMaster;
import pl.mini.projectgame.GameMasterConfiguration;
import pl.mini.projectgame.models.*;

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
    }

    @AfterAll
    void cleanUp() {
        gameMaster.getMasterBoard().setCells(cells);
    }

    @BeforeEach
    public void setup() {
        GameMasterConfiguration config = new GameMasterConfiguration();
        testBoard = new MasterBoard(config);
        testBoard.getCellByPosition(new Position(1,1)).addContent(Piece.class,new Piece(0));
        gameMaster.setMasterBoard(testBoard);
        testMessage = new Message();
        Position testPosition = new Position(1,1);
        testMessage.setPosition(testPosition);
        Player testPlayer = new Player();
        testPlayer.setPosition(testPosition);
        testMessage.setPlayer(testPlayer);
        testMessage.setAction("pickUp");
    }

    @Test
    public void serverShouldReturnOKMessage() {
        Message response = gameMaster.processAndReturn(testMessage);
        Assert.assertEquals(Message.Status.OK, response.getStatus());
    }
    @Test
    public void serverShouldReturnErrorMessage() {
        testMessage.setPosition(new Position(3,3));
        Message response = gameMaster.processAndReturn(testMessage);
        Assert.assertEquals("error", response.getAction());
    }


}
