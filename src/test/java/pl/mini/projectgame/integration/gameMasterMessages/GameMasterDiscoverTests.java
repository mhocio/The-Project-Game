package pl.mini.projectgame.integration.gameMasterMessages;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import pl.mini.projectgame.GameMaster;
import pl.mini.projectgame.models.Message;
import pl.mini.projectgame.models.Position;

@SpringBootTest
@ComponentScan
public class GameMasterDiscoverTests {

    @Autowired
    private GameMaster gameMaster;

    private Message testMessage;

    @BeforeEach
    public void setup() {
        testMessage = new Message();
        testMessage.setAction("discover");
        testMessage.setPosition(new Position(
                gameMaster.getMasterBoard().getWidth() / 2,
                gameMaster.getMasterBoard().getGoalAreaHeight() + 4));
    }

    @Test
    public void serverShouldReturnEightFields() {
        Message response = gameMaster.processAndReturn(testMessage);
        Assert.assertEquals(8, response.getFields().size());
    }

    @Test
    public void serverShouldReturnThreeFields() {
        testMessage.getPosition().setX(0);
        testMessage.getPosition().setY(gameMaster.getMasterBoard().getGoalAreaHeight());
        Message response = gameMaster.processAndReturn(testMessage);
        Assert.assertEquals(3, response.getFields().size());
    }

    @Test
    public void serverShouldReturnFiveFields() {
        testMessage.getPosition().setY(gameMaster.getMasterBoard().getGoalAreaHeight());
        Message response = gameMaster.processAndReturn(testMessage);
        Assert.assertEquals(5, response.getFields().size());
    }

    @Test
    public void serverShouldReturnErrorMessage() {
        testMessage.setPosition(null);
        Message response = gameMaster.processAndReturn(testMessage);
        Assert.assertEquals("error", response.getAction());
    }
}
