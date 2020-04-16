package pl.mini.projectgame.GameMasterMessages;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pl.mini.projectgame.GameMaster;
import pl.mini.projectgame.models.Message;
import pl.mini.projectgame.models.Piece;
import pl.mini.projectgame.models.Player;
import pl.mini.projectgame.models.Position;
import static org.junit.Assert.*;


@SpringBootTest
public class GameMasterTestTests {
    @Autowired
    private GameMaster gameMaster;

    Message message;
    Player player;
    Piece piece;

    @BeforeEach
    void initTestPiece() {
        message = new Message();
        player = new Player();
        piece = new Piece(0.5);
    }

    @Test
    void testAction(){
        message.setPlayer(player);
        message.setAction("test");

        player.setPosition(new Position(1,1));

        gameMaster.getMasterBoard().addBoardObject(player, player.getPosition());
        gameMaster.getMasterBoard().addBoardObject(piece, player.getPosition());

        Message response = gameMaster.processAndReturn(message);
        assertNotEquals("error", response.getAction());
    }

    @Test
    void testActionNullPlayer(){
        message.setAction("test");
        message.setPlayer(null);

        Message response = gameMaster.processAndReturn(message);
        assertEquals("error", response.getAction());
    }

    @Test
    void testActionNoPiece(){
        message.setPlayer(player);
        message.setAction("test");

        player.setPosition(new Position(1,2));

        gameMaster.getMasterBoard().addBoardObject(player, player.getPosition());

        Message response = gameMaster.processAndReturn(message);
        assertEquals(Message.Status.DENIED, response.getStatus());
    }


    @Test
    void testActionDoubleTest(){
        message.setPlayer(player);
        message.setAction("test");

        player.setPosition(new Position(1,1));

        gameMaster.getMasterBoard().addBoardObject(player, player.getPosition());
        gameMaster.getMasterBoard().addBoardObject(piece, player.getPosition());

        gameMaster.processAndReturn(message);
        Message response = gameMaster.processAndReturn(message);
        assertEquals(Message.Status.DENIED, response.getStatus());
    }
}
