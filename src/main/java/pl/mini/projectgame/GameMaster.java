package pl.mini.projectgame;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.RequestHandledEvent;
import pl.mini.projectgame.exceptions.DeniedMoveException;
import pl.mini.projectgame.models.*;
import java.net.InetAddress;
import java.util.List;
import java.util.Random;

@Component
@Getter
@Setter
public class GameMaster {

    private int portNumber;
    private InetAddress ipAddress;

    public enum GameMasterStatus {
        active, finished, idle;
    }

    private GameMasterConfiguration configuration;
    private int blueTeamGoals;
    private int redTeamGoals;
    private Team redTeam;
    private Team blueTeam;
    private int currentPieces;
    private Board GMboard;

    public void startGame()
    {
        System.out.println("The game has been started.");
    }

    @EventListener
    private void listen(RequestHandledEvent e)
    {
        System.out.println("RequestHandledEvent");
        System.out.println(e);
    }

    public void loadConfiguration(GameMasterConfiguration configuration)
    {
        configuration = new GameMasterConfiguration();
        var board = new Board(configuration.boardWidth, configuration.boardGoalHeight, configuration.boardTaskHeight);

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

        target.setY(random.nextInt() % GMboard.getTaskAreaHeight() + GMboard.getGoalAreaHeight());
        target.setX(random.nextInt(GMboard.getWidth()));

        while(GMboard.getCells().get(target).getContent().getClass().equals(Player.class)){
            target.setY(random.nextInt() % GMboard.getTaskAreaHeight() + GMboard.getGoalAreaHeight());
            target.setX(random.nextInt(GMboard.getWidth()));
        }

        GMboard.updateCell(piece, target);

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

}