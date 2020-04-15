package pl.mini.projectgame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.GsonBuilderUtils;
import org.springframework.stereotype.Component;
import pl.mini.projectgame.exceptions.DeniedMoveException;
import pl.mini.projectgame.models.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.Random;
import lombok.Setter;
import lombok.Getter;

@Getter
@Setter
@Component
public class GameMaster {

    private int portNumber;
    private InetAddress ipAddress;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public enum GameMasterStatus {
        ACTIVE, FINISHED, IDLE;
    }

    private GameMasterConfiguration configuration;
    private int blueTeamGoals;
    private int redTeamGoals;
    private Team redTeam;
    private Team blueTeam;
    private int currentPieces;
    private Board masterBoard;

    @Autowired
    public GameMaster(GameMasterConfiguration config) {
        configuration = config;
        loadConfiguration(configuration);
    }

    public void startGame()
    {
        System.out.println("The game has been started.");
    }

    public void loadConfiguration(GameMasterConfiguration configuration)
    {
        configuration = new GameMasterConfiguration();
        masterBoard = new Board(configuration.boardWidth, configuration.boardGoalHeight, configuration.boardTaskHeight);

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

    public synchronized Message processAndReturn(Message request) {

        Message response;

        try {
            Method method = this.getClass().getMethod("action" + request.getAction(), Message.class);
            response = (Message)method.invoke(this, request);

        } catch(NoSuchMethodException | InvocationTargetException | IllegalAccessException ex1) {
            logger.warn(ex1.getMessage());

            var msg = new Message();
            msg.setAction("error");
            return msg;
        }
        return response;
    }

    private Message actionConnect(Message message) {

        var player = new Player();

        Message response = new Message();
        response.setAction(message.getAction());

        return response;
    }

    private Message actionPlace(Message message) {
        if(message.getPlayer().placePiece())
            message.getPlayer().getTeam().addPoints(1);
        Message response = new Message();
        response.setAction(message.getAction());
        response.setStatus(Message.Status.OK);
        response.setPlayer(message.getPlayer());
        //TODO send the new score to all players message
        return response;
    }

    private Message actionReady(Message message) {
        //TODO edge case - disconnection before the start of the game
        message.getPlayer().setReady(true);
        Message response = new Message();
        response.setAction(message.getAction());
        response.setStatus(Message.Status.OK);
        response.setPlayer(message.getPlayer());
        return response;
    }
}
