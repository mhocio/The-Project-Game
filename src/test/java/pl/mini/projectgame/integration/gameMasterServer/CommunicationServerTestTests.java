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

    private Message message;
    private Player player;
    private Piece piece;

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
    }

    @AfterAll
    void cleanUp() {
        gameMaster.getMasterBoard().setCells(cells);
    }

    @BeforeEach
    void initTestPiece() throws IOException {
        client = new Socket(InetAddress.getLocalHost().getHostName(), 8080);
        out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
        in = new BufferedReader(new InputStreamReader(client.getInputStream()));

        JsonFactory jsonFactory = new JsonFactory();
        jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        mapper = new ObjectMapper(jsonFactory);

        message = new Message();
        player = new Player();
        piece = new Piece(0.5);

        player.setPosition(new Position(1,1));

        gameMaster.getMasterBoard().getCellByPosition(player.getPosition()).getContent().clear();

        gameMaster.getMasterBoard().addBoardObject(player, player.getPosition());
        gameMaster.getMasterBoard().addBoardObject(piece, player.getPosition());

    }

    @AfterEach
    public void tearDown() throws IOException {
        in.close();
        out.close();
        client.close();
    }

    @Test
    void testAction() throws IOException {
        message.setPlayer(player);
        message.setAction("test");

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
        message.setAction("test");
        message.setPlayer(null);

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
        message.setPlayer(player);
        message.setAction("test");

        gameMaster.getMasterBoard().getCellByPosition(player.getPosition()).removeContent(Piece.class);

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
        message.setPlayer(player);
        message.setAction("test");

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
