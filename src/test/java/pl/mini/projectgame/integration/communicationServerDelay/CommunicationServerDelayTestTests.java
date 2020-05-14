package pl.mini.projectgame.integration.communicationServerDelay;

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

import static org.junit.Assert.*;


@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CommunicationServerDelayTestTests {

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

    private int originalDelay;
    private boolean result;
    double mean;
    int sum;
    int numOfRuns;

    @BeforeAll
    void saveConfig() {
        cells = gameMaster.getMasterBoard().getCells();
        gameMaster.setMode(GameMaster.gmMode.GAME);
        originalDelay = gameMaster.getConfiguration().getDelayTest();
    }

    @AfterAll
    void cleanUp() {
        gameMaster.getMasterBoard().setCells(cells);
        gameMaster.setMode(GameMaster.gmMode.NONE);
    }

    @BeforeEach
    void initTestPiece() throws IOException {
        client = new Socket(InetAddress.getLocalHost().getHostName(), 8080);
        out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
        in = new BufferedReader(new InputStreamReader(client.getInputStream()));

        JsonFactory jsonFactory = new JsonFactory();
        jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        mapper = new ObjectMapper(jsonFactory);

        piece = new Piece(0.5);
        position = new Position(1, 1);
        player = new Player();

        gameMaster.getPlayerMap().put(player.getPlayerUuid(), player);
        gameMaster.getMasterBoard().getCellByPosition(position).getContent().clear();
        gameMaster.getMasterBoard().addBoardObject(player, position);
        gameMaster.getMasterBoard().addBoardObject(piece, position);
    }

    @AfterEach
    public void tearDown() throws IOException {
        in.close();
        out.close();
        client.close();
    }

    @Test
    void testActionDelay() throws IOException {
        numOfRuns = 50;
        sum = 0;

        for (int i = 1; i <= numOfRuns; i++) {
            message = new Message();
            message.setPosition(position);
            message.setPlayer(player);
            message.setAction("test");
            message.setPlayerUuid(player.getPlayerUuid());

            mapper.writeValue(out, message);

            long startTime = System.nanoTime();

            out.flush();
            CharBuffer cb = CharBuffer.allocate(1024);
            int ret = in.read(cb);
            cb.flip();
            Message response = mapper.readValue(cb.toString(), Message.class);

            long endTime = System.nanoTime();
            long diffInMiliseconds = (endTime - startTime) / 1000000;
            sum += diffInMiliseconds;
            assertNotEquals("error", response.getAction());
        }

        result = false;
        mean = sum / numOfRuns;
        if (Math.abs(originalDelay - mean) < 10.0)
            result = true;

        System.out.println("mean: " + mean);
        assertTrue(result);
    }

    @Test
    void testActionNullPlayerDelay() throws IOException {
        numOfRuns = 50;
        sum = 0;

        for (int i = 1; i <= numOfRuns; i++) {
            message = new Message();
            message.setPosition(position);
            message.setPlayer(player);
            message.setAction("test");
            message.setPlayerUuid(null);
            mapper.writeValue(out, message);

            long startTime = System.nanoTime();

            out.flush();
            CharBuffer cb = CharBuffer.allocate(1024);
            int ret = in.read(cb);
            cb.flip();
            Message response = mapper.readValue(cb.toString(), Message.class);

            long endTime = System.nanoTime();
            long diffInMiliseconds = (endTime - startTime) / 1000000;
            sum += diffInMiliseconds;

            assertEquals("error", response.getAction());
        }

        result = false;
        mean = sum / numOfRuns;
        if (Math.abs(originalDelay - mean) < 10.0)
            result = true;

        System.out.println("mean: " + mean);
        assertTrue(result);
    }

    @Test
    void testActionNoPiece() throws IOException {
        numOfRuns = 50;
        sum = 0;

        for (int i = 1; i <= numOfRuns; i++) {
            message = new Message();
            message.setPosition(position);
            message.setPlayer(player);
            message.setAction("test");
            message.setPlayerUuid(player.getPlayerUuid());
            gameMaster.getMasterBoard().getCellByPosition(position).removeContent(Piece.class);

            mapper.writeValue(out, message);

            long startTime = System.nanoTime();

            out.flush();
            CharBuffer cb = CharBuffer.allocate(1024);
            int ret = in.read(cb);
            cb.flip();
            Message response = mapper.readValue(cb.toString(), Message.class);

            long endTime = System.nanoTime();
            long diffInMiliseconds = (endTime - startTime) / 1000000;
            sum += diffInMiliseconds;

            assertEquals(Message.Status.DENIED, response.getStatus());
        }

        result = false;
        mean = sum / numOfRuns;
        if (Math.abs(originalDelay - mean) < 10.0)
            result = true;

        System.out.println("mean: " + mean);
        assertTrue(result);
    }
}
