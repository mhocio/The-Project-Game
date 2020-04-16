package pl.mini.projectgame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import pl.mini.projectgame.exceptions.DeniedMoveException;
import pl.mini.projectgame.models.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lombok.Setter;
import lombok.Getter;

@Component
@Getter
@Setter
public class GameMaster {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public enum GameMasterStatus {
        ACTIVE, FINISHED, IDLE;
    }

    private boolean lastTeamWasRed;
    private int portNumber;
    private InetAddress ipAddress;
    private int blueTeamGoals;
    private int redTeamGoals;
    private Team redTeam;
    private Team blueTeam;
    private int currentPieces;
    private MasterBoard masterBoard;
    private GameMasterConfiguration configuration;
    private List<Piece> pieces;

    @Autowired
    public GameMaster(GameMasterConfiguration config, MasterBoard board) {
        lastTeamWasRed = false;
        configuration = config;
        masterBoard = board;
        blueTeam = new Team();
        redTeam = new Team();
        pieces = new ArrayList<>();
    }

    public void startGame() {
        System.out.println("The game has been started.");
    }

    public void loadConfiguration() {

//        shamProbability = 0.5;
//        maxTeamSize = 4;

//        DelayDestroyPiece = 2950;
//        DelayNextPiecePlace = 3000;
//        DelayMove = 100;
//        DelayDiscover = 500;
//        DelayTest = 1000;
//        DelayPick = 100;
//        DelayPlace = 100;

        System.out.println("Configuration loaded.");
    }

//    public void saveConfiguration()
//    {
//        System.out.println("Configuration saved.");
//    }

    private void putNewPiece() throws DeniedMoveException {
        var target = new Position();

        Random random = new Random();
        var piece = new Piece();

        target.setY(random.nextInt() % masterBoard.getTaskAreaHeight() + masterBoard.getGoalAreaHeight());
        target.setX(random.nextInt(masterBoard.getWidth()));

        while (masterBoard.getCells().get(target).getContent().containsKey(Player.class)) {
            target.setY(random.nextInt() % masterBoard.getTaskAreaHeight() + masterBoard.getGoalAreaHeight());
            target.setX(random.nextInt(masterBoard.getWidth()));
        }

        masterBoard.getCells().get(target).addContent(Piece.class, piece);

        System.out.println("New piece has been put.");
    }

    private void printBoard(Board board) {
        System.out.println("Board printed.");
    }

    public void messageHandler(String message) {
        System.out.println("Message handled.");
    }

    /**
     * @author buensons
     * @param request - message to be passed by CommunicationServer
     * @return Message - response message to be returned to the CommunicationServer
     */
    public synchronized Message processAndReturn(Message request) {

        Message response;

        try {
            Method method = this.getClass().getDeclaredMethod(
                    "action" + StringUtils.capitalize(request.getAction()),
                    Message.class);

            response = (Message) method.invoke(this, request);

        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex1) {
            logger.warn(ex1.getMessage());

            var msg = new Message();
            msg.setAction("error");
            return msg;
        }
        return response;
    }

    /**
     * @author buensons
     * @param message - message to be passed by processAndReturn method
     * @return Message - response message to be returned to processAndReturn method
     */
    private Message actionConnect(Message message) {

        Message response = new Message();
        Team team = lastTeamWasRed ? blueTeam : redTeam;
        lastTeamWasRed = !lastTeamWasRed;
        Player player;

        try {
            player = new Player(team, message.getPlayer().getPlayerName());
            team.addPlayer(player);
        } catch (Exception e) {
            logger.warn(e.getMessage());

            response = new Message();
            response.setAction("error");
            return response;
        }

        response.setAction(message.getAction());
        response.setPlayer(player);
        response.setStatus(Message.Status.OK);

        return response;
    }

    /**
     * @author buensons
     * @param message - message to be passed by processAndReturn method
     * @return Message - response message to be returned to processAndReturn method
     */
    private Message actionDiscover(Message message) {
        Message response = new Message();
        List<Field> fields = new ArrayList<>();

        try {
            Position playerPosition = message.getPosition();

            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    var position = new Position(
                            playerPosition.getX() + x,
                            playerPosition.getY() + y);

                    if (position.equals(playerPosition)) continue;

                    if(position.getX() >= masterBoard.getWidth()
                            || position.getX() < 0
                            || position.getY() < masterBoard.getGoalAreaHeight()
                            || position.getY() >= masterBoard.getGoalAreaHeight() + masterBoard.getTaskAreaHeight()) {

                        continue;
                    }

                    var currentCell = masterBoard.getCellByPosition(position);
                    int minDistance = Integer.MAX_VALUE;

                    for (Piece piece : pieces) {
                        int distance = currentCell.calculateDistance(piece.getPosition());
                        if (distance < minDistance) minDistance = distance;
                    }

                    // if there is no pieces on the board
                    minDistance = minDistance == Integer.MAX_VALUE ? -1 : minDistance;

                    currentCell.setDistance(minDistance);
                    fields.add(new Field(currentCell));
                }
            }
        } catch(Exception e) {
            logger.warn(e.getMessage());
            response.setAction("error");
            return response;
        }

        response.setAction(message.getAction());
        response.setPosition(message.getPosition());
        response.setFields(fields);

        return response;
    }

    private Message actionMove(Message message) {

        Message response = new Message();
        Player player;
        Position target;

        try {
            player = message.getPlayer();
            var source = player.getPosition();
            target = new Position();

            switch (message.getDirection()) {
                case UP:
                    target.setX(source.getX());
                    target.setY(source.getY() + 1);
                    break;
                case DOWN:
                    target.setX(source.getX());
                    target.setY(source.getY() - 1);
                    break;
                case LEFT:
                    target.setX(source.getX() - 1);
                    target.setY(source.getY());
                    break;
                case RIGHT:
                    target.setX(source.getX() + 1);
                    target.setY(source.getY());
                    break;
                default:
            }
            masterBoard.movePlayer(player, source, target);

        } catch (Exception e) {
            logger.warn(e.getMessage());

            response = new Message();
            response.setAction("error");
            response.setStatus(Message.Status.DENIED);
            response.setPosition(null);
            return response;
        }

        response.setAction(message.getAction());
        response.setPlayer(player);
        response.setPosition(target);
        response.setStatus(Message.Status.OK);

        return response;
    }
}