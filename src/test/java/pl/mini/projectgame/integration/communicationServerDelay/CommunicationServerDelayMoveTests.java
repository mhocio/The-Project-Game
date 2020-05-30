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

    @AfterAll
    void cleanUp() {
        gameMaster.getMasterBoard().getCells().forEach((k, v) -> v.setContent(new HashMap<>()));
    }

    @BeforeEach
    void initTeam() throws IOException {
        client = new Socket(InetAddress.getLocalHost().getHostName(), 8000);
        out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
        in = new BufferedReader(new InputStreamReader(client.getInputStream()));

        JsonFactory jsonFactory = new JsonFactory();
        jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        mapper = new ObjectMapper(jsonFactory);

        message = new Message();
        player = new Player();
        message.setAction("move");
        message.setPlayerUuid(player.getPlayerUuid());
        gameMaster.getPlayerMap().put(player.getPlayerUuid(), player);

        player.setPosition(new Position(1, 1));
        gameMaster.getMasterBoard().addBoardObject(player, player.getPosition());
        gameMaster.setMode(GameMaster.gmMode.GAME);
        message.setPosition(player.getPosition());
    }

    @AfterEach
    public void tearDown() throws IOException {
        in.close();
        out.close();
        client.close();
    }

    @Test
    void testMoveActionMessageUp() throws DeniedMoveException, IOException {

        // TODO: board generated correctly?

        System.out.println("config delay: " + gameMaster.getConfiguration().getDelayMove());
        int originalDelay = gameMaster.getConfiguration().getDelayMove();
        int numOfRuns = 50;
        long sum = 0;

        for (int i = 0; i < numOfRuns; i++) {
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
                assertEquals(new Position(1, 2 + i), response.getPosition());
            else
                assertEquals(response.getStatus(), Message.Status.DENIED);

            assertEquals("move", response.getAction());
        }

        boolean result = false;
        double mean = sum / numOfRuns;
        if (Math.abs(originalDelay - mean) < 7.0)
            result = true;

        System.out.println("mean: " + mean);
        assertTrue(result);
    }
}
