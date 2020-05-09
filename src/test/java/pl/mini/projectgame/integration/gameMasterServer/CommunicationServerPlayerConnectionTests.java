package pl.mini.projectgame.integration.gameMasterServer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;
import pl.mini.projectgame.GameMaster;
import pl.mini.projectgame.models.Message;
import pl.mini.projectgame.models.Player;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.CharBuffer;

@RunWith(SpringRunner.class)
@SpringBootTest
@ComponentScan
public class CommunicationServerPlayerConnectionTests {

    private Socket client;
    private BufferedWriter out;
    private BufferedReader in;
    private ObjectMapper mapper;
    private Message testMessage;
    private Player player;

    @Autowired
    private GameMaster gameMaster;

    @Before
    public void setup() throws IOException {
        client = new Socket(InetAddress.getLocalHost().getHostName(), 8080);
        out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
        in = new BufferedReader(new InputStreamReader(client.getInputStream()));

        JsonFactory jsonFactory = new JsonFactory();
        jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        mapper = new ObjectMapper(jsonFactory);

        testMessage = new Message();
        player = new Player();
        player.setPlayerName("Test");

        testMessage.setAction("connect");
        testMessage.setPlayer(player);
    }

    @After
    public void tearDown() throws IOException {
        in.close();
        out.close();
        client.close();
    }

    @Test
    public void serverShouldReturnOkStatus() throws IOException {
        mapper.writeValue(out, testMessage);
        out.flush();

        CharBuffer cb = CharBuffer.allocate(1024);
        int ret = in.read(cb);
        cb.flip();

        Message response = mapper.readValue(cb.toString(), Message.class);
        Assert.assertEquals(Message.Status.OK, response.getStatus());
    }

    @Test
    public void serverShouldAddPlayerToTheTeam() throws IOException {

        mapper.writeValue(out, testMessage);
        out.flush();

        CharBuffer cb = CharBuffer.allocate(1024);
        int ret = in.read(cb);
        cb.flip();

        Message response = mapper.readValue(cb.toString(), Message.class);

        System.out.println(response.getPlayer().toString());
        gameMaster.getRedTeam().getPlayers().keySet().forEach(e -> System.out.println(e.toString()));

        if(gameMaster.isLastTeamWasRed()) {
            Assert.assertTrue(gameMaster.getRedTeam().getPlayers().containsKey(response.getPlayer()));
        } else {
            Assert.assertTrue(gameMaster.getBlueTeam().getPlayers().containsKey(response.getPlayer()));
        }
    }

    @Test
    public void serverShouldReturnTheSamePlayer() throws IOException {
        mapper.writeValue(out, testMessage);
        out.flush();

        CharBuffer cb = CharBuffer.allocate(1024);
        int ret = in.read(cb);
        cb.flip();

        Message response = mapper.readValue(cb.toString(), Message.class);
        Assert.assertEquals(player.getPlayerName(), response.getPlayer().getPlayerName());
    }

    @Test
    public void serverShouldReturnErrorMessage() throws IOException {
        testMessage.setPlayer(null);
        mapper.writeValue(out, testMessage);
        out.flush();

        CharBuffer cb = CharBuffer.allocate(1024);
        int ret = in.read(cb);
        cb.flip();

        Message response = mapper.readValue(cb.toString(), Message.class);
        Assert.assertEquals("error", response.getAction());
    }
}
