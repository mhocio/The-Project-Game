package pl.mini.projectgame.integration.gameMasterMessages;

import org.junit.Assert;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;
import pl.mini.projectgame.GameMaster;
import pl.mini.projectgame.models.Message;
import pl.mini.projectgame.models.Player;
import pl.mini.projectgame.models.Team;

@SpringBootTest
@ComponentScan
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GameMasterPlayerConnectionTests {

    @Autowired
    private GameMaster gameMaster;

    private Message testMessage;
    private Player player;

    @AfterAll
    void cleanUp() {
        gameMaster.setBlueTeam(new Team(Team.TeamColor.BLUE));
        gameMaster.setRedTeam(new Team(Team.TeamColor.RED));
    }

    @BeforeEach
    public void setup() {
        testMessage = new Message();
        player = new Player();
        player.setPlayerName("Test");

        testMessage.setAction("connect");
        testMessage.setPlayer(player);
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
            Assert.assertTrue(gameMaster.getRedTeam().getPlayers().containsKey(response.getPlayer()));
        } else {
            Assert.assertTrue(gameMaster.getBlueTeam().getPlayers().containsKey(response.getPlayer()));
        }
    }

    @Test
    public void serverShouldReturnTheSamePlayer() {
        Message response = gameMaster.processAndReturn(testMessage);
        Assert.assertEquals(player.getPlayerName(), response.getPlayer().getPlayerName());
    }

    @Test
    public void serverShouldReturnErrorMessage() {
        testMessage.setPlayer(null);
        Message response = gameMaster.processAndReturn(testMessage);
        Assert.assertEquals("error", response.getAction());
    }
}
