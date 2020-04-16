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

    @Autowired
    public GameMaster(GameMasterConfiguration config, MasterBoard board) {
        lastTeamWasRed = false;
        configuration = config;
        masterBoard = board;
    }

    public void startGame()
    {
        System.out.println("The game has been started.");
    }

    public void loadConfiguration()
    {

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

    private void putNewPiece() throws DeniedMoveException
    {
        var target = new Position();

        Random random = new Random();
        var piece = new Piece();

        target.setY(random.nextInt() % masterBoard.getTaskAreaHeight() + masterBoard.getGoalAreaHeight());
        target.setX(random.nextInt(masterBoard.getWidth()));

        while(masterBoard.getCells().get(target).getContent().containsKey(Player.class)){
            target.setY(random.nextInt() % masterBoard.getTaskAreaHeight() + masterBoard.getGoalAreaHeight());
            target.setX(random.nextInt(masterBoard.getWidth()));
        }

        masterBoard.getCells().get(target).addContent(Piece.class, piece);

        System.out.println("New piece has been put.");
    }

    private void printBoard(Board board)
    {
        System.out.println("Board printed.");
    }

    public void messageHandler(String message)
    {
        System.out.println("Message handled.");
    }

    private Message actionConnect(Message message) {

        Message response = new Message();
        Team team = lastTeamWasRed ? blueTeam : redTeam;
        lastTeamWasRed = !lastTeamWasRed;
        var player = new Player(team, message.getPlayer().getPlayerName());

        try {
            team.addPlayer(player);
        } catch(Exception e) {
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

    private Message actionMove(Message message) {

        Message response = new Message();
        var player = message.getPlayer();
        var source = player.getPosition();
        var target = new Position();

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
        }

        try {
            masterBoard.movePlayer(player, source, target);
        } catch (Exception e) {
            System.out.println("target: " + target);
            System.out.println("source: " + source);
            System.out.println("player: " + player);
            logger.warn(e.toString());
            response = new Message();
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

    public synchronized Message processAndReturn(Message request) {

        Message response;

        try {
            Method method = this.getClass().getDeclaredMethod("action" + StringUtils.capitalize(request.getAction()), Message.class);
            response = (Message)method.invoke(this, request);

        } catch(NoSuchMethodException | InvocationTargetException | IllegalAccessException ex1) {
            logger.warn(ex1.toString());

            var msg = new Message();
            msg.setAction("error");
            return msg;
        }
        return response;
    }
}
