package pl.mini.projectgame;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.RequestHandledEvent;
import jdk.jshell.spi.ExecutionControl;

import java.net.InetAddress;
import java.util.List;
import java.util.UUID;
import java.lang.UnsupportedOperationException;

@Component
public class GameMaster {

    private int portNumber;
    private InetAddress ipAddress;

    public enum GameMasterStatus {
        active, finished, idle;
    }

    private GameMasterConfiguration configuration;
    private int blueTeamGoals;
    private int redTeamGoals;
    private List<UUID> teamRedUuids;
    private List<UUID> teamBlueUuids;

    public void startGame()
    {
        System.out.println("The game has been started.");
    }

    @EventListener
    private void listen(RequestHandledEvent e) {
        System.out.println("RequestHandledEvent");
        System.out.println(e);
    }

    public GameMasterConfiguration loadConfiguration()
    {
        System.out.println("Configuration loaded.");
    }

    public void saveConfiguration()
    {
        System.out.println("Configuration saved.");
    }

    private void putNewPiece()
    {
        System.out.println("New piece put.");
    }

    private void printBoard()
    {
        System.out.println("Board printed.");
    }

    public void messageHandler(String message)
    {
        System.out.println("Message handled.");
    }

}
