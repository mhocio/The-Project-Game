package projectgame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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

    private final int GAME_MASTER_PORT = 8000;
    private final String GAME_MASTER_IP = "127.0.0.1";
    private final int MAX_BUFFER_SIZE = 5012;

    private final int COMMUNICATION_SERVER_PORT = 8080;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ServerSocket serverSocket;

    private final Set<Socket> connections;
    private final Thread listeningThread;
    private final List<Thread> threads;

    public CommunicationServer() throws IOException {

        Runtime.getRuntime().addShutdownHook(new Thread(this::close));

        serverSocket = new ServerSocket(COMMUNICATION_SERVER_PORT);
        threads = new ArrayList<>();
        connections = new HashSet<>();

        listeningThread = new Thread(this::listen);
        listeningThread.setName("Listening");
        listeningThread.start();
    }

    private void listen() {
        logger.info("Server is listening on port " + COMMUNICATION_SERVER_PORT + "...");
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
        BufferedReader clientInput;
        BufferedWriter masterOutput;
        Socket master;

        try {
            master = new Socket(GAME_MASTER_IP, GAME_MASTER_PORT);
            clientInput = new BufferedReader(new InputStreamReader(client.getInputStream()));
            masterOutput = new BufferedWriter(new OutputStreamWriter(master.getOutputStream()));

            startReadingThread(master, client);

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
                if (clientInput.ready()) readAndSend(clientInput, masterOutput);
            } catch (IOException e) {
                logger.warn(e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
    }

    private void startReadingThread(Socket master, Socket client) {
        var thread = new Thread(() -> fromMasterToClient(master, client));
        threads.add(thread);
        thread.start();
    }

    private void fromMasterToClient(Socket master, Socket client) {
        BufferedReader reader;
        BufferedWriter writer;
        try {
            reader = new BufferedReader(new InputStreamReader(master.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
        } catch(IOException e) {
            logger.warn(e.toString());
            Thread.currentThread().interrupt();
            return;
        }

        while(true) {
            try {
                if (reader.ready()) readAndSend(reader, writer);
            } catch (IOException e) {
                logger.warn(e.toString());
            }
        }
    }

    private void readAndSend(BufferedReader in, BufferedWriter out) throws IOException {
        CharBuffer cb = CharBuffer.allocate(MAX_BUFFER_SIZE);
        if (in.read(cb) < 0) {
            logger.warn("Error while reading InputStream!");
            return;
        }
        cb.flip();

        var message = cb.toString();
        out.write(message);
        out.flush();
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
    }
}
