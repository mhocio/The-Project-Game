package pl.mini.projectgame.integration.communicationServerDelay;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pl.mini.projectgame.GameMaster;
import pl.mini.projectgame.exceptions.DeniedMoveException;
import pl.mini.projectgame.models.Message;
import pl.mini.projectgame.models.Player;
import pl.mini.projectgame.models.Position;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CommunicationServerDelayMoveTests {

    @Autowired
    private GameMaster gameMaster;

    Message message;
    Player player;

    @AfterAll
    void cleanUp() {
        gameMaster.getMasterBoard().getCells().forEach((k, v) -> v.setContent(new HashMap<>()));
    }

    @BeforeEach
    void initTeam() {
        message = new Message();
        player = new Player();
        message.setAction("move");
        message.setPlayerUuid(player.getPlayerUuid());
        gameMaster.getPlayerMap().put(player.getPlayerUuid(), player);

        player.setPosition(new Position(1, 1));
        gameMaster.getMasterBoard().addBoardObject(player, player.getPosition());
        message.setPosition(player.getPosition());
    }

    @AfterEach
    void after() {
        gameMaster.getPlayerMap().clear();
    }

    @Test
    void testMoveActionMessageUp() throws DeniedMoveException {
        long startTime = System.nanoTime();

        message.setDirection(Message.Direction.UP);
        Message response = gameMaster.processAndReturn(message);

        long endTime = System.nanoTime();
        long diffInMiliseconds = (endTime - startTime)/1000;

        boolean result;
        System.out.println("test: " + diffInMiliseconds);
        System.out.println(gameMaster.getConfiguration().getDelayMove());
        assertEquals(new Position(1, 2), response.getPosition());
    }
}