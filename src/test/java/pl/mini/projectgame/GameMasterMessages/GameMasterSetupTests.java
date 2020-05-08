package pl.mini.projectgame.GameMasterMessages;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;
import pl.mini.projectgame.GameMaster;
import pl.mini.projectgame.GameMasterConfiguration;
import pl.mini.projectgame.models.MasterBoard;
import pl.mini.projectgame.models.Message;
import pl.mini.projectgame.models.Player;
import pl.mini.projectgame.models.Position;

@RunWith(SpringRunner.class)
@SpringBootTest
@ComponentScan
public class GameMasterSetupTests {

    @Autowired
    private GameMaster gameMaster;
    private Message testMessage;

    @Before
    public void setup() {
        testMessage = new Message();
        testMessage.setAction("setup");
    }
    @Test
    public void serverShouldReturnErrorMessage1() {
        gameMaster.setConfiguration(null);
        Message response = gameMaster.processAndReturn(testMessage);
        Assert.assertEquals("error", response.getAction());
    }
    @Test
    public void serverShouldReturnErrorMessage2() {
        gameMaster.setMasterBoard(null);
        Message response = gameMaster.processAndReturn(testMessage);
        Assert.assertEquals("error", response.getAction());
    }
    @Test
    public void serverShouldReturnOKMessage() {
        gameMaster = new GameMaster(new GameMasterConfiguration(),new MasterBoard(new GameMasterConfiguration()));
        Message response = gameMaster.processAndReturn(testMessage);
        Assert.assertEquals("OK", response.getAction());
    }

}
