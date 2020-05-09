package pl.mini.projectgame.integration.gameMasterMessages;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;
import pl.mini.projectgame.GameMaster;
import pl.mini.projectgame.GameMasterConfiguration;
import pl.mini.projectgame.models.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@ComponentScan
public class GameMasterPickupTests {

    @Autowired
    private GameMaster gameMaster;

    private Message testMessage;
    private MasterBoard testBoard;

    @Before
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
