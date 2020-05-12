package pl.mini.projectgame.integration.gameMasterServer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import pl.mini.projectgame.GameMaster;
import pl.mini.projectgame.models.Message;
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
        testMessage.setPosition(new Position(
                gameMaster.getMasterBoard().getWidth() / 2,
                gameMaster.getMasterBoard().getGoalAreaHeight() + 4));
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
        testMessage.getPosition().setX(0);
        testMessage.getPosition().setY(gameMaster.getMasterBoard().getGoalAreaHeight());
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
        testMessage.getPosition().setY(gameMaster.getMasterBoard().getGoalAreaHeight());
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
        testMessage.setPosition(null);
        mapper.writeValue(out, testMessage);
        out.flush();

        CharBuffer cb = CharBuffer.allocate(1024);
        int ret = in.read(cb);
        cb.flip();

        Message response = mapper.readValue(cb.toString(), Message.class);
        Assert.assertEquals("error", response.getAction());
    }
}
