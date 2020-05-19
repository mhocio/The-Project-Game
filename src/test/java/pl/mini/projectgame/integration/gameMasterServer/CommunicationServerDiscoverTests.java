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
import pl.mini.projectgame.models.MasterBoard;
import pl.mini.projectgame.models.Message;
import pl.mini.projectgame.models.Player;
import pl.mini.projectgame.models.Position;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.CharBuffer;

@SpringBootTest
@ComponentScan
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CommunicationServerDiscoverTests {

    @Autowired
    private GameMaster gameMaster;
    private Message testMessage;

    private Socket client;
    private BufferedWriter out;
    private BufferedReader in;
    private ObjectMapper mapper;

    private MasterBoard testBoard;
    private Position testPosition;
    private Player testPlayer = new Player();

    @BeforeAll
    void beforeAll() {
        gameMaster.setMode(GameMaster.gmMode.GAME);
    }

    @AfterAll
    void afterAll() {
        gameMaster.setMode(GameMaster.gmMode.NONE);
    }

    @BeforeEach
    public void setup() throws IOException {
        client = new Socket(InetAddress.getLocalHost().getHostName(), 8080);
        out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
        in = new BufferedReader(new InputStreamReader(client.getInputStream()));

        JsonFactory jsonFactory = new JsonFactory();
        jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        mapper = new ObjectMapper(jsonFactory);

        testMessage = new Message();
        testMessage.setAction("discover");
        GameMasterConfiguration config = new GameMasterConfiguration();

        testBoard = new MasterBoard(config);
        gameMaster.setMasterBoard(testBoard);

        testPosition = new Position(
                gameMaster.getMasterBoard().getWidth() / 2,
                gameMaster.getMasterBoard().getGoalAreaHeight() + 4);
        testPlayer.setPosition(testPosition);

        gameMaster.getPlayerMap().put(testPlayer.getPlayerUuid(), testPlayer);
        testMessage.setPlayerUuid(testPlayer.getPlayerUuid());
    }

    @AfterEach
    public void tearDown() throws IOException {
        in.close();
        out.close();
        client.close();
    }


    @Test
    public void serverShouldReturnEightFields() throws IOException {
        mapper.writeValue(out, testMessage);
        out.flush();

        CharBuffer cb = CharBuffer.allocate(1024);
        int ret = in.read(cb);
        cb.flip();

        Message response = mapper.readValue(cb.toString(), Message.class);
        Assert.assertEquals(8, response.getFields().size());
    }

    @Test
    public void serverShouldReturnThreeFields() throws IOException {
        testPosition.setX(0);
        testPosition.setY(gameMaster.getMasterBoard().getGoalAreaHeight());
        testPlayer.setPosition(testPosition);
        mapper.writeValue(out, testMessage);
        out.flush();

        CharBuffer cb = CharBuffer.allocate(1024);
        int ret = in.read(cb);
        cb.flip();

        Message response = mapper.readValue(cb.toString(), Message.class);
        Assert.assertEquals(3, response.getFields().size());
    }

    @Test
    public void serverShouldReturnFiveFields() throws IOException {
        testPosition.setY(gameMaster.getMasterBoard().getGoalAreaHeight());
        testPlayer.setPosition(testPosition);
        mapper.writeValue(out, testMessage);
        out.flush();

        CharBuffer cb = CharBuffer.allocate(1024);
        int ret = in.read(cb);
        cb.flip();

        Message response = mapper.readValue(cb.toString(), Message.class);
        Assert.assertEquals(5, response.getFields().size());
    }

    @Test
    public void serverShouldReturnErrorMessage() throws IOException {
        testPlayer.setPosition(null);
        mapper.writeValue(out, testMessage);
        out.flush();

        CharBuffer cb = CharBuffer.allocate(1024);
        int ret = in.read(cb);
        cb.flip();

        Message response = mapper.readValue(cb.toString(), Message.class);
        Assert.assertEquals(Message.Status.DENIED, response.getStatus());
    }
}
