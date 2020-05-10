package pl.mini.projectgame.GameMasterMessages;

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

import javax.swing.plaf.metal.MetalIconFactory;

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
        gameMaster = new GameMaster(new GameMasterConfiguration(),testBoard);
        testMessage = new Message();
        Position testPosition = new Position(1,1);
        testMessage.setPosition(testPosition);
        Player testPlayer = new Player();
        testPlayer.setPosition(testPosition);
        testMessage.setPlayer(testPlayer);
        testMessage.setAction("pickup");
    }
    @Test
    public void serverShouldReturnOKMessage() {
        Message response = gameMaster.processAndReturn(testMessage);
        Assert.assertEquals("OK", response.getAction());
    }
    @Test
    public void serverShouldReturnErrorMessage() {
        Player WrongPlacePlayer = new Player();
        WrongPlacePlayer.setPosition(new Position(3,3));
        testMessage.setPlayer(WrongPlacePlayer);
        Message response = gameMaster.processAndReturn(testMessage);
        Assert.assertEquals("error", response.getAction());
    }


}
