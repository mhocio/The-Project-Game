package pl.mini.projectgame.integration.gameMasterMessages;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import pl.mini.projectgame.GameMaster;
import pl.mini.projectgame.GameMasterConfiguration;
import pl.mini.projectgame.models.MasterBoard;
import pl.mini.projectgame.models.Message;

@SpringBootTest
@ComponentScan
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GameMasterSetupTests {

    @Autowired
    private GameMaster gameMaster;

    private Message testMessage;

    private GameMasterConfiguration config;
    private MasterBoard masterBoard;

    @BeforeAll
    void saveConfig() {
        masterBoard = gameMaster.getMasterBoard();
        config = gameMaster.getConfiguration();
    }

    @AfterAll
    void cleanUp() {
        gameMaster.setMasterBoard(masterBoard);
        gameMaster.setConfiguration(config);
    }

    @BeforeEach
    public void setup() {
        testMessage = new Message();
        testMessage.setAction("setup");
    }

    @Test
    public void serverShouldReturnOKMessage() {
        Message response = gameMaster.processAndReturn(testMessage);
        Assert.assertEquals(Message.Status.OK, response.getStatus());
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

}
