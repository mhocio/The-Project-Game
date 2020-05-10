package pl.mini.projectgame.integration.gameMasterMessages;

import org.junit.Assert;

import org.junit.jupiter.api.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;
import pl.mini.projectgame.GameMaster;
import pl.mini.projectgame.models.Message;
import pl.mini.projectgame.models.Player;
import pl.mini.projectgame.models.Team;

import java.util.UUID;

@SpringBootTest
@ComponentScan
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GameMasterPlayerConnectionTests {

    @Autowired
    private GameMaster gameMaster;

    private Message testMessage;

    @AfterEach
    void cleanUp() {
        gameMaster.setBlueTeam(new Team(Team.TeamColor.BLUE));
        gameMaster.setRedTeam(new Team(Team.TeamColor.RED));
    }

    @BeforeEach
    public void setup() {
        testMessage = new Message();
        testMessage.setAction("connect");
    }

    @Test
    public void serverShouldReturnOkStatus() {
        Message response = gameMaster.processAndReturn(testMessage);
        Assert.assertEquals(Message.Status.OK, response.getStatus());
    }

    @Test
    public void serverShouldAddPlayerToTheTeam() {
        Message response = gameMaster.processAndReturn(testMessage);

        if(gameMaster.isLastTeamWasRed()) {
            Assert.assertFalse(gameMaster.getRedTeam().getPlayers().isEmpty());
        } else {
            Assert.assertFalse(gameMaster.getBlueTeam().getPlayers().isEmpty());
        }
    }

    @Test
    public void serverShouldReturnPlayer() {
        Message response = gameMaster.processAndReturn(testMessage);
        Assert.assertNotNull(response.getPlayerUuid());
    }

    @Test
    public void serverShouldReturnErrorMessage() {
        testMessage.setAction("coNnect");
        Message response = gameMaster.processAndReturn(testMessage);
        Assert.assertEquals("error", response.getAction());
    }
}
