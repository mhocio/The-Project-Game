package pl.mini.projectgame.integration.communicationServerDelay;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pl.mini.projectgame.GameMaster;
import pl.mini.projectgame.exceptions.DeniedMoveException;
import pl.mini.projectgame.models.Message;
import pl.mini.projectgame.models.Player;
import pl.mini.projectgame.models.Position;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CommunicationServerDelayMoveTests {

    @Autowired
    private GameMaster gameMaster;
    private Socket client;
    private BufferedWriter out;
    private BufferedReader in;
    private ObjectMapper mapper;
    private Message message;
    private Player player;

    private int originalDelay;
    private boolean result;
    double mean;
    int sum;
    int numOfRuns;

    @BeforeAll
    void beforeAll() {
        gameMaster.setMode(GameMaster.gmMode.GAME);
        originalDelay = gameMaster.getConfiguration().getDelayMove();
    }

    @AfterAll
    void cleanUp() {
        gameMaster.getMasterBoard().getCells().forEach((k, v) -> v.setContent(new HashMap<>()));
        gameMaster.setMode(GameMaster.gmMode.NONE);
    }

    @BeforeEach
    void initTeam() throws IOException {
        client = new Socket(InetAddress.getLocalHost().getHostName(), 8080);
        out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
        in = new BufferedReader(new InputStreamReader(client.getInputStream()));

        JsonFactory jsonFactory = new JsonFactory();
        jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        mapper = new ObjectMapper(jsonFactory);

        player = new Player();
        player.setPosition(new Position(5, 21));
        gameMaster.getPlayerMap().put(player.getPlayerUuid(), player);
        gameMaster.getMasterBoard().addBoardObject(player, player.getPosition());
        gameMaster.setMode(GameMaster.gmMode.GAME);
    }

    @AfterEach
    public void tearDown() throws IOException {
        in.close();
        out.close();
        client.close();
    }

    @Test
    void testDelayMoveActionMessageUp() throws DeniedMoveException, IOException {
        sum = 0;
        numOfRuns = 50;

        for (int i = 1; i <= numOfRuns; i++) {
            message = new Message();
            message.setAction("move");
            message.setDirection(Message.Direction.UP);
            message.setPlayerUuid(player.getPlayerUuid());
            message.setPosition(player.getPosition());

            mapper.writeValue(out, message);

            long startTime = System.nanoTime();

            out.flush();
            CharBuffer cb = CharBuffer.allocate(1024);
            int ret = in.read(cb);
            cb.flip();
            Message response = mapper.readValue(cb.toString(), Message.class);

            long endTime = System.nanoTime();

            if (response.getPosition() != null)
                player.setPosition(response.getPosition());
            else
                player.setPosition(player.getPosition());

            System.out.println("new position: " + player.getPosition());

            long diffInMiliseconds = (endTime - startTime) / 1000000;
            sum += diffInMiliseconds;

            if (response.getPosition() != null)
                assertEquals(new Position(5, 21 + i), response.getPosition());
            else
                assertEquals(response.getStatus(), Message.Status.DENIED);

            assertEquals("move", response.getAction());
        }

        result = false;
        mean = sum / numOfRuns;
        if (Math.abs(originalDelay - mean) < 7.0)
            result = true;

        System.out.println("mean: " + mean);
        assertTrue(result);
    }

    @Test
    void testDelayMoveActionMessageDown() throws DeniedMoveException, IOException {
        numOfRuns = 50;
        sum = 0;

        for (int i = 1; i <= numOfRuns; i++) {
            message = new Message();
            message.setAction("move");
            message.setDirection(Message.Direction.DOWN);
            message.setPlayerUuid(player.getPlayerUuid());
            message.setPosition(player.getPosition());

            mapper.writeValue(out, message);

            long startTime = System.nanoTime();

            out.flush();
            CharBuffer cb = CharBuffer.allocate(1024);
            int ret = in.read(cb);
            cb.flip();
            Message response = mapper.readValue(cb.toString(), Message.class);

            long endTime = System.nanoTime();

            if (response.getPosition() != null)
                player.setPosition(response.getPosition());
            else
                player.setPosition(player.getPosition());

            System.out.println("new position: " + player.getPosition());

            long diffInMiliseconds = (endTime - startTime) / 1000000;
            sum += diffInMiliseconds;

            if (response.getPosition() != null)
                assertEquals(new Position(5, 21 - i), response.getPosition());
            else
                assertEquals(response.getStatus(), Message.Status.DENIED);

            assertEquals("move", response.getAction());
        }

        result = false;
        mean = sum / numOfRuns;
        if (Math.abs(originalDelay - mean) < 7.0)
            result = true;

        System.out.println("mean: " + mean);
        assertTrue(result);
    }

    @Test
    void testDelayMoveActionMessageLeft() throws DeniedMoveException, IOException {
        int numOfRuns = 50;
        long sum = 0;

        for (int i = 1; i <= numOfRuns; i++) {
            message = new Message();
            message.setAction("move");
            message.setDirection(Message.Direction.LEFT);
            message.setPlayerUuid(player.getPlayerUuid());
            message.setPosition(player.getPosition());

            mapper.writeValue(out, message);
            long startTime = System.nanoTime();
            out.flush();
            CharBuffer cb = CharBuffer.allocate(1024);
            int ret = in.read(cb);
            cb.flip();
            Message response = mapper.readValue(cb.toString(), Message.class);
            long endTime = System.nanoTime();

            if (response.getPosition() != null)
                player.setPosition(response.getPosition());
            else
                player.setPosition(player.getPosition());

            System.out.println("new position: " + player.getPosition());

            long diffInMiliseconds = (endTime - startTime) / 1000000;
            sum += diffInMiliseconds;

            if (response.getPosition() != null)
                assertEquals(new Position(5 - i, 21), response.getPosition());
            else
                assertEquals(response.getStatus(), Message.Status.DENIED);

            assertEquals("move", response.getAction());
        }

        result = false;
        mean = sum / numOfRuns;
        if (Math.abs(originalDelay - mean) < 7.0)
            result = true;

        System.out.println("mean: " + mean);
        assertTrue(result);
    }

    @Test
    void testDelayMoveActionMessageRight() throws DeniedMoveException, IOException {
        numOfRuns = 50;
        sum = 0;

        for (int i = 1; i <= numOfRuns; i++) {
            message = new Message();
            message.setAction("move");
            message.setDirection(Message.Direction.RIGHT);
            message.setPlayerUuid(player.getPlayerUuid());
            message.setPosition(player.getPosition());

            mapper.writeValue(out, message);
            long startTime = System.nanoTime();
            out.flush();
            CharBuffer cb = CharBuffer.allocate(1024);
            int ret = in.read(cb);
            cb.flip();
            Message response = mapper.readValue(cb.toString(), Message.class);
            long endTime = System.nanoTime();

            if (response.getPosition() != null)
                player.setPosition(response.getPosition());
            else
                player.setPosition(player.getPosition());

            System.out.println("new position: " + player.getPosition());

            long diffInMiliseconds = (endTime - startTime) / 1000000;
            sum += diffInMiliseconds;

            if (response.getPosition() != null)
                assertEquals(new Position(5 + i, 21), response.getPosition());
            else
                assertEquals(response.getStatus(), Message.Status.DENIED);

            assertEquals("move", response.getAction());
        }

        result = false;
        mean = sum / numOfRuns;
        if (Math.abs(originalDelay - mean) < 7.0)
            result = true;

        System.out.println("mean: " + mean);
        assertTrue(result);
    }
}
