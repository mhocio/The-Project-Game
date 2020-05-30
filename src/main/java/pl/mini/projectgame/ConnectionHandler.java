package pl.mini.projectgame;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.mini.projectgame.models.Message;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.*;

/**
 * @author buensons
 */

@Component
public class ConnectionHandler {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ServerSocket serverSocket;
    @Getter
    private final Thread listeningThread;
    private final List<Thread> threads;
    @Getter
    private final Set<Socket> connections;
    @Getter
    private final Map<UUID, Socket> conn;
    private final GameMaster gameMaster;
    private final ObjectMapper objectMapper;

    @Autowired
    public ConnectionHandler(GameMaster gameMaster) throws IOException {
        JsonFactory jsonFactory = new JsonFactory();
        jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        objectMapper = new ObjectMapper(jsonFactory);

        this.gameMaster = gameMaster;
        serverSocket = new ServerSocket(8000);
        threads = new ArrayList<>();
        connections = new HashSet<>();
        conn = new HashMap<>();

        listeningThread = new Thread(this::listen);
        listeningThread.setName("Listening");
        listeningThread.start();
    }

    private void listen() {
        logger.info("Connection handler is running on port 8000...");
        while (true) {
            try {
                if (!serverSocket.isClosed()) {
                    Socket client = serverSocket.accept();
                    connections.add(client);
                    var thread = new Thread(() -> handle(client));
                    thread.setName("Handle for " + client.getInetAddress().toString());
                    thread.start();
                    threads.add(thread);
                    logger.info("New player connected from " + client.getInetAddress().toString());
                }
            } catch (IOException e) {
                logger.warn(e.getMessage());
            }
        }
    }

    private void handle(Socket client) {
        BufferedReader reader;
        BufferedWriter writer;

        try {
            reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));

        } catch (IOException e) {
            logger.warn(e.getMessage());
            try {
                client.close();
            } catch (IOException ex) {
                logger.warn(ex.getMessage());
            }
            return;
        }

        while (true) {
            try {
                if (reader.ready()) {
                    long startTime = System.nanoTime();

                    CharBuffer cb = CharBuffer.allocate(1024);
                    if (reader.read(cb) < 0) {
                        logger.warn("Error while reading InputStream!");
                        return;
                    }
                    cb.flip();

                    var message = objectMapper.readValue(cb.toString(), Message.class);

                    if (message == null || message.getAction() == null) {
                        message = new Message();
                        message.setAction("error");
                        objectMapper.writeValue(writer, message);
                        writer.flush();
                        logger.warn("Could not process client's message!");
                        continue;
                    }

                    int responseTime = getResponseTime(message);
                    responseTime -= 5;

                    message = gameMaster.processAndReturn(message);

                    if (message.getAction().equals("connect")) {
                        conn.put(message.getPlayerUuid(), client);
                    }

                    long endTime = System.nanoTime();
                    long remTime = (endTime - startTime) / 1000000;

                    if (responseTime > 0 && (remTime < responseTime)) {
                        try {
                            Thread.sleep(responseTime - remTime);
                        } catch(InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    objectMapper.writeValue(writer, message);
                    writer.flush();
                }
            } catch (IOException e) {
                logger.warn(e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
    }

    public void sendToEveryone(String message) {
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
                logger.info("Client socket closed!");
            } catch (IOException e) {
                logger.warn(e.getMessage());
            }
        });

        threads.forEach(Thread::interrupt);

        listeningThread.interrupt();
        try {
            serverSocket.close();
            logger.info("Server Socket Closed!");
        } catch (IOException e) {
            logger.warn(e.toString());
        }
        System.exit(0);
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
            case "pick":
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
