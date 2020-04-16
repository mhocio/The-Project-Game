package pl.mini.projectgame.server;

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
import pl.mini.projectgame.models.Message;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.CharBuffer;

@RunWith(SpringRunner.class)
@SpringBootTest
@ComponentScan
public class CommunicationServerTests {

    private Socket client;
    private BufferedWriter out;
    private BufferedReader in;
    private ObjectMapper mapper;

    @Autowired
    private CommunicationServer server;

    @Before
    public void setup() throws IOException {
        client = new Socket(InetAddress.getLocalHost().getHostName(), 8080);
        out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
        in = new BufferedReader(new InputStreamReader(client.getInputStream()));

        JsonFactory jsonFactory = new JsonFactory();
        jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        mapper = new ObjectMapper(jsonFactory);
    }

    @After
    public void tearDown() throws IOException {
        in.close();
        out.close();
        client.close();
    }

    @Test
    public void serverShouldReturnErrorMessage() throws IOException {

        Message test = new Message();
        test.setAction("thisShouldNotWork");

        mapper.writeValue(out, test);
        out.flush();

        CharBuffer cb = CharBuffer.allocate(1024);
        int ret = in.read(cb);
        cb.flip();

        Message response = mapper.readValue(cb.toString(), Message.class);
        Assert.assertEquals("error", response.getAction());
    }

    @Test
    public void serverShouldHaveAtLeastOneConnection() throws IOException {
        Assert.assertFalse(server.getConnections().isEmpty());
    }

    @Test
    public void serverShouldListenForConnections() {
        Assert.assertNotEquals(null, server.getListeningThread());
    }
}
