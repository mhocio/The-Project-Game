package pl.mini.projectgame.integration.gameMasterMessages;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import pl.mini.projectgame.GameMaster;
import pl.mini.projectgame.models.*;

import java.util.Map;

import static org.junit.Assert.*;


@SpringBootTest
@ComponentScan
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GameMasterTestTests {

    @Autowired
    private GameMaster gameMaster;

    Message message;
    Player player;
    Piece piece;

    private Map<Position, Cell> cells;

    @BeforeAll
    void saveConfig() {
        cells = gameMaster.getMasterBoard().getCells();
    }

    @AfterAll
    void cleanUp() {
        gameMaster.getMasterBoard().setCells(cells);
    }

    @BeforeEach
    void initTestPiece() {
        message = new Message();
        player = new Player();
        piece = new Piece(0.5);

        player.setPosition(new Position(1,1));

        gameMaster.getMasterBoard().getCellByPosition(player.getPosition()).getContent().clear();

        gameMaster.getMasterBoard().addBoardObject(player, player.getPosition());
        gameMaster.getMasterBoard().addBoardObject(piece, player.getPosition());

    }

    @Test
    void testAction(){
        message.setPlayer(player);
        message.setAction("test");

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

        gameMaster.getMasterBoard().getCellByPosition(player.getPosition()).removeContent(Piece.class);

        Message response = gameMaster.processAndReturn(message);
        assertEquals(Message.Status.DENIED, response.getStatus());
    }


    @Test
    void testActionDoubleTest(){
        message.setPlayer(player);
        message.setAction("test");

        gameMaster.processAndReturn(message);
        Message response = gameMaster.processAndReturn(message);
        assertEquals(Message.Status.DENIED, response.getStatus());
    }
}
