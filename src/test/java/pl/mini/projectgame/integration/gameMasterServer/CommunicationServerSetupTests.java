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
public class CommunicationServerSetupTests {

    @Autowired
    private GameMaster gameMaster;
    private Message testMessage;
    private Socket client;
    private BufferedWriter out;
    private BufferedReader in;
    private ObjectMapper mapper;

    private GameMasterConfiguration config;
    private MasterBoard masterBoard;

    @BeforeAll
    void saveConfig() {
        masterBoard = gameMaster.getMasterBoard();
        config = gameMaster.getConfiguration();
    }

    @AfterAll
    void cleanUp() {
        gameMaster.setMasterBoard(masterBoard);
        gameMaster.setConfiguration(config);
    }

    @BeforeEach
    public void setup() throws IOException {
        testMessage = new Message();
        testMessage.setAction("setup");
        Player player = new Player();
        gameMaster.getPlayerMap().put(player.getPlayerUuid(), player);
        testMessage.setPlayerUuid(player.getPlayerUuid());

        client = new Socket(InetAddress.getLocalHost().getHostName(), 8080);
        out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
        in = new BufferedReader(new InputStreamReader(client.getInputStream()));

        JsonFactory jsonFactory = new JsonFactory();
        jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        mapper = new ObjectMapper(jsonFactory);
    }

    @AfterEach
    public void tearDown() throws IOException {
        in.close();
        out.close();
        client.close();
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
    public void serverShouldReturnErrorMessage1() throws IOException {
        gameMaster.setConfiguration(null);
        mapper.writeValue(out, testMessage);
        out.flush();

        CharBuffer cb = CharBuffer.allocate(1024);
        int ret = in.read(cb);
        cb.flip();

        Message response = mapper.readValue(cb.toString(), Message.class);
        Assert.assertEquals("error", response.getAction());
    }
    @Test
    public void serverShouldReturnErrorMessage2() throws IOException {
        gameMaster.setMasterBoard(null);
        mapper.writeValue(out, testMessage);
        out.flush();

        CharBuffer cb = CharBuffer.allocate(1024);
        int ret = in.read(cb);
        cb.flip();

        Message response = mapper.readValue(cb.toString(), Message.class);
        Assert.assertEquals("error", response.getAction());
    }

}
