package pl.mini.projectgame.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.RequestHandledEvent;
import jdk.jshell.spi.ExecutionControl;

import java.net.InetAddress;
import java.util.List;
import java.lang.UnsupportedOperationException;
import java.util.UUID;

import pl.mini.projectgame.models.*;

@Getter
@Setter
@EqualsAndHashCode
public class Player extends BoardObject {

    public enum Direction {
        UP, DOWN, LEFT, RIGHT;
    }

    public enum PlayerState {
        INITIALIZING, ACTIVE, COMPLETED;
    }


    private enum ActionType {
        Move, Pickup, Test,Place,Destroy,Send;
    }
    private Team team;
    public Position position;
    private InetAddress ipAddress;
    private int portNumber;
    private String playerName;
    private ActionType lastAction;
    private Direction lastDirection;
    private Piece piece;
    private Board board;
    private UUID playerUuid;
    private PlayerState playerState;
    private boolean ready = false;



    public Player(Team _team, InetAddress _ipAddress, int _portNumber, String _playerName){
        this.playerUuid = UUID.randomUUID();
        this.team = _team;
        this.ipAddress = _ipAddress;
        this.portNumber = _portNumber;
        this.playerName = _playerName;
        this.playerState = PlayerState.INITIALIZING;
    }

    public Player(Team team, String playerName) {
        this.playerUuid = UUID.randomUUID();
        this.team = team;
        this.playerName = playerName;
        this.playerState = PlayerState.INITIALIZING;
    }
  
    public Player() {
        this.playerUuid = UUID.randomUUID();
        this.playerState = PlayerState.INITIALIZING;
    }
    public boolean placePiece(){
        lastAction=ActionType.Place;
        if(!piece.getIsGood()){ piece = null; return false; }
        if(board.getCells().get(position).getContent().containsKey(Goal.class)) { piece = null; return false; }
        piece = null; return false;
    }
}

