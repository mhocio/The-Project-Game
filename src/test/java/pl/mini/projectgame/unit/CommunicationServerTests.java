package pl.mini.projectgame.unit;

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
import pl.mini.projectgame.models.Message;
import pl.mini.projectgame.server.CommunicationServer;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.CharBuffer;

@SpringBootTest
@ComponentScan
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CommunicationServerTests {

    private Socket client;
    private BufferedWriter out;
    private BufferedReader in;
    private ObjectMapper mapper;

    @Autowired
    private CommunicationServer server;

    @BeforeEach
    public void setup() throws IOException {
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
