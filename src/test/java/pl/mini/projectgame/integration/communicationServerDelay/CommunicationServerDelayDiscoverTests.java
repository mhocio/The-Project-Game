package pl.mini.projectgame.integration.communicationServerDelay;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.jupiter.api.*;
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

import static org.junit.Assert.assertTrue;

@SpringBootTest
@ComponentScan
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CommunicationServerDelayDiscoverTests {

    @Autowired
    private GameMaster gameMaster;
    private Message testMessage;

    private Socket client;
    private BufferedWriter out;
    private BufferedReader in;
    private ObjectMapper mapper;

    private int originalDelay;
    private boolean result;
    double mean;
    int sum;
    int numOfRuns;

    @BeforeAll
    void beforeAll() {
        gameMaster.setMode(GameMaster.gmMode.GAME);
        originalDelay = gameMaster.getConfiguration().getDelayDiscover();
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
    public void serverShouldReturnThreeFieldsDelay() throws IOException {
        numOfRuns = 50;
        sum = 0;

        for (int i = 1; i <= numOfRuns; i++) {
            testMessage.getPosition().setX(0);
            testMessage.getPosition().setY(gameMaster.getMasterBoard().getGoalAreaHeight());
            mapper.writeValue(out, testMessage);

            long startTime = System.nanoTime();

            out.flush();
            CharBuffer cb = CharBuffer.allocate(1024);
            int ret = in.read(cb);
            cb.flip();
            Message response = mapper.readValue(cb.toString(), Message.class);

            long endTime = System.nanoTime();
            long diffInMiliseconds = (endTime - startTime) / 1000000;
            sum += diffInMiliseconds;

            Assert.assertEquals(3, response.getFields().size());
        }

        result = false;
        mean = sum / numOfRuns;
        if (Math.abs(originalDelay - mean) < 12.0)
            result = true;

        System.out.println("mean: " + mean);
        assertTrue(result);
    }

    @Test
    public void serverShouldReturnErrorMessageDelay() throws IOException {
        numOfRuns = 10;
        sum = 0;

        for (int i = 1; i <= numOfRuns; i++) {
            testMessage.setPosition(null);
            mapper.writeValue(out, testMessage);

            long startTime = System.nanoTime();

            out.flush();
            CharBuffer cb = CharBuffer.allocate(1024);
            int ret = in.read(cb);
            cb.flip();
            Message response = mapper.readValue(cb.toString(), Message.class);

            long endTime = System.nanoTime();
            long diffInMiliseconds = (endTime - startTime) / 1000000;
            sum += diffInMiliseconds;

            Assert.assertEquals("error", response.getAction());
        }

        result = false;
        mean = sum / numOfRuns;
        if (Math.abs(originalDelay - mean) < 12.0)
            result = true;

        System.out.println("mean: " + mean);
        assertTrue(result);
    }
}
