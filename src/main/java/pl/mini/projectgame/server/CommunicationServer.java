package pl.mini.projectgame.server;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pl.mini.projectgame.models.Message;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.HashSet;
import java.util.Set;

/**
 * @author buensons
 */

@Service
public class CommunicationServer {

    private ServerSocket serverSocket;
    private ObjectMapper objectMapper;
    private Set<Socket> connections;
    private Thread listeningThread;
    private Logger logger = LoggerFactory.getLogger(this.getClass());
//    private GameMaster gameMaster;

    public CommunicationServer(/*@Autowired GameMaster master*/) throws IOException {
//        gameMaster = master;

        JsonFactory jsonFactory = new JsonFactory();
        jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        objectMapper = new ObjectMapper(jsonFactory);
        serverSocket = new ServerSocket(8080);
        connections = new HashSet<>();
        listeningThread = new Thread(this::listen);
        listeningThread.start();
    }

    private void listen() {

        while(true) {
            try {
                if(!serverSocket.isClosed()) {
                    Socket client = serverSocket.accept();
                    connections.add(client);
                    new Thread(() -> handle(client)).start();
                }
            } catch (IOException e) {
                logger.warn(e.getMessage());
            }
        }
    }

    private void handle(Socket socket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            Message message;

            while(true) {
                if(in.ready()) {
                    CharBuffer cb = CharBuffer.allocate(1024);
                    if(in.read(cb) < 0) {
                        logger.warn("Error while reading InputStream!");
                        continue;
                    }
                    cb.flip();

                    message = objectMapper.readValue(cb.toString(), Message.class);

                    if (message == null) {
                        logger.warn("Could not process client's message!");
                        continue;
                    }
//                message = gameMaster.processAndReturn(message);
                    objectMapper.writeValue(out, message);
                    out.flush();
                }
            }

        } catch(IOException e) {
            logger.warn(e.getMessage());
        }
    }

    public void sendToEveryone(Message message) {
        connections.forEach(socket -> {
            try {
                objectMapper.writeValue(socket.getOutputStream(), message);
            } catch (IOException e) {
                logger.warn(e.getMessage());
            }
        });

    }

    public void close() throws IOException {
        connections.forEach(socket -> {
            try {
                socket.close();
            } catch (IOException e) {
                logger.warn(e.getMessage());
            }
        });

        listeningThread.interrupt();
        serverSocket.close();
    }
}
