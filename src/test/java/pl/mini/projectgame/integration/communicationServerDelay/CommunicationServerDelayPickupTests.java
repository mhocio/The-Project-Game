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
import pl.mini.projectgame.GameMasterConfiguration;
import pl.mini.projectgame.models.*;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.Map;

import static org.junit.Assert.assertTrue;

@SpringBootTest
@ComponentScan
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CommunicationServerDelayPickupTests {

    @Autowired
    private GameMaster gameMaster;

    @Autowired
    private MasterBoard masterBoard;

    private Message testMessage;

    private Socket client;
    private BufferedWriter out;
    private BufferedReader in;
    private ObjectMapper mapper;
    private MasterBoard testBoard;
    private GameMasterConfiguration config;

    private Map<Position, Cell> cells;

    private int originalDelay;
    private boolean result;
    double mean;
    int sum;
    int numOfRuns;

    @BeforeAll
    void saveConfig() throws IOException {
        cells = gameMaster.getMasterBoard().getCells();
        client = new Socket(InetAddress.getLocalHost().getHostName(), 8080);
        out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
        in = new BufferedReader(new InputStreamReader(client.getInputStream()));

        JsonFactory jsonFactory = new JsonFactory();
        jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        mapper = new ObjectMapper(jsonFactory);

        gameMaster.setMode(GameMaster.gmMode.GAME);
    }

    @AfterAll
    void cleanUp() throws IOException {
        gameMaster.setMasterBoard(masterBoard);
        gameMaster.getPlayerMap().clear();
        gameMaster.setMode(GameMaster.gmMode.NONE);
        in.close();
        out.close();
        client.close();
    }

    @BeforeEach
    public void setup() throws IOException {
        config = new GameMasterConfiguration();
        testBoard = new MasterBoard(config);
        originalDelay = gameMaster.getConfiguration().getDelayPick();
    }

    @AfterEach
    void after() {
        gameMaster.getPlayerMap().clear();
    }

    @Test
    public void serverShouldReturnOKMessageDelay() throws IOException {
        numOfRuns = 50;
        sum = 0;

        for (int i = 1; i <= numOfRuns; i++) {
            Position testPosition = new Position(1, 1);
            Player testPlayer = new Player();
            testBoard.getCellByPosition(testPosition).addContent(Piece.class, new Piece(0));
            gameMaster.setMasterBoard(testBoard);
            gameMaster.getPlayerMap().put(testPlayer.getPlayerUuid(), testPlayer);
            testMessage = new Message();
            testMessage.setPosition(testPosition);
            testMessage.setAction("pickUp");
            testMessage.setPlayerUuid(testPlayer.getPlayerUuid());
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

            Assert.assertEquals(Message.Status.OK, response.getStatus());

            gameMaster.getPlayerMap().clear();
        }

        result = false;
        mean = sum / numOfRuns;
        if (Math.abs(originalDelay - mean) < 10.0)
            result = true;

        System.out.println("mean: " + mean);
        assertTrue(result);

    }

    @Test
    public void serverShouldReturnErrorMessageDelay() throws IOException {
        numOfRuns = 50;
        sum = 0;

        for (int i = 1; i <= numOfRuns; i++) {
            GameMasterConfiguration config = new GameMasterConfiguration();
            Position testPosition = new Position(1, 1);
            Player testPlayer = new Player();

            testBoard = new MasterBoard(config);
            testBoard.getCellByPosition(testPosition).addContent(Piece.class, new Piece(0));
            gameMaster.setMasterBoard(testBoard);
            gameMaster.getPlayerMap().put(testPlayer.getPlayerUuid(), testPlayer);
            testMessage = new Message();
            testMessage.setPosition(testPosition);
            testMessage.setAction("pickUp");
            testMessage.setPlayerUuid(testPlayer.getPlayerUuid());
            testMessage.setPosition(new Position(3, 3));
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
        if (Math.abs(originalDelay - mean) < 10.0)
            result = true;

        System.out.println("mean: " + mean);
        assertTrue(result);
    }
}
