package pl.mini.projectgame;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pl.mini.projectgame.exceptions.DeniedMoveException;
import pl.mini.projectgame.models.*;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class GameMasterMoveTests {

    @Autowired
    private GameMaster gameMaster;

    Message message;
    Player player;

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
}
