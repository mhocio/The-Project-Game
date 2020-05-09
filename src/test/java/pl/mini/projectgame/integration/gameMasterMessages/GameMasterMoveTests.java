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
        message.setPlayer(player);
        message.setAction("move");
        message.setPosition(player.getPosition());
    }

    @Test
    void testMoveActionMessageUp() throws DeniedMoveException
    {
        player.setPosition(new Position(1, 1));
        gameMaster.getMasterBoard().addBoardObject(player, player.getPosition());

        message.setDirection(Message.Direction.UP);

        Message response = gameMaster.processAndReturn(message);
        assertEquals(new Position(1, 2), response.getPosition());
    }

    @Test
    void testMoveActionMessageDown() throws DeniedMoveException
    {
        player.setPosition(new Position(1, 1));
        gameMaster.getMasterBoard().addBoardObject(player, player.getPosition());

        message.setDirection(Message.Direction.DOWN);

        Message response = gameMaster.processAndReturn(message);
        assertEquals(new Position(1, 0), response.getPosition());
    }

    @Test
    void testMoveActionMessageLeft() throws DeniedMoveException
    {
        player.setPosition(new Position(1, 1));
        gameMaster.getMasterBoard().addBoardObject(player, player.getPosition());

        message.setDirection(Message.Direction.LEFT);

        Message response = gameMaster.processAndReturn(message);
        assertEquals(new Position(0, 1), response.getPosition());
    }

    @Test
    void testMoveActionMessageRight() throws DeniedMoveException
    {
        player.setPosition(new Position(1, 1));
        gameMaster.getMasterBoard().addBoardObject(player, player.getPosition());

        message.setDirection(Message.Direction.RIGHT);

        Message response = gameMaster.processAndReturn(message);
        assertEquals(new Position(2, 1), response.getPosition());
    }
}