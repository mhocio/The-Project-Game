package pl.mini.projectgame.integration.gameMasterMessages;

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
public class GameMasterMoveTests {

    @Autowired
    private GameMaster gameMaster;

    Message message;
    Player player;

    @BeforeAll
    void setup() {
        gameMaster.setMode(GameMaster.gmMode.GAME);
    }

    @AfterAll
    void cleanUp() {
        gameMaster.getMasterBoard().getCells().forEach((k, v) -> v.setContent(new HashMap<>()));
        gameMaster.setMode(GameMaster.gmMode.NONE);
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
        message.setDirection(Message.Direction.Up);

        Message response = gameMaster.processAndReturn(message);
        assertEquals(new Position(1, 2), response.getPosition());
    }

    @Test
    void testMoveActionMessageDown() throws DeniedMoveException {
        message.setDirection(Message.Direction.Down);

        Message response = gameMaster.processAndReturn(message);
        assertEquals(new Position(1, 0), response.getPosition());
    }

    @Test
    void testMoveActionMessageLeft() throws DeniedMoveException {
        message.setDirection(Message.Direction.Left);

        Message response = gameMaster.processAndReturn(message);
        assertEquals(new Position(0, 1), response.getPosition());
    }

    @Test
    void testMoveActionMessageRight() throws DeniedMoveException {
        message.setDirection(Message.Direction.Right);

        Message response = gameMaster.processAndReturn(message);
        assertEquals(new Position(2, 1), response.getPosition());
    }
}