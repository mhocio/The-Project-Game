package pl.mini.projectgame.integration.gameMasterServer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import pl.mini.projectgame.GameMaster;
import pl.mini.projectgame.GameMasterConfiguration;
import pl.mini.projectgame.models.*;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.Map;

@SpringBootTest
@ComponentScan
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CommunicationServerPickupTests {

    @Autowired
    private GameMaster gameMaster;
    private Message testMessage;

    private Socket client;
    private BufferedWriter out;
    private BufferedReader in;
    private ObjectMapper mapper;
    private MasterBoard testBoard;

    private Map<Position, Cell> cells;

    @BeforeAll
    void saveConfig() throws IOException {
        cells = gameMaster.getMasterBoard().getCells();
        client = new Socket(InetAddress.getLocalHost().getHostName(), 8080);
        out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
        in = new BufferedReader(new InputStreamReader(client.getInputStream()));

        JsonFactory jsonFactory = new JsonFactory();
        jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        mapper = new ObjectMapper(jsonFactory);
    }

    @AfterAll
    void cleanUp() throws IOException {
        gameMaster.getMasterBoard().setCells(cells);
        in.close();
        out.close();
        client.close();
    }

    @BeforeEach
    public void setup() throws IOException {
        GameMasterConfiguration config = new GameMasterConfiguration();
        testBoard = new MasterBoard(config);
        testBoard.getCellByPosition(new Position(1, 1)).addContent(Piece.class, new Piece(0));
        gameMaster.setMasterBoard(testBoard);
        testMessage = new Message();
        Position testPosition = new Position(1, 1);
        testMessage.setPosition(testPosition);
        Player testPlayer = new Player();
        testPlayer.setPosition(testPosition);
        testMessage.setPlayer(testPlayer);
        testMessage.setAction("pickUp");
    }

    @Test
    public void serverShouldReturnOKMessage() throws IOException {
        mapper.writeValue(out, testMessage);
        out.flush();

        CharBuffer cb = CharBuffer.allocate(1024);
        int ret = in.read(cb);
        cb.flip();

        Message response = mapper.readValue(cb.toString(), Message.class);
        Assert.assertEquals(Message.Status.OK, response.getStatus());
    }
    @Test
    public void serverShouldReturnErrorMessage() throws IOException {
        testMessage.setPosition(new Position(3,3));
        mapper.writeValue(out, testMessage);
        out.flush();

        CharBuffer cb = CharBuffer.allocate(1024);
        int ret = in.read(cb);
        cb.flip();

        Message response = mapper.readValue(cb.toString(), Message.class);
        Assert.assertEquals("error", response.getAction());
    }


}
