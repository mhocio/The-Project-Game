package pl.mini.projectgame.integration.gameMasterServer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pl.mini.projectgame.GameMaster;
import pl.mini.projectgame.exceptions.DeniedMoveException;
import pl.mini.projectgame.models.*;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CommunicationServerMoveTests {

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
        gameMaster.getMasterBoard().getCells().forEach((k,v) -> v.setContent(new HashMap<>()));
    }

    @BeforeEach
    void initTeam() throws IOException {
        client = new Socket(InetAddress.getLocalHost().getHostName(), 8080);
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
        message.setPosition(player.getPosition());
    }

    @AfterEach
    public void tearDown() throws IOException {
        in.close();
        out.close();
        client.close();
    }

    @Test
    void testMoveActionMessageUp() throws DeniedMoveException, IOException
    {
        message.setDirection(Message.Direction.UP);

        mapper.writeValue(out, message);
        out.flush();

        CharBuffer cb = CharBuffer.allocate(1024);
        int ret = in.read(cb);
        cb.flip();

        Message response = mapper.readValue(cb.toString(), Message.class);
        assertEquals(new Position(1, 2), response.getPosition());
    }

    @Test
    void testMoveActionMessageDown() throws DeniedMoveException, IOException
    {
        message.setDirection(Message.Direction.DOWN);

        mapper.writeValue(out, message);
        out.flush();

        CharBuffer cb = CharBuffer.allocate(1024);
        int ret = in.read(cb);
        cb.flip();

        Message response = mapper.readValue(cb.toString(), Message.class);
        assertEquals(new Position(1, 0), response.getPosition());
    }

    @Test
    void testMoveActionMessageLeft() throws DeniedMoveException, IOException
    {
        message.setDirection(Message.Direction.LEFT);

        mapper.writeValue(out, message);
        out.flush();

        CharBuffer cb = CharBuffer.allocate(1024);
        int ret = in.read(cb);
        cb.flip();

        Message response = mapper.readValue(cb.toString(), Message.class);
        assertEquals(new Position(0, 1), response.getPosition());
    }

    @Test
    void testMoveActionMessageRight() throws DeniedMoveException, IOException
    {
        message.setDirection(Message.Direction.RIGHT);

        mapper.writeValue(out, message);
        out.flush();

        CharBuffer cb = CharBuffer.allocate(1024);
        int ret = in.read(cb);
        cb.flip();

        Message response = mapper.readValue(cb.toString(), Message.class);
        assertEquals(new Position(2, 1), response.getPosition());
    }
}