package pl.mini.projectgame.server;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.mini.projectgame.GameMaster;
import pl.mini.projectgame.models.Message;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.*;

/**
 * @author buensons
 */

@Service
public class CommunicationServer {

    @Getter
    private Thread listeningThread;
    @Getter
    private Set<Socket> connections;
    @Getter
    private Map<UUID, Socket> conn;
    private ServerSocket serverSocket;
    private ObjectMapper objectMapper;
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private GameMaster gameMaster;

    public CommunicationServer(@Autowired GameMaster gameMaster) throws IOException {
        JsonFactory jsonFactory = new JsonFactory();
        jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        objectMapper = new ObjectMapper(jsonFactory);
        serverSocket = new ServerSocket(8080);
        connections = new HashSet<>();
        conn = new HashMap<>();
        this.gameMaster = gameMaster;

        listeningThread = new Thread(this::listen);
        listeningThread.setName("Listening");
        listeningThread.start();
    }

    private void listen() {
        logger.info("Server is listening on port 8080...");
        while (true) {
            try {
                if (!serverSocket.isClosed()) {
                    Socket client = serverSocket.accept();
                    connections.add(client);
                    var thread = new Thread(() -> handle(client));
                    thread.setName("Handle for " + client.getInetAddress().toString());
                    thread.start();
                    logger.info("New player connected from " + client.getInetAddress().toString());
                }
            } catch (IOException e) {
                logger.warn(e.getMessage());
            }
        }
    }

    private void handle(Socket socket) {
        BufferedReader in;
        BufferedWriter out;
        Message message;

        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            logger.warn(e.getMessage());
            try {
                connections.remove(socket);
                socket.close();
            } catch (IOException ex) {
                logger.warn(ex.getMessage());
            }
            return;
        }

        while (true) {
            try {
                if (in.ready()) {
                    long startTime = System.nanoTime();

                    CharBuffer cb = CharBuffer.allocate(1024);
                    if (in.read(cb) < 0) {
                        logger.warn("Error while reading InputStream!");
                        continue;
                    }
                    cb.flip();

                    message = objectMapper.readValue(cb.toString(), Message.class);

                    if (message == null || message.getAction() == null) {
                        message = new Message();
                        message.setAction("error");
                        objectMapper.writeValue(out, message);
                        out.flush();
                        logger.warn("Could not process client's message!");
                        continue;
                    }

                    int responseTime = getResponseTime(message);
                    responseTime -= 5;

                    message = gameMaster.processAndReturn(message);

                    if (message.getAction().equals("connect")) {
                        conn.put(message.getPlayerUuid(), socket);
                    }

                    long endTime = System.nanoTime();
                    long remTime = (endTime - startTime) / 1000000;

                    if (responseTime > 0 && (remTime < responseTime))
                        try {
                            Thread.sleep(responseTime - remTime);
                        } catch(InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        }

                    objectMapper.writeValue(out, message);
                    out.flush();
                }

            } catch (IOException e) {
                logger.warn(e.getMessage());
                connections.remove(socket);
                Thread.currentThread().interrupt();
            }
        }
    }

    public void sendToEveryone(Message message) {
        // TODO: use conn?
        connections.forEach(socket -> {
            try {
                objectMapper.writeValue(socket.getOutputStream(), message);
            } catch (IOException e) {
                logger.warn(e.getMessage());
            }
        });
    }

    public void sendToSpecific(Message message) {
        try {
            var socket = conn.get(message.getPlayerUuid());
            objectMapper.writeValue(socket.getOutputStream(), message);
        } catch (IOException e) {
            logger.warn(e.getMessage());
        }
    }

    public void close() {
        connections.forEach(socket -> {
            try {
                socket.close();
            } catch (IOException e) {
                logger.warn(e.getMessage());
            }
        });
        listeningThread.interrupt();
        try {
            serverSocket.close();
        } catch (IOException e) {
            logger.warn(e.toString());
        }
    }

    private int getResponseTime(Message message) {
        String action;
        try {
            action = message.getAction();
        } catch (Exception e) {
            return 0;
        }

        if (action == null)
            return -1;

        switch (action) {
            case "place":
                return gameMaster.getConfiguration().getDelayPlace();
            case "pickUp":
                return gameMaster.getConfiguration().getDelayPick();
            case "test":
                return gameMaster.getConfiguration().getDelayTest();
            case "discover":
                return gameMaster.getConfiguration().getDelayDiscover();
            case "move":
                return gameMaster.getConfiguration().getDelayMove();
            case "destroy": // not used
                return gameMaster.getConfiguration().getDelayDestroyPiece();
            case "nextPiecePlace": // not used
                return gameMaster.getConfiguration().getDelayNextPiecePlace();
            default:
                return 0;
        }
    }
}
