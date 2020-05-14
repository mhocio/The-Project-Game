package pl.mini.projectgame.integration.gameMasterMessages;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import pl.mini.projectgame.GameMaster;
import pl.mini.projectgame.models.*;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;


@SpringBootTest
@ComponentScan
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GameMasterTestTests {

    @Autowired
    private GameMaster gameMaster;

    Message message;
    Position position;
    Piece piece;
    Player player;

    private Map<Position, Cell> cells;

    @BeforeAll
    void saveConfig() {
        cells = gameMaster.getMasterBoard().getCells();
        gameMaster.setMode(GameMaster.gmMode.GAME);
    }

    @AfterAll
    void cleanUp() {
        gameMaster.getMasterBoard().setCells(cells);
        gameMaster.setMode(GameMaster.gmMode.NONE);
    }

    @BeforeEach
    void initTestPiece() {
        message = new Message();
        piece = new Piece(0.5);
        position = new Position(1, 1);
        player = new Player();

        gameMaster.getPlayerMap().put(player.getPlayerUuid(), player);
        gameMaster.getMasterBoard().getCellByPosition(position).getContent().clear();
        gameMaster.getMasterBoard().addBoardObject(player, position);
        gameMaster.getMasterBoard().addBoardObject(piece, position);

        message.setPosition(position);
        message.setPlayer(player);
        message.setAction("test");
        message.setPlayerUuid(player.getPlayerUuid());
    }

    @AfterEach
    void after() {
        gameMaster.getPlayerMap().clear();
    }

    @Test
    void testAction() {
        Message response = gameMaster.processAndReturn(message);
        assertNotEquals("error", response.getAction());
    }

    @Test
    void testActionNullPlayerUuid() {
        message.setPlayerUuid(null);
        Message response = gameMaster.processAndReturn(message);
        assertEquals("error", response.getAction());
    }

    @Test
    void testActionNoPiece() {
        gameMaster.getMasterBoard().getCellByPosition(position).removeContent(Piece.class);

        Message response = gameMaster.processAndReturn(message);
        assertEquals(Message.Status.DENIED, response.getStatus());
    }


    @Test
    void testActionDoubleTest() {

        gameMaster.processAndReturn(message);
        Message response = gameMaster.processAndReturn(message);
        assertEquals(Message.Status.DENIED, response.getStatus());
    }
}
