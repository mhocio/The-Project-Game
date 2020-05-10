package pl.mini.projectgame.integration.gameMasterMessages;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import pl.mini.projectgame.GameMaster;
import pl.mini.projectgame.exceptions.DeniedMoveException;
import pl.mini.projectgame.models.*;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GameMasterMoveTests {

    @Autowired
    private GameMaster gameMaster;

    Message message;
    Player player;

    @AfterAll
    void cleanUp() {
        gameMaster.getMasterBoard().getCells().forEach((k,v) -> v.setContent(new HashMap<>()));
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
    void testMoveActionMessageUp() throws DeniedMoveException
    {
        message.setDirection(Message.Direction.UP);

        Message response = gameMaster.processAndReturn(message);
        assertEquals(new Position(1, 2), response.getPosition());
    }

    @Test
    void testMoveActionMessageDown() throws DeniedMoveException
    {
        message.setDirection(Message.Direction.DOWN);

        Message response = gameMaster.processAndReturn(message);
        assertEquals(new Position(1, 0), response.getPosition());
    }

    @Test
    void testMoveActionMessageLeft() throws DeniedMoveException
    {
        message.setDirection(Message.Direction.LEFT);

        Message response = gameMaster.processAndReturn(message);
        assertEquals(new Position(0, 1), response.getPosition());
    }

    @Test
    void testMoveActionMessageRight() throws DeniedMoveException
    {
        message.setDirection(Message.Direction.RIGHT);

        Message response = gameMaster.processAndReturn(message);
        assertEquals(new Position(2, 1), response.getPosition());
    }
}