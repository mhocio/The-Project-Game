package pl.mini.projectgame.integration.gameMasterServer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pl.mini.projectgame.GameMaster;
import pl.mini.projectgame.models.*;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;


@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CommunicationServerTestTests {

    Message message;
    Position position;
    Piece piece;
    Player player;

    @Autowired
    private GameMaster gameMaster;
    private Socket client;
    private BufferedWriter out;
    private BufferedReader in;
    private ObjectMapper mapper;

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
    void initTestPiece() throws IOException {
        client = new Socket(InetAddress.getLocalHost().getHostName(), 8000);
        out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
        in = new BufferedReader(new InputStreamReader(client.getInputStream()));

        JsonFactory jsonFactory = new JsonFactory();
        jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        mapper = new ObjectMapper(jsonFactory);

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
    public void tearDown() throws IOException {
        in.close();
        out.close();
        client.close();
    }

    @Test
    void testAction() throws IOException {

        mapper.writeValue(out, message);
        out.flush();

        CharBuffer cb = CharBuffer.allocate(1024);
        int ret = in.read(cb);
        cb.flip();

        Message response = mapper.readValue(cb.toString(), Message.class);
        assertNotEquals("error", response.getAction());
    }

    @Test
    void testActionNullPlayer() throws IOException {
        message.setPlayerUuid(null);
        mapper.writeValue(out, message);
        out.flush();

        CharBuffer cb = CharBuffer.allocate(1024);
        int ret = in.read(cb);
        cb.flip();

        Message response = mapper.readValue(cb.toString(), Message.class);
        assertEquals("error", response.getAction());
    }

    @Test
    void testActionNoPiece() throws IOException {

        gameMaster.getMasterBoard().getCellByPosition(position).removeContent(Piece.class);

        mapper.writeValue(out, message);
        out.flush();

        CharBuffer cb = CharBuffer.allocate(1024);
        int ret = in.read(cb);
        cb.flip();

        Message response = mapper.readValue(cb.toString(), Message.class);
        assertEquals(Message.Status.DENIED, response.getStatus());
    }


    @Test
    void testActionDoubleTest() throws IOException {

        CharBuffer cb = CharBuffer.allocate(1024);

        mapper.writeValue(out, message);
        out.flush();
        int ret = in.read(cb);
        cb.flip();
        cb.clear();

        mapper.writeValue(out, message);
        out.flush();

        ret = in.read(cb);
        cb.flip();

        Message response = mapper.readValue(cb.toString(), Message.class);
        assertEquals(Message.Status.DENIED, response.getStatus());
    }
}
